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


import com.thinkbiganalytics.kylo.spark.model.enums.StatementState;

public class Statement {

    private Integer id;
    private String code;
    private StatementState state;
    private StatementOutputResponse output;
    private Double progress;

    // default constructor
    public Statement() {
    }

    public Statement(Builder sprb) {
        this.id = sprb.id;
        this.code = sprb.code;
        this.state = sprb.state;
        this.output = sprb.output;
        this.progress = progress;
    }

    public Integer getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public StatementState getState() {
        return state;
    }

    public StatementOutputResponse getOutput() {
        return output;
    }

    public Double getProgress() {
        return progress;
    }

    @Override
    public String toString() {
        return new StringBuilder("Statement{")
            .append("id=").append(id)
            .append(", code='").append(code).append('\'')
            .append(", state='").append(state).append('\'')
            .append(", output=").append(output)
            .append(", progress=").append(progress)
            .append('}')
            .toString();
    }

    public static class Builder {

        private Integer id;
        private String code;
        private StatementState state;
        private StatementOutputResponse output;
        private Double progress;

        public Builder id(Integer id) {
            this.id = id;
            return this;
        }

        public Builder code(String code) {
            this.code = code;
            return this;
        }

        public Builder state(StatementState state) {
            this.state = state;
            return this;
        }

        public Builder output(StatementOutputResponse output) {
            this.output = output;
            return this;
        }

        public Builder progress(Double progress) {
            this.progress = progress;
            return this;
        }
    }


}
