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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class IntervalTriggerTest {

    public static final String PUBLISH_CHANNEL = "ndn:/publish-channel";

    @Test
    public void constructor() {
        Map params = new HashMap<String, String>();
        params.put("test", "Value1");

        IntervalTrigger trigger = new IntervalTrigger("foo", 15, "MINUTES", PUBLISH_CHANNEL, "start", params);
        assertEquals("foo", trigger.getId());
    }

    @Test
    public void testEqualsAndHashCode() {
        int interval = 10;
        String unit = "MINUTES";
        String cmd = "start";
        Map params = new HashMap<String, String>();
        params.put("test", "Value1");

        IntervalTrigger trigger1 = new IntervalTrigger("foo", interval, unit, PUBLISH_CHANNEL, cmd, params);
        IntervalTrigger trigger2 = new IntervalTrigger("foo", interval, unit, PUBLISH_CHANNEL, cmd, params);

        assertTrue(trigger1.equals(trigger2) && trigger2.equals(trigger1));
        assertTrue(trigger1.hashCode() == trigger2.hashCode());
    }

    @Test
    public void testGetinterval() {
        int interval = 10;
        String unit = "MINUTES";
        String cmd = "start";
        Map params = new HashMap<String, String>();
        params.put("test", "Value1");

        IntervalTrigger trigger = new IntervalTrigger("foo", interval, unit, PUBLISH_CHANNEL, cmd, params);
        assertEquals(10, trigger.getInterval());
    }

    @Test
    public void testGetunit() {
        int interval = 10;
        String unit = "MINUTES";
        String cmd = "start";
        Map params = new HashMap<String, String>();
        params.put("test", "Value1");

        IntervalTrigger trigger = new IntervalTrigger("foo", interval, unit, PUBLISH_CHANNEL, cmd, params);
        assertEquals(TimeUnit.MINUTES, trigger.getUnit());
    }

    @Test
    public void testToString() {
        int interval = 10;
        String unit = "MINUTES";
        String cmd = "start";
        Map params = new HashMap<String, String>();
        params.put("test", "Value1");

        IntervalTrigger trigger = new IntervalTrigger("foo", interval, unit, PUBLISH_CHANNEL, cmd, params);
        assertTrue(trigger.toString().contains("IntervalTrigger"));
    }
}