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

import org.payball.machine.api.Message;
import org.payball.machine.model.State;

import java.io.PrintStream;
import java.util.function.Function;

public class TransitionPrintOptions {

    private Function<State, String> stateFormatter;

    private PrintStream output;

    private Function<Message<?>, String> messageFormatter;

    public TransitionPrintOptions() {
        stateFormatter = State::getName;
        output = System.out;
      // messageFormatter = { m -> m.getPayload().to};
    }

    public Function<State, String> getStateFormatter() {
        return stateFormatter;
    }

    public void setStateFormatter(Function<State, String> stateFormatter) {
        this.stateFormatter = stateFormatter;
    }

    public PrintStream getOutput() {
        return output;
    }

    public void setOutput(PrintStream output) {
        this.output = output;
    }

    public Function<Message<?>, String> getMessageFormatter() {
        return messageFormatter;
    }

    public void setMessageFormatter(Function<Message<?>, String> messageFormatter) {
        this.messageFormatter = messageFormatter;
    }
}
