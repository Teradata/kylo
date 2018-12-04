package com.thinkbiganalytics.kylo.spark.config;

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


import com.google.common.collect.Lists;
import com.thinkbiganalytics.kylo.spark.exceptions.LivyConfigurationException;
import com.thinkbiganalytics.kylo.spark.model.enums.SessionKind;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.StreamSupport;

import javax.annotation.PostConstruct;

/**
 * Properties specific to configuring Livy
 */
public class LivyProperties {

    private static final Logger logger = LoggerFactory.getLogger(LivyProperties.class);

    /**
     * The hostname for the Livy server. e.g. sandbox.kylo.io
     */
    private String hostname;

    /**
     * The port for the Livy server.  e.g. 8998
     */
    private Integer port;

    /**
     * Instructs Kylo to impersonate the logged in user.  e.g. true
     */
    private Boolean proxyUser = false;

    /**
     * path of the Truststore : Truststore will be necessary if Livy server has been configured for SSL
     **/
    private String truststorePath;

    /**
     * password for the Truststore : Truststore will be necessary if Livy server has been configured for SSL
     **/
    private char[] truststorePassword;

    /**
     * the type of the Truststore : Truststore will be necessary if Livy server has been configured for SSL
     **/
    private String truststoreType;


    /**
     * 'shared' or 'spark'.  Either accept our scala scripts, but there may be differences between them.  Other types not yet supported
     */
    private SessionKind livySessionKind = SessionKind.shared;

    /**
     * If monitorLivy is true then a background thread will periodically check on Livy server status
     */
    private Boolean monitorLivy = false;

    /**
     * How often, in milliseconds, to check Livy under normal circumstances
     */
    private int heartbeatInterval = 1000;

    /**
     * mark the Livy server as not_found after this many attempts
     */
    private Integer triesUntilNotFound = 10;

    /**
     * Got a failed heartbeat from Livy?  This is the base time for checking again, in milliseconds. It follows an exponential growth pattern so if it is 100ms for example, the next will be 200 then
     * 400 etc.
     */
    private int delayCheckOnFail = 100;

    /**
     * Max time to delay for checking on an unresponsive Livy server.
     */
    private int maxDelayCheckOnFail = 10000;

    /**
     * Time, in milliseconds, to wait for start of Livy session, if exceeded, blocked threads release and will produce an exception
     */
    private Long waitForStart = 120000L;

    /**
     * Time, in milliseconds, between polls of the Livy Server when polling for statement results
     */
    private Long pollingInterval = 250L;

    /**
     * Time, in milliseconds, that Kylo Services will poll Livy
     */
    private Long pollingLimit = 500L;

    /**
     * All properties found that start with "spark." but not "spark.shell" or "spark.livy" that can be passed to spark sessions
     */
    private Map<String, String> sparkProperties = new HashMap<>();


    @Autowired
    private Environment env;

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Boolean getProxyUser() {
        return proxyUser;
    }

    public void setProxyUser(Boolean proxyUser) {
        if (proxyUser == null) {
            proxyUser = false;
        }
        this.proxyUser = proxyUser;
    }

    public String getTruststorePath() {
        return truststorePath;
    }

    public void setTruststorePath(String truststorePath) {
        this.truststorePath = truststorePath;
    }

    public char[] getTruststorePassword() {
        return truststorePassword;
    }

    public void setTruststorePassword(char[] truststorePassword) {
        this.truststorePassword = truststorePassword;
    }

    public String getTruststoreType() {
        return truststoreType;
    }

    public void setTruststoreType(String truststoreType) {
        this.truststoreType = truststoreType;
    }

    public Map<String, String> getSparkProperties() {
        return sparkProperties;
    }

    public void setSparkProperties(Map<String, String> sparkProperties) {
        this.sparkProperties = sparkProperties;
    }

    public SessionKind getLivySessionKind() {
        return livySessionKind;
    }

    public void setLivySessionKind(SessionKind livySessionKind) {
        this.livySessionKind = livySessionKind;
    }

    public Boolean isMonitorLivy() {
        return monitorLivy;
    }

    public void setMonitorLivy(Boolean monitorLivy) {
        this.monitorLivy = monitorLivy;
    }

    public int getHeartbeatInterval() {
        return heartbeatInterval;
    }

    public void setHeartbeatInterval(int heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
    }

    public Integer getTriesUntilNotFound() {
        return triesUntilNotFound;
    }

    public void setTriesUntilNotFound(Integer triesUntilNotFound) {
        this.triesUntilNotFound = triesUntilNotFound;
    }

    public int getDelayCheckOnFail() {
        return delayCheckOnFail;
    }

    public void setDelayCheckOnFail(int delayCheckOnFail) {
        this.delayCheckOnFail = delayCheckOnFail;
    }

    public int getMaxDelayCheckOnFail() {
        return maxDelayCheckOnFail;
    }

    public void setMaxDelayCheckOnFail(int maxDelayCheckOnFail) {
        this.maxDelayCheckOnFail = maxDelayCheckOnFail;
    }

    public Long getWaitForStart() {
        return waitForStart;
    }

    public void setWaitForStart(Long waitForStart) {
        this.waitForStart = waitForStart;
    }

    public Long getPollingInterval() {
        return pollingInterval;
    }

    public void setPollingInterval(Long pollingInterval) {
        this.pollingInterval = pollingInterval;
    }

    public Long getPollingLimit() {
        return pollingLimit;
    }

    public void setPollingLimit(Long pollingLimit) {
        this.pollingLimit = pollingLimit;
    }

    @PostConstruct
    private void postConstruct() {
        logger.debug("PostConstruct called for LivyProperties");

        if (!Lists.newArrayList(env.getActiveProfiles()).contains("kylo-livy")) {
            throw new IllegalStateException("Attempting to instantiate LivyProperties bean when 'kylo-livy' is not an active profile");
        }

        if (!StringUtils.isNotEmpty(hostname)) {
            throw new LivyConfigurationException("Attempt to start when 'kylo-livy' is an active profile and property 'spark.livy.hostname' not defined, or invalid.");
        }
        if (port == null || port <= 0) {
            throw new LivyConfigurationException("Attempt to start when 'kylo-livy' is an active profile and property 'spark.livy.port' not defined, or invalid.");
        }

        logger.debug("determine the set of spark properties to pass to Livy");
        MutablePropertySources propSrcs = ((AbstractEnvironment) env).getPropertySources();
        StreamSupport.stream(propSrcs.spliterator(), false)
            .filter(ps -> ps instanceof EnumerablePropertySource)
            .map(ps -> ((EnumerablePropertySource) ps).getPropertyNames())
            .flatMap(Arrays::<String>stream)
            .filter(propName -> propName.startsWith("spark.") &&
                                !(propName.startsWith("spark.livy.") || propName.startsWith("spark.shell.")))
            .forEach(propName -> sparkProperties.put(propName, env.getProperty(propName)));

        logger.debug("Validate session kinds are supportable");
        if (!(livySessionKind.equals(SessionKind.shared) || livySessionKind.equals(SessionKind.spark))) {
            throw new LivyConfigurationException(String.format("Session kind='%s' is not yet supported"));
        }

        logger.info("The following spark properties were found in kylo config files: '{}'", sparkProperties);
    }

    @Override
    public String toString() {
        return new StringBuilder("LivyProperties{")
            .append("hostname='").append(hostname).append('\'')
            .append(", port=").append(port)
            .append(", proxyUser=").append(proxyUser)
            .append(", truststorePath='").append(truststorePath).append('\'')
            .append(", truststorePassword=").append("XXXXXXX")
            .append(", truststoreType='").append(truststoreType).append('\'')
            .append(", livySessionKind=").append(livySessionKind)
            .append(", monitorLivy=").append(monitorLivy)
            .append(", heartbeatInterval=").append(heartbeatInterval)
            .append(", pollingInterval=").append(pollingInterval)
            .append(", pollingLimit=").append(pollingLimit)
            .append(", triesUntilNotFound=").append(triesUntilNotFound)
            .append(", delayCheckOnFail=").append(delayCheckOnFail)
            .append(", maxDelayCheckOnFail=").append(maxDelayCheckOnFail)
            .append(", waitForStart=").append(waitForStart)
            .append(", sparkProperties=").append(sparkProperties)
            .append(", env=").append(env)
            .append('}')
            .toString();
    }


}
