/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.gradle

import org.apache.tools.ant.BuildException
import org.apache.tools.ant.BuildListener
import org.apache.tools.ant.BuildLogger
import org.apache.tools.ant.DefaultLogger
import org.apache.tools.ant.Project
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

import java.nio.charset.Charset

/**
 * A task which will run ant commands.
 *
 * Logging for the task is customizable for subclasses by overriding makeLogger.
 */
public class AntTask extends DefaultTask {

    /**
     * A buffer that will contain the output of the ant code run,
     * if the output was not already written directly to stdout.
     */
    public final ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream()

    @TaskAction
    final void executeTask() {
        // capture the current loggers
        List<BuildLogger> savedLoggers = new ArrayList<>();
        for (BuildListener l : project.ant.project.getBuildListeners()) {
            if (l instanceof BuildLogger) {
                savedLoggers.add(l);
            }
        }
        // remove them
        for (BuildLogger l : savedLoggers) {
            project.ant.project.removeBuildListener(l)
        }

        final int outputLevel = logger.isDebugEnabled() ? Project.MSG_DEBUG : Project.MSG_INFO
        final PrintStream stream = useStdout() ? System.out : new PrintStream(outputBuffer, true, Charset.defaultCharset().name())
        BuildLogger antLogger = makeLogger(stream, outputLevel)

        // now run the command with just our logger
        project.ant.project.addBuildListener(antLogger)
        try {
            runAnt(project.ant)
        } catch (BuildException e) {
            // ant failed, so see if we have buffered output to emit, then rethrow the failure
            String buffer = outputBuffer.toString()
            if (buffer.isEmpty() == false) {
                logger.error("=== Ant output ===\n${buffer}")
            }
            throw e
        } finally {
            project.ant.project.removeBuildListener(antLogger)
            // add back the old loggers before returning
            for (BuildLogger l : savedLoggers) {
                project.ant.project.addBuildListener(l)
            }
        }
    }

    /** Runs the doAnt closure. This can be overridden by subclasses instead of having to set a closure. */
    protected void runAnt(AntBuilder ant) {
        if (doAnt == null) {
            throw new GradleException("Missing doAnt for ${name}")
        }
        doAnt(ant)
    }

    /** Create the logger the ant runner will use, with the given stream for error/output. */
    protected BuildLogger makeLogger(PrintStream stream, int outputLevel) {
        return new DefaultLogger(
            errorPrintStream: stream,
            outputPrintStream: stream,
            messageOutputLevel: outputLevel)
    }

    /**
     * Returns true if the ant logger should write to stdout, or false if to the buffer.
     * The default implementation writes to the buffer when gradle info logging is disabled.
     */
    protected boolean useStdout() {
        return logger.isInfoEnabled()
    }


}
