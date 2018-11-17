package com.thinkbiganalytics.kylo.utils;

/*-
 * #%L
 * kylo-spark-livy-server
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics, a Teradata Company
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import org.apache.commons.exec.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;

public class ProcessRunner {
    private static final Logger logger = LoggerFactory.getLogger(ProcessRunner.class);

    private static final Integer TIMEOUT = 30 * 1000;  // default is 30 seconds

    private ThreadLocal<String> outputFromLastCommand = new ThreadLocal<>();
    private ThreadLocal<String> errorFromLastCommand = new ThreadLocal<>();
    private ThreadLocal<Integer> exitCodeFromLastCommand = new ThreadLocal<>();

    public boolean runScript(@Nonnull String script, @Nonnull Collection<String> arguments) {
        Validate.notEmpty(script);

        CommandLine cmdLine = new CommandLine(script);
        for (String arg : arguments) {
            cmdLine.addArgument(arg);
        }

        DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errStream = new ByteArrayOutputStream();

        ExecuteWatchdog watchdog = new ExecuteWatchdog(TIMEOUT );
        Executor executor = new DefaultExecutor();
        executor.setWatchdog(watchdog);
        try {
            executor.execute(cmdLine, resultHandler);
            PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream, errStream);
            executor.setStreamHandler(streamHandler);
        } catch (IOException e) {
            logger.error("IOException when reading/executing script '{}'.  This should only happen if permissions have changed.", script);
        } // end try/catch

        // some time later the result handler callback was invoked so we
        // can safely request the exit value
        try {
            resultHandler.waitFor();
        } catch (InterruptedException e) {
            logger.error("Execution of script '{}' failed.  commons-exec thread was interrupted", script);
        } // end try/catch

        if (resultHandler.hasResult()) {
            if (resultHandler.getExitValue() == 0) {
                logger.debug("Script '{}' has returned successfully.", script);
            } else {
                logger.error("Script has returned with a non-zero exit value.", resultHandler.getException());
            } // end if


            // log standard out
            String out = outputStream.toString();
            outputFromLastCommand.set(out);
            if(StringUtils.isNotEmpty(out) ) {
                logger.debug("Script '{}' produced the following output stream:\n{}", script, out);
            }

            // log std err
            String err = errStream.toString();
            errorFromLastCommand.set(err);
            if(StringUtils.isNotEmpty(err) ) {
                logger.error("Script '{}' produced the following error stream:\n{}", script, err);
            }

            int exitValue = resultHandler.getExitValue();
            exitCodeFromLastCommand.set(exitValue);
            if (exitValue == 0) {
                return true;
            } else {
                return false;
            } // end if
        } else {
            throw new IllegalStateException("How is the script finished but we have no result to check?");
        } // end if
    } // end static method

    public String getOutputFromLastCommand() {
        return outputFromLastCommand.get();
    }

    public String getErrorFromLastCommand() {
        return errorFromLastCommand.get();
    }

    public Integer getExitCodeFromLastCommand() {
        return exitCodeFromLastCommand.get();
    }
}
