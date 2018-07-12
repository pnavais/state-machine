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
package org.payball.machine.api.transition;

import org.payball.machine.api.Message;
import org.payball.machine.api.Node;
import org.payball.machine.api.Transition;

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
     * Adds all transitions contained in the
     * given index.
     * @param index the index to copy from
     */
    void addAll(TransitionIndex<K, T> index);

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
    void remove(K node);

    /**
     * Finds the node in the transition
     * associated with the give name.
     *
     * @param nodeName the node's name
     * @return the node with the given name
     */
    Optional<K> find(String nodeName);

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
    K getCurrent();

    /**
     * Retrieves the next node upon
     * transitioning from the current node
     * on message key reception.
     *
     * @param messageKey the message key
     * @return the next node
     */
    Optional<K> getNext(String messageKey);

    /**
     * Retrieves the next node upon
     * transitioning from the current node
     * on message reception.
     *
     * @param m the message
     * @return the next node
     */
    Optional<K> getNext(Message m);

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
     * Retrieves the transitions currently
     * defined in the transitioner.
     *
     * @return the transitions
     */
    Collection<T> getTransitions();

    /**
     * Retrieves the transition index used
     * by the transition.
     *
     * @return the transition index
     */
    TransitionIndex<K,T> getTransitionsIndex();


}
