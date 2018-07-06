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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Allows fine grained customization of the filtering as it provides a pair message
 * functionality injection.
 *
 * @param <T> the type of the nodes handling messages
 */
public class MappedFunctionMessageFilter<T extends AbstractNode> implements MessageFilter<T> {

    /** The functions handling the dispatch of messages */
    private Map<Message, Function<T, Status>> dispatchHandlers;

    /** The functions handling the reception of messages */
    private Map<Message, Function<T, Status>> receptionHandlers;

    /**
     * Creates a new {@link MappedFunctionMessageFilter}
     */
    public MappedFunctionMessageFilter() {
        this.dispatchHandlers = new LinkedHashMap<>();
        this.receptionHandlers = new LinkedHashMap<>();
    }

    /**
     * Sets the given function to handle the message to be
     * dispatched.
     *
     * @param message the message to be dispatched
     * @param function the function to handle dispatch messages
     */
    public void setDispatchHandler(Message message, Function<T, Status> function) {
        this.dispatchHandlers.put(message, function);
    }

    /**
     * Sets the given function to handle the message to be
     * received.
     *
     * @param message the message to be received
     * @param function the function to handle received messages
     */
    public void setReceptionHandler(Message message, Function<T, Status> function) {
        this.receptionHandlers.put(message, function);
    }

    /**
     * Removes the reception handler for the
     * given message.
     *
     * @param message the message
     */
    public void removeReceptionHandler(Message message) {
        this.receptionHandlers.remove(message);

    }

    /**
     * Removes the dispatch handler for the
     * given message.
     *
     * @param message the message
     */
    public void removeDispatchHandler(Message message) {
        this.dispatchHandlers.remove(message);
    }

    /**
     * Delegates the execution of the message dispatch
     * to the dispatch function registered with the message.
     *
     * @param message the message to be dispatched
     * @param destination the target node
     * @return the status of the operation
     */
    @Override
    public Status onDispatch(Message message, T destination) {
        return Optional.ofNullable(dispatchHandlers.get(message)).orElse(t -> Status.PROCEED).apply(destination);
    }

    /**
     * Delegates the execution of the message processing
     * to the reception function registered with the message.
     *
     * @param message the message to be received
     * @param source the source node
     * @return the status of the operation
     */
    @Override
    public Status onReceive(Message message, T source) {
        return Optional.ofNullable(receptionHandlers.get(message)).orElse(t -> Status.PROCEED).apply(source);
    }
}
