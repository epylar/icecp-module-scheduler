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
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

public class QuartzScheduleTest {
    private static final String TEST_GROUP = "test-group";
    private Schedule schedule;
    private RangeTrigger range;
    private IntervalTrigger interval;
    
    @Before
    public void setUp() throws Exception {
        schedule = new QuartzSchedule();
        Map params = new HashMap<String, String>();
        params.put("test", "Value1");
        range = new RangeTrigger("range", "1:00 PM", "2:00 PM", "/publish-channel", "start", params);
        interval = new IntervalTrigger("interval", 15, "MINUTES", "/foo", "start", params);
    }

    @Test
    public void testAddIntervalTrigger() {
        schedule.start();
        schedule.scheduleIntervalTrigger(interval, TEST_GROUP);
        assertTrue(schedule.checkJobExists(interval.getId(), TEST_GROUP));
    }

    @Test
    public void testCreateSchedulerInstanceNotNull() {
        assertNotNull(schedule);
    }

    @Test
    public void testStartScheduler() {
        assertTrue(schedule.start());
    }

    @Test
    public void testStopScheduler() {
        assertTrue(schedule.stop());
    }

    @Test
    public void testSuspendScheduler() {
        assertTrue(schedule.suspend());
    }

    @Test
    public void testResumeScheduler() {
        assertTrue(schedule.resume());
    }

    @Test
    public void testAddTriggerWithGroup() {
        schedule.start();
        schedule.scheduleRangeTrigger(range, TEST_GROUP);
        assertTrue(schedule.checkJobExists(range.getId(), TEST_GROUP));
    }

    @Test
    public void testAddTriggerWithEmptyGroup() {
        schedule.start();
        schedule.scheduleRangeTrigger(range, "");
        assertFalse(schedule.checkJobExists(range.getId(), ""));
    }

    @Test
    public void testAddTriggerWithNullConfigDoesNotGetAdded() {
        schedule.start();
        schedule.scheduleRangeTrigger(null, TEST_GROUP);

        // cant give null here, since it creates a range key object
        assertFalse(schedule.checkJobExists("", TEST_GROUP));
    }
}