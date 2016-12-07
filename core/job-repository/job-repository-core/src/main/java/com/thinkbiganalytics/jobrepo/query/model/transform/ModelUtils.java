package com.thinkbiganalytics.jobrepo.query.model.transform;

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
        return (stop != null ? (stop.getMillis() - start.getMillis()) : DateTime.now().getMillis() - start.getMillis());
    }

    public static Long timeSinceEndTime( DateTime stop){
        return (stop != null ? (DateTime.now().getMillis() - stop.getMillis()) : 0L);
    }

    public static SearchResult toSearchResult(Page page){
        SearchResult searchResult = new SearchResultImpl();
        searchResult.setData(page.getContent());
        searchResult.setRecordsTotal(page.getTotalElements());
        searchResult.setRecordsFiltered(page.getTotalElements());
        return searchResult;

    }

}
