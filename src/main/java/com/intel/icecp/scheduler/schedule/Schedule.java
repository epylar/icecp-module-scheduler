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

package com.intel.icecp.scheduler.schedule;


import com.intel.icecp.scheduler.trigger.IntervalTrigger;
import com.intel.icecp.scheduler.trigger.RangeTrigger;

/**
 * Interface for schedule objects, which handle the scheduling and firing of triggers.
 */
public interface Schedule {
    /**
     * Start the schedule. All triggers that have been added to the schedule will be active.
     *
     * @return True if the schedule successfully started, false otherwise.
     */
    boolean start();

    /**
     * Suspend the schedule. All triggers associated with this schedule will be suspended and will not fire.
     *
     * @return True if the schedule successfully suspended, false otherwise.
     */
    boolean suspend();

    /**
     * Resume the schedule. All triggers associated with this schedule will resume and be active.
     *
     * @return True if the schedule successfully resumed, false otherwise.
     */
    boolean resume();

    /**
     * Stop the schedule. All triggers associated with this schedule will be deleted.
     *
     * @return True if the schedule successfully stopped, false otherwise.
     */
    boolean stop();

    /**
     * Check if trigger exists in the schedule
     *
     * @param triggerId the trigger id
     * @param triggerGroup the trigger group
     * @return true if exists, else false
     */
    boolean checkJobExists(String triggerId, String triggerGroup);

    /**
     * Add a trigger to the schedule. Triggers are considered unique by their ID, which can be obtained with the {@link RangeTrigger#getId()} method.
     *
     * @param trigger range trigger to add to the schedule.
     * @param creatorName Additional metadata that indicates originator of the trigger being created.
     */
    void scheduleRangeTrigger(RangeTrigger trigger, String creatorName);

    /**
     * Add a trigger to the schedule. Triggers are considered unique by their ID, which can be obtained with the {@link IntervalTrigger#getId()} method.
     *
     * @param trigger interval trigger to add to the schedule.
     * @param creatorName Additional metadata that indicates originator of the trigger being created.
     */
    void scheduleIntervalTrigger(IntervalTrigger trigger, String creatorName);

}
