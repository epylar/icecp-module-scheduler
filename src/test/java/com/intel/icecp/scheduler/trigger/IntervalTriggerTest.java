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