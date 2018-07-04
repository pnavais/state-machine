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

package org.payball.machine;

import org.payball.machine.api.Message;
import org.payball.machine.api.TransitionIndex;
import org.payball.machine.api.Transitioner;
import org.payball.machine.api.exception.NullStateException;
import org.payball.machine.builder.StateMachineBuilder;
import org.payball.machine.model.State;
import org.payball.machine.model.StateTransition;
import org.payball.machine.model.StateTransitionMap;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

/**
 * The State Machine contains a simple map of Transitions between
 * different nodes (States) triggered by incoming messages.
 */
public class StateMachine implements Transitioner<State, StateTransition> {

    /**
     * The current state
     */
    private State currentState;

    /**
     * The transitions stored by the state machine
     */
    private TransitionIndex<State, StateTransition> transitionsMap;

    /**
     * Creates the state machine.
     */
    public StateMachine() {
        this.transitionsMap = new StateTransitionMap();
    }

    /**
     * Creates the state machine with the given
     * transition map.
     *
     * @param transitionMap the transition map
     */
    public StateMachine(StateTransitionMap transitionMap) {
        Objects.requireNonNull(transitionMap, "Null transitions map supplied");
        this.transitionsMap = transitionMap;
    }

    /**
     * Retrieves a new {@link StateMachineBuilder} instance
     * to ease the State Machine creation process.
     *
     * @return a new StateMachineBuilder instance
     */
    public static StateMachineBuilder newBuilder() {
        return new StateMachineBuilder();
    }

    /**
     * Adds a new Transition to the state
     * machine. If already present, it is
     * replaced silently.
     *
     * @param transition the transition to add
     */
    @Override
    public void add(StateTransition transition) {
        this.transitionsMap.add(transition);
    }

    /**
     * Removes an existing Transition from the state machine.
     * In case the state does not exists is it ignored
     * silently.
     *
     * @param transition the transition to remove
     */
    @Override
    public void remove(StateTransition transition) {
        this.transitionsMap.remove(transition);
    }

    /**
     * Initializes the current state
     * to the first one added to the state machine (if any).
     */
    @Override
    public void init() {
        this.currentState = this.transitionsMap.getFirst().get();
    }

    /**
     * Sets the current state to the
     * given one. Throws a {@link NullStateException}
     * in case the state is not found.
     */
    @Override
    public void setCurrent(String stateName) {
        Objects.requireNonNull(stateName);
        this.currentState = this.transitionsMap.find(stateName).orElseThrow(() -> new NullStateException("State ["+stateName+"] not found"));
    }

    /**
     * Retrieves the next state upon
     * message reception. In case
     * the current state is not defined
     * or no next state is defined , an empty state
     * is returned.
     *
     * @param m the message
     *
     * @return the next state or empty if not found
     */
    @Override
    public Optional<State> getNext(Message<?> m) {
        return transitionsMap.getNext(currentState, m);
    }

    /**
     * Retrieves the number of states currently
     * present in the state machine.
     *
     * @return the number of states of the state machine
     */
    @Override
    public int size() {
        return transitionsMap.size();
    }

    /**
     * Retrieves the state transitions for the given
     * state.
     *
     * @param stateName the state name
     *
     * @return the transitions from the given state
     */
    @Override
    public Collection<StateTransition> getTransitions(String stateName) {
        return transitionsMap.getTransitions(stateName);
    }

    /**
     * Retrieves the transition map
     *
     * @return the transition map
     */
    @Override
    public TransitionIndex<State,StateTransition> getTansitionsIndex() {
        return transitionsMap;
    }
}
