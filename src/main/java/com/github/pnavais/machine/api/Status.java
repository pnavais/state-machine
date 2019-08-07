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

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The status represents the possible outcome of a given movement
 * between nodes.
 */
@Getter
@Setter
@ToString
public class Status {

    /** The aborted status keyword */
    public static final String STATUS_ABORTED = "ABORT";

    /** The proceed status keyword */
    public static final String STATUS_PROCEED = "PROCEED";

    /** The send message status keyword */
    public static final String STATUS_FORWARD = "FORWARD";

    /** Abort the operation */
    public static final Status ABORT = new Status(STATUS_ABORTED);

    /** Proceed with the operation */
    public static final Status PROCEED = new Status(STATUS_PROCEED);

    /** The Status name.*/
    private String statusName;

    /** The emitted message */
    private Message message;

    /**
     * Constructor with name
     * @param statusName the status name
     */
    private Status(String statusName) {
        this.statusName = statusName;
    }

    /**
     * Constructor with name and message
     * @param statusName the status name
     * @param message the message
     */
    private Status(String statusName, Message message) {
        this.statusName = statusName;
        this.message = message;
    }

    /**
     * Creates an status to request the emission of a new
     * message. It is used to force a change of the target destination
     * due to some custom logic applied.
     * @param message the message
     * @return the status
     */
    public static Status forward(Message message) {
        return new Status(STATUS_FORWARD, message);
    }

    /**
     * Verify if the status informs of a
     * redirection (forward status and message is present)
     *
     * @return tue if redirection needed, false otherwise
     */
    public boolean isRedirect() {
        return statusName.equals(STATUS_FORWARD) && (this.message!=null);
    }
}
