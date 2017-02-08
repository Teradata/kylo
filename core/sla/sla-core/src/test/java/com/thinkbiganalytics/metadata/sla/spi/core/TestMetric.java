package com.thinkbiganalytics.metadata.sla.spi.core;

/*-
 * #%L
 * thinkbig-sla-core
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.thinkbiganalytics.metadata.sla.api.Metric;

/**
 *
 */
class TestMetric implements Metric {

    private int intValue;
    private String stringValue;

    public TestMetric() {
    }

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

    protected void setIntValue(int intValue) {
        this.intValue = intValue;
    }

    public String getStringValue() {
        return stringValue;
    }

    protected void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }
}
