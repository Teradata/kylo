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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.thinkbiganalytics.kylo.config.LivyProperties;
import com.thinkbiganalytics.kylo.exceptions.LivyException;
import com.thinkbiganalytics.kylo.exceptions.LivyServerNotReachableException;
import com.thinkbiganalytics.kylo.model.*;
import com.thinkbiganalytics.kylo.model.enums.SessionState;
import com.thinkbiganalytics.kylo.spark.client.LivyRestClient;
import com.thinkbiganalytics.kylo.utils.LivyUtils;
import com.thinkbiganalytics.kylo.utils.ScalaScriptService;
import com.thinkbiganalytics.kylo.utils.ScriptGenerator;
import com.thinkbiganalytics.rest.JerseyClientConfig;
import com.thinkbiganalytics.rest.JerseyRestClient;
import com.thinkbiganalytics.spark.conf.model.KerberosSparkProperties;
import com.thinkbiganalytics.spark.rest.model.RegistrationRequest;
import com.thinkbiganalytics.spark.shell.SparkShellProcess;
import com.thinkbiganalytics.spark.shell.SparkShellProcessListener;
import com.thinkbiganalytics.spark.shell.SparkShellProcessManager;
import com.thinkbiganalytics.spark.shell.SparkShellRestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.*;

public class SparkLivyProcessManager implements SparkShellProcessManager {
    private static final Logger logger = LoggerFactory.getLogger(SparkLivyProcessManager.class);

    List<SparkShellProcessListener> listeners = Lists.newArrayList();

    @Resource
    private ScriptGenerator scriptGenerator;

    @Resource
    private KerberosSparkProperties kerberosSparkProperties;

    @Resource
    private LivyProperties livyProperties;

    /**
     * Map of Spark Shell processes to Jersey REST clients
     */
    @Nonnull
    private final Map<SparkShellProcess, JerseyRestClient> clients = new HashMap<>();

    @Nonnull
    private final Map<SparkShellProcess, Integer /* sessionId */ > clientSessionCache = new HashMap<>();

    @Nonnull
    private final Map<Integer /* sessionId */, Integer /* stmntId */> stmntIdCache = new HashMap<>();

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
        if( processCache.containsKey(username) ) {
            return processCache.get(username);
        } else {
            // TODO: may want to take pulse of connection here..
            // TODO:   username used for proxyUser?
            SparkLivyProcess process = SparkLivyProcess.newInstance(livyProperties.getHostname(),livyProperties.getPort());
            processCache.put(username,process);
            return process;
        } // end if
    }

    @Nonnull
    @Override
    public SparkShellProcess getSystemProcess() {
        throw new UnsupportedOperationException();
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

            if( livyProperties.getTruststorePassword() != null ) {
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

    private void clearClientState(SparkShellProcess sparkProcess) {
        clientSessionCache.remove(sparkProcess);
        clients.remove(sparkProcess);
    }


    @Override
    public void start(@Nonnull String username) {
        logger.debug("JerseyClient='{}'", restClient);

        SparkShellProcess sparkProcess = getProcessForUser(username);
        JerseyRestClient jerseyClient = getClient(sparkProcess);

        // fetch or create new server session
        Session currentSession;

        if (clientSessionCache.containsKey(sparkProcess)) {
            Optional<Session> optSession = getLivySession(sparkProcess);
            if( optSession.isPresent() ) {
                currentSession = optSession.get();
            } else {
                currentSession = startLivySession(sparkProcess);
            }
        } else {
            currentSession = startLivySession(sparkProcess);
        }

        Integer currentSessionId = currentSession.getId();
        if (!currentSession.getState().equals(SessionState.idle)) {
            logger.debug("Created session with id='{}', but it was returned with state != idle, state = '{}'", currentSession.getId(), currentSession.getState());
            if( ! waitForSessionToBecomeIdle(jerseyClient, currentSessionId) ) {
                throw new LivyException("Livy Session did not start successfully");
            }

            // At this point the server is ready and we can send it an initialization command, any following
            //   statement sent by UI will wait for their turn to execute
            initSession(sparkProcess);
        } // end if

        if (sparkProcess != null) {
            for (SparkShellProcessListener listener : listeners) {
                listener.processReady(sparkProcess);
            }
        }
    }

    private void initSession(SparkShellProcess sparkProcess) {
        JerseyRestClient jerseyClient = getClient(sparkProcess);
        String script = scriptGenerator.script("initSession");

        Integer sessionId =  getLivySessionId(sparkProcess);

        StatementsPost sp = new StatementsPost.Builder()
                .kind("spark")
                .code(script)
                .build();

        Statement statement = jerseyClient.post(String.format("/sessions/%s/statements", sessionId),
                sp,
                Statement.class);

        statement = LivyUtils.getStatement(jerseyClient, sessionId, statement.getId());

        logger.debug("statement={}", statement);

        setLastStatementId(sparkProcess,statement.getId());
    }

    public Optional<Session> getLivySession(SparkShellProcess sparkProcess) {
        JerseyRestClient jerseyClient = getClient(sparkProcess);
        SessionsGetResponse sessions = jerseyClient.get("/sessions", null, SessionsGetResponse.class);
        logger.debug("sessions={}", sessions);

        if( sessions == null ) {
            throw new LivyServerNotReachableException("Livy server not reachable");
        }
        Optional<Session> optSession = sessions.getSessionWithId(clientSessionCache.get(sparkProcess));

        if( ! optSession.isPresent() ) {
            // current client not found... let's make a new one
            clearClientState(sparkProcess);
        }
        return optSession;
    }

    public Session startLivySession(SparkShellProcess sparkProcess) {
        JerseyRestClient jerseyClient = getClient(sparkProcess);

        Map<String,String> sparkProps = livyProperties.getSparkProperties();
        SessionsPost.Builder builder = new SessionsPost.Builder()
                .kind(livyProperties.getLivySessionKind().toString())
                //.jars(Lists.newArrayList(""))
                .conf(
                       // "spark.driver.extraJavaOptions", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=8990"
                        sparkProps
                );

        logger.debug("LivyProperties={}", livyProperties);

        if( livyProperties.getProxyUser() ) {
            String user = processCache.inverse().get(sparkProcess);
            builder.proxyUser(user);
        }
        SessionsPost sessionsPost = builder.build();
        logger.info("sessionsPost={}", sessionsPost);


        Session currentSession;
        try {
            currentSession = jerseyClient.post("/sessions", sessionsPost, Session.class);
            if (currentSession == null) {
                throw new LivyServerNotReachableException("Livy server not reachable");
            }
        } catch ( LivyException le ) {
            throw le;
        } catch( Exception e ) {
            // NOTE: you can get "javax.ws.rs.ProcessingException: java.io.IOException: Error writing to server" on Ubuntu
            throw new LivyException(e);
        }
        clientSessionCache.put(sparkProcess, currentSession.getId());
        for (SparkShellProcessListener listener : listeners) {
            listener.processStarted(sparkProcess);
        }
        return currentSession;
    }

    /**
     * returns true is session becomes idle; false if it fails to start
     * @param jerseyClient
     * @param id
     * @return
     */
    private boolean waitForSessionToBecomeIdle(JerseyRestClient jerseyClient, Integer id) {
        Optional<Session> optSession;
        do {
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            SessionsGetResponse sessions = jerseyClient.get("/sessions", null, SessionsGetResponse.class);

            logger.debug("poll server for session with id='{}'", id);
            optSession = sessions.getSessionWithId(id);
            if( optSession.isPresent() &&
                    ( optSession.get().getState() == SessionState.dead || optSession.get().getState() == SessionState.shutting_down ) ) {
                return false;
            }
        } while (!(optSession.isPresent() && optSession.get().getState().equals(SessionState.idle)));

        return true;
    }

    @Nonnull
    public Integer getLivySessionId( @Nonnull  SparkShellProcess process ) {
        return clientSessionCache.get(process);
    }

    @Nonnull
    public Integer getLastStatementId( @Nonnull SparkShellProcess process ) {
        return stmntIdCache.get(clientSessionCache.get(process));
    }

    public void setLastStatementId( @Nonnull SparkShellProcess process, @Nonnull Integer id ) {
        stmntIdCache.put(clientSessionCache.get(process), id);
    }

}
