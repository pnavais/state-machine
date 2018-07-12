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
package org.payball.machine.api.builder;

import org.payball.machine.api.Message;
import org.payball.machine.api.Node;
import org.payball.machine.api.Transition;
import org.payball.machine.api.transition.TransitionIndex;
import org.payball.machine.api.transition.Transitioner;
import org.payball.machine.model.State;

import java.util.Collection;

public interface TransitionerBuilder<T extends Node, K extends Transition<T>> {

    /**
     * Starts the building of a new
     * transition by specifying the source
     * node's name
     *
     * @param originNodeName the source node's name
     * @return the StateMachineFromBuilder machineBuilder clause
     */
    IFromBuilder from(String originNodeName);

    /**
     * Starts the building of a new
     * transition by specifying the source
     * node.
     *
     * @param node the source node
     * @return the StateMachineFromBuilder clause
     */
    IFromBuilder from(T node);

    /**
     * Adds the given transition to the transition map.
     *
     * @param transition the transition to add
     * @return self for chaining purposes.
     */
    TransitionerBuilder add(K transition);

    /**
     * Adds the given transitions to the transition map.
     *
     * @param transitions the transitions to add
     * @return self for chaining purposes.
     */
    TransitionerBuilder addAll(Collection<K> transitions);

    /**
     * Starts the definition of a loop
     * for the given node's name
     *
     * @param nodeName the node name
     * @return the StateMachineToBuilder clause
     */
    IToBuilder selfLoop(String nodeName);

    /**
     * Starts a definition of a loop
     * for the given node
     *
     * @param node the node
     * @return the StateMachineToBuilder clause
     */
    IToBuilder selfLoop(T node);

    /**
     * Creates a new transitionIndex from the
     * transition map currently built.
     *
     * @return the new srcState machine with the current
     * transitions.
     */
    Transitioner<T, K> build();

    /**
     * Retrieves the current transition map.
     *
     * @return the transition map
     */
    TransitionIndex<T, K> getTransitionIndex();


    /**
     * Internal interface to specify the transition origin
     * and provide safe step by step construction to the
     * builder.
     */
    interface IFromBuilder {

        /**
         * Starts the transition by taking
         * the origin's node name.
         *
         * @param srcNodeName the origin's node name
         * @return the next builder for destination
         */
        IToBuilder to(String srcNodeName);

        /**
         * Continues the transition by taking
         * the destination's node name.
         *
         * @param targetNodeName the destination  node's name
         * @return the next builder for message reception
         */
        IToBuilder to(State targetNodeName);
    }

    /**
     * Internal interface to specify the transition destination
     * and provide safe step by step construction to the
     * builder.
     */
    interface IToBuilder {

        /**
         * Finishes the transition by taking
         * the message key.
         *
         * @param messageKey the message key
         * @return the next builder for message
         */
        IOnBuilder on(String messageKey);

        /**
         * Starts the transition by taking
         * the message.
         *
         * @param message the message
         * @return the next builder for message
         */
        IOnBuilder on(Message message);
    }

    /**
     * Internal interface to specify the transition message
     * and provide safe step by step construction to the
     * builder.
     */
    interface IOnBuilder<T extends State, K extends Transition<T>> extends TransitionerBuilder<T, K> {

        IFromBuilder and(String srcStateName);

        IFromBuilder and(State state);
    }


}
