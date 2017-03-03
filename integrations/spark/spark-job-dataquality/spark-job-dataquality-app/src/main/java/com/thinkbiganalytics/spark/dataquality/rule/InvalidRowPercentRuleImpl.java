package com.thinkbiganalytics.spark.dataquality.rule;

/*-
 * #%L
 * kylo-spark-job-dataquality-app
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thinkbiganalytics.spark.dataquality.util.DataQualityConstants;
import com.thinkbiganalytics.spark.dataquality.util.FlowAttributes;
import com.thinkbiganalytics.spark.dataquality.util.MissingAttributeException;

import org.json.simple.JSONObject;

/**
 * Data Quality Rule to ensure that: <br>
 * Percent of Invalid rows out of all rows < invalid threshold defined by
 * DQ_INVALID_ALLOWED_PERCENT_ATTRIBUTE
 * 
 * If no threshold attribute is defined, the default value is used which is 0% i.e. no invalid roes
 */
public class InvalidRowPercentRuleImpl implements DataQualityRule {

    private static final Logger log = LoggerFactory.getLogger(InvalidRowPercentRuleImpl.class);

    private String name;
    private String description;
    private boolean status;

    private long feedRowCount;
    private long invalidRowCount;
    private long invalidThreshold;
    private double pctInvalid;

    public InvalidRowPercentRuleImpl() {
        this.name = "INVALID_ROW_PERCENT_RULE";
        this.description = "Percent of (Invalid rows / Total rows) < " +
                           DataQualityConstants.DEFAULT_INVALID_ALLOWED_PERCENT_VALUE;
        this.status = false;
        this.pctInvalid = Double.MAX_VALUE;
    }

    @Override
    public boolean loadAttributes(FlowAttributes flowAttr) {
        log.info("Loading Attributes for rule: " + name);
        try {
            feedRowCount = flowAttr.getAttributeValueLong(DataQualityConstants.DQ_FEED_ROW_COUNT_ATTRIBUTE);
            invalidRowCount = flowAttr.getAttributeValueLong(DataQualityConstants.DQ_INVALID_ROW_COUNT_ATTRIBUTE);
            invalidThreshold = flowAttr.getAttributeValueLong(DataQualityConstants.DQ_INVALID_ALLOWED_PERCENT_ATTRIBUTE,
                                                              DataQualityConstants.DEFAULT_INVALID_ALLOWED_PERCENT_VALUE);
        } catch (MissingAttributeException e) {
            log.error("Required attributes missing");
            return false;
        }

        return true;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public boolean getStatus() {
        return status;
    }

    @SuppressWarnings("unchecked")
    @Override
    public JSONObject getSummary() {
        JSONObject jsonAttrs = new JSONObject();
        jsonAttrs.put("Invalid Row Count", invalidRowCount);
        jsonAttrs.put("Invalid Threshold", invalidThreshold);
        jsonAttrs.put("Feed Row Count", feedRowCount);
        jsonAttrs.put("Percent Invalid", pctInvalid);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("DESCRIPTION", getDescription());
        jsonObject.put("STATUS", status);
        jsonObject.put("ATTRIBUTES", jsonAttrs);

        return jsonObject;
    }

    @Override
    public boolean evaluate() {
        try {
            // Execute rule
            pctInvalid = (invalidRowCount / ((double) feedRowCount)) * 100;
            status = (pctInvalid <= invalidThreshold);

            if (!status) {
                log.error("Invalid Row Count = " + invalidRowCount
                          +
                          " Invalid Threshold = "
                          + invalidThreshold
                          + " Percent Invalid = "
                          + pctInvalid);
            }

            return status;

        } catch (Exception e) {
            log.error("Error while evaluating rule", e);
        }

        return false;
    }



}
