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
package com.github.pnavais.machine.api.transition;

import com.github.pnavais.machine.api.Node;
import com.github.pnavais.machine.api.Transition;
import com.github.pnavais.machine.api.message.Message;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * An interface for defining methods common to entities
 * handling transitions between nodes.
 *
 * @param <N> the type of nodes
 * @param <M> the type of messages
 * @param <T> the type of transitions
 */
public interface Transitioner<N extends Node, M extends Message, T extends Transition<N,M>>  {

    /**
     * Adds a new transition
     *
     * @param transition the transition to add
     */
    void add(T transition);

    /**
     * Adds a collection of Transition to the state
     * machine.
     *
     * @param transitions the transitions to add
     */
    void addAll(Collection<T> transitions);

    /**
     * Removes an existing transition
     *
     * @param transition the transition to remove
     */
    void remove(T transition);

    /**
     * Removes a node matching the given name
     * and allits transitions
     *
     * @param nodeName the node mame to remove
     */
    void remove(String nodeName);

    /**
     * Removes an existing node and all
     * its transitions.
     *
     * @param node the node to remove
     */
    void remove(N node);

    /**
     * Removes all existing Transitions from the state machine.
     */
    void removeAllTransitions();

    /**
     * Removes all transitions from the state machine
     */
    void clear();

    /**
     * Finds the node in the transition
     * associated with the give name.
     *
     * @param nodeName the node's name
     * @return the node with the given name
     */
    Optional<N> find(String nodeName);

    /**
     * Performs generic initialization e.g.
     * setting the initial node.
     */
    void init();

    /**
     * Sets the current node to the one
     * supplied by the the given name.
     *
     * @param nodeName the node's name
     */
    void setCurrent(String nodeName);

    /**
     * Retrieves the node
     * of the last transition performed.
     *
     * @return the current node
     */
    N getCurrent();

    /**
     * Retrieves the next node upon
     * transitioning from the current node
     * on message reception.
     *
     * @param m the message
     * @return the next node
     */
    Optional<N> getNext(M m);

    /**
     * Sends an empty message to the transitioner triggering
     * a potential transition in case the current node
     * supports empty messages.
     */
    Transitioner<N, M, T> next();

    /**
     * Sends a message to the transitioner triggering
     * a potential transition.
     *
     * @param message the message
     * @return the transitioner for chaining purposes
     */
    Transitioner<N, M, T> send(M message);

    /**
     * Retrieves the number of nodes currently
     * handled by the transition.
     *
     * @return the number of elements
     */
    int size();

    /**
     * Retrieves the transitions currently
     * defined for the given node.
     *
     * @param nodeName the node's name
     * @return the transitions for the given node
     */
    Collection<T> getTransitions(String nodeName);

    /**
     * Retrieves all state transitions currently
     * defined.
     *
     * @return all defined transitions
     */
    Collection<T> getAllTransitions();

    /**
     * Retrieves the transition index used
     * by the transition.
     *
     * @return the transition index
     */
    TransitionIndex<N, M, T> getTransitionsIndex();

    /**
     * Remove orphan nodes.
     * i.e. With no transitions and involved in no
     * other node transitions.
     *
     * @return the list of removed nodes
     */
    List<N> prune();
}
