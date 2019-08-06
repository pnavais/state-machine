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

import com.github.pnavais.machine.api.Transition;
import com.github.pnavais.machine.api.Message;
import com.github.pnavais.machine.api.Node;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * An index allowing to store and fetch defined transitions.
 *
 * @param <N> the type of Node
 * @param <M> the type of Message
 * @param <T> the type of Transition
 */
public interface TransitionIndex<N extends Node, M extends Message, T extends Transition<N>> {

    /**
     * Adds a new transition to the index
     *
     * @param transition the transition to add
     */
    void add(T transition);

    /**
     * Removes an existing transition from the index
     *
     * @param transition the transition to remove
     */
    void remove(T transition);

    /**
     * Removes an existing node from the index
     * and all its transitions.
     *
     * @param node the node to remove
     */
    void remove(N node);

    /**
     * Retrieves the next node after applying the
     * message on the given source node.
     *
     * @param source the origin node
     * @param m the message
     * @return the next node if found or empty otherwise
     */
    Optional<N> getNext(N source, M m);

    /**
     * Search the given node in the transition index
     * by its name.
     *
     * @param name the node's name to search
     * @return the node found or empty otherwise
     */
    Optional<N> find(String name);

    /**
     * Retrieves the first node in the index
     *
     * @return the first element in the index or empty otherwise
     */
    Optional<N> getFirst();

    /**
     * Retrieves the number of elements in the index
     *
     * @return the number of elements in the index
     */
    int size();

    /**
     * Remove orphan states from the index
     * (i.e. States that are not reachable)
     */
    void prune();

    /**
     * Retrieves the transitions stored in the index
     * for the given node.
     *
     * @param name the node's name
     * @return the transitions for the node
     */
    Collection<T> getTransitions(String name);

    /**
     * Retrieves the transitions as a map
     *
     * @return the transition as a map
     */
     Map<N, Map<M, N>> getTransitionsAsMap();
}
