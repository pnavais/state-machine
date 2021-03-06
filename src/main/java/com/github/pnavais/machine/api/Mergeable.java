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

package com.github.pnavais.machine.api;

/**
 * Defines the method to be implemented by
 * classes interested in merging its current
 * state (i.e. model) with a given Node one.
 *
 * @param <N> the type of node
 */
public interface Mergeable<N extends Node> {

    /**
     * Merges the information contained by the given node
     * returning a merged node.
     *
     * @param node the node to be merged
     * @return the merged node
     */
    N merge(N node);
}
