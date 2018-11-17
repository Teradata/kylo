package com.thinkbiganalytics.kylo.spark.client;

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


import com.thinkbiganalytics.kylo.spark.client.model.LivyServer;
import com.thinkbiganalytics.kylo.spark.client.model.enums.LivyServerStatus;
import com.thinkbiganalytics.kylo.spark.config.LivyProperties;
import com.thinkbiganalytics.kylo.spark.exceptions.LivyCodeException;
import com.thinkbiganalytics.kylo.spark.exceptions.LivyException;
import com.thinkbiganalytics.kylo.spark.exceptions.LivyInvalidSessionException;
import com.thinkbiganalytics.kylo.spark.exceptions.LivyUserException;
import com.thinkbiganalytics.kylo.spark.livy.SparkLivyProcess;
import com.thinkbiganalytics.kylo.spark.model.Session;
import com.thinkbiganalytics.kylo.spark.model.SessionsGetResponse;
import com.thinkbiganalytics.kylo.spark.model.SessionsPost;
import com.thinkbiganalytics.kylo.spark.model.Statement;
import com.thinkbiganalytics.kylo.spark.model.StatementsPost;
import com.thinkbiganalytics.kylo.spark.model.enums.StatementState;
import com.thinkbiganalytics.rest.JerseyRestClient;

import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * LivyClient's role is to talk to Livy and simplify the management of get/post/delete calls to Livy.  It is careful to ensure Livy is in a ready state by coordinating it's efforts with a heart beat
 * thread.  It also allows for a simple retry on failure mechanism, with a configurable retry policy, should any calls to Livy throw an unexpected error response.
 */
public class DefaultLivyClient implements LivyClient {

    private static final XLogger logger = XLoggerFactory.getXLogger(DefaultLivyClient.class);

    private LivyServer livyServer;

    private LivyProperties livyProperties;

    private final static String SESSIONS_URL = "/sessions";

    public DefaultLivyClient(LivyServer livyServer, LivyProperties livyProperties) {
        this.livyServer = livyServer;
        this.livyProperties = livyProperties;
    }

    @Override
    public Statement postStatement(JerseyRestClient client, SparkLivyProcess sparkLivyProcess, StatementsPost sp) {
        Integer sessionId = sparkLivyProcess.getSessionId();

        try {
            Statement statement = client.post(STATEMENTS_URL(sessionId), sp, Statement.class);
            livyServer.setLivyServerStatus(LivyServerStatus.alive);
            logger.debug("statement={}", statement);
            return statement;
        } catch (WebApplicationException wae) {
            if (wae.getResponse().getStatus() != 404 || wae.getResponse().getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                livyServer.setLivyServerStatus(LivyServerStatus.http_error);
            }
            throw wae;
        } catch (LivyException le) {
            livyServer.setLivyServerStatus(LivyServerStatus.http_error);
            throw le;
        }
    }

    @Override
    public Statement getStatement(JerseyRestClient client, SparkLivyProcess sparkLivyProcess, Integer statementId) {
        Integer sessionId = sparkLivyProcess.getSessionId();

        try {
            Statement statement = client.get(STATEMENT_URL(sessionId, statementId), Statement.class);
            livyServer.setLivyServerStatus(LivyServerStatus.alive);
            logger.debug("statement={}", statement);
            return statement;
        } catch (WebApplicationException wae) {
            if (wae.getResponse().getStatus() != 404 || wae.getResponse().getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                livyServer.setLivyServerStatus(LivyServerStatus.http_error);
            }
            throw wae;
        } catch (LivyException le) {
            livyServer.setLivyServerStatus(LivyServerStatus.http_error);
            throw le;
        }
    }

    @Override
    public SessionsGetResponse getSessions(JerseyRestClient client) {
        try {
            SessionsGetResponse sessions = client.get(SESSIONS_URL, SessionsGetResponse.class);
            livyServer.setLivyServerStatus(LivyServerStatus.alive);
            logger.debug("sessions={}", sessions);
            return sessions;
        } catch (WebApplicationException wae) {
            if (wae.getResponse().getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                livyServer.setLivyServerStatus(LivyServerStatus.http_error);
            }
            throw wae;
        } catch (LivyException le) {
            livyServer.setLivyServerStatus(LivyServerStatus.http_error);
            throw le;
        }
    }

    @Override
    public Session postSessions(JerseyRestClient client, SessionsPost sessionsPost) {
        try {
            Session session = client.post(SESSIONS_URL, sessionsPost, Session.class);
            livyServer.setLivyServerStatus(LivyServerStatus.alive);
            livyServer.setSessionIdHighWaterMark(session.getId());
            livyServer.setLivySessionState(session.getId(), session.getState());
            logger.debug("session={}", session);
            return session;
        } catch (WebApplicationException wae) {
            if (wae.getResponse().getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                livyServer.setLivyServerStatus(LivyServerStatus.http_error);
            }
            throw wae;
        } catch (LivyException le) {
            livyServer.setLivyServerStatus(LivyServerStatus.http_error);
            throw le;
        }
    }

    @Override
    public Session getSession(JerseyRestClient client, SparkLivyProcess sparkLivyProcess) {
        Integer sessionId = sparkLivyProcess.getSessionId();

        try {
            Session session = client.get(SESSION_URL(sessionId), Session.class);
            livyServer.setLivyServerStatus(LivyServerStatus.alive);
            livyServer.setLivySessionState(session.getId(), session.getState());
            logger.debug("session={}", session);
            return session;
        } catch (WebApplicationException wae) {
            // should catch 404, 500 etc.
            livyServer.recordSessionState(sessionId, wae.getResponse().getStatus(), null);
            throw wae;
        } catch (Exception e) {
            logger.error("Unexpected exception occurred:", e);
            throw e;
        }
    }


    @Override
    public Statement pollStatement(JerseyRestClient jerseyClient, SparkLivyProcess sparkLivyProcess, Integer stmtId) {
        return pollStatement(jerseyClient, sparkLivyProcess, stmtId, null);
    }


    @Override
    public Statement pollStatement(JerseyRestClient jerseyClient, SparkLivyProcess sparkLivyProcess, Integer stmtId, Long wait) {
        logger.entry(jerseyClient, sparkLivyProcess, stmtId, wait);

        long stopPolling = Long.MAX_VALUE;
        long startMillis = System.currentTimeMillis();
        if (wait != null) {
            // Limit the amount of time we will poll for a statement to complete.
            stopPolling = startMillis + livyProperties.getPollingLimit();
        }

        Statement statement;
        int pollCount = 1;
        do {
            statement = getStatement(jerseyClient, sparkLivyProcess, stmtId);

            if (statement.getState().equals(StatementState.error)) {
                // TODO: what about cancelled? or cancelling?
                logger.error("Unexpected error encountered while processing a statement", new LivyCodeException(statement.toString()));
                throw logger.throwing(new LivyUserException("livy.unexpected_error"));
            }

            if (System.currentTimeMillis() > stopPolling || statement.getState().equals(StatementState.available)) {
                break;
            }

            logger.trace("Statement was not ready, polling now with attempt '{}'", pollCount++);
            // statement not ready, wait for some time...
            try {
                Thread.sleep(livyProperties.getPollingInterval());
            } catch (InterruptedException e) {
                logger.error("Thread interrupted while polling Livy", e);
            }
        } while (true);

        logger.debug("exit DefaultLivyClient poll statement in '{}' millis, after '{}' attempts ", System.currentTimeMillis() - startMillis, pollCount);
        return logger.exit(statement);
    }

    private String SESSION_URL(Integer sessionId) {
        validateId(sessionId);
        return String.format(SESSIONS_URL + "/%s", sessionId);
    }

    private String STATEMENT_URL(Integer sessionId, Integer statementId) {
        validateId(sessionId);
        validateId(statementId);
        return String.format(SESSIONS_URL + "/%s/statements/%s", sessionId, statementId);
    }

    private String STATEMENTS_URL(Integer sessionId) {
        validateId(sessionId);
        return String.format(SESSIONS_URL + "/%s/statements", sessionId);
    }

    private void validateId(Integer id) {
        if (id == null) {
            logger.error("Illegal state occurred: ", new LivyInvalidSessionException("id cannot be null"));
            throw new LivyUserException("livy.unexpected_error");
        }
    }
}
