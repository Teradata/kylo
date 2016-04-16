package com.thinkbiganalytics.jobrepo.query.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Created by sr186054 on 8/17/15.
 */
//@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchResultImpl implements SearchResult {

  private List<? extends Object> data;
  private Long recordsTotal;
  private Long recordsFiltered;
  private String error;

  @Override
  public List<? extends Object> getData() {
    return data;
  }

  @Override
  public void setData(List<? extends Object> data) {
    this.data = data;
  }

  @Override
  public Long getRecordsTotal() {
    return recordsTotal;
  }

  @Override
  public void setRecordsTotal(Long recordsTotal) {
    this.recordsTotal = recordsTotal;
  }

  @Override
  public Long getRecordsFiltered() {
    return recordsFiltered;
  }

  @Override
  public void setRecordsFiltered(Long recordsFiltered) {
    this.recordsFiltered = recordsFiltered;
  }

  @Override
  public String getError() {
    return error;
  }

  @Override
  public void setError(String error) {
    this.error = error;
  }
}
