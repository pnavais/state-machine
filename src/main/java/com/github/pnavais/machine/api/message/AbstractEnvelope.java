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

package com.github.pnavais.machine.api.message;

import com.github.pnavais.machine.api.Node;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Base class for {@link Envelope} implementors.
 *
 * @param <N> the type of the node
 * @param <M> the type of message
 */
@Getter
@AllArgsConstructor
public abstract class AbstractEnvelope<N extends Node, M extends Message> implements Envelope<N,M> {

    /** The source node */
    protected N origin;

    /** The message */
    protected M message;

    /* The target node */
    protected N target;

}
