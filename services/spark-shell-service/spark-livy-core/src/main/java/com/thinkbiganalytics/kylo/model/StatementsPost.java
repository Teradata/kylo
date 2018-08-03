package com.thinkbiganalytics.kylo.model;

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

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class StatementsPost {
    private String code;
    private String kind;

    StatementsPost(StatementsPost.Builder sgb ) {
        this.code = sgb.code;
        this.kind = sgb.kind;
    }

    public String getcode() {
        return code;
    }

    public String getkind() {
        return kind;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("StatementsPost{");
        sb.append("code='").append(code).append('\'');
        sb.append(", kind='").append(kind).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public static class Builder {
        private String code;
        private String kind;

        public Builder code(String code) {
            this.code = code;
            return this;
        }

        public Builder kind(String kind) {
            this.kind = kind;
            return this;
        }

        public StatementsPost build() {
            return new StatementsPost(this);
        }
    }

}

