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
