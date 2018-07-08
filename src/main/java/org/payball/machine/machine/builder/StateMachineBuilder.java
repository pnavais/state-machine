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
package org.payball.machine.machine.builder;

import org.payball.machine.machine.StateMachine;
import org.payball.machine.machine.api.Message;
import org.payball.machine.machine.model.State;
import org.payball.machine.machine.model.StateTransition;
import org.payball.machine.machine.model.StateTransitionMap;
import org.payball.machine.machine.model.StringMessage;

import java.util.Objects;

/**
 * A simple machineBuilder for {@link StateMachine} instances
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
     * @return the FromBuilder machineBuilder clause
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
     * @return the FromBuilder machineBuilder clause
     */
    public FromBuilder from(State srcState) {
        return new FromBuilder(this, srcState);
    }

    /**
     * Adds the given transition to the transition map.
     *
     * @param transition the transition to add
     * @return the machineBuilder transitionMap for chaining purposes.
     */
    public StateMachineBuilder add(StateTransition transition) {
        Objects.requireNonNull(transition, "Cannot add null transition");
        this.transitionMap.add(transition);
        return this;
    }

    /**
     * Starts the definition of a loop
     * for the given state's name
     *
     * @param stateName the state name
     * @return the ToBuilder machineBuilder clause
     */
    public ToBuilder selfLoop(String stateName) {
        return selfLoop(new State(stateName));
    }

    /**
     * Starts a definition of a loop
     * for the given state
     *
     * @param state the state
     * @return the ToBuilder machineBuilder clause
     */
    public ToBuilder selfLoop(State state) {
        return new ToBuilder(new FromBuilder(this, state), state);
    }

    /**
     * Creates a new transitionMap from the
     * transition map currently built.
     *
     * @return the new srcState machine with the current
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

    /**
     * Internal machineBuilder class to begin the definition
     * of a new transition.
     */
    public static class FromBuilder {

        /** The machineBuilder instance */
        private final StateMachineBuilder machineBuilder;

        /** The source state */
        private final State srcState;

        /**
         * Creates a new From Builder clause for the
         * State Machine machineBuilder using the given state as source.
         *
         * @param machineBuilder the machineBuilder
         * @param srcState the source state
         */
        private FromBuilder(StateMachineBuilder machineBuilder, State srcState) {
            this.machineBuilder = machineBuilder;
            this.srcState = srcState;
        }

        /**
         * Adds a new target state to the
         * transition under build.
         *
         * @param targetStateName the state
         * @return the machineBuilder ToBuilder clause
         */
        public ToBuilder to(String targetStateName) {
            return to(new State(targetStateName));
        }

        /**
         * Adds a new target state by it's name
         * to the transition under build.
         *
         * @param targetStateName the target state's name
         * @return the ToBuilder machineBuilder clause
         */
        public ToBuilder to(State targetStateName) {
            return new ToBuilder(this, targetStateName);
        }
    }

    /**
     * Internal machineBuilder class to specify
     * the target destination of a new transition.
     */
    public static class ToBuilder {

        /** The from machineBuilder instance */
        private final FromBuilder fromBuilder;

        /** The target state */
        private final State targetState;

        /**
         * Creates a new ToBuilder clause for the machineBuilder
         * using the given from machineBuilder.
         *
         * @param fromBuilder the machineBuilder
         * @param targetState the target state
         */
        private ToBuilder(FromBuilder fromBuilder, State targetState) {
            this.fromBuilder = fromBuilder;
            this.targetState = targetState;
        }

        /**
         * Ends the transition by applying the message and
         * retrieving the initial machineBuilder.
         *
         * @param message the string message to add
         * @return the initial machineBuilder
         */
        public WhenBuilder when(String message) {
            return when(new StringMessage((message)));
        }

        /**
         * Ends the transition by applying a custom message and
         * retrieving the initial machineBuilder.
         *
         * @param message the custom message to add
         * @return the initial machineBuilder
         */
        private WhenBuilder when(Message message) {
            return new WhenBuilder(this, message);
        }
    }

    /**
     * Internal machineBuilder class to end the definition
     * of a new transition.
     */
    public static class WhenBuilder {

        /** The to machineBuilder instance */
        private final ToBuilder toBuilder;

        /** The message */
        private Message message;

        /**
         * Creates a new ToBuilder clause for the machineBuilder
         * using the given from machineBuilder.
         *
         * @param toBuilder the to builder
         * @param message the target state
         */
        private WhenBuilder(ToBuilder toBuilder, Message message) {
            this.toBuilder = toBuilder;
            this.message = message;

            // Adds the transition to the builder
            StateMachineBuilder builder = toBuilder.fromBuilder.machineBuilder;
            builder.add(new StateTransition(toBuilder.fromBuilder.srcState, message, toBuilder.targetState));
        }

        /**
         * Allows continuing the builder chain by
         * adding an additional transition
         *
         * @param srcStateName the origin state's name
         * @return the From builder clause
         */
        public FromBuilder and(String srcStateName) {
            return toBuilder.fromBuilder.machineBuilder.from(srcStateName);
        }

        /**
         * Allows continuing the builder chain by
         * adding an additional transition
         *
         * @param state the origin state's name
         * @return the From builder clause
         */
        public FromBuilder and(State state) {
            return toBuilder.fromBuilder.machineBuilder.from(state);
        }

        /**
         * Terminal action, building the state machine
         * with its currently defined transitions
         *
         * @return the state machine
         */
        public StateMachine build() {
            return toBuilder.fromBuilder.machineBuilder.build();
        }

    }

}
