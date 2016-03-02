/**
 * 
 */
package com.thinkbiganalytics.metadata.rest.model.sla;

import java.text.ParseException;

/**
 *
 * @author Sean Felten
 */
public class DatasourceUpdatedSinceMetric extends DependentDatasetMetric {

    private String cronExpression;

    public DatasourceUpdatedSinceMetric(String datasetName, String cronExpression) throws ParseException {
        super(datasetName);
        this.cronExpression = cronExpression;
    }
}
