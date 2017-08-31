package com.thinkbiganalytics.metadata.rest.model.jcr;

/**
 * Created by sr186054 on 8/31/17.
 */
public class JcrQueryResultColumn {

    private String name;

    public JcrQueryResultColumn(){

    }

    public JcrQueryResultColumn(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
