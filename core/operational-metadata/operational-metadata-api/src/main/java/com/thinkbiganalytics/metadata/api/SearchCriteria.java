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

import org.apache.commons.lang3.StringUtils;

/**
 * SearchCriteria used for querying data
 */
public class SearchCriteria {
    public enum AND_OR {
        AND,OR
    }


    private String key;
    private String operation;
    private Object value;

    private SearchCriteria previousSearchCriteria;

    private AND_OR andOr = AND_OR.AND;

    public SearchCriteria() {

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
        this.andOr = previousSearchCriteria.andOr;
    }

    /**
     * Return the key or field to search
     *
     * @return the key or field to search
     */
    public String getKey() {
        return key;
    }

    /**
     * set the key or field to search
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Return the operation to perform
     *
     * @return the operation to perform
     */
    public String getOperation() {
        return operation;
    }

    /**
     * set the operation
     */
    public void setOperation(String operation) {
        this.operation = operation;
    }

    /**
     * Return the value to search for
     *
     * @return the value to search for
     */
    public Object getValue() {
        return value;
    }

    /**
     * set the value to search for
     */
    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * create a new search criteria using a new key
     * this allows you to update/change the key while maintaining the value to search on
     *
     * @return a new search criteria
     */
    public SearchCriteria withKey(String key) {
        return new SearchCriteria(key, this);
    }

    /**
     * @return {@code true} if the value is a collection of items (i.e. used in an IN clause) {@code false} if its a single value search
     */
    public boolean isValueCollection() {
        return (value != null && (value instanceof String) && StringUtils.contains((String) value, "\"") && StringUtils.contains((String) value, ","));
    }

    /**
     * Return the last search criteria object.
     *
     * @return the last search criteria object.
     */
    public SearchCriteria getPreviousSearchCriteria() {
        return previousSearchCriteria;
    }

    public AND_OR getAndOr() {
        return andOr;
    }

    public void setAndOr(AND_OR andOr) {
        this.andOr = andOr;
    }

    public boolean isOrFilter(){
        return AND_OR.OR.equals(getAndOr());
    }
}
