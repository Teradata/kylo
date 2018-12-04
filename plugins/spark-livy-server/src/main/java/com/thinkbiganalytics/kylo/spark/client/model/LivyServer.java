package com.thinkbiganalytics.kylo.spark.client.model;

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


import com.google.common.collect.Maps;
import com.thinkbiganalytics.kylo.spark.client.model.enums.LivyServerStatus;
import com.thinkbiganalytics.kylo.spark.client.model.enums.LivySessionStatus;
import com.thinkbiganalytics.kylo.spark.model.enums.SessionState;

import org.apache.commons.lang3.Validate;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.ws.rs.core.Response;

public class LivyServer {

    private String hostname;
    private Integer port;

    private AtomicInteger sessionIdHighWaterMark = new AtomicInteger(-1);
    private ConcurrentMap<Integer, SessionState> livySessionStates = Maps.newConcurrentMap();
    private LivyServerStatus livyServerStatus = LivyServerStatus.not_found;

    public LivyServer(String hostname, Integer port) {
        this.hostname = hostname;
        this.port = port;
    }

    /**
     * update the high water mark to the max of current high water and newly observed session id
     *
     * NOTE: if the sessionId is observed to be 0, then it is assumed we are seeing a newly started server (either first ever start of Livy or a restart when not in recovery mode).  This will reset
     * the SessionState cache
     **/
    public void setSessionIdHighWaterMark(Integer sessionId) {
        Validate.notNull(sessionId, "sessionId cannot be null");
        int prevValue = this.sessionIdHighWaterMark.getAndAccumulate(sessionId, Math::max);
        if (sessionId == 0 && prevValue > 0) {
            // Livy server Ids are starting over
            livySessionStates.clear();
        }
    }

    public SessionState getLivySessionState(Integer sessionId) {
        return livySessionStates.get(sessionId);
    }

    public void clearLivySessionState(Integer sessionId) {
        this.livySessionStates.remove(sessionId);
    }

    public void setLivySessionState(Integer sessionId, SessionState sessionState) {
        this.livySessionStates.put(sessionId, sessionState);
    }

    public LivyServerStatus getLivyServerStatus() {
        return livyServerStatus;
    }

    /**
     * Gets set by LivyClient on any successful call.  Set by heartbeat thread if server is down for some time.
     **/
    public void setLivyServerStatus(LivyServerStatus livyServerStatus) {
        this.livyServerStatus = livyServerStatus;
    }

    public Integer getSessionIdHighWaterMark() {
        return sessionIdHighWaterMark.get();
    }

    // used by heartbeat thread
    public LivySessionStatus recordSessionState(int sessionId, int httpCode, SessionState state) {
        if (httpCode == 404) {
            // Livy doesn't know about this session any longer
            this.setLivyServerStatus(LivyServerStatus.alive);
            this.clearLivySessionState(sessionId);
            return LivySessionStatus.completed;
        }

        if (Response.Status.Family.familyOf(httpCode) != Response.Status.Family.SUCCESSFUL) {
            // ideally, we don't see 500 errors.  but if we do, server status can reflect the problem.  May be transient problem...
            this.setLivyServerStatus(LivyServerStatus.http_error);
            return LivySessionStatus.http_error;  // TODO: add to heartbeat test..
        }

        Validate.notNull(state, "SessionState from successful HTTP call cannot be null");

        livySessionStates.put(sessionId, state);

        if (SessionState.FINAL_STATES.contains(state)) {
            return LivySessionStatus.completed;
        }

        if (SessionState.READY_STATES.contains(state)) {
            return LivySessionStatus.ready;
        }

        throw new IllegalStateException(String.format(
            "Illegal State for session id='%s', httpCode='%s', sessionState='%s'", sessionId, httpCode, state));
    }

    @Override
    public String toString() {
        return new StringBuilder("LivyServer{")
            .append("sessionIdHighWaterMark=").append(sessionIdHighWaterMark)
            .append(", livySessionStates=").append(livySessionStates)
            .append(", livyServerStatus=").append(livyServerStatus)
            .append('}')
            .toString();
    }
}
