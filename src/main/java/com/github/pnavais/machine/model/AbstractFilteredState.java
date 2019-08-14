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

package com.github.pnavais.machine.model;

import com.github.pnavais.machine.api.Message;
import com.github.pnavais.machine.api.Status;
import com.github.pnavais.machine.api.filter.MessageFilter;
import lombok.NonNull;

/**
 * Base class for all Message filter state decorators
 */
public abstract class AbstractFilteredState extends State implements MessageFilter<State>  {

    /** The target State. */
    private State state;
    /**
     * Constructor with the state to wrap
     *
     * @param state the state to wrap
     */
    public AbstractFilteredState(@NonNull State state) {
        super(state.getName());
        this.state = state;
    }

    /**
     * Retrieves the message filter
     *
     * @return the message filter
     */
    public abstract MessageFilter<State> getMessageFilter();

    /**
     * Retrieves the name of the state
     * @return the state's name
     */
    @Override
    public String getName() {
        return this.state.getName();
    }

    /**
     * Sets whether the state is
     * final or not.
     *
     * @param finalState the final state flag
     */
    @Override
    public void setFinal(boolean finalState) {
        this.state.setFinal(finalState);
    }

    /**
     * Retrieves the final state condition
     * flag. If final, no further transitions
     * can be allowed from this state.
     *
     * @return the final state flag
     */
    @Override
    public boolean isFinal() {
        return this.state.isFinal();
    }


    /**
     * Intercepts a message to be dispatched to the
     * given destination.
     *
     * @param message     the message to be dispatched
     * @param destination the target node
     * @return whether the operation shall continue or not
     */
    @Override
    public Status onDispatch(Message message, State destination) {
        return getMessageFilter().onDispatch(message, destination);
    }

    /**
     * Intercepts a message to be received from the
     * given origin.
     *
     * @param message the message to be dispatched
     * @param source  the source node
     * @return whether the operation shall continue or not
     */
    @Override
    public Status onReceive(Message message, State source) {
        return getMessageFilter().onReceive(message, source);
    }

    @Override
    public boolean equals(Object o) {
        return state.equals(o);
    }

    @Override
    public int hashCode() {
        return state.hashCode();
    }
}
