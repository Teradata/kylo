package com.thinkbiganalytics.es;

import org.elasticsearch.search.SearchHit;

import java.util.List;

/**
 * Created by sr186054 on 2/10/16.
 */
public class SearchResult {

    private String query;

    private Long totalHits;
    private Long tookInMillis;
    private Double maxScore;
    private Long from;
    private Long to;
    private List<SearchHit> searchHits;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Long getTotalHits() {
        return totalHits;
    }

    public void setTotalHits(Long totalHits) {
        this.totalHits = totalHits;
    }

    public Long getTookInMillis() {
        return tookInMillis;
    }

    public void setTookInMillis(Long tookInMillis) {
        this.tookInMillis = tookInMillis;
    }

    public Double getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(Double maxScore) {
        this.maxScore = maxScore;
    }

    public Long getFrom() {
        return from;
    }

    public void setFrom(Long from) {
        this.from = from;
    }

    public Long getTo() {
        return to;
    }

    public void setTo(Long to) {
        this.to = to;
    }

    public List<SearchHit> getSearchHits() {
        return searchHits;
    }

    public void setSearchHits(List<SearchHit> searchHits) {
        this.searchHits = searchHits;
    }
}
