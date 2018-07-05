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
package org.payball.machine.utils;

import org.payball.machine.api.AbstractNode;
import org.payball.machine.api.Message;
import org.payball.machine.model.State;

import java.io.PrintStream;
import java.util.function.Function;

public class TransitionPrintBuilder {

    private TransitionPrintOptions instance;

    private TransitionPrintBuilder() {
        instance = new TransitionPrintOptions();
    }

    public static TransitionPrintBuilder newBuilder() {
        return new TransitionPrintBuilder();
    }

    public static TransitionPrintOptions getDefault() {
        return DefaultPrintOptionsHolder.instance;
    }

    public TransitionPrintBuilder setStateFormatter(Function<State, String> stateFormatter) {
        instance.setStateFormatter(stateFormatter);
        return this;
    }

    public TransitionPrintBuilder setOutput(PrintStream output) {
        instance.setOutput(output);
        return this;
    }

    public TransitionPrintBuilder setMessageFormatter(Function<Message<?>, String> messageFormatter) {
        instance.setMessageFormatter(messageFormatter);
        return this;
    }

    public TransitionPrintOptions build() {
        return instance;
    }


    private static class DefaultPrintOptionsHolder {
        private static TransitionPrintOptions instance = TransitionPrintBuilder.newBuilder()
                                                                               .setMessageFormatter(m -> m.getPayload().toString())
                                                                               .setStateFormatter(AbstractNode::getName)
                                                                               .setOutput(System.out)
                                                                               .build();
    }

}
