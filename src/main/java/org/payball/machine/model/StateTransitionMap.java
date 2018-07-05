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

import org.payball.machine.api.Message;
import org.payball.machine.api.Transition;
import org.payball.machine.api.TransitionIndex;
import org.payball.machine.api.exception.NullTransitionException;

import java.util.*;

/**
 * The State Machine contains a simple map of Transitions between
 * different nodes (States) triggered by incoming messages.
 */
public class StateTransitionMap implements TransitionIndex<State, StateTransition> {

    /**
     * The transitions stored by the state machine
     */
    private Map<State, Map<Message<?>, State>> transitionMap;

    /**
     * Creates the state machine.
     */
    public StateTransitionMap() {
        this.transitionMap = new LinkedHashMap<>();
    }

    /**
     * Adds a new Transition to the statemachine.
     * If already present, it is replaced silently
     * i.e if a transition is found from a source state with
     * the input message, the destination is updated by the
     * one given in the transition.
     *
     * @param transition the transition to add
     */
    @Override
    public void add(StateTransition transition) {
        Transition.validate(transition);

        // Retrieve the current transitions mapping
        Map<Message<?>, State> messageStateMap = Optional.ofNullable(transitionMap.get(transition.getOrigin())).orElse(new LinkedHashMap<>());

        // Update origin with mappings
        messageStateMap.put(transition.getMessage(), find(transition.getTarget().getName()).orElse(transition.getTarget()));

        // Update the transition map
        if (!transitionMap.containsKey(transition.getOrigin())) {
            transitionMap.put(transition.getOrigin(), messageStateMap);
        }

        // Add target to the index if not found
        if (!transitionMap.containsKey(transition.getTarget())) {
            transitionMap.put(transition.getTarget(), new LinkedHashMap<>());
        }

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
        Transition.validate(transition);

        // Update the current transitions mapping
        Optional.ofNullable(transitionMap.get(transition.getOrigin())).ifPresent(m -> m.remove(transition.getMessage()));
    }

    /**
     * Removes the current state from the machine
     * including defined transitions.
     *
     * @param state the state to remove
     */
    public void remove(State state) {
        Objects.requireNonNull(state);

        // Remove transition mappings
        Map<Message<?>, State> messageStateMap = Optional.ofNullable(transitionMap.get(state))
                .orElseThrow(() -> new NullTransitionException("State [" + state + "] does not exist"));
        messageStateMap.clear();

        // Remove state
        transitionMap.remove(state);
    }

    /**
     * Retrieves the transition map
     *
     * @return the transition map
     */
    public Map<State, Map<Message<?>, State>> getTransitionMap() {
        return transitionMap;
    }

    /**
     * Retrieves the next state in the transition from
     * source state upon message m reception
     * @param source the origin state
     * @param m the received message
     *
     * @return the next state if found or empty otherwise
     */
    @Override
    public Optional<State> getNext(State source, Message<?> m) {
        Objects.requireNonNull(source, "The source state cannot be null");
        Objects.requireNonNull(m, "The message cannot be null");

        return Optional.ofNullable(transitionMap.get(source)).map(messageStateMap -> messageStateMap.get(m));
    }

    /**
     * Finds the given state by its name.
     *
     * @param stateName the name of the state
     *
     * @return the state if found or empty otherwise
     */
    @Override
    public Optional<State> find(String stateName) {
        return transitionMap.keySet().stream().filter(state -> state.getName().compareTo(stateName) == 0).findFirst();
    }

    /**
     * Retrieves the first state in the transition map
     *
     * @return the first state in the transition map
     */
    @Override
    public Optional<State> getFirst() {
        return Optional.ofNullable(transitionMap.keySet().iterator().next());
    }

    /**
     * Retrieves the number of states in the transition map
     *
     * @return the number of states in the transition map
     */
    @Override
    public int size() {
        return transitionMap.size();
    }

    /**
     * Retrieves the transitions from the given state
     * or throws a {@link org.payball.machine.api.exception.NullStateException}
     * if not found.
     * @param stateName the state's name
     *
     * @return
     */
    @Override
    public Collection<StateTransition> getTransitions(String stateName) {
         /*Optional.ofNullable(transitionMap.get(stateName)).ifPresent(m -> {
           m.forEach((message, state) -> {

           };
         }).orElseThrow(() -> new NullStateException("The state [" + stateName + "] was not found")).;*/
        throw new UnsupportedOperationException("Not implemented yet!");
    }

}
