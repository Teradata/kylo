package com.thinkbiganalytics.kylo.spark.livy;

/*-
 * #%L
 * kylo-spark-livy-core
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

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.thinkbiganalytics.kylo.spark.client.LivyClient;
import com.thinkbiganalytics.kylo.spark.client.jersey.LivyRestClient;
import com.thinkbiganalytics.kylo.spark.client.livy.LivyHeartbeatMonitor;
import com.thinkbiganalytics.kylo.spark.cluster.SparkShellClusterDelegate;
import com.thinkbiganalytics.kylo.spark.config.LivyProperties;
import com.thinkbiganalytics.kylo.spark.exceptions.LivyException;
import com.thinkbiganalytics.kylo.spark.exceptions.LivyServerNotReachableException;
import com.thinkbiganalytics.kylo.spark.model.Session;
import com.thinkbiganalytics.kylo.spark.model.SessionsGetResponse;
import com.thinkbiganalytics.kylo.spark.model.SessionsPost;
import com.thinkbiganalytics.kylo.spark.model.Statement;
import com.thinkbiganalytics.kylo.spark.model.StatementsPost;
import com.thinkbiganalytics.kylo.spark.model.enums.SessionState;
import com.thinkbiganalytics.kylo.utils.LivyUtils;
import com.thinkbiganalytics.kylo.utils.ScriptGenerator;
import com.thinkbiganalytics.rest.JerseyClientConfig;
import com.thinkbiganalytics.rest.JerseyRestClient;
import com.thinkbiganalytics.spark.conf.model.KerberosSparkProperties;
import com.thinkbiganalytics.spark.rest.model.RegistrationRequest;
import com.thinkbiganalytics.spark.shell.SparkShellProcess;
import com.thinkbiganalytics.spark.shell.SparkShellProcessListener;
import com.thinkbiganalytics.spark.shell.SparkShellProcessManager;
import com.thinkbiganalytics.spark.shell.SparkShellRestClient;

import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;

public class SparkLivyProcessManager implements SparkShellProcessManager, SparkShellClusterDelegate, SparkShellProcessListener {

    private static final XLogger logger = XLoggerFactory.getXLogger(SparkLivyProcessManager.class);

    List<SparkShellProcessListener> listeners = Lists.newArrayList();

    @Resource
    private ScriptGenerator scriptGenerator;

    @Resource
    private KerberosSparkProperties kerberosSparkProperties;

    @Resource
    private LivyProperties livyProperties;

    @Resource
    private LivyClient livyClient;

    @Resource
    private LivyHeartbeatMonitor heartbeatMonitor;

    /**
     * Map of Spark Shell processes to Jersey REST clients
     */
    @Nonnull
    private final Map<SparkShellProcess, JerseyRestClient> clients = new HashMap<>();

    @Resource
    private Map<String /* transformId */, Integer /* stmntId */> statementIdCache;

    @Nonnull
    private final BiMap<String /* user */, SparkLivyProcess> processCache = HashBiMap.create();

    @Resource
    private SparkShellRestClient restClient;

    @Override
    public void addListener(@Nonnull SparkShellProcessListener listener) {
        // currently only called in KyloHA mode, when SparkShellClusterListener bean is instantiated
        logger.debug("adding listener '{}", listener);
        listeners.add(listener);
    }

    @Override
    public void removeListener(@Nonnull SparkShellProcessListener listener) {
        logger.debug("removing listener '{}", listener);
        listeners.remove(listener);
    }


    @Nonnull
    @Override
    public SparkShellProcess getProcessForUser(@Nonnull String username) {
        return getProcess(username);
    }

    @Nonnull
    @Override
    public SparkShellProcess getSystemProcess() {
        return getProcess(null);
    }

    @Nonnull
    private SparkShellProcess getProcess(String username) {
        if (processCache.containsKey(username)) {
            // we have, created a process and put it in the cache before
            SparkLivyProcess sparkLivyProcess = processCache.get(username);
            // wait for our sparkShellProcess if it is going through a restart...
            if (sparkLivyProcess.waitForStart()) {
                this.processReady(sparkLivyProcess);  // notifies listeners
                return sparkLivyProcess;
            } else {
                throw new LivyException("Livy Session did not start");
            } // end if
        } else {
            // TODO:   username used for proxyUser?
            SparkLivyProcess process = SparkLivyProcess.newInstance(livyProperties.getHostname(), livyProperties.getPort(), username, livyProperties.getWaitForStart());
            processCache.put(username, process);
            start(process);  // does not waitForStart..
            return process;
        } // end if
    }

    @Override
    public void register(@Nonnull String clientId, @Nonnull RegistrationRequest registration) {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets or creates a Jersey REST client for the specified Spark Shell process.
     *
     * @param process the Spark Shell process
     * @return the Jersey REST client
     */
    @Nonnull
    public JerseyRestClient getClient(@Nonnull final SparkShellProcess process) {
        return clients.computeIfAbsent(process, target -> {
            final JerseyClientConfig config = new JerseyClientConfig();
            config.setHost(target.getHostname());
            config.setPort(target.getPort());

            if (livyProperties.getTruststorePassword() != null) {
                config.setHttps(true);
                config.setTruststorePath(livyProperties.getTruststorePath());
                config.setTruststorePassword(livyProperties.getTruststorePassword());
                config.setTrustStoreType(livyProperties.getTruststoreType());
            } // end if

            // TODO: we don't need a Spring bean of the rest client if we are doing this ...
            LivyRestClient.setKerberosSparkProperties(kerberosSparkProperties);  // all clients will have kerberos
            return new LivyRestClient(config);
        });
    }


    @Override
    public void start(@Nonnull String username) {
        SparkShellProcess sparkProcess = getProcessForUser(username);  // will waitForStart, if a session is starting
        if (sparkProcess instanceof SparkLivyProcess) {
            start((SparkLivyProcess) sparkProcess);
        }
    }

    public void start(@Nonnull final SparkLivyProcess sparkLivyProcess) {
        JerseyRestClient jerseyClient = getClient(sparkLivyProcess);

        // fetch or create new server session
        Session currentSession;

        if (sparkLivyProcess.getSessionId() != null) {
            Optional<Session> optSession = getLivySession(sparkLivyProcess);
            if (optSession.isPresent()) {
                currentSession = optSession.get();
            } else {
                currentSession = startLivySession(sparkLivyProcess);
            }
        } else {
            currentSession = startLivySession(sparkLivyProcess);
        }

        Integer currentSessionId = currentSession.getId();
        if (!currentSession.getState().equals(SessionState.idle)) {
            logger.debug("Created session with id='{}', but it was returned with state != idle, state = '{}'", currentSession.getId(), currentSession.getState());
            if (!waitForSessionToBecomeIdle(jerseyClient, currentSessionId)) {
                throw new LivyException("Livy Session did not start successfully");
            }

            // At this point the server is ready and we can send it an initialization command, any following
            //   statement sent by UI will wait for their turn to execute
            initSession(sparkLivyProcess);
        } // end if

        sparkLivyProcess.sessionStarted(); // notifies all and any waiting threads session is started, OK to call many times..
    }

    public Optional<Session> getLivySession(SparkLivyProcess sparkLivyProcess) {
        JerseyRestClient jerseyClient = getClient(sparkLivyProcess);
        SessionsGetResponse sessions = livyClient.getSessions(jerseyClient);

        if (sessions == null) {
            throw new LivyServerNotReachableException("Livy server not reachable");
        }
        Optional<Session> optSession = sessions.getSessionWithId(sparkLivyProcess.getSessionId());

        if (!optSession.isPresent()) {
            // current client not found... let's make a new one
            clearClientState(sparkLivyProcess);
        }
        return optSession;
    }


    public Session startLivySession(SparkLivyProcess sparkLivyProcess) {

        sparkLivyProcess.newSession();  // it was determined we needed a

        JerseyRestClient jerseyClient = getClient(sparkLivyProcess);

        Map<String, String> sparkProps = livyProperties.getSparkProperties();
        SessionsPost.Builder builder = new SessionsPost.Builder()
            .kind(livyProperties.getLivySessionKind().toString()) // "shared" most likely
            //.jars(Lists.newArrayList(""))
            .conf(
                // "spark.driver.extraJavaOptions", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=8990"
                sparkProps
            );

        logger.debug("LivyProperties={}", livyProperties);

        if (livyProperties.getProxyUser()) {
            String user = processCache.inverse().get(sparkLivyProcess);
            if (user != null) {
                builder.proxyUser(user);
            }
        }
        SessionsPost sessionsPost = builder.build();
        logger.info("sessionsPost={}", sessionsPost);

        Session currentSession;
        try {
            currentSession = livyClient.postSessions(jerseyClient, sessionsPost);
            if (currentSession == null) {
                throw new LivyServerNotReachableException("Livy server not reachable");
            }
        } catch (LivyException le) {
            throw le;
        } catch (Exception e) {
            // NOTE: you can get "javax.ws.rs.ProcessingException: java.io.IOException: Error writing to server" on Ubuntu see: https://stackoverflow.com/a/39718929/154461
            throw new LivyException(e);
        }
        sparkLivyProcess.setSessionId(currentSession.getId());
        this.processStarted(sparkLivyProcess);  // notifies listeners

        // begin monitoring this session if configured to do so..
        heartbeatMonitor.monitorSession(sparkLivyProcess);

        return currentSession;
    }


    @Nonnull
    public Integer getStatementId(@Nonnull String transformId) {
        Integer statementId = statementIdCache.get(transformId);
        if (statementId == null) {
            throw new NullPointerException("transformId has aged out or was not recorded properly");
        } else {
            return statementId;
        }
    }


    public void setStatementId(@Nonnull String transformId, @Nonnull Integer statementId) {
        statementIdCache.put(transformId, statementId);
    }


    private void clearClientState(SparkShellProcess sparkProcess) {
        clients.remove(sparkProcess);
    }


    private void initSession(SparkLivyProcess sparkLivyProcess) {
        JerseyRestClient jerseyClient = getClient(sparkLivyProcess);
        String script = scriptGenerator.script("initSession");

        StatementsPost sp = new StatementsPost.Builder()
            .kind("spark")
            .code(script)
            .build();

        Statement statement = livyClient.postStatement(jerseyClient, sparkLivyProcess, sp);

        // NOTE:  why pollStatement now?  so we block on result.
        livyClient.pollStatement(jerseyClient, sparkLivyProcess, statement.getId());
    }


    /**
     * returns true is session becomes idle; false if it fails to start
     */
    private boolean waitForSessionToBecomeIdle(JerseyRestClient jerseyClient, Integer id) {
        Optional<Session> optSession;
        do {
            try {
                Thread.sleep(livyProperties.getPollingInterval());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            SessionsGetResponse sessions = livyClient.getSessions(jerseyClient);

            logger.debug("poll server for session with id='{}'", id);
            optSession = sessions.getSessionWithId(id);
            if (optSession.isPresent() && SessionState.FINAL_STATES.contains(optSession.get().getState())) {
                return false;
            }
        } while (!(optSession.isPresent() && optSession.get().getState().equals(SessionState.idle)));

        return true;
    }

    /**
     * @implNote implementation of SparkShellClusterDelegate.getProcesses
     *
     */
    @Nonnull
    @Override
    public List<SparkShellProcess> getProcesses() {
        logger.entry();

        return logger.exit(Lists.newArrayList(processCache.values()));
    }

    /**
     * @implNote implementation of SparkShellClusterDelegate.updateProcess
     *
     * @param process the SparkLivyProcess coming as a SparkShellProcess
     */
    @Override
    public void updateProcess(@Nonnull SparkShellProcess process) {
        logger.entry(process);

        if (process instanceof SparkLivyProcess) {
            logger.info("############# Rcvd cluster message / Update process cache  ##########");
            SparkLivyProcess livyProcess = (SparkLivyProcess) process;
            processCache.put(livyProcess.getUser(), livyProcess);
        } else {
            throw logger.throwing(new IllegalStateException("SparkLivyProcessManager only processes SparkLivyProcesses"));
        }

        logger.exit();
    }


    /**
     * @implNote implementation of SparkShellClusterDelegate.removeProcess
     *
     * @param clientId the sessionId of the process that was stopped on some node of the cluster
     */
    @Override
    public void removeProcess(@Nonnull String clientId) {
        logger.entry(clientId);

        processCache.values().stream()
            .filter(sparkLivyProcess -> sparkLivyProcess.getClientId() == clientId)
            .forEach(sparkLivyProcess -> {
                logger.debug("SparkLivyProcess will be remove: {}", sparkLivyProcess);
                processCache.remove(sparkLivyProcess);
            });

        logger.exit();
    }

    /**
     * @implNote implementation of SparkShellProcessListener.processReady
     *
     * @param process the Spark Shell process
     */
    @Override
    public void processReady(@Nonnull SparkShellProcess process) {
        // a SparkShell Process is changing state, notify the cluster listener, called internally to this class only.
        listeners.forEach(listener -> listener.processReady(process));
    }

    /**
     *  @implNote implementation SparkShellProcessListener.processStarted
     *
     * @param process the Spark Shell process
     */
    @Override
    public void processStarted(@Nonnull SparkShellProcess process) {
        // a SparkShell Process is changing state, notify the cluster listener, called internally to this class only.
        listeners.forEach(listener -> listener.processStarted(process));
    }

    /**
     * @implNote implementation of SparkShellProcessListener.processStopped
     *
     * @param process the Spark Shell process
     */
    @Override
    public void processStopped(@Nonnull SparkShellProcess process) {
        // a SparkShell Process is changing state, notify the cluster listener, called internally to this class only.
        listeners.forEach(listener -> listener.processStopped(process));
    }
}
