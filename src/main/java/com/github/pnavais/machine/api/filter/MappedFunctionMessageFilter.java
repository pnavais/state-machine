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
import lombok.NonNull;
import com.github.pnavais.machine.api.Message;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

/**
 * Allows fine grained customization of the filtering as it provides a per message
 * functionality injection.
 *
 * @param <T> the type of the nodes handling messages
 */
public class MappedFunctionMessageFilter<T extends AbstractNode> implements MessageFilter<T> {

    /** The functions handling the dispatch of messages */
    private Map<Message, BiFunction<Message, T, Status>> dispatchHandlers;

    /** The functions handling the reception of messages */
    private Map<Message, BiFunction<Message, T, Status>> receptionHandlers;

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
    public void setDispatchHandler(@NonNull Message message, @NonNull BiFunction<Message, T, Status> function) {
        setHandler(dispatchHandlers, message, function);
    }

    /**
     * Sets the given function to handle the message to be
     * received.
     *
     * @param message the message to be received
     * @param function the function to handle received messages
     */
    public void setReceptionHandler(@NonNull Message message, @NonNull BiFunction<Message, T, Status> function) {
        setHandler(receptionHandlers, message, function);
    }

    /**
     * Sets the handler for a given message
     *
     * @param handlerMap the handler map
     * @param message    the message
     * @param function   the function
     */
    private void setHandler(Map<Message, BiFunction<Message, T, Status>> handlerMap, Message message, BiFunction<Message, T, Status> function) {
        if (Message.ANY.equals(message)) {
            handlerMap.clear();
        }

        // If any message already set inhibit other handlers
        if (!handlerMap.containsKey(Message.ANY)) {
            handlerMap.put(message, function);
        }
    }

    /**
     * Removes the reception handler for the
     * given message.
     *
     * @param message the message
     */
    public void removeReceptionHandler(@NonNull Message message) {
        removeHandler(receptionHandlers, message);
    }

    /**
     * Removes the dispatch handler for the
     * given message.
     *
     * @param message the message
     */
    public void removeDispatchHandler(@NonNull Message message) {
       removeHandler(dispatchHandlers, message);
    }

    /**
     * Removes the handler for the given message
     *
     * @param handlerMap the handler map
     * @param message    the message
     */
    private void removeHandler(Map<Message, BiFunction<Message, T, Status>> handlerMap, Message message) {
        if (Message.ANY.equals(message)) {
            handlerMap.clear();
        } else {
            handlerMap.remove(message);
        }
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
        return handleMessage(dispatchHandlers, message, destination);
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
        return handleMessage(receptionHandlers, message, source);
    }

    /**
     * Handles the message by applying the specific handler
     *
     * @param handlerMap the handler map
     * @param message    the message
     * @param state      the state
     * @return the status
     */
    private Status handleMessage(Map<Message, BiFunction<Message, T, Status>> handlerMap, Message message, T state) {
        return Optional.ofNullable(handlerMap.get(message))
                .orElse(Optional.ofNullable(handlerMap.get(Message.ANY)).orElse((m, t) -> Status.PROCEED))
                .apply(message, state);
    }
}
