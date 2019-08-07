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
package com.github.pnavais.machine.impl;

import com.github.pnavais.machine.api.Transition;
import com.github.pnavais.machine.model.State;
import com.github.pnavais.machine.api.Message;
import com.github.pnavais.machine.api.transition.TransitionIndex;
import com.github.pnavais.machine.api.exception.NullStateException;
import com.github.pnavais.machine.api.exception.NullTransitionException;
import com.github.pnavais.machine.model.StateTransition;
import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The State Machine contains a simple map of Transitions between
 * different nodes (States) triggered by incoming messages.
 *
 * This transition index is implemented as a map using as key
 * the source of the transition and as key another map containing
 * the association between messages and destination states.
 * <p><p>
 * For example, the transition [ A -- m --> B ] would be stored in a map
 * as represented in the table below :
 * </p></p>
 * <pre>
 * Key    | Transitions
 * --------------------
 * A      | [ m ->  B ]
 * B      | []
 * </pre>
 * It is important to note that target states will be also stored
 * as key in the transitions map.
 *
 * In case a new transition from A is added , [ A --- n ---> C ] ,
 * the transitions would be updated as :
 * <pre>
 * Key    | Transitions
 * --------------------
 * A      | [ m ->  B,  n ->  C ]
 * B      | []
 * C      | []
 * </pre>
 *
 * In case a transition is added from this state using
 * the same message [ A --- m ---> C, the transition map would be updated as follows :
 * <pre>
 * Key    | Transitions
 * --------------------
 * A      | [ m ->  C,  n ->  C ]
 * B      | []
 * C      | []
 * </pre>
 *
 * After this operation State B is not reachable
 */
@Getter
public class StateTransitionMap implements TransitionIndex<State, Message, StateTransition> {

    /**
     * The transitions stored by the state machine
     */
    private Map<State, Map<Message, State>> transitionMap;

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
        Map<Message, State> messageStateMap = Optional.ofNullable(transitionMap.get(transition.getOrigin())).orElse(new LinkedHashMap<>());

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
    @Override
    public void remove(State state) {
        Objects.requireNonNull(state);

        // Remove transition mappings
        Map<Message, State> messageStateMap = Optional.ofNullable(transitionMap.get(state))
                .orElseThrow(() -> new NullTransitionException("State [" + state + "] does not exist"));
        messageStateMap.clear();

        // Remove state
        transitionMap.remove(state);

        // Remove transitions using the state as target
        transitionMap.values().forEach(m -> m.values().removeIf(s -> s.getName().equals(state.getName())));
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
    public Optional<State> getNext(State source, Message m) {
        Objects.requireNonNull(source, "The source state cannot be null");
        Objects.requireNonNull(m, "The message cannot be null");

        return Optional.ofNullable(transitionMap.get(source)).map(messageStateMap -> messageStateMap.get(m));
    }

    /**
     * Retrieves the previous node upon reception of the
     * message on the given source node.
     *
     * @param source the origin node
     * @param m the message
     * @return the next node if found or empty otherwise
     */
    public Optional<State> getPrevious(State source, Message m) {
        return transitionMap.keySet().stream().filter(state -> source.equals(transitionMap.get(state).get(m))).findFirst();
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
     * Remove orphan states from the transition map
     * (i.e. States not involved in transitions)
     */
    @Override
    public void prune() {
        transitionMap.forEach((state, messageStateMap) -> {
            //messageStateMap
        });
    }

    /**
     * Retrieves the transitions from the given state
     * or throws a {@link NullStateException}
     * if not found.
     * @param stateName the state's name
     *
     * @return the transitions
     */
    @Override
    public Collection<StateTransition> getTransitions(String stateName) {

        State origin = find(stateName).orElseThrow(() -> new NullStateException("State [" + stateName + "] not found"));
        Map<Message, State> messageStateMap = transitionMap.get(origin);

        return messageStateMap.keySet().stream()
                     .map(message -> new StateTransition(origin, message, messageStateMap.get(message)))
                     .collect(Collectors.toList());
    }

    /**
     * Retrieves the transitions as a map
     *
     * @return the transitions as a map
     */
    @Override
    public Map<State, Map<Message, State>> getTransitionsAsMap() {
        return transitionMap;
    }
}
