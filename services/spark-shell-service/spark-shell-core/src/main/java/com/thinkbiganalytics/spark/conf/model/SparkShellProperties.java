package com.thinkbiganalytics.spark.conf.model;

/*-
 * #%L
 * Spark Shell Core
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

public class SparkShellProperties {

    /**
     * Path to the kylo-spark-shell-client jar file
     */
    private String appResource;

    /**
     * Startup timeout in seconds
     */
    private int clientTimeout = 60;

    /**
     * Spark deploy mode
     */
    private String deployMode = "client";

    /**
     * Additional files to be submitted with the application
     */
    private String files;

    /**
     * Request timeout in seconds
     */
    private int idleTimeout = 900;

    /**
     * Additional jars to be submitted with the Spark application
     */
    private String jars;

    /**
     * The {@code JAVA_HOME} for launching the Spark application
     */
    private String javaHome;

    /**
     * Spark master
     */
    private String master = "yarn";

    /**
     * Maximum port number
     */
    private int portMax = 45999;

    /**
     * Minimum port number
     */
    private int portMin = 45000;

    /**
     * Custom properties file with Spark configuration for the application
     */
    private String propertiesFile;

    /**
     * Enables user impersonation
     */
    private boolean proxyUser = false;

    /**
     * Password for keystore
     */
    private String registrationKeystorePassword;

    /**
     * Path to keystore
     */
    private String registrationKeystorePath;

    /**
     * Registration URL
     */
    private String registrationUrl;

    /**
     * Externally managed process
     */
    private SparkShellServerProperties server;

    /**
     * Additional command-line options
     */
    private String sparkArgs;

    /**
     * Custom Spark installation location
     */
    private String sparkHome;

    /**
     * Enables verbose reporting for Spark Submit
     */
    private boolean verbose;

    public String getAppResource() {
        return appResource;
    }

    public void setAppResource(String appResource) {
        this.appResource = appResource;
    }

    public int getClientTimeout() {
        return clientTimeout;
    }

    public void setClientTimeout(int clientTimeout) {
        this.clientTimeout = clientTimeout;
    }

    public String getDeployMode() {
        return deployMode;
    }

    public void setDeployMode(String deployMode) {
        this.deployMode = deployMode;
    }

    public String getFiles() {
        return files;
    }

    public void setFiles(String files) {
        this.files = files;
    }

    public int getIdleTimeout() {
        return idleTimeout;
    }

    public void setIdleTimeout(int idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    public String getJars() {
        return jars;
    }

    public void setJars(String jars) {
        this.jars = jars;
    }

    public String getJavaHome() {
        return javaHome;
    }

    public void setJavaHome(String javaHome) {
        this.javaHome = javaHome;
    }

    public String getMaster() {
        return master;
    }

    public void setMaster(String master) {
        this.master = master;
    }

    public int getPortMax() {
        return portMax;
    }

    public void setPortMax(int portMax) {
        this.portMax = portMax;
    }

    public int getPortMin() {
        return portMin;
    }

    public void setPortMin(int portMin) {
        this.portMin = portMin;
    }

    public String getPropertiesFile() {
        return propertiesFile;
    }

    public void setPropertiesFile(String propertiesFile) {
        this.propertiesFile = propertiesFile;
    }

    public boolean isProxyUser() {
        return proxyUser;
    }

    public void setProxyUser(boolean proxyUser) {
        this.proxyUser = proxyUser;
    }

    public String getRegistrationKeystorePassword() {
        return registrationKeystorePassword;
    }

    public void setRegistrationKeystorePassword(String registrationKeystorePassword) {
        this.registrationKeystorePassword = registrationKeystorePassword;
    }

    public String getRegistrationKeystorePath() {
        return registrationKeystorePath;
    }

    public void setRegistrationKeystorePath(String registrationKeystorePath) {
        this.registrationKeystorePath = registrationKeystorePath;
    }

    public String getRegistrationUrl() {
        return registrationUrl;
    }

    public void setRegistrationUrl(String registrationUrl) {
        this.registrationUrl = registrationUrl;
    }

    public SparkShellServerProperties getServer() {
        return server;
    }

    public void setServer(SparkShellServerProperties server) {
        this.server = server;
    }

    public String getSparkArgs() {
        return sparkArgs;
    }

    public void setSparkArgs(String sparkArgs) {
        this.sparkArgs = sparkArgs;
    }

    public String getSparkHome() {
        return sparkHome;
    }

    public void setSparkHome(String sparkHome) {
        this.sparkHome = sparkHome;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
}
