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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Used to schedule a trigger with a {@link com.intel.icecp.scheduler.schedule.Schedule} instance.
 * <p>
 * Simple Triggers will help schedule triggers at the current time instant repeats it periodically as per the
 * specified interval.
 * <p>
 * When a trigger fires, a {@link com.intel.icecp.scheduler.message.TriggerMessage} object is created and published to the
 * publishChannel specified by the trigger.
 *
 */
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class IntervalTrigger extends BaseTrigger {
    private static final Logger LOGGER = LogManager.getLogger();

    private final int interval;
    private final String unit;

    /**
     * Constructor
     *
     * @param id Unique identifier for the trigger. This unique identifier is used for published trigger event messages
     * to determine the trigger that was fired.* @param interval the number of minutes at which the trigger should repeat.
     * @param interval the interval at which the trigger is repeated
     * @param unit the time unit for this triggers - enum value of {@link TimeUnit} MINUTES, HOURS, SECONDS, MILLISECONDS
     * @param publishChannel Channel the trigger event should be published on.
     */
    @JsonCreator
    public IntervalTrigger(
            @JsonProperty(value = "id") String id,
            @JsonProperty(value = "interval") int interval,
            @JsonProperty(value = "unit") String unit,
            @JsonProperty(value = "publishChannel") String publishChannel,
            @JsonProperty(value = "cmd") String cmd,
            @JsonProperty(value = "params") Map<String, String> params) {
        super(id, publishChannel, cmd, params);
        this.interval = interval;
        this.unit = unit;
    }

    /**
     * @return the time unit of the trigger
     */
    public TimeUnit getUnit() {
        if(unit == null){
            LOGGER.error("Value of time unit is null!");
            return null;
        }
        try {
            return TimeUnit.valueOf(unit);
        } catch (IllegalArgumentException e) {
            LOGGER.error("Illegal value of time unit: {}", unit, e);
            return null;
        }
    }

    /**
     * Get the interval at which the trigger should repeat
     *
     * @return integer representing the interval value
     */
    public int getInterval() {
        return interval;
    }

    /**
     *  method to check if a {@link IntervalTrigger} trigger is valid and contains all the required fields
     *
     * @return true if valid, else false
     */
    @Override
    public boolean isValid() {
        return getInterval() > 0 && getUnit() != null && super.isValid();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        IntervalTrigger that = (IntervalTrigger) o;

        if (getInterval() != that.getInterval()) return false;
        return getUnit() != null ? getUnit().equals(that.getUnit()) : that.getUnit() == null;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + getInterval();
        result = 31 * result + (getUnit() != null ? getUnit().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "IntervalTrigger{" +
                "interval=" + interval +
                "} " + super.toString();
    }
}
