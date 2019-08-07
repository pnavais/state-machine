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
package com.github.pnavais.machine.api.filter;

import com.github.pnavais.machine.api.AbstractNode;
import com.github.pnavais.machine.api.Message;
import com.github.pnavais.machine.api.Status;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.function.BiFunction;

/**
 * A placeholder implementation  of the {@link MessageFilter}
 * interface allowing to inject the implementation through
 * external functions. By default accepts both dispatch/reception operations.
 */
@Getter
@Setter
public class FunctionMessageFilter<T extends AbstractNode> implements MessageFilter<T> {

    /** The reception function */
    @NonNull
    private BiFunction<Message, T, Status> receptionHandler;

    /** The dispatch function */
    @NonNull
    private BiFunction<Message, T, Status> dispatchHandler;

    /**
     *  Creates a {@link FunctionMessageFilter} instance
     *  accepting both reception/dispatch operations
     */
    public FunctionMessageFilter() {
        receptionHandler = (message, state) -> Status.PROCEED;
        dispatchHandler = (message, state) -> Status.PROCEED;
    }

    /**
     * Delegates the execution of the message dispatch
     * to the dispatch function.
     *
     * @param message the message to be dispatched
     * @param destination the target node
     * @return the status of the operation
     */
    @Override
    public Status onDispatch(Message message, T destination) {
        return dispatchHandler.apply(message, destination);
    }

    /**
     * Delegates the execution of the message processing
     * to the reception function.
     *
     * @param message the message to be received
     * @param source the source node
     * @return the status of the operation
     */
    @Override
    public Status onReceive(Message message, T source) {
        return receptionHandler.apply(message, source);
    }

}
