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
package com.github.pnavais.machine;

import com.github.pnavais.machine.api.Message;
import com.github.pnavais.machine.api.Status;
import com.github.pnavais.machine.api.exception.NullStateException;
import com.github.pnavais.machine.api.transition.TransitionChecker;
import com.github.pnavais.machine.api.transition.TransitionIndex;
import com.github.pnavais.machine.api.transition.Transitioner;
import com.github.pnavais.machine.builder.StateMachineBuilder;
import com.github.pnavais.machine.impl.StateTransitionChecker;
import com.github.pnavais.machine.impl.StateTransitionMap;
import com.github.pnavais.machine.model.State;
import com.github.pnavais.machine.model.StateTransition;
import com.github.pnavais.machine.model.StringMessage;
import lombok.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * The State Machine contains a simple map of Transitions between
 * different nodes (States) triggered by incoming messages.
 */
public class StateMachine implements Transitioner<State, Message, StateTransition> {

    /**
     * The current state
     */
    private State currentState;

    /**
     * The transitions stored by the state machine
     */
    private TransitionIndex<State, Message, StateTransition> transitionsIndex;

    /**
     * The validator used to check transitions
     */
    private TransitionChecker<State, Message, StateTransition> transitionChecker;

    /**
     * Creates the state machine.
     */
    public StateMachine() {
        this.transitionsIndex = new StateTransitionMap();
        this.transitionChecker = new StateTransitionChecker();
    }

    /**
     * Creates the state machine with the given
     * transition map.
     *
     * @param transitionIndex the transition map
     */
    public StateMachine(@NonNull TransitionIndex<State, Message, StateTransition> transitionIndex) {
        this.transitionsIndex = transitionIndex;
    }

    /**
     * Creates the state machine with the given
     * transition map.
     *
     * @param transitionIndex the transition map
     */
    public StateMachine(@NonNull TransitionIndex<State, Message, StateTransition> transitionIndex,
                        @NonNull TransitionChecker<State, Message, StateTransition> transitionChecker) {
        this.transitionsIndex = transitionIndex;
        this.transitionChecker = transitionChecker;
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
     * Removes all existing Transitions from the state machine.
     */
    @Override
    public void removeAllTransitions() {
        this.transitionsIndex.removeAllTransitions();
    }

    /**
     * Removes all transitions from the state machine
     */
    @Override
    public void clear() {
        this.transitionsIndex.clear();
    }

    /**
     * Finds the state referenced by the given name
     * in the state machine
     * machine.
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
    public void setCurrent(@NonNull String stateName) {
        this.currentState = this.transitionsIndex.find(stateName).orElseThrow(() -> new NullStateException("State ["+stateName+"] not found"));
    }

    /**
     * Sets the current state if present
     * @param state the state to set
     */
    public State setCurrent(@NonNull State state) {
        if (find(state.getName()).isPresent()) {
            this.currentState = state;
        }
        return this.currentState;
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
     * @param m the message
     *
     * @return the next state or empty if not found
     */
    @Override
    public Optional<State> getNext(Message m) {
        Optional<State> targetState = Optional.empty();

        // Validates departure from current state
        Status status = transitionChecker.validateDeparture(transitionsIndex, m, currentState);

        // Validates arrival to next state
        if (status.equals(Status.PROCEED)) {
            targetState = transitionsIndex.getNext(currentState, m);
            status = targetState.isPresent() ? transitionChecker.validateArrival(transitionsIndex, m, targetState.get()) : Status.ABORT;
        }

        // Handles potential redirection on departure/arrival
        if (status.isRedirect()) {
            targetState = getNext(status.getMessage());
        }

        // Update current state if transitions are successful
        if (targetState.isPresent() && (status.equals(Status.PROCEED))) {
            currentState = targetState.get();
        }

        return targetState;
    }

    /**
     * Sends a message to the state machine triggering
     * a potential transition.
     *
     * @param message a string message
     * @return the state machine for chaining purposes
     */
    public StateMachine send(String message) {
        return send(StringMessage.from(message));
    }

    /**
     * Sends a message to the state machine triggering
     * a potential transition.
     *
     * @param message the message
     * @return the state machine for chaining purposes
     */
    @Override
    public StateMachine send(Message message) {
        getNext(message);
        return this;
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
     * Retrieves all state transitions currently
     * defined.
     *
     * @return all defined transitions
     */
    @Override
    public Collection<StateTransition> getAllTransitions() {
        return transitionsIndex.getAllTransitions();
    }

    /**
     * Retrieves the transition map
     *
     * @return the transition map
     */
    @Override
    public TransitionIndex<State, Message, StateTransition> getTransitionsIndex() {
        return transitionsIndex;
    }

    /**
     * Remove orphan states
     */
    @Override
    public List<State> prune() {
        return transitionsIndex.prune();
    }

}
