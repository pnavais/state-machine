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


import com.github.pnavais.machine.api.Message;
import com.github.pnavais.machine.api.Transition;
import lombok.Getter;

/**
 * Represents a simple transition between states
 */
@Getter
public class StateTransition extends Transition<State, Message> {

    /**
     * Creates a new state transition with the given
     * origin and destination states after applying the message.
     *
     * @param origin the origin state
     * @param message the message
     * @param target the destination state
     */
    public StateTransition(State origin, Message message, State target) {
        super(origin, message, target);
    }

    /**
     * Creates a new state transition with the given
     * origin and destination state names after applying the message.
     *
     * @param origin the origin state
     * @param message the message
     * @param target the destination state
     */
    public StateTransition(String origin, Message message, String target) {
        super(State.from(origin).build(), message, State.from(target).build());
    }

    @Override
    public String toString() {
        return getOrigin() + " - " + getMessage() + " -> " + getTarget();
    }
}
