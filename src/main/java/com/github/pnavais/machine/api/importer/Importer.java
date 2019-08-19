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

package com.github.pnavais.machine.api.importer;

import com.github.pnavais.machine.api.Node;
import com.github.pnavais.machine.api.Transition;
import com.github.pnavais.machine.api.message.Message;
import com.github.pnavais.machine.api.transition.Transitioner;

import java.nio.file.Path;

/**
 * Defines the methods to import a {@link Transitioner} from
 * a given input source.
 *
 * @param <I> the input type
 * @param <T> the type ot transitioner
 */
public interface Importer<I, N extends Node, M extends Message, T extends Transitioner<N, M, ? extends Transition<N,M>>> {

    /**
     * Parses the given input format containing
     * the information allowing to deserialize
     * the transitioner.
     *
     * @param input the input format
     * @return the build transitioner
     */
    T parse(I input);

    /**
     * Parses the contents of the input
     * file containing the information allowing
     * to build the transitioner.
     *
     * @param inputFile the input file
     */
    T parseFile(Path inputFile);
}
