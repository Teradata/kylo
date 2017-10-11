package com.thinkbiganalytics.metadata.sla.alerts;

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

import com.thinkbiganalytics.metadata.sla.api.MetricAssessment;
import com.thinkbiganalytics.metadata.sla.api.ObligationAssessment;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAssessment;

/**
 */
public class ServiceLevelAssessmentAlertUtil {


    public static String getDescription(ServiceLevelAssessment slaAssmt) {
        return getDescription(slaAssmt,"\n\n");
    }

    public static String getDescription(ServiceLevelAssessment slaAssmt, String newlineChar) {

        StringBuilder descrBldr = new StringBuilder();

        // Generate a description string for the issue in outline form using the results and messages
        // of the assessment components: sla assessment->obligation assessments->metric assessments
        if (slaAssmt.getObligationAssessments() != null) {
            for (ObligationAssessment obAssmnt : slaAssmt.getObligationAssessments()) {
                if (obAssmnt.getMetricAssessments() != null) {
                    for (MetricAssessment metricAssmnt : obAssmnt.getMetricAssessments()) {

                        descrBldr
                            .append("Requirement: ")
                            .append(metricAssmnt.getMetricDescription())
                            .append(newlineChar)
                            .append("Result: ")
                            .append(metricAssmnt.getMessage())
                            .append(newlineChar);
                    }
                }
            }
        }
        descrBldr.append("\nAssessed on ").append(slaAssmt.getTime());

        return descrBldr.toString();
    }

}
