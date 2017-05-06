package com.thinkbiganalytics.nifi.v2.spark;

import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.annotation.lifecycle.OnDisabled;
import org.apache.nifi.annotation.lifecycle.OnEnabled;
import org.apache.nifi.annotation.lifecycle.OnShutdown;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.controller.AbstractControllerService;
import org.apache.nifi.controller.ConfigurationContext;
import org.apache.nifi.controller.ControllerServiceInitializationContext;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.reporting.InitializationException;
import com.bluebreezecf.tools.sparkjobserver.api.ISparkJobServerClient;
import com.bluebreezecf.tools.sparkjobserver.api.ISparkJobServerClientConstants;
import com.bluebreezecf.tools.sparkjobserver.api.SparkJobResult;
import com.bluebreezecf.tools.sparkjobserver.api.SparkJobServerClientFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.Nonnull;

/*-
 * #%L
 * thinkbig-nifi-spark-service
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

@Tags({"thinkbig", "spark"})
@CapabilityDescription("Provides long running spark contexts and shared RDDs using Spark jobserver.")
public class SparkJobserverService extends AbstractControllerService implements JobService {

    /**
     * A property to get the Spark Jobserver connection URL used to connect to a jobserver.
     */
    public static final PropertyDescriptor JOBSERVER_URL = new PropertyDescriptor.Builder()
        .name("Jobserver URL")
        .description("A URL used to connect to Spark jobserver.")
        .defaultValue(null)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .required(true)
        .build();

    /**
     * A property to set the default timeout for Sync Spark Jobserver calls
     */
    public static final PropertyDescriptor SYNC_TIMEOUT = new PropertyDescriptor.Builder()
        .name("Sync Timeout")
        .description("Number of seconds before timeout for Sync Spark Jobserver calls.")
        .defaultValue("6000")
        .addValidator(StandardValidators.LONG_VALIDATOR)
        .required(true)
        .build();

    /**
     * List of properties
     */
    private List<PropertyDescriptor> properties;
    private volatile ISparkJobServerClient client;
    private volatile String jobServerUrl;
    private volatile String syncTimeout;
    private volatile Timer contextTimeoutTimer = new Timer();
    private volatile Map<String, SparkContextState> sparkContextsStates = Collections.synchronizedMap(new HashMap<String, SparkContextState>());

    @Override
    protected void init(@Nonnull final ControllerServiceInitializationContext config) throws InitializationException {

        // Create list of properties
        final List<PropertyDescriptor> props = new ArrayList<>();
        props.add(JOBSERVER_URL);
        props.add(SYNC_TIMEOUT);
        properties = Collections.unmodifiableList(props);
    }

    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return properties;
    }

    /**
     * Configures Spark Jobserver client by creating an instance of the
     * {@link ISparkJobServerClient} based on configuration provided with
     * {@link ConfigurationContext}.
     * <p>
     * This operation makes no guarantees that the actual connection could be
     * made since the underlying system may still go off-line during normal
     * operation of the service.
     *
     * A Context Timeout task is also created to delete any contexts which have
     * been inactive for some time.
     *
     * @param context the configuration context
     * @throws InitializationException if unable to create a Spark Jobserver connection
     */
    @OnEnabled
    public void onConfigured(final ConfigurationContext context) throws InitializationException {

        jobServerUrl = context.getProperty(JOBSERVER_URL).getValue();

        getLogger().info("Creating a new connection to Spark Jobserver: " + jobServerUrl);

        try {
            client =
                SparkJobServerClientFactory
                    .getInstance()
                    .createSparkJobServerClient(jobServerUrl);

            syncTimeout = context.getProperty(SYNC_TIMEOUT).getValue();
            setupContextTimeoutTask();

        } catch (Exception ex) {
            getLogger().error(ex.getMessage());
        }
        getLogger().info("Created new connection to Spark Jobserver: " + jobServerUrl);
    }

    @Override
    public boolean checkIfContextExists(String contextName) {

        Boolean contextExists = false;

        try {
            contextExists = client.getContexts().contains(contextName);

            if (!contextExists) {
                getLogger().info(String.format("Context %s not found on Spark Jobserver %s", contextName, jobServerUrl));
            } else {
                getLogger().info(String.format("Context %s exists on Spark Jobserver %s", contextName, jobServerUrl));
                contextExists = true;
            }

        } catch (Exception ex) {
            getLogger().error(ex.getMessage());
        }
        return contextExists;
    }

    @Override
    public boolean createContext(String contextName, String numExecutors, String memPerNode, String numCPUCores, SparkContextType contextType, int contextTimeout, boolean async) {

        boolean newContext = isNewContext(contextName);

        if (newContext) {
            Boolean contextRunning = checkIfContextExists(contextName);
            if (!contextRunning) {
                try {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put(ISparkJobServerClientConstants.PARAM_NUM_EXECUTORS, numExecutors);
                    params.put(ISparkJobServerClientConstants.PARAM_SPARK_EXECUTOR_MEMORY, memPerNode);
                    params.put(ISparkJobServerClientConstants.PARAM_NUM_CPU_CORES, numCPUCores);

                    if (contextType != SparkContextType.SPARK_CONTEXT) {
                        if (contextType == SparkContextType.SQL_CONTEXT) {
                            params.put(ISparkJobServerClientConstants.PARAM_CONTEXT_TYPE, "spark.jobserver.context.SQLContextFactory");
                        } else if (contextType == SparkContextType.HIVE_CONTEXT) {
                            params.put(ISparkJobServerClientConstants.PARAM_CONTEXT_TYPE, "spark.jobserver.context.HiveContextFactory");
                        }
                    }

                    if (async) {
                        params.put(ISparkJobServerClientConstants.PARAM_SYNC, "false");
                    } else {
                        params.put(ISparkJobServerClientConstants.PARAM_SYNC, "true");
                        params.put(ISparkJobServerClientConstants.PARAM_TIMEOUT, syncTimeout);
                    }

                    getLogger().info(
                        String.format("Creating %s %s with %s executors, %s cores and %s memory per executor on Spark Jobserver %s", contextType, contextName, numExecutors, numCPUCores, memPerNode,
                                      jobServerUrl));
                    contextRunning = client.createContext(contextName, params);
                    getLogger().info(String.format("Created %s %s on Spark Jobserver %s", contextType, contextName, jobServerUrl));

                } catch (Exception ex) {
                    getLogger().error(ex.getMessage());
                } finally {
                    synchronized (sparkContextsStates) {
                        if (contextRunning) {
                            sparkContextsStates.get(contextName).isRunning = true;
                            sparkContextsStates.get(contextName).setTimeoutSeconds(contextTimeout);
                        } else {
                            sparkContextsStates.remove(contextName);
                        }
                        sparkContextsStates.notifyAll();
                    }
                }
            }
            return contextRunning;
        } else {
            return true;
        }
    }

    @Override
    public boolean deleteContext(String contextName) {

        Boolean contextDeleted = !checkIfContextExists(contextName);

        if (!contextDeleted) {
            try {
                synchronized (sparkContextsStates) {
                    contextDeleted = client.deleteContext(contextName);
                    if (contextDeleted){
                        sparkContextsStates.remove(contextName);
                        getLogger().info(String.format("Deleted context %s from Spark Jobserver %s", contextName, jobServerUrl));
                    }
                }
            } catch (Exception ex) {
                getLogger().error(ex.getMessage());
            }
        }
        return contextDeleted;
    }

    @Override
    public boolean executeSparkContextJob(String appName, String classPath, String contextName, String args, boolean async) {

        String id = contextName + System.nanoTime();

        synchronized (sparkContextsStates) {
            if (sparkContextsStates.containsKey(contextName)) {
                sparkContextsStates.get(contextName).addExecutionLock(id);
            }
        }

        SparkJobResult jobResult = null;
        Boolean success = false;

        try {
            getLogger().info(String.format("Executing Spark App %s %s on context %s with args %s on Spark Jobserver %s", appName, classPath, contextName, args, jobServerUrl));

            Map<String, String> params = new HashMap<String, String>();

            params.put(ISparkJobServerClientConstants.PARAM_APP_NAME, appName);
            params.put(ISparkJobServerClientConstants.PARAM_CLASS_PATH, classPath);
            params.put(ISparkJobServerClientConstants.PARAM_CONTEXT, contextName);

            if (async) {
                params.put(ISparkJobServerClientConstants.PARAM_SYNC, "false");
            } else {
                params.put(ISparkJobServerClientConstants.PARAM_SYNC, "true");
                params.put(ISparkJobServerClientConstants.PARAM_TIMEOUT, syncTimeout);
            }

            jobResult = client.startJob(args, params);

            getLogger().info(String.format("Executed %s %s on context %s on Spark Jobserver %s", appName, classPath, contextName, jobServerUrl));

        } catch (Exception ex) {
            getLogger().error(ex.getMessage());
        }
        finally {
            synchronized (sparkContextsStates) {
                if (sparkContextsStates.containsKey(contextName)) {
                    sparkContextsStates.get(contextName).resetTimeoutTime();
                    sparkContextsStates.get(contextName).removeExecutionLock(id);
                }
            }
        }

        if (jobResult != null && jobResult.getStatus() != "ERROR") {
            success = true;
        }
        return success;
    }

    /**
     * Using a timer task to periodically delete Spark contexts which have timed out
     */
    private void setupContextTimeoutTask() {

        getLogger().info("Creating context timeout task");
        TimerTask contextTimeoutTask = new TimerTask() {
            @Override
            public void run() {
                synchronized (sparkContextsStates) {
                    final long currentTime = System.nanoTime();
                    if (!sparkContextsStates.isEmpty()) {
                        for (SparkContextState sparkContextState: sparkContextsStates.values()) {
                            if (!sparkContextState.isLocked() && sparkContextState.hasTimedOut()) {
                                getLogger().info(String.format("Found context %s which timed out", sparkContextState.ContextName));
                                deleteContext(sparkContextState.ContextName);
                            }
                        }
                    }
                }
            }
        };
        long delay = 0;
        long intevalPeriod = 10000;

        // schedules the task to be run in an interval
        contextTimeoutTimer.scheduleAtFixedRate(contextTimeoutTask, delay,
                                  intevalPeriod);
        getLogger().info("Created and scheduled context timeout task");
    }

    /**
     * Stop timer task and kill all running Spark contexts.
     */
    @OnDisabled
    public void shutdown() {
        contextTimeoutTimer.cancel();
        for (SparkContextState sparkContextState: sparkContextsStates.values()) {
            deleteContext(sparkContextState.ContextName);
        }
    }

    /**
     * Checks to see if a Spark Context is new on the Controller Service
     * @param ContextName the name of the Context
     * @return true or false
     */
    private boolean isNewContext(String ContextName) {
        boolean newContext = false;
        synchronized (sparkContextsStates) {
            try {
                boolean exit = false;
                while (true) {
                    if (!sparkContextsStates.containsKey(ContextName)) {
                        getLogger().info(String.format("Context: %s is a new Context in Controller service", ContextName));
                        sparkContextsStates.put(ContextName, new SparkContextState(ContextName));
                        newContext = true;
                        break;
                    } else if (!sparkContextsStates.get(ContextName).isRunning){
                        getLogger().info(String.format("Context: %s is being created... waiting", ContextName));
                        sparkContextsStates.wait();
                    } else {
                        break;
                    }
                }
            } catch (Exception ex) {
                getLogger().error(ex.getMessage());
            }
        };
        return newContext;
    }
}