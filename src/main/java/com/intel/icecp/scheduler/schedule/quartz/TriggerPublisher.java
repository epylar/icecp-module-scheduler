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
import com.intel.icecp.core.Node;
import com.intel.icecp.core.metadata.Persistence;
import com.intel.icecp.core.misc.ChannelIOException;
import com.intel.icecp.core.misc.ChannelLifetimeException;
import com.intel.icecp.node.utils.ChannelUtils;
import com.intel.icecp.rpc.CommandRequest;
import com.intel.icecp.scheduler.SchedulerModule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.net.URI;
import java.util.Map;

/**
 * Class used to publish messages from events triggered by
 * 
 */
//***NOTE*** This needs to be a public class so Quartz can instantiate it.
public class TriggerPublisher implements Job {
    private static final Logger LOGGER = LogManager.getLogger(TriggerPublisher.class);
    protected static final String URI_SUFFIX = "$cmd";
    private final Node pubNode;

    /**
     * Constructor. ***NOTE*** Instances implementing Job interface must have a public no-argument constructor
     */
    public TriggerPublisher() {
        this.pubNode = SchedulerModule.getNode();
    }

    /**
     * Constructor
     *
     * @param node Node used to open publish channel and send message
     */
    TriggerPublisher(Node node) {
        this.pubNode = node;
    }

    /**
     * Operation to perform when a Quartz trigger is fired.
     *
     * @param ctx Context associated with the trigger that fired
     * @throws JobExecutionException Thrown if unable process trigger event
     */
    @Override
    public void execute(JobExecutionContext ctx) throws JobExecutionException {
        //Get the publish channel out of the details, and then remove that property. The publishChannel info does not
        //need to be sent across the wire as a part of the message.
        JobDataMap jobDetailMap = ctx.getJobDetail().getJobDataMap();
        String pubChannelName = jobDetailMap.getString("publishChannel");
        jobDetailMap.remove("publishChannel");
        String cmd = jobDetailMap.getString("cmd");
        // TODO: This cannot take non string parameters. Passing a non string parameter will cause RPC to throw a casting error.
        Map<String, Object> params = (Map<String, Object>) jobDetailMap.get("params");
        CommandRequest request;
        if (params != null) {
            request = CommandRequest.from(cmd, params);
        } else {
            request = CommandRequest.from(cmd);
        }
        publishMessage(request, URI.create(pubChannelName));

        LOGGER.info("Next fire date for trigger {} is {}", ctx.getJobDetail().getKey().getName(), ctx.getNextFireTime());
    }

    private void publishMessage(CommandRequest request, URI pubChannelName) {
        URI publishChannel = pubChannelName;
        if (request.name != null && !request.name.isEmpty()) {
            publishChannel = ChannelUtils.join(pubChannelName, URI_SUFFIX);
        }
        try (Channel<CommandRequest> requestChannel = pubNode.openChannel(publishChannel, CommandRequest.class, Persistence.DEFAULT)) {
            requestChannel.publish(request);
        } catch (ChannelLifetimeException | ChannelIOException e) {
            LOGGER.error("Command request failed, no channel available for request: {}", request, e);
        }
    }
}