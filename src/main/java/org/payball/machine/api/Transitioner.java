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
package org.payball.machine.api;

import java.util.Collection;
import java.util.Optional;

/**
 * An interface for defining methods common to entities
 * handling transitions between nodes.
 *
 * @param <K> the type of nodes
 * @param <T> the type of transitions
 */
public interface Transitioner<K extends Node, T extends Transition<K>>  {

    /**
     * Adds a new transition
     *
     * @param transition the transition to add
     */
    void add(T transition);

    /**
     * Removes an existing transition
     *
     * @param transition the transition to remove
     */
    void remove(T transition);

    /**
     * Performs generic initialization e.g.
     * setting the current initial node.
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
     * Retrieves the next node upon
     * transitioning from the current node
     * on message reception.
     *
     * @param m the message
     * @return the next node
     */
    Optional<K> getNext(Message<?> m);

    /**
     * Retrieves the number of nodes currently
     * handled by the transitioner.
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
     * Retrieves the transition index used
     * by the transitioner.
     *
     * @return the transition index
     */
    TransitionIndex<K,T> getTansitionsIndex();
}
