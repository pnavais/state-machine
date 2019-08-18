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

package com.github.pnavais.machine.api.exporter;

import com.github.pnavais.machine.api.Node;
import com.github.pnavais.machine.api.Transition;
import com.github.pnavais.machine.api.message.Message;
import com.github.pnavais.machine.api.transition.Transitioner;

import java.nio.file.Path;

/**
 * Defines the methods to export a {@link Transitioner} to
 * a given result type.
 *
 * @param <R> the result type
 * @param <T> the type ot transitioner
 */
public interface Exporter<R, N extends Node, M extends Message, T extends Transitioner<N, M, ? extends Transition<N,M>>> {

    /**
     * Exports the current contents of the transitioner
     * to the given output format.
     *
     * @param transitioner the transitioner to export
     * @return the output format
     */
    R export(T transitioner);

    /**
     * Exports the current contents of the transitioner
     * to the given output file.
     *
     * @param transitioner the transitioner to export
     * @param outputFile the output file
     */
    void exportToFile(T transitioner, Path outputFile);
}
