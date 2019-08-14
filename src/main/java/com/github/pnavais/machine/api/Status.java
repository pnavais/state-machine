/*
 * Copyright 2019 Pablo Navais
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.github.pnavais.machine.api;

import lombok.Builder;
import lombok.Getter;

/**
 * The status represents the possible outcome of a given movement
 * between nodes.
 */
@Getter
public class Status {

    /** The aborted status keyword */
    public static final String STATUS_ABORTED = "ABORT";

    /** The proceed status keyword */
    public static final String STATUS_PROCEED = "PROCEED";

    /** The send message status keyword */
    public static final String STATUS_FORWARD = "FORWARD";

    /** Abort the operation */
    public static final Status ABORT = Status.builder().statusName(STATUS_ABORTED).build();

    /** Proceed with the operation */
    public static final Status PROCEED = Status.builder().statusName(STATUS_PROCEED).validity(true).build();

    /** The Status name.*/
    protected String statusName;

    /** The emitted message */
    protected Message message;

    /** The validity flag */
    protected boolean valid;

    /**
     * Constructor with name, message and validity.
     *
     * @param statusName the status name
     * @param message the message
     * @param validity the validity
     */
    @Builder
    protected Status(String statusName, Message message, boolean validity) {
        this.statusName = statusName;
        this.message = message;
        this.valid = validity;
    }

    /**
     * Creates an status to request the emission of a new
     * message. It is used to force a change of the target destination
     * due to some custom logic applied.
     * @param message the message
     * @return the status
     */
    public static Status forward(Message message) {
        return new Status(STATUS_FORWARD, message, true);
    }

    /**
     * Verify if the status informs of a
     * redirection (forward status and message is present)
     *
     * @return tue if redirection needed, false otherwise
     */
    public boolean isRedirect() {
        return statusName.equals(STATUS_FORWARD) && (this.message!=null) && (this.valid);
    }
}
