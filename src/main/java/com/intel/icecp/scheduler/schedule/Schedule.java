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
