/*
 * Copyright 2019 Pablo Navais
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.github.pnavais.machine.model;

import com.github.pnavais.machine.api.Status;
import com.github.pnavais.machine.api.message.Event;
import lombok.Getter;
import lombok.NonNull;

/**
 * This class acts as a wrapper for Status instances allowing to associate their
 * originating events.
 */
@Getter
public class InfoStatus {

    /** The wrapper instance */
    private final Status status;

    /** The originating event */
    private final Event event;

    /**
     * Constructor with status and event
     *
     * @param status the status
     * @param event the originating event
     */
    private InfoStatus(Status status, Event event) {
        this.status = status;
        this.event = event;
    }

    /**
     * Static factory method to create the wrapped
     * information status instance.
     *
     * @param status the status
     * @param event the originating event
     * @return the {@link InfoStatus} instance
     */
    public static InfoStatus from(@NonNull Status status, @NonNull Event event) {
        return new InfoStatus(status, event);
    }
}
