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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.intel.icecp.scheduler.configuration.ConfigConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Map;

/**
 * Used to schedule a trigger with a {@link com.intel.icecp.scheduler.schedule.Schedule} instance.
 * <p>
 * Range triggers will pick a random time within a provided time range for the trigger. Range triggers only support
 * scheduling on a daily basis. This means that once the trigger fires at the randomly selected time for the first time,
 * it will fire again 24 hours later.
 * <p>
 * If the end time is null for time range, the trigger will be scheduled to fire daily at the exact start time.
 * <p>
 * When a trigger fires, a {@link com.intel.icecp.scheduler.message.TriggerMessage} object is created and published to the
 * publishChannel specified by the trigger.
 * <p>
 * Range triggers do not account for daylight saving time. So when creating a range trigger, take into account the selected
 * random time could move an hour forward and/or backward as daylight savings time starts or stops.
 * 
 */
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class RangeTrigger extends BaseTrigger {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final double GUARD_FACTOR = 0.10;
    private final LocalTime time;
    private final LocalTime startTime;
    private final LocalTime endTime;

    /**
     * Constructor
     *
     * @param id Unique identifier for the trigger. This unique identifier is used for published trigger event messages
     * to determine the trigger that was fired.
     * @param startTime Starting time for the range window in local time. The supported TIME_FORMAT is h:mm a.
     * @param endTime Optional ending time for the range window, in local time. The supported TIME_FORMAT is h:mm a. If endTime
     * is null, the trigger will be set at the startTime value.
     * @param publishChannel Channel the trigger event should be published on.
     */
    @JsonCreator
    public RangeTrigger(
            @JsonProperty(value = "id") String id,
            @JsonProperty(value = "startTime") String startTime,
            @JsonProperty(value = "endTime") String endTime,
            @JsonProperty(value = "publishChannel") String publishChannel,
            @JsonProperty(value = "cmd") String cmd,
            @JsonProperty(value = "params") Map<String, String> params) {
        super(id, publishChannel, cmd, params);
        this.time = (startTime != null) ? LocalTime.parse(createTriggerTime(startTime, endTime), ConfigConstants.TIME_FORMAT) : null;
        this.startTime = (startTime != null) ? LocalTime.parse(startTime, ConfigConstants.TIME_FORMAT) : null;
        this.endTime = (endTime != null) ? LocalTime.parse(endTime, ConfigConstants.TIME_FORMAT) : null;
    }

    private static String createTriggerTime(String start, String end) {
        if (end == null) {
            return start;
        }
        LocalTime startTime = LocalTime.parse(start, ConfigConstants.TIME_FORMAT);
        LocalTime endTime = LocalTime.parse(end, ConfigConstants.TIME_FORMAT);

        // based on the time range, a random offset from the start time that occurs BEFORE the end time is going
        // to be generated. The algorithm is:
        //  1. Determine the period, in seconds, between the start time and end time. Take into account that the
        //    period time could run across days (e.g. 11:00 PM - 5:00 AM)
        //  2. Reduce the time period by a guard factor. This is done so that a time at or very near the end of the
        //    time period is not selected. This give the subscriber to the trigger a grace period where if whatever
        //    operation they are trying to perform fails, they could retry and likely (although not guaranteed) still
        //    be in the original time period range.
        //  3. Select a random offset based on the time period with guard factor and add it to the start time.
        long period;
        if (endTime.isAfter(startTime)) {
            period = Duration.between(startTime, endTime).getSeconds();
        } else {
            period = Duration
                    .ofDays(1)
                    .plusSeconds(Duration
                            .between(startTime, endTime)
                            .getSeconds())
                    .getSeconds();

        }

        SecureRandom random = new SecureRandom();
        period -= Double.valueOf(period * GUARD_FACTOR).longValue();
        LocalTime retVal = startTime.plusSeconds(random.nextInt((int) period));
        LOGGER.debug("Calculated trigger time of {}, between {} and {}", retVal, startTime, endTime);
        //Note, even though period is a long, the value we are going to get is well within an int range, so we are
        //treating the period value as an int for the random number calculation.
        return retVal.format(ConfigConstants.TIME_FORMAT);
    }

    /**
     * return the start time for the trigger window.
     *
     * @return the start time.
     */
    public String getStartTime() {
        return (startTime != null) ? startTime.format(ConfigConstants.TIME_FORMAT) : null;
    }

    /**
     * return the end time for the trigger window.
     *
     * @return the end time.
     */
    public String getEndTime() {
        return (endTime != null) ? endTime.format(ConfigConstants.TIME_FORMAT) : null;
    }

    /**
     * Get the time the trigger is set for. This should fall somewhere within the trigger window.
     *
     * @return the trigger time.
     */
    @JsonIgnore
    public LocalTime getTriggerTime() {
        return time;
    }

    /**
     *  method to check if a {@link RangeTrigger} trigger is valid and contains all the required fields
     *
     * @return true if valid, else false
     */
    @Override
    public boolean isValid() {
        return getStartTime() != null && super.isValid();
    }

    @Override
    public String toString() {
        return "RangeTrigger{" +
                "startTime=" + startTime +
                ", endTime=" + endTime +
                ", time=" + time +
                "} " + super.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        RangeTrigger that = (RangeTrigger) o;

        if(getStartTime() == null && that.getStartTime() != null) return false;
        if (!getStartTime().equals(that.getStartTime())) return false;
        return getEndTime() != null ? getEndTime().equals(that.getEndTime()) : that.getEndTime() == null;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (getStartTime() != null ? getStartTime().hashCode() : 0);
        result = 31 * result + (getEndTime() != null ? getEndTime().hashCode() : 0);
        return result;
    }
}
