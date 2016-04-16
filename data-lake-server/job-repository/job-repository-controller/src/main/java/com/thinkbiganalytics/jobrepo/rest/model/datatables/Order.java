package com.thinkbiganalytics.jobrepo.rest.model.datatables;

/**
 * Created by sr186054 on 8/12/15.
 */
public class Order {

    private int column;
    private String dir;

    public Order() {

    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }
}
