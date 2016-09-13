package com.thinkbiganalytics.datalake.authorization.rest.model;

import java.util.List;

/**
 * Created by Jeremy Merrifield on 9/12/16.
 */
public class RangerGroups {
    private int totalCount;
    private List<RangerGroup> vXGroups;

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public List<RangerGroup> getvXGroups() {
        return vXGroups;
    }

    public void setvXGroups(List<RangerGroup> vXGroups) {
        this.vXGroups = vXGroups;
    }
}
