package com.thinkbiganalytics.spark.dataquality.output;

/*-
 * #%L
 * thinkbig-spark-job-dataquality-app
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

import java.io.Serializable;

import com.thinkbiganalytics.spark.dataquality.rule.DataQualityRule;

/**
 * Class to represent a row in data quality output<br>
 * Format of output:<br>
 *
 * RuleName, Description, Status, Comments
 */
@SuppressWarnings("serial")
public class DataQualityRow implements Serializable {

    /**
     * Need to suppress squid:S00116 because these variables
     * names need to align with Hive table column names
     */
    @SuppressWarnings("squid:S00116")
    private String rule_name;
    @SuppressWarnings("squid:S00116")
    private String rule_description;
    @SuppressWarnings("squid:S00116")
    private boolean status;
    @SuppressWarnings("squid:S00116")
    private String rule_resultdetail;

    /**
     * No-argument constructor
     */
    public DataQualityRow() {
        rule_name = null;
        rule_description = null;
        status = false;
        rule_resultdetail = null;
    }


    /**
     * Constructor which populates using passed in Data Quality Rule.
     * 
     * @param rule DataQualityRule to use to populate object
     */
    public DataQualityRow(DataQualityRule rule) {
        this.rule_name = rule.getName();
        this.rule_description = rule.getDescription();
        this.status = rule.getStatus();
        this.rule_resultdetail = rule.getSummary().toJSONString();
    }


    /**
     * Get the rule name.
     *
     * @return rule name
     */
    public String getRuleName() {
        return rule_name;
    }


    /**
     * Set the rule name.
     *
     * @param ruleName name of the rule
     */
    public void setRuleName(String ruleName) {
        this.rule_name = ruleName;
    }


    /**
     * Get the rule description.
     *
     * @return rule description
     */
    public String getDescription() {
        return rule_description;
    }


    /**
     * Set the rule description.
     *
     * @param description description of the data quality rule
     */
    public void setDescription(String description) {
        this.rule_description = description;
    }

    /**
     * Get the rule status.
     *
     * @return rule status
     */
    public boolean getStatus() {
        return status;
    }


    /**
     * Set the rule status.
     *
     * @param status The status of the rule (pass or fail)
     */
    public void setStatus(boolean status) {
        this.status = status;
    }


    /**
     * Set the rule result details.
     *
     * @return rule result details
     */
    public String getResultDetail() {
        return rule_resultdetail;
    }


    /**
     * Set the rule result details.
     *
     * @param resultDetail The result details
     */
    public void setResultDetail(String resultDetail) {
        this.rule_resultdetail = resultDetail;
    }


    /**
     * Print verbose description of row to console
     */
    @Override
    public String toString() {
        return "DataQualityRow [ruleName=" + rule_name
               + ", description="
               + rule_description
               + ", status="
               + status
               + ", resultDetail ="
               + rule_resultdetail
               + "]";
    }



}
