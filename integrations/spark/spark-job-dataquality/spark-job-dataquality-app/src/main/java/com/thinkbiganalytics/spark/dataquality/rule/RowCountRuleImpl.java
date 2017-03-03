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

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thinkbiganalytics.spark.dataquality.util.DataQualityConstants;
import com.thinkbiganalytics.spark.dataquality.util.FlowAttributes;
import com.thinkbiganalytics.spark.dataquality.util.MissingAttributeException;

/**
 * Data Quality Rule to ensure that: <br>
 * Number of Source rows = Valid row count + Invalid row count
 */
public class RowCountRuleImpl implements DataQualityRule {
    private static final Logger log = LoggerFactory.getLogger(RowCountRuleImpl.class);

    private String name;
    private String description;
    private boolean status;

    long sourceRowCount;
    long invalidRowCount;
    long validRowCount;

    public RowCountRuleImpl() {
        this.name = "ROW_COUNT_TOTAL_RULE";
        this.description = "Source Row Count == (Valid and Invalid Row Counts)";
        this.status = false;
    }

    @Override
    public boolean loadAttributes(FlowAttributes flowAttr) {
        try {
            sourceRowCount = flowAttr.getAttributeValueLong(DataQualityConstants.SOURCE_ROW_COUNT_ATTRIBUTE);
            invalidRowCount = flowAttr.getAttributeValueLong(DataQualityConstants.DQ_INVALID_ROW_COUNT_ATTRIBUTE);
            validRowCount = flowAttr.getAttributeValueLong(DataQualityConstants.DQ_VALID_ROW_COUNT_ATTRIBUTE);
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
        jsonAttrs.put("Source Row Count", sourceRowCount);
        jsonAttrs.put("Valid Row Count", validRowCount);
        jsonAttrs.put("Invalid Row Count", invalidRowCount);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("DESCRIPTION", getDescription());
        jsonObject.put("STATUS", status);
        jsonObject.put("ATTRIBUTES", jsonAttrs);

        return jsonObject;

    }

    @Override
    public boolean evaluate() {
        try {
            status = (sourceRowCount == (invalidRowCount + validRowCount));
            if (!status) {
                log.error("Source Row Count = " + sourceRowCount
                          +
                          " Valid Row Count = "
                          + validRowCount
                          +
                          " Invalid Row Count = "
                          + invalidRowCount);
            }
            return status;
        } catch (Exception e) {
            log.error("Error while evaluating rule", e);
        }
        return false;
    }
}
