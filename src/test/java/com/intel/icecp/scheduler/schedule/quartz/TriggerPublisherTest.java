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

import com.intel.icecp.core.Channel;
import com.intel.icecp.core.Message;
import com.intel.icecp.core.Node;
import com.intel.icecp.core.misc.ChannelIOException;
import com.intel.icecp.scheduler.message.TriggerMessage;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;

import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TriggerPublisherTest {
    @Mock
    private Node mockNode;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private JobExecutionContext mockContext;
    @Mock
    private Channel<Message> mockResponseChannel;
    @Mock
    private JobDataMap mockJobDetailMap;
    @Mock
    private JobDetail mockJobDetail;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testConstructor() {
        assertNotNull(new TriggerPublisher(mockNode));
    }

    @Test
    public void testExecute() throws Exception {
        String jobId = "jobid";
        JobKey key = new JobKey(jobId);
        Date date = new Date();
        String cmd = "Start";
        Map params = new HashMap<String, String>();
        params.put("test1", "Value1");

        when(mockNode.openChannel(any(URI.class), any(), any())).thenReturn(mockResponseChannel);
        createMockJobContext(key, date, "mock.uri", cmd, params);

        TriggerPublisher publisher = new TriggerPublisher(mockNode);
        publisher.execute(mockContext);

        verify(mockResponseChannel, times(1)).publish(any(TriggerMessage.class));
        verify(mockResponseChannel, times(1)).close();
    }

    @Test
    public void testExecuteWhenPublishThrows() throws Exception {
        String jobId = "jobid";
        JobKey key = new JobKey(jobId);
        Date date = new Date();
        String cmd = "stop";
        Map params = new HashMap<String, String>();
        params.put("test2", "Value2");

        when(mockNode.openChannel(any(URI.class), any(), any())).thenReturn(mockResponseChannel);
        createMockJobContext(key, date, "mock.uri", cmd, params);

        doThrow(new ChannelIOException("mock exception")).when(mockResponseChannel).publish(any(TriggerMessage.class));

        TriggerPublisher publisher = new TriggerPublisher(mockNode);
        publisher.execute(mockContext);

        verify(mockResponseChannel, times(1)).publish(any(TriggerMessage.class));
        verify(mockResponseChannel, times(1)).close();
    }

    @Test (expected = IllegalArgumentException.class)
    public void testExecuteWhenUriIsInvalid() throws Exception {
        String jobId = "jobid";
        JobKey key = new JobKey(jobId);
        Date date = new Date();
        String cmd = "pause";
        Map params = new HashMap<String, String>();
        params.put("test3", "Value3");

        when(mockNode.openChannel(any(URI.class), any(), any())).thenReturn(mockResponseChannel);
        createMockJobContext(key, date, "foo:\\bad.uri", cmd, params);

        TriggerPublisher publisher = new TriggerPublisher(mockNode);
        publisher.execute(mockContext);
    }

    private void createMockJobContext(JobKey key, Date date, String uri, String cmd, Map<String, String> params) {
        when(mockContext.getJobDetail().getJobDataMap()).thenReturn(mockJobDetailMap);

        when(mockJobDetailMap.getString("publishChannel")).thenReturn(uri);
        when(mockJobDetailMap.getString("cmd")).thenReturn(cmd);
        when((Map<String, String>) mockJobDetailMap.get("params")).thenReturn(params);
        when(mockJobDetailMap.getWrappedMap()).thenReturn(mockJobDetailMap);

        when(mockContext.getFireTime()).thenReturn(date);
        when(mockContext.getJobDetail().getKey()).thenReturn(key);
    }
}