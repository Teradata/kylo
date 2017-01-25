package com.thinkbiganalytics.metadata.api;

/*-
 * #%L
 * thinkbig-operational-metadata-api
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

/**
 * Created by sr186054 on 11/29/16.
 */
public class SearchCriteria {
    private String key;
    private String operation;
    private Object value;

    private SearchCriteria previousSearchCriteria;


    public SearchCriteria(){

    }
    public SearchCriteria(String key, String operation, Object value) {
        this.key = key;
        this.operation = operation;
        this.value = value;
    }
    public SearchCriteria(String key, SearchCriteria previousSearchCriteria) {
        this.key = key;
        this.operation = previousSearchCriteria.getOperation();
        this.value = previousSearchCriteria.getValue();
        this.previousSearchCriteria = previousSearchCriteria;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public SearchCriteria withKey(String key){
         return new SearchCriteria(key,this);
    }

    public SearchCriteria getPreviousSearchCriteria() {
        return previousSearchCriteria;
    }
}
