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
import com.github.pnavais.machine.api.exception.TransitionInitializationException;

/**
 * Represents a simple transition between states
 */
public class StateTransition extends Transition<State, Message> {

    /** The origin of the transition */
    private final State source;

    /** The message received on the origin state */
    private final Message message;

    /** The target destination upon message processing */
    private final State target;

    /**
     * Creates a new state transition with the given
     * origin and destination state names after applying the message.
     *
     * @param source the origin state
     * @param message the message
     * @param target the destination state
     */
    public StateTransition(String source, Message message, String target) {
        this(State.from(source).build(), message, State.from(target).build());
    }

    /**
     * Creates a new state transition with the given
     * origin and destination states after applying the message.
     *
     * @param source the origin state
     * @param message the message
     * @param target the destination state
     */
    public StateTransition(State source, Message message, State target) {

        if (source == null || message == null|| target == null) {
            throw new TransitionInitializationException("Cannot create transitions with null components");
        }
        if (source.isFinal()) {
            throw new TransitionInitializationException("Cannot create transition from final state ["+source.getName()+"]");
        }
        this.source = source;
        this.message = message;
        this.target = target;
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
