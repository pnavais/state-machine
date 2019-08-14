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
package com.github.pnavais.machine.builder;

import com.github.pnavais.machine.StateMachine;
import com.github.pnavais.machine.api.Message;
import com.github.pnavais.machine.api.Messages;
import com.github.pnavais.machine.impl.StateTransitionMap;
import com.github.pnavais.machine.model.State;
import com.github.pnavais.machine.model.StateTransition;
import com.github.pnavais.machine.model.StringMessage;
import lombok.NonNull;

/**
 * A simple builder for {@link StateMachine} instances
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
     * Starts the building of a new
     * transition by specifying the source
     * state's name
     *
     * @param srcStateName the source state's name
     * @return the FromBuilder builder clause
     */
    public FromBuilder from(String srcStateName) {
        return from(new State(srcStateName));
    }

    /**
     * Starts the building of a new
     * transition by specifying the source
     * state.
     *
     * @param srcState the source state
     * @return the FromBuilder builder clause
     */
    public FromBuilder from(State srcState) {
        return new FromBuilder(this, srcState);
    }

    /**
     * Adds the given transition to the transition map.
     *
     * @param transition the transition to add
     * @return the builder transitionMap for chaining purposes.
     */
    public StateMachineBuilder add(@NonNull StateTransition transition) {
        this.transitionMap.add(transition);
        return this;
    }

    /**
     * Starts the definition of a loop
     * for the given state's name
     *
     * @param stateName the state name
     * @return the ToBuilder builder clause
     */
    public ToBuilder selfLoop(String stateName) {
        return selfLoop(new State(stateName));
    }

    /**
     * Starts a definition of a loop
     * for the given state
     *
     * @param state the state
     * @return the ToBuilder builder clause
     */
    public ToBuilder selfLoop(State state) {
        return new ToBuilder(this, state, state);
    }

    /**
     * Creates and initializes a State Machine
     * from the transition map currently built.
     *
     * @return the new state machine initialized with the current
     * transitions.
     */
    public StateMachine build() {
        StateMachine stateMachine = new StateMachine(transitionMap);
        stateMachine.init();
        return stateMachine;
    }

    /**
     * Retrieves the current transition map.
     *
     * @return the transition map
     */
    public StateTransitionMap getTransitionMap() {
        return transitionMap;
    }

    /**
     * Internal builder class to begin the definition
     * of a new transition.
     */
    public static class FromBuilder {

        /** The builder instance */
        private final StateMachineBuilder builder;

        /** The source state */
        private final State srcState;

        /**
         * Creates a new From Builder clause for the
         * State Machine builder using the given state as source.
         *
         * @param builder the builder
         * @param srcState the source state
         */
        private FromBuilder(StateMachineBuilder builder, State srcState) {
            this.builder = builder;
            this.srcState = srcState;
        }

        /**
         * Adds a new target state to the
         * transition under build.
         *
         * @param targetStateName the state
         * @return the builder ToBuilder clause
         */
        public ToBuilder to(String targetStateName) {
            return to(new State(targetStateName));
        }

        /**
         * Adds a new target state by it's name
         * to the transition under build.
         *
         * @param targetStateName the target state's name
         * @return the ToBuilder builder clause
         */
        public ToBuilder to(State targetStateName) {
            return new ToBuilder(builder, srcState, targetStateName);
        }
    }

    /**
     * Internal builder class to end the definition
     * of a new transition.
     */
    public static class ToBuilder {

        /** The builder instance */
        private final StateMachineBuilder builder;

        /** The source state */
        private final State srcState;

        /** The target state */
        private final State targetState;

        /**
         * Creates a new ToBuilder clause for the builder
         * using the given source and target states.
         *
         * @param builder the builder
         * @param srcState the source state
         * @param targetState the target state
         */
        private ToBuilder(StateMachineBuilder builder, State srcState, State targetState) {
            this.srcState = srcState;
            this.targetState = targetState;
            this.builder = builder;
        }

        /**
         * Starts a new transition from the given
         * state name using an empty
         * message for the current transition.
         *
         * @param state the next source state
         * @return the from builder
         */
        public FromBuilder from(String state) {
            return on(Messages.EMPTY).from(state);
        }

        /**
         * Starts a new transition from the given
         * state using an empty
         * message for the current transition.
         *
         * @param state the next source state
         * @return the from builder
         */
        public FromBuilder from(State state) {
            return on(Messages.EMPTY).from(state);
        }

        /**
         * Creates a self loop for the state
         * specified by the given name.
         *
         * @param state the state name
         * @return the from builder
         */
        public ToBuilder selfLoop(String state) {
            return on(Messages.EMPTY).selfLoop(state);
        }

        /**
         * Creates a self loop for the given state.
         *
         * @param state the state
         * @return the from builder
         */
        public ToBuilder selfLoop(State state) {
            return on(Messages.EMPTY).selfLoop(state);
        }

        /**
         * Finish the current transition and retrieves
         * the current state machine built instance.
         *
         * @return the current state machine instance
         */
        public StateMachine build() {
            return on(Messages.EMPTY).build();
        }

        /**
         * Ends the transition by applying the message and
         * retrieving the initial builder.
         *
         * @param message the string message to add
         * @return the initial builder
         */
        public StateMachineBuilder on(String message) {
            return on(new StringMessage((message)));
        }

        /**
         * Ends the transition by applying a custom message and
         * retrieving the initial builder.
         *
         * @param message the custom message to add
         * @return the initial builder
         */
        private StateMachineBuilder on(Message message) {
            builder.add(new StateTransition(srcState, message, targetState));
            return builder;
        }
    }

}
