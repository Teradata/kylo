package com.thinkbiganalytics.jobrepo.rest.model.datatables;

/**
 * Created by sr186054 on 8/12/15.
 */
public class Column {

    private boolean orderable;
    private boolean searchable;
    private String data;
    private String name;
    private Search search;

    public Column() {
    }

    public boolean isOrderable() {
        return orderable;
    }

    public void setOrderable(boolean orderable) {
        this.orderable = orderable;
    }

    public boolean isSearchable() {
        return searchable;
    }

    public void setSearchable(boolean searchable) {
        this.searchable = searchable;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Search getSearch() {
        return search;
    }

    public void setSearch(Search search) {
        this.search = search;
    }


}
