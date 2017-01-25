package com.thinkbiganalytics.jobrepo.repository.rest.model;

/*-
 * #%L
 * thinkbig-job-repository-rest-model
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Created by sr186054 on 4/15/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchResult {

    private List<? extends Object> data;
    private Long recordsTotal;
    private Long recordsFiltered;
    private String error;

    public SearchResult() {

    }

    public List<? extends Object> getData() {
        return data;
    }


    public void setData(List<? extends Object> data) {
        this.data = data;
    }


    public Long getRecordsTotal() {
        return recordsTotal;
    }


    public void setRecordsTotal(Long recordsTotal) {
        this.recordsTotal = recordsTotal;
    }


    public Long getRecordsFiltered() {
        return recordsFiltered;
    }


    public void setRecordsFiltered(Long recordsFiltered) {
        this.recordsFiltered = recordsFiltered;
    }


    public String getError() {
        return error;
    }


    public void setError(String error) {
        this.error = error;
    }
}
