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

package com.intel.icecp;

import com.intel.icecp.core.Module;
import com.intel.icecp.core.Node;
import com.intel.icecp.core.attributes.AttributeNotFoundException;
import com.intel.icecp.core.attributes.AttributeNotWriteableException;
import com.intel.icecp.core.attributes.Attributes;
import com.intel.icecp.core.attributes.IdAttribute;
import com.intel.icecp.core.attributes.ModuleStateAttribute;
import com.intel.icecp.scheduler.SchedulerModule;
import com.intel.icecp.scheduler.attributes.SchedulerTriggersAttribute;
import com.intel.icecp.scheduler.schedule.Schedule;
import com.intel.icecp.scheduler.trigger.IntervalTrigger;
import com.intel.icecp.scheduler.trigger.RangeTrigger;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;

import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SchedulerModuleTest {
    private static final long MODULE_ID = 99L;

    @Mock
    private Schedule mockSchedule;
    @Mock
    private Node mockNode;
    @Mock
    private Attributes mockAttributes;
    @Mock
    private RangeTrigger mockTrigger;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void whenRunWithNullNodePublishError() throws Exception {
        when(mockSchedule.start()).thenReturn(true);
        when(mockAttributes.get(eq(IdAttribute.class))).thenReturn(MODULE_ID);
        SchedulerModule module = getSchedulerModule();

        module.run(null, mockAttributes);

        verify(mockAttributes, times(1)).set(eq(ModuleStateAttribute.class), eq(Module.State.ERROR));
    }

    @Test
    public void testAttributesGetThrowsException() throws Exception {
        doThrow(new AttributeNotFoundException("testMessage")).when(mockAttributes).get(SchedulerTriggersAttribute.class);
        SchedulerModule module = getSchedulerModule();
        module.run(mockNode, mockAttributes);

        assertFalse(mockAttributes.has(SchedulerTriggersAttribute.class));
    }

    @Test
    public void testAttributesSetThrowsException() throws Exception {
        when(mockAttributes.get(anyString(), any())).thenReturn("test-scheduler");
        doThrow(new AttributeNotWriteableException("testMessage")).when(mockAttributes).set(eq(ModuleStateAttribute.class), any());
        SchedulerModule module = new SchedulerModule();
        module.run(mockNode, mockAttributes);

        assertFalse(mockAttributes.has(ModuleStateAttribute.NAME));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testThrowsExceptionWhenDeprecatedRunIsCalled() {
        SchedulerModule module = new SchedulerModule();
        module.run(null, null, null, 0L);
    }

    @Test
    public void whenRunAndScheduleFailsToStartPublishError() throws Exception {
        when(mockSchedule.start()).thenReturn(false);

        setupMockTriggers();

        SchedulerModule module = getSchedulerModule();

        module.run(mockNode, mockAttributes);

        verify(mockAttributes, times(1)).set(eq(ModuleStateAttribute.class), eq(Module.State.ERROR));
    }

    @Test
    public void whenRunAndScheduleSucceedsPublishRunning() throws Exception {
        when(mockSchedule.start()).thenReturn(true);

        setupMockTriggers();

        SchedulerModule module = getSchedulerModule();

        module.run(mockNode, mockAttributes);

        verify(mockAttributes, times(1)).set(eq(ModuleStateAttribute.class), eq(Module.State.RUNNING));
    }

    @Test
    public void whenRunAndConfigTriggersEmptyListPublishRunning() throws Exception {
        when(mockSchedule.start()).thenReturn(true);
        when(mockAttributes.get(SchedulerTriggersAttribute.class)).thenReturn(new ArrayList<>().toString());

        SchedulerModule module = getSchedulerModule();

        module.run(mockNode, mockAttributes);

        verify(mockAttributes, times(1)).set(eq(ModuleStateAttribute.class), eq(Module.State.ERROR));
        verify(mockSchedule, never()).scheduleRangeTrigger(any(RangeTrigger.class), anyString());
        verify(mockSchedule, never()).scheduleIntervalTrigger(any(IntervalTrigger.class), anyString());

    }

    @Test
    public void whenRunAndConfigTriggerExistsPublishRunning() throws Exception {
        when(mockSchedule.start()).thenReturn(true);

        setupMockTriggers();

        SchedulerModule module = getSchedulerModule();

        module.run(mockNode, mockAttributes);

        verify(mockAttributes, times(1)).set(eq(ModuleStateAttribute.class), eq(Module.State.RUNNING));
        verify(mockSchedule, times(2)).scheduleRangeTrigger(any(RangeTrigger.class), anyString());
        verify(mockSchedule, times(1)).scheduleIntervalTrigger(any(IntervalTrigger.class), anyString());
    }

    @Test
    public void testOneInvalidTriggerDoesNotAddInvalidAndAddsOnlyValid() throws Exception {
        when(mockSchedule.start()).thenReturn(true);

        setupMockOneInvalidTriggers();

        SchedulerModule module = getSchedulerModule();

        module.run(mockNode, mockAttributes);

        verify(mockAttributes, times(1)).set(eq(ModuleStateAttribute.class), eq(Module.State.RUNNING));
        verify(mockSchedule, never()).scheduleRangeTrigger(any(RangeTrigger.class), anyString());
        verify(mockSchedule, times(1)).scheduleIntervalTrigger(any(IntervalTrigger.class), anyString());
    }

    @Test
    public void testAllInvalidTriggersDoesNotAddTrigger() throws Exception {
        when(mockSchedule.start()).thenReturn(true);

        setupMockRangeTwoInvalidTriggers();

        SchedulerModule module = getSchedulerModule();

        module.run(mockNode, mockAttributes);

        verify(mockAttributes, times(1)).set(eq(ModuleStateAttribute.class), eq(Module.State.ERROR));
        verify(mockSchedule, never()).scheduleRangeTrigger(any(RangeTrigger.class), anyString());
        verify(mockSchedule, never()).scheduleIntervalTrigger(any(IntervalTrigger.class), anyString());

    }

    @Test
    public void whenStopCalledStateIsPublished() throws Exception {
        when(mockSchedule.stop()).thenReturn(true);

        setupMockTriggers();

        SchedulerModule module = getSchedulerModule();

        module.run(mockNode, mockAttributes);
        module.stop(Module.StopReason.USER_DIRECTED);

        verify(mockAttributes, times(1)).set(eq(ModuleStateAttribute.class), eq(Module.State.STOPPED));
    }

    @Test
    public void whenModuleIsRunningSchedulerModuleDefaultConstructorIsCalled() throws Exception {
        when(mockSchedule.start()).thenReturn(true);

        setupMockTriggers();

        SchedulerModule module = new SchedulerModule();

        module.run(mockNode, mockAttributes);

        verify(mockAttributes, times(1)).set(eq(ModuleStateAttribute.class), eq(Module.State.RUNNING));
    }

    @Test
    public void testTriggerAttributeEmptySetModuleIsInRunningStateAndAddTriggerIsNotCalled() throws Exception {
        SchedulerModule module = new SchedulerModule();
        when(mockAttributes.get(SchedulerTriggersAttribute.class)).thenReturn("");

        module.run(mockNode, mockAttributes);

        verify(mockAttributes, times(1)).set(eq(ModuleStateAttribute.class), eq(Module.State.ERROR));
        verify(mockSchedule, never()).scheduleRangeTrigger(any(RangeTrigger.class), anyString());
        verify(mockSchedule, never()).scheduleIntervalTrigger(any(IntervalTrigger.class), anyString());

    }

    @Test
    public void testTriggerAttributeNullSetModuleIsInRunningStateAndAddTriggerIsNotCalled() throws Exception {
        SchedulerModule module = new SchedulerModule();
        when(mockAttributes.get(SchedulerTriggersAttribute.class)).thenReturn(null);

        module.run(mockNode, mockAttributes);

        verify(mockAttributes, times(1)).set(eq(ModuleStateAttribute.class), eq(Module.State.ERROR));
        verify(mockSchedule, never()).scheduleRangeTrigger(any(RangeTrigger.class), anyString());

    }

    @Test
    public void testSendGarbageValuesForTriggerDoesNotAddAnyTrigger() throws Exception {
        when(mockSchedule.start()).thenReturn(true);

        setupMockTriggersWithSomeGarbageKeys();

        SchedulerModule module = getSchedulerModule();

        module.run(mockNode, mockAttributes);

        verify(mockAttributes, times(1)).set(eq(ModuleStateAttribute.class), eq(Module.State.ERROR));

        // no triggers (even the valid ones) got added
        verify(mockSchedule, never()).scheduleRangeTrigger(any(RangeTrigger.class), anyString());
        verify(mockSchedule, never()).scheduleIntervalTrigger(any(IntervalTrigger.class), anyString());
    }

    @Test
    public void testIfInValidTimeUnitDoesNotAddTrigger() throws Exception {
        when(mockSchedule.start()).thenReturn(true);

        setupMockIntervalTriggerWithInvalidTimeUnit();

        SchedulerModule module = getSchedulerModule();

        module.run(mockNode, mockAttributes);

        verify(mockAttributes, times(1)).set(eq(ModuleStateAttribute.class), eq(Module.State.ERROR));

        // no triggers (even the valid ones) got added
        verify(mockSchedule, never()).scheduleRangeTrigger(any(RangeTrigger.class), anyString());
        verify(mockSchedule, never()).scheduleIntervalTrigger(any(IntervalTrigger.class), anyString());
    }

    @Test
    public void testOnlyValidIntervalTriggersGetsAddedSuccessfully() throws Exception {
        when(mockSchedule.start()).thenReturn(true);

        setupMockValidIntervalTrigger();

        SchedulerModule module = getSchedulerModule();

        module.run(mockNode, mockAttributes);

        verify(mockAttributes, times(1)).set(eq(ModuleStateAttribute.class), eq(Module.State.RUNNING));

        verify(mockSchedule, never()).scheduleRangeTrigger(any(RangeTrigger.class), anyString());
        verify(mockSchedule, times(1)).scheduleIntervalTrigger(any(IntervalTrigger.class), anyString());
    }


    @Test
    public void testOnlyValidRangeTriggersGetsAddedSuccessfully() throws Exception {
        when(mockSchedule.start()).thenReturn(true);

        setupMockValidRangeTrigger();

        SchedulerModule module = getSchedulerModule();

        module.run(mockNode, mockAttributes);

        verify(mockAttributes, times(1)).set(eq(ModuleStateAttribute.class), eq(Module.State.RUNNING));

        verify(mockSchedule, times(1)).scheduleRangeTrigger(any(RangeTrigger.class), anyString());
        verify(mockSchedule, never()).scheduleIntervalTrigger(any(IntervalTrigger.class), anyString());
    }

    private void setupMockValidIntervalTrigger() throws Exception {
        String triggers = "{\"intervalTriggers\":[{\"id\":\"ack-trigger\",\"interval\": 15, " +
                "\"unit\": \"HOURS\",\"publishChannel\": \"/ACK-SCHEDULER\"}]}";
        when(mockAttributes.get(SchedulerTriggersAttribute.class)).thenReturn(triggers);
    }

    private SchedulerModule getSchedulerModule() {
        return new SchedulerModule(mockSchedule);
    }

    private void setupMockValidRangeTrigger() throws Exception {
        String triggers = "{\"rangeTriggers\": [{\"id\": \"some\",\"startTime\": \"11:00 PM\", \"endTime\": \"5:00 AM\", " +
                "\"publishChannel\":\"/DEX-SCHEDULER\"}]}";
        when(mockAttributes.get(SchedulerTriggersAttribute.class)).thenReturn(triggers);
    }

    private void setupMockIntervalTriggerWithInvalidTimeUnit() throws Exception {
        String triggers = "{\"intervalTriggers\":[{\"id\":\"ack-trigger\",\"interval\": 15, " +
                "\"unit\": \"MIN\",\"publishChannel\": \"/ACK-SCHEDULER\"}]}";
        when(mockAttributes.get(SchedulerTriggersAttribute.class)).thenReturn(triggers);
    }

    // rangeTriggers has a garbage key "foo"
    private void setupMockTriggersWithSomeGarbageKeys() throws Exception {
        String triggers = "{\"rangeTriggers\": [{\"foo\": \"bar\",\"startTime\": \"11:00 PM\", \"endTime\": \"5:00 AM\", " +
                "\"publishChannel\":\"/DEX-SCHEDULER\"}], \"intervalTriggers\":[{\"id\":\"ack-trigger\",\"interval\": 15, " +
                "\"unit\": \"MINUTES\",\"publishChannel\": \"/ACK-SCHEDULER\"}]}";
        when(mockAttributes.get(SchedulerTriggersAttribute.class)).thenReturn(triggers);
    }

    // all valid triggers
    private void setupMockTriggers() throws Exception {
        String triggers = "{\"rangeTriggers\": [{\"id\": \"dex-trigger\",\"startTime\": \"11:00 PM\", \"endTime\": \"5:00 AM\", " +
                "\"publishChannel\":\"/DEX-SCHEDULER\"}, {\"id\": \"dex1-trigger\",\"startTime\": \"11:00 PM\", \"endTime\": \"5:00 AM\", " +
                "\"publishChannel\":\"/DEX-SCHEDULER\"}], \"intervalTriggers\":[{\"id\":\"ack-trigger\",\"interval\": 15, " +
                "\"unit\": \"MINUTES\",\"publishChannel\": \"/ACK-SCHEDULER\"}, {\"id\":\"ack-trigger-2\",\"interval\": 15, " +
                "\"unit\": \"min\",\"publishChannel\": \"/ACK-SCHEDULER\"}]}";
        when(mockAttributes.get(SchedulerTriggersAttribute.class)).thenReturn(triggers);
    }

    // rangeTriggers is missing the "startTime" field
    private void setupMockOneInvalidTriggers() throws Exception {
        String triggers = "{\"rangeTriggers\": [{\"id\": \"dex-trigger\",\"endTime\": \"5:00 AM\", " +
                "\"publishChannel\":\"/DEX-SCHEDULER\"}], \"intervalTriggers\":[{\"id\":\"ack-trigger\",\"interval\": 15, " +
                "\"unit\": \"MINUTES\",\"publishChannel\": \"/ACK-SCHEDULER\"}]}";
        when(mockAttributes.get(SchedulerTriggersAttribute.class)).thenReturn(triggers);
    }

    // both invalid triggers with missing fields
    private void setupMockRangeTwoInvalidTriggers() throws Exception {
        String triggers = "{\"rangeTriggers\": [{\"id\": \"dex-trigger\", \"endTime\": \"5:00 AM\", " +
                "\"publishChannel\":\"/DEX-SCHEDULER\"}], \"intervalTriggers\":[{\"id\":\"ack-trigger\",\"interval\": 15, " +
                "\"unit\": \"MINUTES\"}]}";
        when(mockAttributes.get(SchedulerTriggersAttribute.class)).thenReturn(triggers);
    }

}