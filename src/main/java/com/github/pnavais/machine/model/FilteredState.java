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

import lombok.NonNull;
import com.github.pnavais.machine.api.Message;
import com.github.pnavais.machine.api.filter.MappedFunctionMessageFilter;
import com.github.pnavais.machine.api.filter.MessageFilter;

import java.util.function.BiFunction;

/**
 * A decorator adding message filtering functionality
 * to regular states.
 */
public class FilteredState extends State  {

    /**
     * The target State.
     */
    protected AbstractState state;

    /**
     * The Message filter.
     */
    private MappedFunctionMessageFilter<State> messageFilter;

    /**
     * Constructor with state
     *
     * @param state the state
     */
    public FilteredState(@NonNull State state) {
        super(state.getName());
        this.state = state;
        this.messageFilter = new MappedFunctionMessageFilter<>();
    }

    /**
     * Sets the reception handler for the given message
     *
     * @param receptionHandler the reception handler
     */
    public void setReceptionHandler(Message message, BiFunction<Message, State, MessageFilter.Status> receptionHandler) {
        this.messageFilter.setReceptionHandler(message, receptionHandler);
    }

    /**
     * Sets the dispatch handler for the given message
     *
     * @param dispatchHandler the dispatch handler
     */
    public void setDispatchHandler(Message message, BiFunction<Message, State, MessageFilter.Status> dispatchHandler) {
        this.messageFilter.setDispatchHandler(message, dispatchHandler);
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
