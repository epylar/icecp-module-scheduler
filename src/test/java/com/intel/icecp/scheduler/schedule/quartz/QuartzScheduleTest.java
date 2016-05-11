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