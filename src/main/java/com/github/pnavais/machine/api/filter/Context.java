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

package com.github.pnavais.machine.api.filter;

import com.github.pnavais.machine.api.AbstractNode;
import com.github.pnavais.machine.api.message.Event;
import com.github.pnavais.machine.api.message.Message;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Simple holder for all components involved in a transition event i.e.
 * source and target nodes, triggering message and the current event
 * determining where the transition is being processed.
 *
 * @param <T> the type of node
 */
@Getter
@AllArgsConstructor
public class Context<T extends AbstractNode> {

    /** The current event */
    private final Event event;

    /** The source node */
    private final T source;

    /** The target node */
    private final T target;

    /** The triggering message */
    private final Message message;
}
