package com.thinkbiganalytics.rest.model.search;

/*-
 * #%L
 * thinkbig-job-repository-api
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

import java.util.List;

/**
 * Searching/Filtering object to store the results and summary information about the resulting data
 */
public interface SearchResult<T extends Object> {

    /**
     * Return the data from the search
     *
     * @return the data from the search
     */
    List<T> getData();

    /**
     * set the data
     */
    void setData(List<T> data);

    /**
     * Return the total record count (not filtered)
     *
     * @return the total count (not filtered)
     */
    Long getRecordsTotal();

    /**
     * set the total count
     */
    void setRecordsTotal(Long recordsTotal);

    /**
     * Return the number of records from the {@link #getRecordsTotal()} that exist in the {@link #getData()} as a result of the search/filter
     *
     * @return the number of records from the {@link #getRecordsTotal()} that exist in the {@link #getData()} as a result of the search/filter
     */
    Long getRecordsFiltered();

    /**
     * set the number of records from the {@link #getRecordsTotal()} that exist in the {@link #getData()} as a result of the search/filter
     */
    void setRecordsFiltered(Long recordsFiltered);

    /**
     * Return any error string message if an error was found
     *
     * @return a message of the error, if an error was found
     */
    String getError();

    /**
     * set an error message to the result
     */
    void setError(String error);
}
