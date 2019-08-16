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
import com.github.pnavais.machine.api.Status;

/**
 * A {@link MessageFilter} allows performing custom
 * logic upon reception or dispatch of messages deciding
 * whether the operation can continue or not.
 */
public interface MessageFilter<T extends AbstractNode, C extends Context<T>> {

    /**
     * Intercepts a message to be dispatched to the
     * given destination.
     *
     * @param context the context
     * @return whether the operation shall continue or not
     */
    Status onDispatch(C context);

    /**
     * Intercepts a message to be received from the
     * given origin.
     *
     * @param context the context
     * @return whether the operation shall continue or not
     */
    Status onReceive(C context);
}
