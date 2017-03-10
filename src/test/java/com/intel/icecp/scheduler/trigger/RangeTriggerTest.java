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