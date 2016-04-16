package com.thinkbiganalytics.jobrepo.repository.rest.model;

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
