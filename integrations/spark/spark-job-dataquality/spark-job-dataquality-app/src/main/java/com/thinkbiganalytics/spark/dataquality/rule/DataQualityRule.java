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

import com.thinkbiganalytics.spark.dataquality.util.FlowAttributes;
import org.json.simple.JSONObject;

/**
 * Interface for Data Quality rules
 */
public interface DataQualityRule {

    /**
     * Sets the attributes based on the passed in parameter
     * If a required attribute is missing, this will return false
     * 
     * @param flowAttr Attributes coming from the Flow file
     * @return Boolean indicating result of the load
     */
    boolean loadAttributes(FlowAttributes flowAttr);

    /**
     * Get the rule name
     * 
     * @return Name of the rule
     */
    String getName();

    /**
     * Gets the status of the rule (pass or fail)
     * 
     * @return Status of the rule
     */
    boolean getStatus();

    /**
     * Returns a string description of the rule
     * 
     * @return Description of the rule
     */
    String getDescription();

    /**
     * Returns a JSON object containing a summary of the rule. This includes all the attributes
     * pertaining to the rule (name, description, status, etc)
     * 
     * @return JSON summary of the rule
     */
    JSONObject getSummary();

    /**
     * Evaluates the data quality rule
     * 
     * @return Boolean indicating the result of the evaluation
     */
    boolean evaluate();
}
