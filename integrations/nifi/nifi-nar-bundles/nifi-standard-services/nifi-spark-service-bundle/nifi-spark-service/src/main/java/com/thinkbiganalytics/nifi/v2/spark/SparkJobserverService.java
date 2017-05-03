package com.thinkbiganalytics.nifi.v2.spark;

import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
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

import java.sql.Timestamp;
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
        .defaultValue("60")
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
    private volatile Map<String, TimeoutContext> timeoutContexts = Collections.synchronizedMap(new HashMap<String, TimeoutContext>());

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
    public boolean checkIfContextExists(String ContextName) {

        Boolean contextExists = false;

        try {
            contextExists = client.getContexts().contains(ContextName);

            if (!contextExists) {
                getLogger().info(String.format("Context %s not found on Spark Jobserver %s", ContextName, jobServerUrl));
            } else {
                getLogger().info(String.format("Context %s exists on Spark Jobserver %s", ContextName, jobServerUrl));
                contextExists = true;
            }

        } catch (Exception ex) {
            getLogger().error(ex.getMessage());
        }
        return contextExists;
    }

    @Override
    public boolean createContext(String ContextName, String NumExecutors, String MemPerNode, String NumCPUCores, SparkContextType ContextType, int ContextTimeout, boolean Async) {

        boolean newContext = isNewContext(ContextName);

        if (newContext) {
            Boolean contextRunning = checkIfContextExists(ContextName);
            if (!contextRunning) {
                try {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put(ISparkJobServerClientConstants.PARAM_NUM_EXECUTORS, NumExecutors);
                    params.put(ISparkJobServerClientConstants.PARAM_SPARK_EXECUTOR_MEMORY, MemPerNode);
                    params.put(ISparkJobServerClientConstants.PARAM_NUM_CPU_CORES, NumCPUCores);

                    if (ContextType != SparkContextType.SPARK_CONTEXT) {
                        if (ContextType == SparkContextType.SQL_CONTEXT) {
                            params.put(ISparkJobServerClientConstants.PARAM_CONTEXT_TYPE, "spark.jobserver.context.SQLContextFactory");
                        } else if (ContextType == SparkContextType.HIVE_CONTEXT) {
                            params.put(ISparkJobServerClientConstants.PARAM_CONTEXT_TYPE, "spark.jobserver.context.HiveContextFactory");
                        }
                    }

                    if (Async) {
                        params.put(ISparkJobServerClientConstants.PARAM_SYNC, "false");
                    } else {
                        params.put(ISparkJobServerClientConstants.PARAM_SYNC, "true");
                        params.put(ISparkJobServerClientConstants.PARAM_TIMEOUT, syncTimeout);
                    }

                    getLogger().info(
                        String.format("Creating %s %s with %s executors, %s cores and %s memory per executor on Spark Jobserver %s", ContextType, ContextName, NumExecutors, NumCPUCores, MemPerNode,
                                      jobServerUrl));
                    contextRunning = client.createContext(ContextName, params);
                    synchronized (timeoutContexts) {
                        if (contextRunning) {
                            timeoutContexts.get(ContextName).isRunning = true;
                            timeoutContexts.get(ContextName).setTimeoutSeconds(ContextTimeout);
                        } else {
                            timeoutContexts.remove(ContextName);
                        }
                        timeoutContexts.notifyAll();
                    }
                    getLogger().info(String.format("Created %s %s on Spark Jobserver %s", ContextType, ContextName, jobServerUrl));

                } catch (Exception ex) {
                    getLogger().error(ex.getMessage());
                }
            }
            return contextRunning;
        } else {
            return true;
        }
    }

    @Override
    public boolean deleteContext(String ContextName) {

        Boolean contextDeleted = !checkIfContextExists(ContextName);

        if (!contextDeleted) {
            try {
                synchronized (timeoutContexts) {
                    contextDeleted = client.deleteContext(ContextName);
                    if (contextDeleted){
                        timeoutContexts.remove(ContextName);
                        getLogger().info(String.format("Deleted context %s from Spark Jobserver %s", ContextName, jobServerUrl));
                    }
                }
            } catch (Exception ex) {
                getLogger().error(ex.getMessage());
            }
        }
        return contextDeleted;
    }

    @Override
    public boolean executeSparkContextJob(String AppName, String ClassPath, String ContextName, String Args, boolean Async) {

        String id = ContextName + System.nanoTime();

        synchronized (timeoutContexts) {
            if (timeoutContexts.containsKey(ContextName)) {
                timeoutContexts.get(ContextName).addExecutionLock(id);
            }
        }

        SparkJobResult jobResult = null;
        Boolean success = false;

        try {
            getLogger().info(String.format("Executing Spark App %s %s on context %s with args %s on Spark Jobserver %s", AppName, ClassPath, ContextName, Args, jobServerUrl));

            Map<String, String> params = new HashMap<String, String>();

            params.put(ISparkJobServerClientConstants.PARAM_APP_NAME, AppName);
            params.put(ISparkJobServerClientConstants.PARAM_CLASS_PATH, ClassPath);
            params.put(ISparkJobServerClientConstants.PARAM_CONTEXT, ContextName);

            if (Async) {
                params.put(ISparkJobServerClientConstants.PARAM_SYNC, "false");
            } else {
                params.put(ISparkJobServerClientConstants.PARAM_SYNC, "true");
                params.put(ISparkJobServerClientConstants.PARAM_TIMEOUT, syncTimeout);
            }

            jobResult = client.startJob(Args, params);

            getLogger().info(String.format("Executed %s %s on context %s on Spark Jobserver %s", AppName, ClassPath, ContextName, jobServerUrl));

        } catch (Exception ex) {
            getLogger().error(ex.getMessage());
        }
        finally {
            synchronized (timeoutContexts) {
                if (timeoutContexts.containsKey(ContextName)) {
                    timeoutContexts.get(ContextName).resetTimeoutTime();
                    timeoutContexts.get(ContextName).removeExecutionLock(id);
                }
            }
        }

        if (jobResult != null && jobResult.getStatus() != "ERROR") {
            success = true;
        }
        return success;
    }

    private void setupContextTimeoutTask() {

        getLogger().info("Creating context timeout task");
        TimerTask contextTimeoutTask = new TimerTask() {
            @Override
            public void run() {
                synchronized (timeoutContexts) {
                    final long currentTime = System.nanoTime();
                    if (!timeoutContexts.isEmpty()) {
                        for (TimeoutContext timeoutContext: timeoutContexts.values()) {
                            if (!timeoutContext.isLocked() && timeoutContext.hasTimedOut()) {
                                getLogger().info(String.format("Found context %s which timed out", timeoutContext.ContextName));
                                deleteContext(timeoutContext.ContextName);
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

    @OnShutdown
    public void shutdown() {
        contextTimeoutTimer.cancel();
        for (TimeoutContext timeoutContext: timeoutContexts.values()) {
            deleteContext(timeoutContext.ContextName);
        }
    }

    private boolean isNewContext(String ContextName) {
        boolean newContext = false;
        synchronized (timeoutContexts) {
            try {
                boolean exit = false;
                while (true) {
                    if (!timeoutContexts.containsKey(ContextName)) {
                        getLogger().info("its a new context: " + ContextName);
                        timeoutContexts.put(ContextName, new TimeoutContext(ContextName));
                        newContext = true;
                        break;
                    } else if (!timeoutContexts.get(ContextName).isRunning){
                        getLogger().info("its being created so waiting context: " + ContextName);
                        timeoutContexts.wait();
                    } else {
                        getLogger().info("its already running: " + ContextName);
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