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
import com.github.pnavais.machine.api.filter.FunctionMessageFilter;
import lombok.Getter;
import lombok.NonNull;

import java.util.function.BiFunction;

/**
 * A decorator adding message filtering functionality
 * to regular states.
 */
@Getter
public class FilteredState extends AbstractFilteredState {

    /** The message filter */
    private FunctionMessageFilter<State> messageFilter;

    /**
     * Static factory method to decorate the state with
     * message filtering capabilities
     *
     * @param state the state to wrap
     * @return the filtered state
     */
    public static FilteredState from(@NonNull State state) {
        return new FilteredState(state);
    }

    /**
     * Constructor with state
     *
     * @param state the state
     */
    public FilteredState(@NonNull State state) {
        super(state);
        this.messageFilter = new FunctionMessageFilter<>();
    }

    /**
     * Sets the reception handler for the given message
     *
     * @param receptionHandler the reception handler
     */
    public void setReceptionHandler(BiFunction<Message, State, Status> receptionHandler) {
        this.messageFilter.setReceptionHandler(receptionHandler);
    }

    /**
     * Sets the dispatch handler for the given message
     *
     * @param dispatchHandler the dispatch handler
     */
    public void setDispatchHandler(BiFunction<Message, State, Status> dispatchHandler) {
        this.messageFilter.setDispatchHandler(dispatchHandler);
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
        return this.messageFilter.onDispatch(message, destination);
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
        return this.messageFilter.onReceive(message, source);
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

}
