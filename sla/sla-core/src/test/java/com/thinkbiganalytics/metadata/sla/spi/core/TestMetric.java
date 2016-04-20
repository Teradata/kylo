package com.thinkbiganalytics.metadata.sla.spi.core;

import com.thinkbiganalytics.metadata.sla.api.Metric;

/**
 *
 * @author Sean Felten
 */
class TestMetric implements Metric {
    
    private int intValue;
    private String stringValue;
    
    public TestMetric(int field1, String field2) {
        super();
        this.intValue = field1;
        this.stringValue = field2;
    }

    @Override
    public String getDescription() {
        return "Test metric: " + this.intValue + " " + this.stringValue;
    }
    
    public int getIntValue() {
        return intValue;
    }
    
    public String getStringValue() {
        return stringValue;
    }
    
    protected void setIntValue(int intValue) {
        this.intValue = intValue;
    }
    
    protected void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }
}