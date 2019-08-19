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

import com.github.pnavais.machine.api.Status;
import com.github.pnavais.machine.api.filter.MessageFilter;
import com.github.pnavais.machine.api.message.Message;
import lombok.NonNull;

import java.util.function.Function;

/**
 * Base class for all Message filter state decorators
 */
public abstract class AbstractFilteredState extends AbstractWrappedState implements MessageFilter<State, StateContext> {
    
    /**
     * Constructor with the state to wrap
     *
     * @param state the state to wrap
     */
    public AbstractFilteredState(@NonNull State state) {
        super(state);
    }

    /**
     * Retrieves the message filter
     *
     * @return the message filter
     */
    public abstract MessageFilter<State, StateContext> getMessageFilter();

    /**
     * Sets the reception handler for ANY message
     *
     * @param receptionHandler the reception handler
     */
    public abstract void setReceptionHandler(Function<StateContext, Status> receptionHandler);

    /**
     * Sets the reception handler for the given message
     *
     * @param receptionHandler the reception handler
     */
    public abstract void setReceptionHandler(Message message, Function<StateContext, Status> receptionHandler);

    /**
     * Sets the dispatch handler for ANY message
     *
     * @param dispatchHandler the dispatch handler
     */
    public abstract void setDispatchHandler(Function<StateContext, Status> dispatchHandler);

    /**
     * Sets the dispatch handler for the given message
     *
     * @param dispatchHandler the dispatch handler
     */
    public abstract void setDispatchHandler(Message message, Function<StateContext, Status> dispatchHandler);

    /**
     * Intercepts a message to be dispatched to the
     * given destination.
     *
     * @param context the context
     * @return whether the operation shall continue or not
     */
    @Override
    public Status onDispatch(StateContext context) {
        return getMessageFilter().onDispatch(context);
    }

    /**
     * Intercepts a message to be received from the
     * given origin.
     *
     * @param context the context
     * @return whether the operation shall continue or not
     */
    @Override
    public Status onReceive(StateContext context) {
        return getMessageFilter().onReceive(context);
    }


    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
