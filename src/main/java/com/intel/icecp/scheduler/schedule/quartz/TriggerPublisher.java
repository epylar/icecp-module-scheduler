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