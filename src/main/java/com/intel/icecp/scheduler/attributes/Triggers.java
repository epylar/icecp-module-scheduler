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
