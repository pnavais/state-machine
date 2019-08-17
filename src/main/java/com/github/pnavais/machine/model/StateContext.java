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

import com.github.pnavais.machine.api.filter.Context;
import com.github.pnavais.machine.api.message.Event;
import com.github.pnavais.machine.api.message.Message;
import lombok.Builder;

/**
 * The context implementation for State nodes
 */
public class StateContext extends Context<State> {

    /**
     * Default constructor with all context information.
     *
     * @param event the current event
     * @param source the origin of the transition
     * @param target the destination of the transition
     * @param message the triggering message
     */
    @Builder
    public StateContext(Event event, State source, State target, Message message) {
        super(event, source, target, message);
    }
}
