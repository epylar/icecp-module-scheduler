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
