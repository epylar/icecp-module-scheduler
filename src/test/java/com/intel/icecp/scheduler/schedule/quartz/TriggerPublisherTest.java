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