package com.thinkbiganalytics.spark.rest.model;

/*-
 * #%L
 * Spark Shell Service REST Model
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

public class ServerStatusResponse {

    public enum ServerStatus {
        not_found,
        alive,
        http_error
    }

    public enum SessionStatus {
        starting,
        ready,
        completed,
        http_error
    }

    static public class SessionInfo {

        private String id;

        private ServerStatusResponse.SessionStatus sessionStatus;

        private SessionInfo(String id, SessionStatus sessionStatus) {
            this.id = id;
            this.sessionStatus = sessionStatus;
        }

        public static SessionInfo newInstance(String id, SessionStatus sessionStatus) {
            return new SessionInfo(id, sessionStatus);
        }

        public String getId() {
            return id;
        }

        public SessionStatus getSessionStatus() {
            return sessionStatus;
        }
    }

    final private ServerStatus serverStatus;

    final private SessionInfo sessionInfo; // can be null

    public ServerStatus getServerStatus() {
        return serverStatus;
    }

    public SessionInfo getSessionInfo() {
        return sessionInfo;
    }

    private ServerStatusResponse(ServerStatus serverStatus, SessionInfo sessionInfo) {
        this.serverStatus = serverStatus;
        this.sessionInfo = sessionInfo;
    }

    public static ServerStatusResponse newInstance(ServerStatus serverStatus, String id, SessionStatus sessionStatus) {
        return new ServerStatusResponse(serverStatus, SessionInfo.newInstance(id, sessionStatus));
    }

}
