/*
 * Copyright 2019 Pablo Navais
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.github.pnavais.machine.model;

import com.github.pnavais.machine.api.Envelope;
import com.github.pnavais.machine.api.Message;
import com.github.pnavais.machine.api.transition.TransitionIndex;
import lombok.Builder;
import lombok.Getter;

/**
 * An implementation of the envelope handling state messaging.
 */
@Getter
@Builder
public class SimpleEnvelope implements Envelope<State, Message> {

    /** The source state */
    private State source;

    /** The target state */
    private State target;

    /** The message */
    private Message message;

    /** The transition index */
    private TransitionIndex<State, Message, StateTransition> transitionIndex;

}
