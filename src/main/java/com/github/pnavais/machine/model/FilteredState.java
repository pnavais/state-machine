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

import com.github.pnavais.machine.api.AbstractNode;
import com.github.pnavais.machine.api.Status;
import com.github.pnavais.machine.api.filter.FunctionMessageFilter;
import com.github.pnavais.machine.api.message.Message;
import com.github.pnavais.machine.api.message.Messages;
import lombok.Getter;
import lombok.NonNull;

import java.util.function.Function;

/**
 * A decorator adding message filtering functionality
 * to regular states.
 */
@Getter
public class FilteredState extends AbstractFilteredState {

    /** The Message filter. */
    private FunctionMessageFilter<State, StateContext> messageFilter;

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
     * Sets the reception handler for ANY message
     *
     * @param receptionHandler the reception handler
     */
    @Override
    public void setReceptionHandler(Function<StateContext, Status> receptionHandler) {
        this.messageFilter.setReceptionHandler(Messages.ANY, receptionHandler);
    }

    /**
     * Sets the reception handler for the given message
     *
     * @param receptionHandler the reception handler
     */
    @Override
    public void setReceptionHandler(Message message, Function<StateContext, Status> receptionHandler) {
        this.messageFilter.setReceptionHandler(message, receptionHandler);
    }

    /**
     * Sets the dispatch handler for ANY message
     *
     * @param dispatchHandler the dispatch handler
     */
    @Override
    public void setDispatchHandler(Function<StateContext, Status> dispatchHandler) {
        this.messageFilter.setDispatchHandler(Messages.ANY, dispatchHandler);
    }

    /**
     * Sets the dispatch handler for the given message
     *
     * @param dispatchHandler the dispatch handler
     */
    @Override
    public void setDispatchHandler(Message message, Function<StateContext, Status> dispatchHandler) {
        this.messageFilter.setDispatchHandler(message, dispatchHandler);
    }

    /**
     * Adds the filter mappings from the given
     * state to the current instance.
     *
     * @param state the state to merge
     * @return the merged state
     */
    @Override
    public AbstractNode merge(AbstractNode state) {
        super.merge(state);
        if (state instanceof FilteredState) {
            // Add or override dispatch/reception handlers mappings from the given state
            FunctionMessageFilter<State, StateContext> filter = ((FilteredState)state).getMessageFilter();
            filter.getDispatchHandlers().forEach((message, handler) -> this.messageFilter.setDispatchHandler(message, handler));
            filter.getReceptionHandlers().forEach((message, handler) -> this.messageFilter.setReceptionHandler(message, handler));
        }
        return this;
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
