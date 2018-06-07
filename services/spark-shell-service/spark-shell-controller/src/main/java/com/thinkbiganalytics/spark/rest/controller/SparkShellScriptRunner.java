package com.thinkbiganalytics.spark.rest.controller;

/*-
 * #%L
 * kylo-spark-shell-controller
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
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

import com.google.common.util.concurrent.Uninterruptibles;
import com.thinkbiganalytics.spark.rest.model.TransformRequest;
import com.thinkbiganalytics.spark.rest.model.TransformResponse;
import com.thinkbiganalytics.spark.shell.SparkShellProcess;
import com.thinkbiganalytics.spark.shell.SparkShellRestClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Runs thes supplied script in spark shell until the task is complete
 */
public class SparkShellScriptRunner implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(SparkShellScriptRunner.class);

    private SparkShellUserProcessService sparkShellUserProcessService;
    private SparkShellRestClient restClient;
    private String username;
    private String script;
    private Long startTime = 0L;
    private Long endTime = 0L;
    private boolean isFinished;
    private TransformResponse finalResponse;
    private String executionException;
    private String descriptor;

    public SparkShellScriptRunner(SparkShellUserProcessService sparkShellUserProcessService, SparkShellRestClient restClient, String username, String script, String descriptor) {
        this.username = username;
        this.sparkShellUserProcessService = sparkShellUserProcessService;
        this.restClient = restClient;
        this.script = script;
        this.descriptor = descriptor;
    }

    public void finished(TransformResponse response) {
        this.endTime = System.currentTimeMillis();
        this.isFinished = true;
        this.finalResponse = response;
    }

    public Long getTotalTime() {
        if (startTime != null) {
            if (endTime == null) {
                return System.currentTimeMillis() - startTime;
            } else {
                return endTime - startTime;
            }
        } else {
            return 0L;
        }
    }

    /**
     * Run the script with spark shell until finished
     *
     * @return the response
     */
    public Optional<TransformResponse> runScript() throws Exception {
        this.startTime = System.currentTimeMillis();
        this.endTime = null;
        this.isFinished = false;
        TransformRequest request = new TransformRequest();
        request.setScript(script);
        final SparkShellProcess process = sparkShellUserProcessService.getSparkShellProcess(username);
        TransformResponse response = restClient.transform(process, request);
        if (response.getStatus() == TransformResponse.Status.PENDING) {
            log.debug("runScript progress {}", response.getTable());
            return fetchProgress(response.getTable());
        } else {
            log.debug("finished runScript {}", response.getTable());
            finished(response);
            return Optional.of(response);
        }
    }

    private Optional<TransformResponse> fetchProgress(String id) throws Exception {
        log.debug("start fetching {}", id);
        final SparkShellProcess process = sparkShellUserProcessService.getSparkShellProcess(username);
        Optional<TransformResponse> response = restClient.getTransformResult(process, id);
        if (response.isPresent()) {
            if (response.get().getStatus() == TransformResponse.Status.SUCCESS) {
                finished(response.get());
                log.debug("finished progress {}", response.get().getTable());
                return response;
            } else {
                log.debug("runScript again {} ", response.get().getTable());
                Uninterruptibles.sleepUninterruptibly(300, TimeUnit.MILLISECONDS);
                return fetchProgress(response.get().getTable());
            }
        } else {
            log.debug("Not found.  returning empty {}", id);
            return Optional.empty();
        }
    }

    public boolean isFinished() {
        return isFinished;
    }

    public TransformResponse getFinalResponse() {
        return finalResponse;
    }

    public String getExecutionException() {
        return executionException;
    }

    public boolean hasExecutionException() {
        return executionException != null;
    }

    @Override
    public void run() {
        try {
            runScript();
        } catch (Exception e) {
            executionException = e.getMessage();
        }
    }
}
