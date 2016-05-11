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

package com.intel.icecp.scheduler.trigger;

import org.junit.Test;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

import static com.intel.icecp.scheduler.configuration.ConfigConstants.TIME_FORMAT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RangeTriggerTest {

    public static final String PUBLISH_CHANNEL = "ndn:/publish-channel";

    @Test
    public void constructor() {
        Map params = new HashMap<String, String>();
        params.put("test", "Value1");

        RangeTrigger trigger = new RangeTrigger("foo", "1:00 PM", "2:00 PM", PUBLISH_CHANNEL, "start", params);
        assertEquals("foo", trigger.getId());
    }

    @Test
    public void calculateAValidTimeWhenEndTimeGreaterThanStartTime() {
        String start = "1:00 PM";
        String end = "1:05 PM";
        String cmd = "start";
        Map params = new HashMap<String, String>();
        params.put("test", "Value1");

        RangeTrigger trigger = new RangeTrigger("foo", start, end, PUBLISH_CHANNEL, cmd, params);
        LocalTime triggerTime = trigger.getTriggerTime();
        LocalTime startTime = LocalTime.parse(start, TIME_FORMAT);
        LocalTime endTime = LocalTime.parse(end, TIME_FORMAT);

        assertTrue((triggerTime.isAfter(startTime) && triggerTime.isBefore(endTime)) ||
                (triggerTime.equals(startTime)));
    }

    @Test
    public void calculateAValidTimeWhenEndTimeNull() {
        String start = "1:00 PM";
        String cmd = "start";
        Map params = new HashMap<String, String>();
        params.put("test", "Value1");

        RangeTrigger trigger = new RangeTrigger("foo", start, null, PUBLISH_CHANNEL, cmd, params);
        LocalTime triggerTime = trigger.getTriggerTime();
        LocalTime startTime = LocalTime.parse(start, TIME_FORMAT);

        assertEquals(startTime, triggerTime);
    }

    @Test
    public void calculateAValidTimeWhenEndTimeLessThatStartTime() {
        String start = "11:55 PM";
        String end = "12:05 AM";
        String cmd = "start";
        Map params = new HashMap<String, String>();
        params.put("test", "Value1");

        RangeTrigger trigger = new RangeTrigger("foo", start, end, PUBLISH_CHANNEL, cmd, params);
        LocalTime triggerTime = trigger.getTriggerTime();
        LocalTime startTime = LocalTime.parse(start, TIME_FORMAT);
        LocalTime endTime = LocalTime.parse(end, TIME_FORMAT);

        assertTrue((triggerTime.isAfter(startTime) && triggerTime.isAfter(endTime)) ||
                (triggerTime.isBefore(startTime) && triggerTime.isBefore(endTime)) ||
                (triggerTime.equals(startTime)));
    }
    
    @Test
    public void testEqualsAndHashCode() {
        String start = "11:00 PM";
        String end = "12:05 AM";
        String cmd = "start";
        Map params = new HashMap<String, String>();
        params.put("test", "Value1");

        RangeTrigger trigger1 = new RangeTrigger("foo", start, end, PUBLISH_CHANNEL, cmd, params);
        RangeTrigger trigger2 = new RangeTrigger("foo", start, end, PUBLISH_CHANNEL, cmd, params);
        
        assertTrue(trigger1.equals(trigger2) && trigger2.equals(trigger1));
        assertTrue(trigger1.hashCode() == trigger2.hashCode());
    }
    
    @Test
    public void testGetStartTime() {
        String start = "1:00 PM";
        String end = "2:00 PM";
        String cmd = "start";
        Map params = new HashMap<String, String>();
        params.put("test", "Value1");

        RangeTrigger trigger = new RangeTrigger("foo", start, end, PUBLISH_CHANNEL, cmd, params);
        assertEquals("1:00 PM", trigger.getStartTime());
    }
    
    @Test
    public void testGetEndTime() {
        String start = "4:00 PM";
        String end = "5:00 PM";
        String cmd = "start";
        Map params = new HashMap<String, String>();
        params.put("test", "Value1");

        RangeTrigger trigger = new RangeTrigger("foo", start, end, PUBLISH_CHANNEL, cmd, params);
        assertEquals("5:00 PM", trigger.getEndTime());
    }
    
    @Test
    public void testToString() {
        String start = "10:00 PM";
        String end = "11:00 PM";
        String cmd = "start";
        Map params = new HashMap<String, String>();
        params.put("test", "Value1");

        RangeTrigger trigger = new RangeTrigger("foo", start, end, PUBLISH_CHANNEL, cmd, params);
        assertTrue(trigger.toString().contains("RangeTrigger"));
    }
    
    
    
    
}