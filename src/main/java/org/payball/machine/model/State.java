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
package org.payball.machine.model;

import org.payball.machine.api.AbstractNode;
import org.payball.machine.api.Message;
import org.payball.machine.api.filter.MappedFunctionMessageFilter;
import org.payball.machine.api.filter.MessageFilter;

import java.util.function.Function;

/**
 * An state represents an arbitrary node in a state machine
 * containing its transitions.
 */
public class State extends AbstractNode<State> {

     /**
     * Constructor with node name
     *
     * @param name the name of the node
     */
    public State(String name) {
        super(name);
        this.messageFilter = new MappedFunctionMessageFilter<>();
    }

    /**
     * Static constructor using
     * the given name.
     *
     * @param name the state's name
     * @return the new state
     */
    public static State named(String name) {
        return new State(name);
    }


    /**
     * Sets the reception handler for the given message
     *
     * @param receptionHandler the reception handler
     */
    public void setReceptionHandler(Message message, Function<State, MessageFilter.Status> receptionHandler) {
        ((MappedFunctionMessageFilter<State>)this.messageFilter).setReceptionHandler(message, receptionHandler);
    }

    /**
     * Sets the dispatch handler for the given message
     *
     * @param dispatchHandler the dispatch handler
     */
    public void setDispatchHandler(Message message, Function<State, MessageFilter.Status> dispatchHandler) {
        ((MappedFunctionMessageFilter<State>)this.messageFilter).setDispatchHandler(message, dispatchHandler);
    }

    @Override
    public String toString() {
        return "State{" +
                "name='" + name + '\'' +
                ", id=" + id +
                '}';
    }

}
