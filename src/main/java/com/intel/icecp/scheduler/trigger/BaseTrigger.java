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

package com.intel.icecp.scheduler.trigger;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Abstract base class for triggers
 *
 */
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public abstract class BaseTrigger {
    private final String id;
    private String publishChannel;
    private final String cmd;
    private final Map<String, String> params;

    BaseTrigger(@JsonProperty(value = "id") String id,
                @JsonProperty(value = "publishChannel") String publishChannel,
                @JsonProperty(value = "cmd") String cmd,
                @JsonProperty(value = "params") Map<String, String> params) {
        this.id = id;
        this.publishChannel = publishChannel;
        this.cmd = cmd;
        this.params = params;
    }

    /**
     * return the unique identifier for the trigger.
     *
     * @return the unique identifier.
     */
    @JsonProperty("id")
    public String getId() {
        return id;
    }

    /**
     * get the channel to publish the trigger event message on
     *
     * @return the publish channel
     */
    @JsonProperty("publishChannel")
    public String getPublishChannel() {
        return publishChannel;
    }

    /**
     * get the command that will be triggered
     *
     * @return the rpc command
     */
    @JsonProperty("cmd")
    public String getCmd() {
        return cmd;
    }

    /**
     * get the parameter Map for rpc command that will be triggered
     *
     * @return the parameter map
     */
    @JsonProperty("params")
    public Map<String, String> getParams() {
        return params;
    }

    /**
     *  method to check if a {@link BaseTrigger} trigger is valid and contains all the required fields
     *
     * @return true if valid, else false
     */
    public boolean isValid() {
        return getId() != null && getPublishChannel() != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BaseTrigger that = (BaseTrigger) o;

        if (getId() != null ? !getId().equals(that.getId()) : that.getId() != null) return false;
        else if (getCmd() != null ? !getCmd().equals(that.getCmd()) : that.getCmd() != null) return false;
        else if (getParams() != null ? !getParams().equals(that.getParams()) : that.getParams() != null) return false;
        return getPublishChannel() != null ? getPublishChannel().equals(that.getPublishChannel()) : that.getPublishChannel() == null;

    }

    @Override
    public int hashCode() {
        int result = getId() != null ? getId().hashCode() : 0;
        result = 31 * result + (getPublishChannel() != null ? getPublishChannel().hashCode() : 0);
        result = 31 * result + (getCmd() != null ? getCmd().hashCode() : 0);
        result = 31 * result + (getParams() != null ? getParams().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Trigger{" +
                "id='" + id + '\'' +
                ", publishChannel='" + publishChannel + '\'' +
                ", cmd='" + cmd + '\'' +
                ", params='" + params + '\'' +
                "}";
    }
}