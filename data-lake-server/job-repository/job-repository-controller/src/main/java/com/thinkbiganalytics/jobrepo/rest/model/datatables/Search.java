package com.thinkbiganalytics.jobrepo.rest.model.datatables;

/**
 * Created by sr186054 on 8/12/15.
 */
public class Search {
    private String regex;
    private String value;

    public Search() {

    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }
}
