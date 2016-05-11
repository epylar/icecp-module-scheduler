/*
 * ******************************************************************************
 *
 * INTEL CONFIDENTIAL
 *
 * Copyright 2013 - 2016 Intel Corporation All Rights Reserved.
 *
 * The source code contained or described herein and all documents related to
 * the source code ("Material") are owned by Intel Corporation or its suppliers
 * or licensors. Title to the Material remains with Intel Corporation or its
 * suppliers and licensors. The Material contains trade secrets and proprietary
 * and confidential information of Intel or its suppliers and licensors. The
 * Material is protected by worldwide copyright and trade secret laws and treaty
 * provisions. No part of the Material may be used, copied, reproduced,
 * modified, published, uploaded, posted, transmitted, distributed, or disclosed
 * in any way without Intel's prior express written permission.
 *
 * No license under any patent, copyright, trade secret or other intellectual
 * property right is granted to or conferred upon you by disclosure or delivery
 * of the Materials, either expressly, by implication, inducement, estoppel or
 * otherwise. Any license under such intellectual property rights must be
 * express and approved by Intel in writing.
 *
 * Unless otherwise agreed by Intel in writing, you may not remove or alter this
 * notice or any other notice embedded in Materials by Intel or Intel's
 * suppliers or licensors in any way.
 *
 * ******************************************************************************
 */

package com.intel.icecp.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.icecp.core.Channel;
import com.intel.icecp.core.Module;
import com.intel.icecp.core.Node;
import com.intel.icecp.core.attributes.AttributeNotFoundException;
import com.intel.icecp.core.attributes.AttributeNotWriteableException;
import com.intel.icecp.core.attributes.Attributes;
import com.intel.icecp.core.attributes.ModuleStateAttribute;
import com.intel.icecp.core.misc.Configuration;
import com.intel.icecp.core.modules.ModuleProperty;
import com.intel.icecp.scheduler.attributes.SchedulerTriggersAttribute;
import com.intel.icecp.scheduler.attributes.Triggers;
import com.intel.icecp.scheduler.schedule.Schedule;
import com.intel.icecp.scheduler.schedule.ScheduleFactory;
import com.intel.icecp.scheduler.trigger.IntervalTrigger;
import com.intel.icecp.scheduler.trigger.RangeTrigger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;

/**
 * Module used to schedule time based triggers. Triggers that are fired result in publishing trigger event messages used
 * to synchronize time based activities of subscribers.
 * 
 */
@ModuleProperty(name = "scheduler-module", attributes = {SchedulerTriggersAttribute.class})
public class SchedulerModule implements Module {
    private static final Logger LOGGER = LogManager.getLogger();
    private static Node node;
    private Attributes schedulerAttributes;

    private final Schedule schedule;

    /**
     * Constructor
     */
    public SchedulerModule() {
        schedule = ScheduleFactory.create();
    }

    /**
     * Constructor
     *
     * @param schedule Schedule object used to schedule and process triggers
     */
    public SchedulerModule(Schedule schedule) {
        this.schedule = schedule;
    }

    public static synchronized Node getNode() {
        return node;
    }

    private static synchronized void setNode(Node node) {
        SchedulerModule.node = node;
    }

    /**
     * @deprecated use {@link #run(Node, Attributes)} instead.
     */
    @Override
    @Deprecated
    public void run(Node node, Configuration moduleConfiguration, Channel<State> moduleStateChannel, long moduleId) {
        throw new UnsupportedOperationException("Deprecated version of run, will be removed entirely in a future release");
    }

    /**
     * Startup an instance of the module. <br>
     *
     * @param node the node the module is currently running on
     * @param attributes set of attributes {@link Attributes} defined for the module
     */
    @Override
    public void run(Node node, Attributes attributes) {
        this.schedulerAttributes = attributes;
        if (node == null) {
            LOGGER.error("Null node has been passed in");
            setAttribute(ModuleStateAttribute.class, State.ERROR);
            return;
        }

        setNode(node);

        String schedulerConfig;
        try {
            schedulerConfig = schedulerAttributes.get(SchedulerTriggersAttribute.class);
            LOGGER.info("Retrieved value {} from trigger attribute", schedulerConfig);
        } catch (AttributeNotFoundException e) {
            LOGGER.error("Scheduler Trigger attribute not found", e);
            setAttribute(ModuleStateAttribute.class, State.ERROR);
            return;
        }

        if (!parseTriggers(schedulerConfig)) {
            setAttribute(ModuleStateAttribute.class, State.ERROR);
            return;
        }

        // start the schedule manager
        if (!schedule.start()) {
            LOGGER.error("Unable to start the schedule manager");
            setAttribute(ModuleStateAttribute.class, State.ERROR);
            return;
        }
        LOGGER.info("Started schedule manager");
        setAttribute(ModuleStateAttribute.class, State.RUNNING);
    }

    /**
     * Parse all triggers returned from attribute
     * @param schedulerConfig attribute value of triggers
     * @return true if parsing and scheduling was successful, else false
     */
    private boolean parseTriggers(String schedulerConfig) {
        ObjectMapper mapper = new ObjectMapper();
        if (schedulerConfig != null && !schedulerConfig.isEmpty()) {
            try {
                // retrieve all triggers
                Triggers allTriggers = mapper.readValue(schedulerConfig, Triggers.class);
                List<RangeTrigger> validRangeTriggers = allTriggers.getValidRangeTriggers();
                LOGGER.info("Module contains {} valid interval triggers: {}", validRangeTriggers.size(), validRangeTriggers.toString());

                List<IntervalTrigger> validIntervalTriggers = allTriggers.getValidIntervalTriggers();
                LOGGER.info("Module contains {} valid interval triggers: {}", validIntervalTriggers.size(), validIntervalTriggers.toString());

                boolean added = false;
                added |= scheduleRangeTriggers(validRangeTriggers);
                added |= scheduleIntervalTriggers(validIntervalTriggers);

                // return true if either of the triggers was successfully scheduled
                return added;
            } catch (IOException e) {
                LOGGER.error("Unable to parse scheduler config:{} into triggers", schedulerConfig, e);
                return false;
            }
        }
        // return false since schedulerConfig value was empty/null
        return false;
    }

    /**
     * Add trigger to schedule
     * @param intervalTriggers list of interval triggers
     * @return true if schedules, else false
     */
    private boolean scheduleIntervalTriggers(List<IntervalTrigger> intervalTriggers) {
        if (intervalTriggers.isEmpty()) {
            LOGGER.error("No valid range triggers defined! Please check config file.");
            return false;
        }

        intervalTriggers
                .parallelStream()
                .forEach(trigger -> schedule.scheduleIntervalTrigger(trigger, this.getClass().getName()));
        return true;
    }

    /**
     * Add trigger to schedule
     * @param rangeTriggers list of range triggers
     * @return true if schedules, else false
     */
    private boolean scheduleRangeTriggers(List<RangeTrigger> rangeTriggers) {
        if (rangeTriggers.isEmpty()) {
            LOGGER.error("No valid range triggers defined! Please check config file.");
            return false;
        }

        rangeTriggers
                .parallelStream()
                .forEach(trigger -> schedule.scheduleRangeTrigger(trigger, this.getClass().getName()));
        return true;
    }

    /**
     * Set an attribute with class and value pair with error handling
     *
     * @param attributeClass class of the attribute to be set
     * @param attributeValue value of the attribute to be set
     */
    private void setAttribute(Class attributeClass, Object attributeValue) {
        try {
            schedulerAttributes.set(attributeClass, attributeValue);
        } catch (AttributeNotFoundException | AttributeNotWriteableException e) {
            LOGGER.error("Attribute {} could not be set", attributeClass.getName(), e);
        }
    }

    /**
     * Stop module execution. All triggers will be stopped and removed.
     *
     * @param reason Reason for publishing module
     */
    @Override
    public void stop(StopReason reason) {
        LOGGER.info("Stopping module, reason = {}", reason);
        schedule.stop();
        setAttribute(ModuleStateAttribute.class, State.STOPPED);
    }
}