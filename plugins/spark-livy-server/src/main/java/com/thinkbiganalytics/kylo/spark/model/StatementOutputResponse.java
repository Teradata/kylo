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


import com.fasterxml.jackson.databind.JsonNode;
import com.thinkbiganalytics.kylo.spark.model.enums.StatementOutputStatus;

public class StatementOutputResponse {

    private StatementOutputStatus status;
    private Integer execution_count;
    private JsonNode data;
    private String ename;
    private String evalue;
    private JsonNode traceback;

    // default constructor
    public StatementOutputResponse() {
    }

    StatementOutputResponse(StatementOutputResponse.Builder sgb) {
        this.status = sgb.status;
        this.execution_count = sgb.execution_count;
        this.data = data;
    }

    public StatementOutputStatus getStatus() {
        return status;
    }

    public Integer getExecution_count() {
        return execution_count;
    }

    public JsonNode getData() {
        return data;
    }

    public String getEname() {
        return ename;
    }

    public JsonNode getTraceback() {
        return traceback;
    }

    public String getEvalue() {
        return evalue;
    }

    @Override
    public String toString() {
        return new StringBuilder("StatementOutputResponse{")
            .append("status='").append(status).append('\'')
            .append(", execution_count=").append(execution_count)
            .append(", data=").append(data)
            .append(", ename='").append(ename).append('\'')
            .append(", evalue='").append(evalue).append('\'')
            .append(", traceback=").append(traceback)
            .append('}')
            .toString();
    }

    // response objects don't typically utilize a builder, unless in tests
    public static class Builder {

        private StatementOutputStatus status;
        private Integer execution_count;
        private JsonNode data;
        private String ename;
        private String evalue;
        private JsonNode traceback;

        public Builder status(StatementOutputStatus status) {
            this.status = status;
            return this;
        }

        public Builder execution_count(Integer execution_count) {
            this.execution_count = execution_count;
            return this;
        }

        public Builder data(JsonNode data) {
            this.data = data;
            return this;
        }

        public Builder ename(String ename) {
            this.ename = ename;
            return this;
        }

        public Builder evalue(String evalue) {
            this.evalue = evalue;
            return this;
        }

        public Builder traceback(JsonNode traceback) {
            this.traceback = traceback;
            return this;
        }

        public StatementOutputResponse build() {
            return new StatementOutputResponse(this);
        }
    }

}

