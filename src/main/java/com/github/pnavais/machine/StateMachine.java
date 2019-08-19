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

import com.github.pnavais.machine.api.Status;
import com.github.pnavais.machine.api.exception.NullStateException;
import com.github.pnavais.machine.api.message.Envelope;
import com.github.pnavais.machine.api.message.Event;
import com.github.pnavais.machine.api.message.Message;
import com.github.pnavais.machine.api.message.Messages;
import com.github.pnavais.machine.api.transition.TransitionChecker;
import com.github.pnavais.machine.api.transition.TransitionIndex;
import com.github.pnavais.machine.api.transition.Transitioner;
import com.github.pnavais.machine.builder.StateMachineBuilder;
import com.github.pnavais.machine.impl.StateTransitionChecker;
import com.github.pnavais.machine.impl.StateTransitionMap;
import com.github.pnavais.machine.model.*;
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
    private TransitionChecker<State, Message> transitionChecker;

    /**
     * Creates the state machine.
     */
    public StateMachine() {
        this(new StateTransitionMap());
    }

    /**
     * Creates the state machine with the given
     * transition map.
     *
     * @param transitionIndex the transition map
     */
    public StateMachine(@NonNull TransitionIndex<State, Message, StateTransition> transitionIndex) {
        this(transitionIndex, new StateTransitionChecker());
    }

    /**
     * Creates the state machine with the given
     * transition checker.
     *
     * @param transitionChecker the transition checker
     */
    public StateMachine(@NonNull TransitionChecker<State, Message> transitionChecker) {
        this(new StateTransitionMap(), transitionChecker);
    }

    /**
     * Creates the state machine with the given
     * transition map and checker
     *
     * @param transitionIndex the transition map
     * @param transitionChecker the transition checker
     */
    public StateMachine(@NonNull TransitionIndex<State, Message, StateTransition> transitionIndex,
                        @NonNull TransitionChecker<State, Message> transitionChecker) {
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
     * Adds a collection of Transition to the state
     * machine. If already present, they are
     * replaced silently.
     *
     * @param transitions the transition to add
     */
    @Override
    public void addAll(@NonNull Collection<StateTransition> transitions) {
        this.transitionsIndex.addAll(transitions);
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
    public void setCurrent(@NonNull State state) {
        setCurrent(state.getName());
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
        return getNext(m, true);
    }

    /**
     * Retrieves the next state upon
     * message reception. In case
     * the current state is not defined
     * or no next state is defined , an empty state
     * is returned.
     *
     * @param m the message
     * @param handleDeparture flag to control departure handling
     *
     * @return the next state or empty if not found
     */
    private Optional<State> getNext(Message m, boolean handleDeparture) {
        // Obtain next state
        Optional<State> targetState = obtainTargetState(m);

        // If available do the processing
        if (targetState.isPresent()) {
            // Create the envelope
            SimpleEnvelope envelope = SimpleEnvelope.builder()
                    .source(currentState)
                    .target(targetState.get())
                    .message(m)
                    .transitionIndex(transitionsIndex)
                    .build();

            // Validates departure/arrival from current state to target state
            InfoStatus infoStatus = handleMessageFiltering(envelope, handleDeparture);

            // Handles potential redirection on departure/arrival
            if (infoStatus.getStatus().isRedirect()) {
                // Update state before redirection
                currentState = (infoStatus.getEvent() != Event.DEPARTURE) ? targetState.get() : currentState;
                targetState = getNext(infoStatus.getStatus().getMessage(), (infoStatus.getEvent() != Event.DEPARTURE));
            }

            // Update current state only if transitions are successful
            if (!infoStatus.getStatus().isValid()) {
                targetState = Optional.empty();
            } else targetState.ifPresent(state -> currentState = state);
        }

        return targetState;
    }

    /**
     * Applies the departure and arrival validation functions
     * using the given envelope.
     * @param envelope the envelope containing the message
     * @return the status after validation
     */
    private InfoStatus handleMessageFiltering(Envelope<State, Message> envelope, boolean handleDeparture) {
        Event event = Event.DEPARTURE;
        Status status = handleDeparture ? transitionChecker.validateDeparture(envelope) : Status.PROCEED;

        // Validates arrival to next state
        if (status.isValid() && !status.isRedirect()) {
            status = transitionChecker.validateArrival(envelope);
            event = Event.ARRIVAL;
        }

        return InfoStatus.from(status, event);
    }

    /**
     * Obtains the potential target state for the given message
     * using the optional fallback (*) in case direct transition
     * not found.
     * @param m the message
     * @return the potential target state
     */
    private Optional<State> obtainTargetState(Message m) {
        Optional<State> targetState = transitionsIndex.getNext(currentState, m);

        // Check if ANY mapping is available as fallback
        return !(targetState.isPresent()) ? transitionsIndex.getNext(currentState, Messages.ANY) : targetState;
    }

    /**
     * Sends a void message to the state machine triggering
     * a potential transition.
     */
    @Override
    public StateMachine next() {
        return send(Messages.EMPTY);
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
