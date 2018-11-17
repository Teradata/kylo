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


import com.thinkbiganalytics.kylo.spark.model.enums.SessionKind;
import com.thinkbiganalytics.kylo.spark.model.enums.SessionState;

import java.util.List;
import java.util.Map;

/**
 * @implNote https://livy.incubator.apache.org/docs/latest/rest-api.html#session {"id":0,"appId":null,"owner":null,"proxyUser":null,"state":"starting","kind":"shared",\
 * "appInfo":{"driverLogUrl":null,"sparkUiUrl":null},"log":["stdout: ","\nstderr: "]} ]}
 */
public class Session {

    private Integer id;
    private String appId;
    private String size;
    private String owner;
    private String proxyUser;
    private SessionState state;
    private SessionKind kind;
    private Map<String, String> appInfo;
    private List<String> log;

    public Session() {
    }

    Session(Session.Builder sgb) {
        this.id = sgb.id;
        this.appId = sgb.appId;
        this.size = sgb.size;
        this.owner = sgb.owner;
        this.proxyUser = sgb.proxyUser;
        this.state = sgb.state;
        this.kind = sgb.kind;
        this.appInfo = sgb.appInfo;
        this.log = sgb.log;
    }

    public Integer getId() {
        return id;
    }

    public String getAppId() {
        return appId;
    }

    public String getSize() {
        return size;
    }

    public String getOwner() {
        return owner;
    }

    public String getProxyUser() {
        return proxyUser;
    }

    public SessionState getState() {
        return state;
    }

    public SessionKind getKind() {
        return kind;
    }

    public Map<String, String> getAppInfo() {
        return appInfo;
    }

    public List<String> getLog() {
        return log;
    }

    @Override
    public String toString() {
        return new StringBuilder("Session{")
            .append("id=").append(id)
            .append(", appId='").append(appId).append('\'')
            .append(", size='").append(size).append('\'')
            .append(", owner='").append(owner).append('\'')
            .append(", proxyUser='").append(proxyUser).append('\'')
            .append(", state='").append(state).append('\'')
            .append(", kind='").append(kind).append('\'')
            .append(", appInfo=").append(appInfo)
            .append(", log=").append(log)
            .append('}')
            .toString();
    }

    // Only used in testing, this is typically a response object
    public static class Builder {

        private Integer id;
        private String size;
        private String appId;
        private String owner;
        private String proxyUser;
        private SessionState state;
        private SessionKind kind;
        private Map<String, String> appInfo;
        private List<String> log;

        public Builder id(Integer id) {
            this.id = id;
            return this;
        }

        public Builder appId(String appId) {
            this.appId = appId;
            return this;
        }

        public Builder size(String size) {
            this.size = size;
            return this;
        }

        public Builder owner(String owner) {
            this.owner = owner;
            return this;
        }

        public Builder proxyUser(String proxyUser) {
            this.proxyUser = proxyUser;
            return this;
        }

        public Builder state(SessionState state) {
            this.state = state;
            return this;
        }

        public Builder kind(SessionKind kind) {
            this.kind = kind;
            return this;
        }

        public Builder appInfo(Map<String, String> appInfo) {
            this.appInfo = appInfo;
            return this;
        }

        public Builder log(List<String> log) {
            this.log = log;
            return this;
        }

        public Session build() {
            return new Session(this);
        }
    }

}

