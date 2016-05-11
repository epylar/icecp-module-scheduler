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