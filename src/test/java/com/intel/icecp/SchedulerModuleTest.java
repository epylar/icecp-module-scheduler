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