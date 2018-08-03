package com.thinkbiganalytics.repository.api;

/*-
 * #%L
 * kylo-repository-service
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

import java.util.Comparator;

public class TemplateSearchFilter {
    Integer limit = 0;
    Integer start = 0;
    String sort;

    public TemplateSearchFilter(String sort, Integer limit, Integer start) {
        this.sort = sort;
        this.limit = limit;
        this.start = start;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public static enum TemplateComparator {
        NAME("name", Comparator.comparing((w) -> w.getTemplateName())),
        REPOSITORY_TYPE("repositoryType", Comparator
            .comparing((r) -> r.getRepository().getType().getKey()));

        private Comparator<TemplateMetadataWrapper> comparator;
        private String key;

        TemplateComparator(String key, Comparator<TemplateMetadataWrapper> comparator){
            this.key = key;
            this.comparator = comparator;
        }

        public String getKey() { return this.key; }

        public Comparator<TemplateMetadataWrapper> getComparator() { return this.comparator; }
    }
}
