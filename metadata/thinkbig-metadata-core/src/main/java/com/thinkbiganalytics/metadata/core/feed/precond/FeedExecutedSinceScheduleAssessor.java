/**
 * 
 */
package com.thinkbiganalytics.metadata.core.feed.precond;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;

import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedCriteria;
import com.thinkbiganalytics.metadata.api.feed.FeedDestination;
import com.thinkbiganalytics.metadata.api.op.Dataset;
import com.thinkbiganalytics.metadata.api.op.ChangeSet;
import com.thinkbiganalytics.metadata.api.op.DataOperation;
import com.thinkbiganalytics.metadata.api.op.DataOperation.State;
import com.thinkbiganalytics.metadata.api.sla.FeedExecutedSinceSchedule;
import com.thinkbiganalytics.metadata.api.op.DataOperationCriteria;
import com.thinkbiganalytics.metadata.sla.api.AssessmentResult;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.spi.MetricAssessmentBuilder;
import com.thinkbiganalytics.scheduler.util.CronExpressionUtil;

/**
 *
 * @author Sean Felten
 */
public class FeedExecutedSinceScheduleAssessor extends MetadataMetricAssessor<FeedExecutedSinceSchedule> {

    @Override
    public boolean accepts(Metric metric) {
        return metric instanceof FeedExecutedSinceSchedule;
    }

    @Override
    public void assess(FeedExecutedSinceSchedule metric, MetricAssessmentBuilder<ArrayList<Dataset<Datasource, ChangeSet>>> builder) {
        Date prev = CronExpressionUtil.getPreviousFireTime(metric.getCronExpression(), 2);
        DateTime schedTime = new DateTime(prev);
        String feedName = metric.getFeedName();
        FeedCriteria crit = getFeedProvider().feedCriteria().name(feedName);
        Collection<Feed> set = getFeedProvider().getFeeds(crit);
        
        if (set.size() > 0) {
            Feed feed = set.iterator().next();
            DataOperationCriteria opCriteria = this.getDataOperationsProvider().dataOperationCriteria()
                    .feed(feed.getId())
                    .state(State.SUCCESS)
                    .limit(1);
            List<DataOperation> list = this.getDataOperationsProvider().getDataOperations(opCriteria);
            
            if (! list.isEmpty()) {
                DataOperation latest = list.get(0);
                
                if (latest.getStopTime().isAfter(schedTime)) {
                    builder
                        .result(AssessmentResult.SUCCESS)
                        .message("Feed " + feed.getName() + " has executed at least 1 data operation since " + schedTime)
                        .data(new ArrayList<Dataset<Datasource, ChangeSet>>());
                } else {
                    builder
                        .result(AssessmentResult.FAILURE)
                        .message("Feed " + feed.getName() + " has not executed any data operations since " + schedTime);
                }
            }
        }
    }

}
