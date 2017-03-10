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

import com.intel.icecp.core.attributes.BaseAttribute;

/**
 * Read-only attribute used for defining the scheduler-triggers attribute which holds the list of triggers defined for this module.
 *
 */
// TODO: Hack, attribute defined to return String (Bug:EAPE-1536); should be type: List<RangeTrigger>
public class SchedulerTriggersAttribute extends BaseAttribute<String> {
    private final String value;

    /**
     * Constructor to create the scheduler-triggers attribute with a value
     *
     * @param value value of the attribute
     */
    public SchedulerTriggersAttribute(String value) {
        super("scheduler-triggers", String.class);
        this.value = value;
    }

    @Override
    public String value() {
        return value;
    }
}
