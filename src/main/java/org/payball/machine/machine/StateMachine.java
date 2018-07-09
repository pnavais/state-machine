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
package org.payball.machine.machine;

import org.payball.machine.machine.api.Message;
import org.payball.machine.machine.api.exception.NullStateException;
import org.payball.machine.machine.api.transition.TransitionIndex;
import org.payball.machine.machine.api.transition.Transitioner;
import org.payball.machine.machine.model.State;
import org.payball.machine.machine.model.StateTransition;
import org.payball.machine.machine.model.StateTransitionMap;
import org.payball.machine.machine.model.StringMessage;

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
    private TransitionIndex<State, StateTransition> transitionsIndex;

    /**
     * Creates the state machine.
     */
    public StateMachine() {
        this.transitionsIndex = new StateTransitionMap();
    }

    /**
     * Creates the state machine with the given
     * transition map.
     *
     * @param transitionIndex the transition map
     */
    public StateMachine(TransitionIndex<State, StateTransition> transitionIndex) {
        Objects.requireNonNull(transitionIndex, "Null transitions index supplied");
        this.transitionsIndex = transitionIndex;
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
        this.transitionsIndex.add(transition);
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
        this.transitionsIndex.remove(transition);
    }

    /**
     * Removes an existing state from the state machine.
     * All Transitions having state as origin or target
     * will be removed.
     * In case the state does not exists a {@link NullStateException}
     * is thrown.
     *
     * @param stateName the state to remove
     */
    @Override
    public void remove(String stateName) {
        this.transitionsIndex.find(stateName).ifPresent(s -> this.transitionsIndex.remove(s));
    }

    /**
     * Removes an existing state from the state machine.
     * All Transitions having state as origin or target
     * will be removed.
     * In case the state does not exists a {@link NullStateException}
     * is thrown.
     *
     * @param state the state to remove
     */
    @Override
    public void remove(State state) {
        this.transitionsIndex.remove(state);
    }

    /**
     * Finds the state referenced by the given name
     * in the state machine
     * machine. If already present, it is
     * replaced silently.
     *
     * @param stateName the name of the node
     */
    @Override
    public Optional<State> find(String stateName) {
        return this.transitionsIndex.find(stateName);
    }

    /**
     * Initializes the current state
     * to the first one added to the state machine (if any).
     */
    @Override
    public void init() {
        this.currentState = this.transitionsIndex.getFirst().orElse(null);
    }

    /**
     * Sets the current state to the
     * given one. Throws a {@link NullStateException}
     * in case the state is not found.
     */
    @Override
    public void setCurrent(String stateName) {
        Objects.requireNonNull(stateName, "State name cannot be null");
        this.currentState = this.transitionsIndex.find(stateName).orElseThrow(() -> new NullStateException("State ["+stateName+"] not found"));
    }

    /**
     * Retrieves the current state
     */
    @Override
    public State getCurrent() {
        return currentState;
    }

    /**
     * Retrieves the next state upon
     * message reception. In case
     * the current state is not defined
     * or no next state is defined , an empty state
     * is returned.
     *
     * @param messageKey the message key
     *
     * @return the next state or empty if not found
     */
    @Override
    public Optional<State> getNext(String messageKey) {
        return getNext(StringMessage.from(messageKey));
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
    public Optional<State> getNext(Message m) {
        Optional<State> next = transitionsIndex.getNext(currentState, m);
        next.ifPresent(state -> currentState = state);
        return next;
    }

    /**
     * Retrieves the number of states currently
     * present in the state machine.
     *
     * @return the number of states of the state machine
     */
    @Override
    public int size() {
        return transitionsIndex.size();
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
        return transitionsIndex.getTransitions(stateName);
    }

    /**
     * Retrieves the transition map
     *
     * @return the transition map
     */
    @Override
    public TransitionIndex<State,StateTransition> getTransitionsIndex() {
        return transitionsIndex;
    }

}
