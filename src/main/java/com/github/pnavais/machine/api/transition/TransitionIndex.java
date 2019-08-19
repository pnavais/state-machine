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
import lombok.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * An index allowing to store and fetch defined transitions.
 *
 * @param <N> the type of Node
 * @param <M> the type of Message
 * @param <T> the type of Transition
 */
public interface TransitionIndex<N extends Node, M extends Message, T extends Transition<N,M>> {

    /**
     * Adds a new transition to the index
     *
     * @param transition the transition to add
     */
    void add(T transition);

    /**
     * Adds all supplied transitions to the index.
     * @param transitions the transitions to add
     */
    void addAll(@NonNull Collection<T> transitions);

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
     * Removes an existing node from the index
     * and all its transitions by its name.
     *
     * @param node the node to remove
     */
    void remove(String node);

    /**
     * Removes all transitions from the map
     */
    void removeAllTransitions();

    /**
     * Removes all nodes and transitions
     */
    void clear();

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
     * Retrieves the previous node upon reception of the
     * message on the given source node.
     *
     * @param source the origin node
     * @param m the message
     * @return the next node if found or empty otherwise
     */
    Optional<N> getPrevious(N source, M m);

    /**
     * Search the given node in the transition index
     * by its name.
     *
     * @param name the node's name to search
     * @return the node found or empty otherwise
     */
    Optional<N> find(String name);

    /**
     * Checks the presence of the given node in
     * the index.
     *
     * @param node the node to find
     * @return true if node present, false otherwise
     */
    boolean contains(N node);

    /**
     * Checks the presence of the given transition in
     * the index.
     *
     * @param transition the transition to find
     * @return true if transition present, false otherwise
     */
    boolean contains(T transition);

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
     * Remove orphan nodes from the index
     * (i.e. Nodes that are not connected to other nodes
     * by any transition)
     *
     * @return the list of removed nodes
     */
    List<N> prune();

    /**
     * Retrieves the transitions stored in the index
     * for the given node.
     *
     * @param node the node
     * @return the transitions for the node
     */
    Collection<T> getTransitions(N node);
    /**
     * Retrieves the transitions stored in the index
     * for the given named node.
     *
     * @param name the node's name
     * @return the transitions for the node
     */
    Collection<T> getTransitions(String name);

    /**
     * Retrieve all transitions stored in the index.
     *
     * @return all transitions currently stored in the index
     */
    Collection<T> getAllTransitions();

    /**
     * Retrieves the transitions as a map
     *
     * @return the transition as a map
     */
     Map<N, Map<M, N>> getTransitionsAsMap();

}
