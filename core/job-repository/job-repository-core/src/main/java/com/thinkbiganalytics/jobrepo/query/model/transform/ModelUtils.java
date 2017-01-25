package com.thinkbiganalytics.jobrepo.query.model.transform;

/*-
 * #%L
 * thinkbig-job-repository-core
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

import com.thinkbiganalytics.DateTimeUtil;
import com.thinkbiganalytics.jobrepo.query.model.SearchResult;
import com.thinkbiganalytics.jobrepo.query.model.SearchResultImpl;

import org.joda.time.DateTime;
import org.springframework.data.domain.Page;

/**
 * Created by sr186054 on 12/1/16.
 */
public class ModelUtils {

    public static Long runTime(DateTime start, DateTime stop){
        if(start == null){
            return 0L;
        }
        return (stop != null ? (stop.getMillis() - start.getMillis()) : DateTimeUtil.getNowUTCTime().getMillis() - start.getMillis());
    }

    public static Long timeSince(DateTime start, DateTime stop) {
        DateTime now = DateTimeUtil.getNowUTCTime();
        DateTime startTime = start != null ? start : now;
        return (stop != null ? (now.getMillis() - stop.getMillis()) : now.getMillis() - startTime.getMillis());
    }

    public static SearchResult toSearchResult(Page page){
        SearchResult searchResult = new SearchResultImpl();
        searchResult.setData(page.getContent());
        searchResult.setRecordsTotal(page.getTotalElements());
        searchResult.setRecordsFiltered(page.getTotalElements());
        return searchResult;

    }

}
