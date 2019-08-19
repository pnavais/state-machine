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
import com.github.pnavais.machine.api.Status;
import com.github.pnavais.machine.api.message.Message;
import com.github.pnavais.machine.api.message.Messages;
import lombok.Getter;
import lombok.NonNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Allows fine grained customization of the filtering as it provides a per message
 * functionality injection.
 *
 * @param <T> the type of the nodes handling messages
 */
@Getter
public class FunctionMessageFilter<T extends AbstractNode,C extends Context<T>> implements MessageFilter<T, C> {

    /** The functions handling the dispatch of messages */
    private Map<Message, Function<C, Status>> dispatchHandlers;

    /** The functions handling the reception of messages */
    private Map<Message, Function<C, Status>> receptionHandlers;

    /**
     * Creates a new {@link FunctionMessageFilter}
     */
    public FunctionMessageFilter() {
        this.dispatchHandlers = new LinkedHashMap<>();
        this.receptionHandlers = new LinkedHashMap<>();
        setReceptionHandler(Messages.ANY, c -> Status.PROCEED);
        setDispatchHandler(Messages.ANY,  c -> Status.PROCEED);
    }

    /**
     * Sets the given function to handle the message to be
     * dispatched.
     *
     * @param message the message to be dispatched
     * @param function the function to handle dispatch messages
     */
    public void setDispatchHandler(@NonNull Message message, @NonNull Function<C, Status> function) {
        setHandler(dispatchHandlers, message, function);
    }

    /**
     * Sets the given function to handle the message to be
     * received.
     *
     * @param message the message to be received
     * @param function the function to handle received messages
     */
    public void setReceptionHandler(@NonNull Message message, @NonNull Function<C, Status> function) {
        setHandler(receptionHandlers, message, function);
    }

    /**
     * Retrieves the dispatch handler for the given message
     *
     * @param message the message
     * @return the dispatch handler
     */
    public Function<C, Status> getDispatchHandler(@NonNull Message message) {
        return dispatchHandlers.get(message);
    }

    /**
     * Retrieves the reception handler for the given message
     *
     * @param message the message
     * @return the reception handler
     */
    public Function<C, Status> getReceptionHandler(@NonNull Message message) {
        return receptionHandlers.get(message);
    }

    /**
     * Sets the handler for a given message
     *
     * @param handlerMap the handler map
     * @param message    the message
     * @param function   the function
     */
    private void setHandler(Map<Message, Function<C, Status>> handlerMap, Message message, Function<C, Status> function) {
        handlerMap.put(message, function);
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
    private void removeHandler(Map<Message, Function<C, Status>> handlerMap, Message message) {
        handlerMap.remove(message);
    }

    /**
     * Delegates the execution of the message dispatch
     * to the dispatch function registered with the message.
     *
     * @param context the context
     * @return the status of the operation
     */
    @Override
    public Status onDispatch(C context) {
        return handleMessage(dispatchHandlers, context);
    }

    /**
     * Delegates the execution of the message processing
     * to the reception function registered with the message.
     *
     * @param context the context
     * @return the status of the operation
     */
    @Override
    public Status onReceive(C context) {
        return handleMessage(receptionHandlers, context);
    }

    /**
     * Handles the message by applying the specific handler
     *
     * @param handlerMap the handler map
     * @param context the context
     * @return the status
     */
    private Status handleMessage(Map<Message, Function<C, Status>> handlerMap, C context) {
        return Optional.ofNullable(handlerMap.get(context.getMessage()))
                .orElse(Optional.ofNullable(handlerMap.get(Messages.ANY))
                        .orElse(c -> Status.PROCEED))
                .apply(context);
    }

}
