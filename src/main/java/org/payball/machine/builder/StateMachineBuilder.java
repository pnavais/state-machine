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

package org.payball.machine.builder;

import org.payball.machine.StateMachine;
import org.payball.machine.api.Message;
import org.payball.machine.model.State;
import org.payball.machine.model.StateTransition;
import org.payball.machine.model.StringMessage;
import org.payball.machine.model.StateTransitionMap;

import java.util.Objects;

/**
 * A static builder for {@link StateMachine} instances
 */
public class StateMachineBuilder {

    /** The transitions map */
    private StateTransitionMap transitionMap;

    /**
     * The constructor
     */
    public StateMachineBuilder() {
        transitionMap = new StateTransitionMap();
    }

    /**
     * Adds a new transition from a given source state to a target state
     * on message reception
     *
     * @param srcStateName the source state's name
     * @param  messageId the received message
     * @param targetStateName the target state's name
     *
     * @return the builder transitionMap for chaining purposes
     */
    public StateMachineBuilder add(String srcStateName, String messageId, String targetStateName) {
        return add(new StateTransition(new State(srcStateName), new StringMessage(messageId), new State(targetStateName)));
    }

    /**
     * Adds a new transition from a given source state to a target state
     * on message reception
     *
     * @param srcStateName the source state's name
     * @param message the received message
     * @param targetStateName the target state's name
     *
     * @return the builder transitionMap for chaining purposes
     */
    public StateMachineBuilder add(String srcStateName, Message<?> message, String targetStateName) {
        return add(new StateTransition(new State(srcStateName), message, new State(targetStateName)));
    }

    /**
     * Adds a new transition from a given source state to a target state
     * on message reception.
     *
     * @param srcStateName the source state's name
     * @param  messageId the received message
     * @param target the target state
     *
     * @return the builder transitionMap for chaining purposes
     */
    public StateMachineBuilder add(String srcStateName, String messageId, State target) {
        return add(new StateTransition(new State(srcStateName), new StringMessage(messageId), target));
    }

    /**
     * Adds a new transition from a given source state to a target state
     * on message reception.
     *
     * @param srcStateName the source state's name
     * @param  message the received message
     * @param target the target state
     *
     * @return the builder transitionMap for chaining purposes
     */
    public StateMachineBuilder add(String srcStateName, Message message, State target) {
         return add(new StateTransition(new State(srcStateName), message, target));
    }

    /**
     * Adds a new transition from a given source state to a target state
     * on message reception.
     *
     * @param origin the source state
     * @param  messageId the received message
     * @param target the target state
     *
     * @return the builder transitionMap for chaining purposes
     */
    public StateMachineBuilder add(State origin, String messageId, State target) {
        return add(new StateTransition(origin, new StringMessage(messageId), target));
    }

    /**
     * Adds a new transition from a given source state to a target state
     * on message reception.
     *
     * @param origin the source state
     * @param  message the received message
     * @param target the target state
     *
     * @return the builder transitionMap for chaining purposes
     */
    public StateMachineBuilder add(State origin, Message message, State target) {
        return add(new StateTransition(origin, message, target));
    }

    /**
     * Adds a new transition from a given source state to a target state
     * on message reception.
     *
     * @param origin the source state
     * @param  messageId the received message
     * @param targetStateName the target state's name
     *
     * @return the builder transitionMap for chaining purposes
     */
    public StateMachineBuilder add(State origin, String messageId, String targetStateName) {
        return add(new StateTransition(origin, new StringMessage(messageId), new State(targetStateName)));
    }

    /**
     * Creates a loop for the given origin state's name
     * and message description.
     *
     * @param srcStateName the source state's name
     * @param message the message
     *
     * @return the builder transitionMap for chaining purposes
     */
    public StateMachineBuilder selfLoop(String srcStateName, String message) {
        return add(srcStateName, message, srcStateName);
    }

    /**
     * Creates a loop for the given origin state's name
     * and message.
     *
     * @param srcStateName the source state's name
     * @param message the message
     *
     * @return the builder transitionMap for chaining purposes
     */
    public StateMachineBuilder selfLoop(String srcStateName, Message message) {
        return add(srcStateName, message, srcStateName);
    }

    /**
     * Creates a loop for the given origin state
     * and message.
     *
     * @param origin the source state's name
     * @param message the message
     *
     * @return the builder transitionMap for chaining purposes
     */
    public StateMachineBuilder selfLoop(State origin, Message message) {
        return add(origin, message, origin);
    }

    /**
     * Creates a loop for the given origin state
     * and message description.
     *
     * @param origin the source state's name
     * @param message the message
     *
     * @return the builder transitionMap for chaining purposes
     */
    public StateMachineBuilder selfLoop(State origin, String message) {
        return add(origin, message, origin);
    }

    /**
     * Adds the given transition to the transition map.
     *
     * @param transition the transition to add
     *
     * @return the builder transitionMap for chaining purposes.
     */
    public StateMachineBuilder add(StateTransition transition) {
        Objects.requireNonNull(transition, "Cannot add null transition");
        this.transitionMap.add(transition);
        return this;
    }

    /**
     * Creates a new transitionMap from the
     * transition map currently built.
     *
     * @return the new state machine with the current
     * transitions.
     */
    public StateMachine build() {
        return new StateMachine(transitionMap);
    }

    /**
     * Retrieves the current transition map.
     *
     * @return the transition map
     */
    public StateTransitionMap getTransitionMap() {
        return transitionMap;
    }

}
