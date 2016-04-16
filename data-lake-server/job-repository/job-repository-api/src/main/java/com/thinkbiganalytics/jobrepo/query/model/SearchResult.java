package com.thinkbiganalytics.jobrepo.query.model;

import java.util.List;

/**
 * Created by sr186054 on 4/13/16.
 */
public interface SearchResult {
    List<? extends Object> getData();

    void setData(List<? extends Object> data);

    Long getRecordsTotal();

    void setRecordsTotal(Long recordsTotal);

    Long getRecordsFiltered();

    void setRecordsFiltered(Long recordsFiltered);

    String getError();

    void setError(String error);
}
