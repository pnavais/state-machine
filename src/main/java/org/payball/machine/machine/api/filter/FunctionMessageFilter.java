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
package org.payball.machine.machine.api.filter;

import org.payball.machine.machine.api.AbstractNode;
import org.payball.machine.machine.api.Message;

import java.util.function.BiFunction;

/**
 * A placeholder implementation  of the {@link MessageFilter}
 * interface allowing to inject the implementation through
 * external functions. By default accepts both dispatch/reception operations.
 */
public class FunctionMessageFilter<T extends AbstractNode> implements MessageFilter<T> {

    /** The reception function */
    private BiFunction<Message, T, Status> onReceptionFunction;

    /** The dispatch function */
    private BiFunction<Message, T, Status> onDispatchFunction;

    /**
     *  Creates a {@link FunctionMessageFilter} instance
     *  accepting both reception/dispatch operations
     */
    public FunctionMessageFilter() {
        onReceptionFunction = (message, state) -> Status.PROCEED;
        onDispatchFunction = (message, state) -> Status.PROCEED;
    }

    /**
     * Retrieves the reception function
     *
     * @return the reception function
     */
    public BiFunction<Message, T, Status> getOnReceptionFunction() {
        return onReceptionFunction;
    }

    /**
     * Sets the reception function
     *
     * @param onReceptionFunction the reception function
     */
    public void setOnReceptionFunction(BiFunction<Message, T, Status> onReceptionFunction) {
        this.onReceptionFunction = onReceptionFunction;
    }

    /**
     * Retrieves the dispatch function
     *
     * @return the dispatch function
     */
    public BiFunction<Message, T, Status> getOnDispatchFunction() {
        return onDispatchFunction;
    }

    /**
     * Sets the dispatch function
     *
     * @param onDispatchFunction the dispatch function
     */
    public void setOnDispatchFunction(BiFunction<Message, T, Status> onDispatchFunction) {
        this.onDispatchFunction = onDispatchFunction;
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
        return onReceptionFunction.apply(message, destination);
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
        return onDispatchFunction.apply(message, source);
    }
}
