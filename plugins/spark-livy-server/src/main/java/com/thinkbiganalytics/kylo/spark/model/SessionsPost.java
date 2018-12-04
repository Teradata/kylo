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


import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

/**
 * @implNote https://livy.incubator.apache.org/docs/latest/rest-api.html
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SessionsPost {

    private String kind;
    private String proxyUser;
    private Map<String, String> conf;
    private List<String> jars;

    SessionsPost(SessionsPost.Builder sgb) {
        this.kind = sgb.kind;
        this.proxyUser = sgb.proxyUser;
        this.conf = sgb.conf;
        this.jars = sgb.jars;
    }

    public String getKind() {
        return kind;
    }

    public String getProxyUser() {
        return proxyUser;
    }

    public Map<String, String> getConf() {
        return conf;
    }

    public List<String> getJars() {
        return jars;
    }

    @Override
    public String toString() {
        return new StringBuilder("SessionsPost{")
            .append("kind='").append(kind).append('\'')
            .append(", proxyUser='").append(proxyUser).append('\'')
            .append(", conf=").append(conf)
            .append(", jars=").append(jars)
            .append('}')
            .toString();
    }

    public static class Builder {

        private String kind;
        private String proxyUser;
        private Map<String, String> conf;
        private List<String> jars;

        public Builder kind(String kind) {
            this.kind = kind;
            return this;
        }

        public Builder proxyUser(String proxyUser) {
            this.proxyUser = proxyUser;
            return this;
        }

        public Builder conf(Map<String, String> conf) {
            this.conf = conf;
            return this;
        }

        public Builder jars(List<String> jars) {
            this.jars = jars;
            return this;
        }

        public SessionsPost build() {
            return new SessionsPost(this);
        }
    }

}

