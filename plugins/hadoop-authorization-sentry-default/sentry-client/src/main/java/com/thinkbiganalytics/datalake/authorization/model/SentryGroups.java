package com.thinkbiganalytics.datalake.authorization.model;

import java.util.List;

/**
 * Created by Jeremy Merrifield on 9/12/16.
 */
public class SentryGroups {

    private int totalCount;
    private List<SentryGroup> vXGroups;

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public List<SentryGroup> getvXGroups() {
        return vXGroups;
    }

    public void setvXGroups(List<SentryGroup> vXGroups) {
        this.vXGroups = vXGroups;
    }
}
