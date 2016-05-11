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

package com.intel.icecp.scheduler.attributes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.intel.icecp.scheduler.trigger.IntervalTrigger;
import com.intel.icecp.scheduler.trigger.RangeTrigger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class for holding all triggers
 *
 */
// TODO: Refactor after EAPE-1536 is fixed
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class Triggers {

    private final RangeTrigger[] rangeTriggers;
    private final IntervalTrigger[] intervalTriggers;

    /**
     * Constructor
     *
     * @param intervalTriggers array of interval triggers
     * @param rangeTriggers array of range triggers
     */
    @JsonCreator
    public Triggers(@JsonProperty("intervalTriggers") IntervalTrigger[] intervalTriggers,
                    @JsonProperty("rangeTriggers") RangeTrigger[] rangeTriggers) {
        this.rangeTriggers = rangeTriggers;
        this.intervalTriggers = intervalTriggers;
    }

    /**
     * Get all valid range triggers
     *
     * @return list of valid range triggers
     */
    public List<RangeTrigger> getValidRangeTriggers() {
        List<RangeTrigger> validRangeTriggers = new ArrayList<>();
        if (rangeTriggers != null) {
            validRangeTriggers = Arrays.asList(rangeTriggers)
                    .stream()
                    .filter(RangeTrigger::isValid)
                    .collect(Collectors.toList());
        }
        return validRangeTriggers;
    }

    /**
     * Get all valid interval triggers
     *
     * @return list of valid interval triggers
     */
    public List<IntervalTrigger> getValidIntervalTriggers() {
        List<IntervalTrigger> validIntervalTriggers = new ArrayList<>();
        if (intervalTriggers != null) {
            validIntervalTriggers = Arrays.asList(intervalTriggers)
                    .stream()
                    .filter(IntervalTrigger::isValid)
                    .collect(Collectors.toList());
        }
        return validIntervalTriggers;
    }

}
