package com.thinkbiganalytics.kylo.spark.model;

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


import java.util.List;
import java.util.Optional;

public class SessionsGetResponse {

    private Integer from;
    private Integer total;
    private List<Session> sessions;

    // default constructor
    public SessionsGetResponse() {
    }

    SessionsGetResponse(SessionsGetResponse.Builder sgb) {
        this.from = sgb.from;
        this.total = sgb.total;
        this.sessions = sgb.sessions;
    }

    public Integer getFrom() {
        return from;
    }

    public Integer getTotal() {
        return total;
    }

    public List<Session> getSessions() {
        return sessions;
    }

    public Optional<Session> getSessionWithId(Integer id) {
        return sessions.stream().
            filter(session -> session.getId().equals(id)).findFirst();
    }

    @Override
    public String toString() {
        return new StringBuilder("SessionsGetResponse{")
            .append("from=").append(from)
            .append(", total=").append(total)
            .append(", sessions=").append(sessions)
            .append('}')
            .toString();
    }

    public static class Builder {

        private Integer from;
        private Integer total;
        private List<Session> sessions;

        public Builder from(Integer from) {
            this.from = from;
            return this;
        }

        public Builder total(Integer total) {
            this.total = total;
            return this;
        }

        public Builder sessions(List<Session> sessions) {
            this.sessions = sessions;
            return this;
        }

        public SessionsGetResponse build() {
            return new SessionsGetResponse(this);
        }
    }

}

