/*
 * Copyright (c) 2017 Intel Corporation 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intel.icecp.scheduler.schedule.quartz;

import com.intel.icecp.scheduler.schedule.Schedule;
import com.intel.icecp.scheduler.trigger.IntervalTrigger;
import com.intel.icecp.scheduler.trigger.RangeTrigger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleTrigger;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * Implementation of a schedule object using the open source
 * <a href="http://www.quartz-scheduler.org/">Quartz Enterprise Job Scheduler</a> project.
 * 
 */
public class QuartzSchedule implements Schedule {
    private static final Logger LOGGER = LogManager.getLogger();
    private Scheduler scheduler;

    /**
     * Constructor to create a new instance of a Quartz scheduler
     */
    public QuartzSchedule() {
        //Set Quartz specific configuration that we don't want accessible to the outside world.
        Properties props = new Properties();
        props.setProperty("org.quartz.scheduler.skipUpdateCheck", "true");
        props.setProperty("org.quartz.threadPool.threadCount", "1");

        //each creation of a Quartz scheduler will have a unique name, so that each schedule created
        //by a constructor is unique. Using the default value, or same name, would mean every time this constuctor is
        //called, the SAME Quartz scheduler would be returned
        props.setProperty("org.quartz.scheduler.instanceName", UUID.randomUUID().toString());
        try {
            SchedulerFactory factory = new StdSchedulerFactory(props);
            scheduler = factory.getScheduler();
            scheduler.clear();
        } catch (SchedulerException e) {
            LOGGER.error("Unable to create schedule", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean start() {
        try {
            scheduler.start();
            return true;
        } catch (SchedulerException e) {
            LOGGER.error("Unable to start schedule", e);
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean suspend() {
        try {
            scheduler.pauseAll();
            return true;
        } catch (SchedulerException e) {
            LOGGER.error("Unable to suspend schedule", e);
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean resume() {
        try {
            scheduler.resumeAll();
            return true;
        } catch (SchedulerException e) {
            LOGGER.error("Unable to resume schedule", e);
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean stop() {
        try {
            scheduler.shutdown(true);
            return true;
        } catch (SchedulerException e) {
            LOGGER.error("Unable to stop schedule", e);
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean checkJobExists(String triggerId, String triggerGroup) {
        try {
            return scheduler.checkExists(new TriggerKey(triggerId, triggerGroup));
        }
        catch (SchedulerException e) {
            LOGGER.error("Unable to create trigger key with {}:{}", triggerId, triggerGroup, e);
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void scheduleIntervalTrigger(IntervalTrigger config, String creatorName) {
        if (config == null) {
            LOGGER.error("Received null config object");
            return;
        }
        LOGGER.debug("Adding Trigger = {}", config);

        try {
            long intervalInMillis = TimeUnit.MILLISECONDS.convert(config.getInterval(), config.getUnit());

            JobDataMap jobMap = new JobDataMap();
            jobMap.put("params", config.getParams());

            JobDetail job = JobBuilder
                    .newJob(TriggerPublisher.class)
                    .withIdentity(config.getId(), creatorName)
                    .usingJobData("publishChannel", config.getPublishChannel())
                    .usingJobData(jobMap)
                    .usingJobData("cmd", config.getCmd())
                    .build();

            SimpleTrigger trigger = newTrigger()
                    .withIdentity(config.getId(), creatorName)
                    .startNow()
                    .withSchedule(simpleSchedule().withIntervalInMilliseconds(intervalInMillis).repeatForever())
                    .build();

            Date nextFireTime = scheduler.scheduleJob(job, trigger);
            LOGGER.debug("{}", scheduler.checkExists(job.getKey()));

            LOGGER.info("Date from schedule job = {}", nextFireTime);
        } catch (IllegalArgumentException e) {
            LOGGER.error("Unable to create trigger = {}, creatorName = {}", config, creatorName, e);
        } catch (SchedulerException e) {
            LOGGER.error("Unable to schedule trigger = {}", config, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void scheduleRangeTrigger(RangeTrigger config, String creatorName) {
        if (config == null) {
            LOGGER.error("Received null config object");
            return;
        }
        LOGGER.debug("Adding Trigger = {}", config);

        try {
            //Build a date object for the Quartz Scheduler from the trigger time for the range trigger. Need to make
            //sure the date that comes out is AFTER the current time. Setting a trigger with a date in the past will
            //cause the trigger to fire immediately when the scheduler starts.
            LocalDateTime localDate = LocalDateTime.of(LocalDate.now(), config.getTriggerTime());
            if (localDate.isBefore(LocalDateTime.now())) {
                localDate = localDate.plusDays(1);
            }
            Date date = Date.from(localDate.atZone(ZoneId.systemDefault()).toInstant());
            LOGGER.info("Setting daily trigger for {} starting at {}", config.getId(), date);

            JobDataMap jobMap = new JobDataMap();
            jobMap.put("params", config.getParams());

            JobDetail job = JobBuilder
                    .newJob(TriggerPublisher.class)
                    .withIdentity(config.getId(), creatorName)
                    .usingJobData("publishChannel", config.getPublishChannel())
                    .usingJobData(jobMap)
                    .usingJobData("cmd", config.getCmd())
                    .build();

            SimpleTrigger trigger = newTrigger()
                    .withIdentity(config.getId(), creatorName)
                    .startAt(date)
                    .withSchedule(simpleSchedule().withIntervalInHours(24).repeatForever())
                    .build();

            Date nextFireTime = scheduler.scheduleJob(job, trigger);
            LOGGER.info("Date from schedule job = {}", nextFireTime);
        } catch (IllegalArgumentException e) {
            LOGGER.error("Unable to create trigger = {}, creatorName = {}", config, creatorName, e);
        } catch (SchedulerException e) {
            LOGGER.error("Unable to schedule trigger = {}", config, e);
        }
    }
}
