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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.intel.icecp.core.Message;

import java.util.Date;

/**
 * Class that represents the published message of a trigger result. Every time a trigger fires, a message will be created
 * and published, representing an event that other modules can subscribe to and take action on. The message will be
 * published to the channel that was specified when the trigger was created.
 */
@SuppressWarnings("serial")
@JsonInclude(value = Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "ts"
})
public class TriggerMessage implements Message {

    private final String triggerId;
    private final Date timestamp;

    /**
     * Constructor
     *
     * @param triggerId The unique trigger identifier.
     * @param timestamp Time that the trigger was fired.
     */
    @JsonCreator
    public TriggerMessage(@JsonProperty(value = "id", required = true) String triggerId,
                          @JsonProperty(value = "ts", required = true) Date timestamp) {
        this.triggerId = triggerId;
        this.timestamp = timestamp;
    }

    /**
     * Gets the timestamp of the time the trigger was fired
     *
     * @return The trigger fired timestamp
     */
    @JsonGetter("ts")
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * Gets the unique trigger ID associated with this message.
     *
     * @return The trigger ID.
     */
    @JsonGetter("id")
    public String getTriggerId() {
        return triggerId;
    }

    @Override
    public String toString() {
        return "TriggerMessage{" +
                ", triggerId='" + triggerId + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
