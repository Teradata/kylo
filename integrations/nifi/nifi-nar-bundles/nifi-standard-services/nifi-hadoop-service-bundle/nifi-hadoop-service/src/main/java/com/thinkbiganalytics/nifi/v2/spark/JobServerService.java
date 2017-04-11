package com.thinkbiganalytics.nifi.v2.spark;

import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.annotation.lifecycle.OnEnabled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.controller.AbstractControllerService;
import org.apache.nifi.controller.ConfigurationContext;
import org.apache.nifi.controller.ControllerServiceInitializationContext;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.reporting.InitializationException;
import org.khaleesi.carfield.tools.sparkjobserver.api.ISparkJobServerClient;
import org.khaleesi.carfield.tools.sparkjobserver.api.ISparkJobServerClientConstants;
import org.khaleesi.carfield.tools.sparkjobserver.api.SparkJobResult;
import org.khaleesi.carfield.tools.sparkjobserver.api.SparkJobServerClientFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

/*-
 * #%L
 * thinkbig-nifi-hadoop-service
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
public class JobServerService extends AbstractControllerService implements JobService {

    /**
     * A property to get the database connection URL used to connect to a database.
     */
    public static final PropertyDescriptor JOBSERVER_URL = new PropertyDescriptor.Builder()
        .name("JOBSERVER URL")
        .description("A URL used to connect to Spark jobserver.")
        .defaultValue(null)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .required(true)
        .build();

    /**
     * List of properties
     */
    private List<PropertyDescriptor> properties;

    private volatile ISparkJobServerClient client;

    private String jobServerUrl;

    @Override
    protected void init(@Nonnull final ControllerServiceInitializationContext config) throws InitializationException {

        // Create list of properties
        final List<PropertyDescriptor> props = new ArrayList<>();
        props.add(JOBSERVER_URL);

        properties = Collections.unmodifiableList(props);
    }

    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return properties;
    }

    /**
     * Configures Spark jobserver client by creating an instance of the
     * {@link ISparkJobServerClient} based on configuration provided with
     * {@link ConfigurationContext}.
     * <p>
     * This operation makes no guarantees that the actual connection could be
     * made since the underlying system may still go off-line during normal
     * operation of the service.
     *
     * @param context the configuration context
     * @throws InitializationException if unable to create a jobserver connection
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

        } catch (Exception ex) {

            getLogger().error(ex.getMessage());
        }
        getLogger().info("Created new connection to Spark Jobserver: " + jobServerUrl);
    }

    @Override
    public Boolean checkIfContextExists(String ContextName) {

        Boolean contextExists = false;

        try {
            getLogger().info(String.format("Checking if context %s exists on Spark Jobserver %s", ContextName, jobServerUrl));

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
    public Boolean createContext(String ContextName, String NumExecutors, String MemPerNode, String NumCPUCores) {

        Boolean contextRunning = false;

        try {
            Map<String, String> params = new HashMap<String, String>();
            params.put("spark.executor.instances", NumExecutors);
            params.put(ISparkJobServerClientConstants.PARAM_MEM_PER_NODE, MemPerNode);
            params.put(ISparkJobServerClientConstants.PARAM_NUM_CPU_CORES, NumCPUCores);
            params.put(ISparkJobServerClientConstants.PARAM_SYNC, "true");

            getLogger().info(String.format("Creating context %s with %s executors, %s cores and %s memory per core on Spark Jobserver %s", ContextName, NumExecutors, MemPerNode, NumCPUCores, jobServerUrl));
            contextRunning = client.createContext(ContextName, params);

            getLogger().info(String.format("Created context %s on Spark Jobserver %s", ContextName, jobServerUrl));

        } catch (Exception ex) {
            getLogger().error(ex.getMessage());
        }
        return contextRunning;
    }

    @Override
    public Boolean deleteContext(String ContextName) {

        Boolean contextDeleted = false;

        try {
            getLogger().info(String.format("Deleting context %s on Spark Jobserver %s", ContextName, jobServerUrl));
            contextDeleted = client.deleteContext(ContextName);

            getLogger().info(String.format("Deleted context %s from Spark Jobserver %s", ContextName, jobServerUrl));

        } catch (Exception ex) {
            getLogger().error(ex.getMessage());
        }
        return contextDeleted;
    }

    @Override
    public Boolean uploadJar(String JarPath, String AppName) {

        Boolean success = false;

        try {
            getLogger().info(String.format("Uploading jar %s with app name %s to Spark Jobserver %s",  JarPath, AppName, jobServerUrl));
            File jar = new File(JarPath);
            success = client.uploadSparkJobJar(jar, AppName);
            getLogger().info(String.format("Uploaded jar %s to Spark Jobserver %s", JarPath, jobServerUrl));
        } catch (Exception ex) {
            getLogger().error(ex.getMessage());
        }
        return success;
    }

    @Override
    public Boolean executeSparkContextJob(String AppName, String ClassPath, String ContextName, String Args) {

        SparkJobResult jobResult = null;
        Boolean success = false;

        try {
            getLogger().info(String.format("Starting Spark App %s %s on context %s with args %s on Spark Jobserver %s", AppName, ClassPath, ContextName, Args, jobServerUrl));

            Map<String, String> params = new HashMap<String, String>();

            params.put(ISparkJobServerClientConstants.PARAM_APP_NAME, AppName);
            params.put(ISparkJobServerClientConstants.PARAM_CLASS_PATH, ClassPath);
            params.put(ISparkJobServerClientConstants.PARAM_CONTEXT, ContextName);
            params.put(ISparkJobServerClientConstants.PARAM_SYNC, "true");

            jobResult = client.startJob(Args, params);

            getLogger().info(String.format("Completed %s %s on context %s with args %s on Spark Jobserver %s", AppName, ClassPath, ContextName, Args, jobServerUrl));

        } catch (Exception ex) {
            getLogger().error(ex.getMessage());
        }

        if (jobResult != null && jobResult.getStatus() != "ERROR") {
            success = true;
        }
        return success;
    }
}