package com.thinkbiganalytics.nifi.v2.spark;

import com.bluebreezecf.tools.sparkjobserver.api.ISparkJobServerClient;
import com.bluebreezecf.tools.sparkjobserver.api.ISparkJobServerClientConstants;
import com.bluebreezecf.tools.sparkjobserver.api.SparkJobServerClientFactory;

import org.apache.nifi.annotation.behavior.Stateful;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.annotation.lifecycle.OnEnabled;
import org.apache.nifi.annotation.lifecycle.OnShutdown;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.components.state.Scope;
import org.apache.nifi.components.state.StateManager;
import org.apache.nifi.components.state.StateMap;
import org.apache.nifi.controller.AbstractControllerService;
import org.apache.nifi.controller.ConfigurationContext;
import org.apache.nifi.controller.ControllerServiceInitializationContext;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.reporting.InitializationException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

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
@Stateful(scopes = {Scope.LOCAL}, description = "Gives a UUID to the instance of the controller service. This key is used as a suffix for context keys to avoid collision between instances.")
public class SparkJobserverService extends AbstractControllerService implements JobService {

    /**
     * A property to get the Spark Jobserver connection URL used to connect to a jobserver.
     */
    public static final PropertyDescriptor JOBSERVER_URL = new PropertyDescriptor.Builder()
        .name("Jobserver URL")
        .description("A URL used to connect to Spark jobserver.")
        .defaultValue(null)
        .addValidator(StandardValidators.URL_VALIDATOR)
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
    private volatile ISparkJobServerClient client;
    private List<PropertyDescriptor> properties;
    private volatile String jobServerUrl;
    private volatile String syncTimeout;
    private volatile Timer contextTimeoutTimer = new Timer();
    private volatile Map<String, SparkContextState> sparkContextsStates = Collections.synchronizedMap(new HashMap<String, SparkContextState>());
    private volatile UUID uuid;

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
     * Configures Spark Jobserver client by creating an instance of the {@link ISparkJobServerClient} based on configuration provided with {@link ConfigurationContext}. <p> This operation makes no
     * guarantees that the actual connection could be made since the underlying system may still go off-line during normal operation of the service.
     *
     * A Context Timeout task is also created to delete any contexts which have been inactive for some time.
     *
     * @param context the configuration context
     */
    @OnEnabled
    public void onConfigured(final ConfigurationContext context) {

        jobServerUrl = context.getProperty(JOBSERVER_URL).getValue();

        getLogger().info("Creating a new connection to Spark Jobserver: {}", new Object[]{jobServerUrl});

        try {
            if (client == null) {
                client =
                    SparkJobServerClientFactory
                        .getInstance()
                        .createSparkJobServerClient(jobServerUrl);

            }
            syncTimeout = context.getProperty(SYNC_TIMEOUT).getValue();
            setUUID();
            setupContextTimeoutTask();

        } catch (Exception ex) {
            getLogger().trace(ex.getMessage(), ex);
        }
        getLogger().info("Created new connection to Spark Jobserver: {}", new Object[]{jobServerUrl});
    }

    @Override
    public boolean checkIfContextExists(String contextName) {

        Boolean contextExists = false;

        try {
            List<String> contexts = client.getContexts();
            contextExists = contexts.contains(getServerContextName(contextName));

            if (!contextExists) {
                getLogger().info("Context {} not found on Spark Jobserver {}", new Object[]{getServerContextName(contextName), jobServerUrl});
            } else {
                getLogger().info("Context {} exists on Spark Jobserver {}", new Object[]{getServerContextName(contextName), jobServerUrl});
                contextExists = true;
            }

        } catch (Exception ex) {
            getLogger().trace(ex.getMessage(), ex);
        }
        return contextExists;
    }

    @Override
    public boolean createContext(String contextName, String numExecutors, String memPerNode, String numCPUCores, SparkContextType contextType, int contextTimeout, boolean async) {

        boolean newContext = isNewContext(contextName);

        Boolean contextRunning = checkIfContextExists(contextName);
        try {
            if (newContext && !contextRunning) {

                Map<String, String> params = new HashMap<>();
                params.put(ISparkJobServerClientConstants.PARAM_NUM_EXECUTORS, numExecutors);
                params.put(ISparkJobServerClientConstants.PARAM_SPARK_EXECUTOR_MEMORY, memPerNode);
                params.put(ISparkJobServerClientConstants.PARAM_NUM_CPU_CORES, numCPUCores);
                params.put(ISparkJobServerClientConstants.PARAM_CONTEXT_TYPE, getContextFactoryParam(contextType));
                params.put(ISparkJobServerClientConstants.PARAM_SYNC, async ? "false" : "true");
                params.put(ISparkJobServerClientConstants.PARAM_TIMEOUT, syncTimeout);

                getLogger().info("Creating {} {} with {} executors, {} cores and {} memory per executor on Spark Jobserver {}",
                                 new Object[]{contextType, getServerContextName(contextName), numExecutors, numCPUCores, memPerNode, jobServerUrl});
                contextRunning = client.createContext(getServerContextName(contextName), params);

                if (contextRunning) {
                    getLogger().info("Created {} {} on Spark Jobserver {}",
                                     new Object[]{contextType, getServerContextName(contextName), jobServerUrl});
                }
            }
        } catch (Exception ex) {
            getLogger().trace(ex.getMessage(), ex);
        } finally {
            synchronized (sparkContextsStates) {
                if (contextRunning) {
                    sparkContextsStates.get(contextName).setStateAsRunning();
                    sparkContextsStates.get(contextName).setTimeoutSeconds(contextTimeout);
                } else {
                    sparkContextsStates.remove(contextName);
                }
                sparkContextsStates.notifyAll();
            }
        }
        return contextRunning;
    }

    @Override
    public boolean deleteContext(String contextName) {

        Boolean contextDeleted = !checkIfContextExists(contextName);

        if (!contextDeleted) {
            try {
                synchronized (sparkContextsStates) {
                    if (sparkContextsStates.containsKey(contextName)) {
                        contextDeleted = client.deleteContext(getServerContextName(contextName));
                        if (contextDeleted) {
                            sparkContextsStates.remove(contextName);
                            getLogger().info("Deleted context {} from Spark Jobserver {}", new Object[]{getServerContextName(contextName), jobServerUrl});
                        }
                    }
                }
            } catch (Exception ex) {
                getLogger().trace(ex.getMessage(), ex);
            }
        }
        return contextDeleted;
    }

    /**
     * Gets the UUID for this controller service.
     */
    public UUID getUuid() {
        return uuid;
    }

    @Override
    public SparkJobResult executeSparkContextJob(String appName, String classPath, String contextName, String args, boolean async) {

        String id = String.format("%s:%s", contextName, System.nanoTime());

        synchronized (sparkContextsStates) {
            if (sparkContextsStates.containsKey(contextName)) {
                sparkContextsStates.get(contextName).addExecutionLock(id);
            }
        }

        com.bluebreezecf.tools.sparkjobserver.api.SparkJobResult jobResult = null;
        SparkJobResult result;

        try {
            getLogger().info("Executing Spark App {} {} on context {} with args {} on Spark Jobserver {}", new Object[]{appName, classPath, contextName, args, jobServerUrl});

            Map<String, String> params = new HashMap<>();

            params.put(ISparkJobServerClientConstants.PARAM_APP_NAME, appName);
            params.put(ISparkJobServerClientConstants.PARAM_CLASS_PATH, classPath);
            params.put(ISparkJobServerClientConstants.PARAM_CONTEXT, getServerContextName(contextName));

            if (async) {
                params.put(ISparkJobServerClientConstants.PARAM_SYNC, "false");
            } else {
                params.put(ISparkJobServerClientConstants.PARAM_SYNC, "true");
                params.put(ISparkJobServerClientConstants.PARAM_TIMEOUT, syncTimeout);
            }

            jobResult = client.startJob(args, params);

            getLogger().info("Executed {} {} on context {} on Spark Jobserver {}", new Object[]{appName, classPath, getServerContextName(contextName), jobServerUrl});

        } catch (Exception ex) {
            getLogger().trace(ex.getMessage(), ex);
        } finally {
            synchronized (sparkContextsStates) {
                if (sparkContextsStates.containsKey(contextName)) {
                    sparkContextsStates.get(contextName).resetTimeoutTime();
                    sparkContextsStates.get(contextName).removeExecutionLock(id);
                }
            }
        }

        if (jobResult != null && !Objects.equals(jobResult.getStatus(), "ERROR")) {
            result = new SparkJobResult(true, jobResult.getResult());
        } else {
            result = new SparkJobResult(false, null);
        }

        return result;
    }

    /**
     * Using a timer task to periodically delete Spark contexts which have timed out
     */
    private void setupContextTimeoutTask() {

        getLogger().info("Creating context timeout task");
        TimerTask contextTimeoutTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    synchronized (sparkContextsStates) {
                        Map<String, SparkContextState> tempSparkContextsStates = new HashMap<>();
                        tempSparkContextsStates.putAll(sparkContextsStates);
                        if (!sparkContextsStates.isEmpty()) {
                            for (SparkContextState sparkContextState : tempSparkContextsStates.values()) {
                                if (!sparkContextState.isLocked() && sparkContextState.hasTimedOut()) {
                                    getLogger().info("Found context {} which timed out", new Object[]{sparkContextState.contextName});
                                    deleteContext(sparkContextState.contextName);
                                }
                            }
                        }
                    }
                } catch (Exception ex) {
                    getLogger().trace(ex.getMessage(), ex);
                }
            }
        };
        long delay = 0;
        long intervalPeriod = 10000;

        // schedules the task to be run in an interval
        contextTimeoutTimer.scheduleAtFixedRate(contextTimeoutTask, delay,
                                                intervalPeriod);
        getLogger().info("Created and scheduled context timeout task");
    }

    /**
     * Stop timer task and kill all running Spark contexts.
     */
    @OnShutdown
    public void shutdown() {
        contextTimeoutTimer.cancel();
        for (SparkContextState sparkContextState : sparkContextsStates.values()) {
            deleteContext(sparkContextState.contextName);
        }
    }

    /**
     * Checks to see if a Spark Context is new on the Controller Service
     *
     * @param contextName the name of the Context
     * @return true or false
     */
    private boolean isNewContext(String contextName) {
        boolean newContext = false;
        synchronized (sparkContextsStates) {
            try {
                boolean contextInitializing = true;
                while (contextInitializing) {
                    if (!sparkContextsStates.containsKey(contextName)) {
                        getLogger().info("Context: {} is a new Context in Controller service", new Object[]{contextName});
                        sparkContextsStates.put(contextName, new SparkContextState(contextName));
                        newContext = true;
                        contextInitializing = false;
                    } else if (!sparkContextsStates.get(contextName).getRunningState()) {
                        getLogger().info("Context: {} is being created... waiting", new Object[]{contextName});
                        sparkContextsStates.wait();
                    } else {
                        contextInitializing = false;
                    }
                }
            } catch (Exception ex) {
                getLogger().trace(ex.getMessage(), ex);
            }
        }

        return newContext;
    }

    /**
     * Gets the context-factory parameter based of the type of Spark Context  to be created
     *
     * @param contextType the type of Spark Context
     * @return true or false
     */
    private String getContextFactoryParam(SparkContextType contextType) {
        if (contextType == SparkContextType.SQL_CONTEXT) {
            return "spark.jobserver.context.SQLContextFactory";
        } else if (contextType == SparkContextType.HIVE_CONTEXT) {
            return "spark.jobserver.context.HiveContextFactory";
        } else {
            return "spark.jobserver.context.DefaultSparkContextFactory";
        }
    }

    /**
     * Uses StateManager to set or get a UUID for the controller service
     */
    private void setUUID() {
        try {
            StateManager stateManager = getStateManager();
            StateMap stateMap = stateManager.getState(Scope.LOCAL);

            if (stateMap.getVersion() == -1L) {
                Map<String, String> stateProperties = new HashMap<>(stateMap.toMap());
                uuid = UUID.randomUUID();
                stateProperties.put("serverID", uuid.toString());
                stateManager.setState(stateProperties, Scope.LOCAL);
                getLogger().info("Created new SparkJobserverService UUID: {}", new Object[]{uuid.toString()});
            } else {
                uuid = UUID.fromString(stateMap.get("serverID"));
                getLogger().info("Retrieved SparkJobserverServiceUUID: {}", new Object[]{uuid.toString()});
            }

        } catch (IOException ioe) {
            getLogger().trace(ioe.getMessage(), ioe);
        }
    }

    /**
     * Gets the controller service friendly name of the Spark Context
     *
     * @param contextName the name of the Spark Context
     * @return contextName@uuid
     */
    private String getServerContextName(String contextName) {
        return String.format("%s@%s", contextName, uuid.toString());
    }
}
