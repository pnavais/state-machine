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
import org.payball.machine.api.builder.TransitionerBuilder;
import org.payball.machine.api.transition.TransitionIndex;
import org.payball.machine.model.State;
import org.payball.machine.model.StateTransition;
import org.payball.machine.model.StateTransitionMap;
import org.payball.machine.model.StringMessage;

import java.util.Collection;
import java.util.Objects;

/**
 * A simple machineBuilder for {@link StateMachine} instances
 */
public class StateMachineBuilder implements TransitionerBuilder<State, StateTransition> {

    /** The transitions map */
    private TransitionIndex<State, StateTransition> transitionIndex;

    /**
     * The constructor with default
     * transition index.
     */
    private StateMachineBuilder() {
        transitionIndex = new StateTransitionMap();
    }

    /**
     * Creates a builder with the given transitioner
     *
     * @param transitionIndex the transition index
     */
    private StateMachineBuilder(TransitionIndex<State, StateTransition> transitionIndex) {
        Objects.requireNonNull(transitionIndex);
        this.transitionIndex = transitionIndex;
    }

    /**
     * Retrieves a new {@link StateMachineBuilder} instance
     * with the default transitioner.
     *
     * @return a new StateMachineBuilder instance
     */
    public static StateMachineBuilder newBuilder() {
        return new StateMachineBuilder();
    }

    /**
     * Retrieves a new {@link StateMachineBuilder} instance
     * with the given transaitioner.
     *
     * @return a new StateMachineBuilder instance
     */
    public static StateMachineBuilder newBuilder(TransitionIndex<State, StateTransition> transitionIndex) {
        return new StateMachineBuilder(transitionIndex);
    }

    /**
     * Starts the building of a new
     * transition by specifying the source
     * state's name
     *
     * @param srcStateName the source state's name
     * @return the StateMachineFromBuilder machineBuilder clause
     */
    @Override
    public StateMachineFromBuilder from(String srcStateName) {
        return from(new State(srcStateName));
    }

    /**
     * Starts the building of a new
     * transition by specifying the source
     * state.
     *
     * @param srcState the source state
     * @return the StateMachineFromBuilder machineBuilder clause
     */
    @Override
    public StateMachineFromBuilder from(State srcState) {
        return new StateMachineFromBuilder(this, srcState);
    }

    /**
     * Adds the given transition to the transition map.
     *
     * @param transition the transition to add
     * @return the machineBuilder transitionIndex for chaining purposes.
     */
    @Override
    public StateMachineBuilder add(StateTransition transition) {
        Objects.requireNonNull(transition, "Cannot add null transition");
        this.transitionIndex.add(transition);
        return this;
    }

    /**
     * Adds the given transitions to the transition map.
     *
     * @param transitions the transitions to add
     * @return the machineBuilder transitionIndex for chaining purposes.
     */
    @Override
    public StateMachineBuilder addAll(Collection<StateTransition> transitions) {
        transitions.forEach(this::add);
        return this;
    }

    /**
     * Starts the definition of a loop
     * for the given state's name
     *
     * @param nodeName the state name
     * @return the StateMachineToBuilder machineBuilder clause
     */
    @Override
    public StateMachineToBuilder selfLoop(String nodeName) {
        return selfLoop(new State(nodeName));
    }

    /**
     * Starts a definition of a loop
     * for the given state
     *
     * @param state the state
     * @return the StateMachineToBuilder machineBuilder clause
     */
    @Override
    public StateMachineToBuilder selfLoop(State state) {
        return new StateMachineToBuilder(new StateMachineFromBuilder(this, state), state);
    }

    /**
     * Creates a new transitionIndex from the
     * transition map currently built.
     *
     * @return the new srcState machine with the current
     * transitions.
     */
    @Override
    public StateMachine build() {
        return new StateMachine(transitionIndex);
    }

    /**
     * Retrieves the current transition map.
     *
     * @return the transition map
     */
    @Override
    public TransitionIndex<State, StateTransition> getTransitionIndex() {
        return transitionIndex;
    }

    /**
     * Internal machineBuilder class to begin the definition
     * of a new transition.
     */
    public static class StateMachineFromBuilder implements IFromBuilder {

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
        private StateMachineFromBuilder(StateMachineBuilder machineBuilder, State srcState) {
            this.machineBuilder = machineBuilder;
            this.srcState = srcState;
        }

        /**
         * Adds a new target state to the
         * transition under build.
         *
         * @param srcNodeName the state
         * @return the machineBuilder StateMachineToBuilder clause
         */
        @Override
        public StateMachineToBuilder to(String srcNodeName) {
            return to(new State(srcNodeName));
        }

        /**
         * Adds a new target state by it's name
         * to the transition under build.
         *
         * @param targetStateName the target state's name
         * @return the StateMachineToBuilder machineBuilder clause
         */
        @Override
        public StateMachineToBuilder to(State targetStateName) {
            return new StateMachineToBuilder(this, targetStateName);
        }
    }

    /**
     * Internal machineBuilder class to specify
     * the target destination of a new transition.
     */
    public static class StateMachineToBuilder implements IToBuilder {

        /** The from machineBuilder instance */
        private final StateMachineFromBuilder fromBuilder;

        /** The target state */
        private final State targetState;

        /**
         * Creates a new StateMachineToBuilder clause for the machineBuilder
         * using the given from machineBuilder.
         *
         * @param fromBuilder the machineBuilder
         * @param targetState the target state
         */
        private StateMachineToBuilder(StateMachineFromBuilder fromBuilder, State targetState) {
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
        @Override
        public StateMachineOnBuilder on(String message) {
            return on(new StringMessage((message)));
        }

        /**
         * Ends the transition by applying a custom message and
         * retrieving the initial machineBuilder.
         *
         * @param message the custom message to add
         * @return the initial machineBuilder
         */
        @Override
        public StateMachineOnBuilder on(Message message) {
            return new StateMachineOnBuilder(this, message);
        }
    }

    /**
     * Internal machineBuilder class to end the definition
     * of a new transition.
     */
    public static class StateMachineOnBuilder implements IOnBuilder<State, StateTransition> {

        /** The to machineBuilder instance */
        private final StateMachineBuilder builder;

        /**
         * Creates a new StateMachineToBuilder clause for the machineBuilder
         * using the given from machineBuilder.
         *
         * @param toBuilder the to builder
         * @param message the target state
         */
        private StateMachineOnBuilder(StateMachineToBuilder toBuilder, Message message) {
            // Adds the transition to the builder
            this.builder = toBuilder.fromBuilder.machineBuilder;
            builder.add(new StateTransition(toBuilder.fromBuilder.srcState, message, toBuilder.targetState));
        }

        /**
         * Allows continuing the builder chain by
         * adding an additional transition
         *
         * @param srcStateName the origin state's name
         * @return the From builder clause
         */
        @Override
        public StateMachineFromBuilder and(String srcStateName) {
            return builder.from(srcStateName);
        }

        /**
         * Delegates the start of transition to the builder
         * wrapped instance.
         *
         * @param state the state to begin the transition
         * @return the from builder
         */
        @Override
        public StateMachineFromBuilder and(State state) {
            return builder.from(state);
        }

        /**
         * Delegates the start of transition to the builder
         * wrapped instance.
         *
         * @param srcStateName the state name to begin the transition
         * @return the from builder
         */
        @Override
        public StateMachineFromBuilder from(String srcStateName) {
            return builder.from(srcStateName);
        }

        /**
         * Delegates the start of transition to the builder
         * wrapped instance.
         *
         * @param state the state to begin the transition
         * @return the from builder
         */
        @Override
        public StateMachineFromBuilder from(State state) {
            return builder.from(state);
        }

        /**
         * Delegates the addition of the transition to the builder
         * wrapped instance.
         *
         * @param transition the transition to add
         * @return the from builder
         */
        @Override
        public StateMachineBuilder add(StateTransition transition) {
            return builder.add(transition);
        }

        /**
         * Delegates the addition of the transition to the builder
         * wrapped instance.
         *
         * @param transitions the transition to add
         * @return the from builder
         */
        @Override
        public StateMachineBuilder addAll(Collection<StateTransition> transitions) {
            return builder.addAll(transitions);
        }

        /**
         * Delegates the addition of the self loop to the builder
         * wrapped instance.
         *
         * @param stateName the state name to create the loop
         * @return the IToBuilder clause
         */
        @Override
        public StateMachineToBuilder selfLoop(String stateName) {
            return builder.selfLoop(stateName);
        }

        /**
         * Delegates the addition of the self loop to the builder
         * wrapped instance.
         *
         * @param state the state to create the loop
         * @return @return the IToBuilder clause
         */
        @Override
        public StateMachineToBuilder selfLoop(State state) {
            return builder.selfLoop(state);
        }

        /**
         * Delegates the addition of the self loop to the builder
         * wrapped instance.
         *
         * @return the from builder
         */
        @Override
        public StateMachine build() {
            return builder.build();
        }

        /**
         * Returns the transition index from the wrapped
         * builder.
         *
         * @return the transition index
         */
        @Override
        public TransitionIndex<State, StateTransition> getTransitionIndex() {
            return builder.getTransitionIndex();
        }

    }

}
