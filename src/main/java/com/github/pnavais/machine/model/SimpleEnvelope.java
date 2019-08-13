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

import com.github.pnavais.machine.api.AbstractEnvelope;
import com.github.pnavais.machine.api.Message;
import com.github.pnavais.machine.api.transition.TransitionIndex;
import lombok.Builder;
import lombok.Getter;

/**
 * An implementation of the envelope handling state messaging.
 */
@Getter
public class SimpleEnvelope extends AbstractEnvelope<State, Message> {

    /** The transition index */
    private TransitionIndex<State, Message, StateTransition> transitionIndex;

    @Builder
    public SimpleEnvelope(State source, Message message, State target, TransitionIndex<State, Message, StateTransition> transitionIndex) {
        super(source, message, target);
        this.transitionIndex = transitionIndex;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() { return super.hashCode(); }

}
