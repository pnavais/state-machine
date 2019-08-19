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

package com.github.pnavais.machine.exporter;

import com.github.pnavais.machine.api.Transition;
import com.github.pnavais.machine.api.exception.FileExportException;
import com.github.pnavais.machine.api.exporter.Exporter;
import com.github.pnavais.machine.api.message.Message;
import com.github.pnavais.machine.api.transition.Transitioner;
import com.github.pnavais.machine.model.State;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.java.Log;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

@Log
@Getter
@Setter
public abstract class AbstractStatesExporter<R, S extends State, M extends Message, T extends Transitioner<S, M, ? extends Transition<S,M>>> implements Exporter<R,S, M, T> {

    /** The default color for final states */
    public static final String DEFAULT_FINAL_COLOR = "#C2B3FF";

    /** The default color for current state */
    public static final String DEFAULT_CURRENT_COLOR = "#1122DD";

    /** The system's new line character */
    protected static final String NL = System.lineSeparator();

    /** The tabulator character */
    protected static final char TB = '\t';

    /** The file system to use when locating paths */
    protected FileSystem fileSystem = FileSystems.getDefault();

    /** The name of the graph */
    protected String graphName = "G";

    /** The color for final states */
    protected Color finalStateColor = Color.decode(DEFAULT_FINAL_COLOR);

    /** Use HSB colors in the output format. (Defaults to RGB) */
    protected boolean useHSB;

    /** Show current status in different color */
    protected boolean showCurrent;

    /** The color for final states */
    protected Color currentStateColor = Color.decode(DEFAULT_CURRENT_COLOR);

    /**
     * All arguments constructor
     *
     * @param fileSystem the filesystem
     * @param graphName the graph name
     * @param finalStateColor the final state color
     * @param useHSB the flag to control exporting using HSB color format
     * @param showCurrent the flag to control annotating current status
     * @param currentStateColor the current state color
     */
    public AbstractStatesExporter(FileSystem fileSystem, String graphName, Color finalStateColor, boolean useHSB, boolean showCurrent, Color currentStateColor) {
        this.fileSystem = fileSystem;
        this.graphName = graphName;
        this.finalStateColor = finalStateColor;
        this.useHSB = useHSB;
        this.showCurrent = showCurrent;
        this.currentStateColor = currentStateColor;
        fillDefaults();
    }

    /**
     * No arguments constructor. Using default values
     */
    public AbstractStatesExporter() {
        fillDefaults();
    }

    /**
     * Fills default values for all non-primitive types
     */
    protected void fillDefaults() {
        fileSystem = (fileSystem == null) ? FileSystems.getDefault() : fileSystem;
        graphName = (graphName == null) ? "G" : graphName;
        finalStateColor = (finalStateColor == null) ? Color.decode(DEFAULT_FINAL_COLOR) : finalStateColor;
        currentStateColor = (currentStateColor == null) ?  Color.decode(DEFAULT_CURRENT_COLOR) : currentStateColor;
    }

    /**
     * Export the current contents of the transitioner
     * to the DOT language to the given file path.
     *
     * @param transitioner the state machine to export
     * @param outputFile the output file path
     */
    public void exportToFile(@NonNull T transitioner, @NonNull String outputFile) {
        exportToFile(transitioner, getFileSystem().getPath(outputFile));
    }

    /**
     * Export the current contents of the transitioner
     * to the DOT language to the given file path.
     *
     * @param transitioner the transitioner to export
     * @param outputFile the output file path
     */
    @Override
    public void exportToFile(@NonNull T transitioner, @NonNull Path outputFile) {
        try (BufferedWriter writer = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8)) {
            writer.write(export(transitioner).toString());
        } catch (IOException ex) {
            log.throwing(getClass().getSimpleName(), "exportToFile", ex);
            throw new FileExportException("Error exporting output file", ex);
        }
    }
}
