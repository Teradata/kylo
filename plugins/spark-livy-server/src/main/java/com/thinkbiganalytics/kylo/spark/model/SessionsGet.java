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


public class SessionsGet {

    private Integer from;
    private Integer size;

    SessionsGet(SessionsGet.Builder sgb) {
        this.from = sgb.from;
        this.size = sgb.size;
    }

    public Integer getFrom() {
        return from;
    }

    public void setFrom(Integer from) {
        this.from = from;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return new StringBuilder("SessionsGet{")
            .append("from=").append(from)
            .append(", size=").append(size)
            .append('}')
            .toString();
    }

    public static class Builder {

        private Integer from;
        private Integer size;

        public Builder from(Integer from) {
            this.from = from;
            return this;
        }

        public Builder size(Integer size) {
            this.size = size;
            return this;
        }

        public SessionsGet build() {
            return new SessionsGet(this);
        }
    }

}

