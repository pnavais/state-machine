/*
 * Copyright 2018 Pablo Navais
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

package org.payball.machine.model;


import org.payball.machine.api.Message;
import org.payball.machine.api.Transition;

import java.util.Objects;

/**
 * Represents a simple transition between states
 */
public class StateTransition implements Transition<State> {

    /**
     * The origin of the transition
     */
    private final State source;

    /**
     * The message received on the origin state
     */
    private final Message<?> message;

    /**
     * The target destination upon message processing
     */
    private final State target;

    /**
     * Creates a new state transition with the given
     * origin and destination states after applying the message.
     *
     * @param source the origin state
     * @param message the message
     * @param target the destination state
     */
    public StateTransition(State source, Message<?> message, State target) {
        this.source = source;
        this.message = message;
        this.target = target;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StateTransition that = (StateTransition) o;
        return Objects.equals(source, that.source) &&
                Objects.equals(message, that.message) &&
                Objects.equals(target, that.target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, message, target);
    }

    @Override
    public Message getMessage() {
        return message;
    }

    @Override
    public State getOrigin() { return source; }

    @Override
    public State getTarget() {
        return target;
    }
}
