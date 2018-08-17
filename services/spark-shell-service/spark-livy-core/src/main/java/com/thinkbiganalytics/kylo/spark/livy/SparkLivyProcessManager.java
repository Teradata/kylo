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

public class SparkLivyProcessManager implements SparkShellProcessManager {

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

    @Resource
    private Map<SparkShellProcess, Integer /* sessionId */> clientSessionCache;

    /**
     * Map of Spark Shell processes to Jersey REST clients
     */
    @Nonnull
    private final Map<SparkShellProcess, JerseyRestClient> clients = new HashMap<>();

    @Resource
    private Map<String /* transformId */, Integer /* stmntId */> statementIdCache;

    @Nonnull
    private final BiMap<String /* user */, SparkShellProcess> processCache = HashBiMap.create();

    @Resource
    private SparkShellRestClient restClient;

    @Override
    public void addListener(@Nonnull SparkShellProcessListener listener) {
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
            SparkLivyProcess sparkLivyProcess = (SparkLivyProcess) processCache.get(username);
            // wait for our sparkShellProcess if it is going through a restart...
            if (sparkLivyProcess.waitForStart()) {
                return sparkLivyProcess;
            } else {
                throw new LivyException("Livy Session did not start");
            } // end if
        } else {
            // TODO:   username used for proxyUser?
            SparkLivyProcess process = SparkLivyProcess.newInstance(livyProperties.getHostname(), livyProperties.getPort(), livyProperties.getWaitForStart());
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

        if (clientSessionCache.containsKey(sparkLivyProcess)) {
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

    public Optional<Session> getLivySession(SparkShellProcess sparkProcess) {
        JerseyRestClient jerseyClient = getClient(sparkProcess);
        SessionsGetResponse sessions = livyClient.getSessions(jerseyClient);

        if (sessions == null) {
            throw new LivyServerNotReachableException("Livy server not reachable");
        }
        Optional<Session> optSession = sessions.getSessionWithId(clientSessionCache.get(sparkProcess));

        if (!optSession.isPresent()) {
            // current client not found... let's make a new one
            clearClientState(sparkProcess);
        }
        return optSession;
    }


    public Session startLivySession(SparkLivyProcess sparkLivyProcess) {

        sparkLivyProcess.newSession();  // it was determined we needed a

        JerseyRestClient jerseyClient = getClient(sparkLivyProcess);

        Map<String, String> sparkProps = livyProperties.getSparkProperties();
        SessionsPost.Builder builder = new SessionsPost.Builder()
            .kind(livyProperties.getLivySessionKind().toString())
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
        clientSessionCache.put(sparkLivyProcess, currentSession.getId());

        // begin monitoring this session if configured to do so..
        if (livyProperties.isMonitorLivy()) {
            heartbeatMonitor.monitorSession(sparkLivyProcess);
        }

        return currentSession;
    }


    @Nonnull
    public Integer getLivySessionId(@Nonnull SparkShellProcess process) {
        return clientSessionCache.get(process);
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
        clientSessionCache.remove(sparkProcess);
        clients.remove(sparkProcess);
    }


    private void initSession(SparkShellProcess sparkProcess) {
        JerseyRestClient jerseyClient = getClient(sparkProcess);
        String script = scriptGenerator.script("initSession");

        Integer sessionId = getLivySessionId(sparkProcess);

        StatementsPost sp = new StatementsPost.Builder()
            .kind("spark")
            .code(script)
            .build();

        Statement statement = livyClient.postStatement(jerseyClient, sparkProcess, sp);

        // TODO: why getStatement now?  so initSession blocks on result.
        LivyUtils.getStatement(livyClient, jerseyClient, sparkProcess, statement.getId());
    }


    /**
     * returns true is session becomes idle; false if it fails to start
     */
    private boolean waitForSessionToBecomeIdle(JerseyRestClient jerseyClient, Integer id) {
        Optional<Session> optSession;
        do {
            try {
                Thread.sleep(250);
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
}
