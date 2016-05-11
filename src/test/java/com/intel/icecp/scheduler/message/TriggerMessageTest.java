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

package com.intel.icecp.scheduler.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.assertNotNull;

public class TriggerMessageTest {

    private static final String TRIGGER_NAME = "sample-trigger";
    private final ObjectMapper o = new ObjectMapper();
    private TriggerMessage triggerMessage;

    @Before
    public void setUp() throws Exception {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date date = dateFormat.parse("01/01/2016 12:00:00");

        triggerMessage = new TriggerMessage(TRIGGER_NAME, date);
    }

    @Test
    public void testTriggerMessageObjectNotNull() throws JsonProcessingException {
        assertNotNull(o.writeValueAsString(triggerMessage));
    }

    @Test
    public void testTriggerIdExists() throws JsonProcessingException {
        assertNotNull(o.writeValueAsString(triggerMessage.getTriggerId()));
    }

    @Test
    public void testTimestampExists() throws JsonProcessingException {
        assertNotNull(o.writeValueAsString(triggerMessage.getTimestamp()));
    }
}
