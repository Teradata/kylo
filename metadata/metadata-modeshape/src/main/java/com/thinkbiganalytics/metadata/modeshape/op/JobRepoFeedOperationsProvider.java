/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.op;

import com.thinkbiganalytics.jobrepo.query.model.ExecutedJob;
import com.thinkbiganalytics.jobrepo.query.model.ExecutionStatus;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedNotFoundExcepton;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeedProvider;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecution;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecutionProvider;
import com.thinkbiganalytics.metadata.api.op.FeedDependencyDeltaResults;
import com.thinkbiganalytics.metadata.api.op.FeedOperation;
import com.thinkbiganalytics.metadata.api.op.FeedOperation.ID;
import com.thinkbiganalytics.metadata.api.op.FeedOperation.State;
import com.thinkbiganalytics.metadata.api.op.FeedOperationCriteria;
import com.thinkbiganalytics.metadata.api.op.FeedOperationsProvider;
import com.thinkbiganalytics.metadata.core.AbstractMetadataCriteria;
import com.thinkbiganalytics.metadata.modeshape.feed.JcrFeedProvider;
import com.thinkbiganalytics.metadata.modeshape.op.FeedOperationExecutedJobWrapper.OpId;
import com.thinkbiganalytics.support.FeedNameUtil;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;

import javax.inject.Inject;

/**
 *
 * @author Sean Felten
 */
public class JobRepoFeedOperationsProvider implements FeedOperationsProvider {
    
    @Inject
    private JcrFeedProvider feedProvider;

    // @Inject
    // private FeedRepository feedRepo;

    //  @Inject
    //  private JobRepository jobRepo;

    @Inject
    private BatchJobExecutionProvider jobExecutionProvider;

    @Inject
    OpsManagerFeedProvider opsManagerFeedProvider;
    
    @Inject
    private MetadataAccess metadata;


    protected static FeedOperation.State asOperationState(ExecutionStatus status) {
        switch (status) {
            case ABANDONED:
                return State.CANCELED;
            case COMPLETED:
                return State.SUCCESS;
            case FAILED:
                return State.FAILURE;
            case STARTED:
                return State.STARTED;
            case STARTING:
                return State.STARTED;
            case STOPPING:
                return State.STARTED;
            case STOPPED:
                return State.CANCELED;
            case UNKNOWN:
                return State.STARTED;
            default:
                return State.FAILURE;
        }
    }

    protected static FeedOperation.State asOperationState(BatchJobExecution.JobStatus status) {
        switch (status) {
            case ABANDONED:
                return State.CANCELED;
            case COMPLETED:
                return State.SUCCESS;
            case FAILED:
                return State.FAILURE;
            case STARTED:
                return State.STARTED;
            case STARTING:
                return State.STARTED;
            case STOPPING:
                return State.STARTED;
            case STOPPED:
                return State.CANCELED;
            case UNKNOWN:
                return State.STARTED;
            default:
                return State.FAILURE;
        }
    }

    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.FeedOperationsProvider#criteria()
     */
    @Override
    public FeedOperationCriteria criteria() {
        return new Criteria();
    }

    public boolean isFeedRunning(Feed.ID feedId) {
        if (feedId != null) {
            return opsManagerFeedProvider.isFeedRunning(opsManagerFeedProvider.resolveId(feedId.toString()));
        }
        return false;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.FeedOperationsProvider#getOperation(com.thinkbiganalytics.metadata.api.op.FeedOperation.ID)
     */
    @Override
    public FeedOperation getOperation(ID id) {
        OpId opId = (OpId) id;
        BatchJobExecution jobExecution = jobExecutionProvider.findByJobExecutionId(new Long(opId.toString()));
        if (jobExecution != null) {
            return createOperation(jobExecution);
        } else {
            return null;
        }
    }
    /*


    @Override
    public List<FeedOperation> find(FeedOperationCriteria criteria) {
        // TODO Replace with more efficient, sql-based filtering...
        Criteria execCriteria = (Criteria) criteria;

        return this.jobRepo.findJobs(0, Integer.MAX_VALUE).stream()
                        .filter(execCriteria)
                        .limit(execCriteria.getLimit())
                        .map(exec -> createOperation(exec))
                        .collect(Collectors.toList());
    }

*/


    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.FeedOperationsProvider#find(Feed.ID, int)
     */
    @Override
    public List<FeedOperation> find(Feed.ID feedId) {
        return metadata.<List<FeedOperation>>read(() -> {
            List<FeedOperation> operations = new ArrayList<FeedOperation>();
            Feed<?> feed = this.feedProvider.getFeed(feedId);

            if (feed != null) {
                BatchJobExecution latestJobExecution = this.jobExecutionProvider.findLatestCompletedJobForFeed(feed.getQualifiedName());

                if (latestJobExecution != null) {
                    operations.add(createOperation(latestJobExecution));
                }
            }
            return operations;
        });
    }
    
    @Override
    public FeedDependencyDeltaResults getDependentDeltaResults(Feed.ID feedId, Set<String> props) {
        Feed feed = this.feedProvider.getFeed(feedId);
        
        if (feed != null) {
            String systemFeedName = FeedNameUtil.fullName(feed.getCategory().getName(), feed.getName());
            FeedDependencyDeltaResults results = new FeedDependencyDeltaResults(feed.getId().toString(),systemFeedName);

            //find this feeds latest completion
            BatchJobExecution latest = jobExecutionProvider.findLatestCompletedJobForFeed(systemFeedName);

            if (latest == null) {
                return results;
            } else {
                //get the dependent feeds
                List<Feed<?>> dependents =  feed.getDependentFeeds();
                for(Feed depFeed:dependents){

                    String depFeedSystemName = FeedNameUtil.fullName(depFeed.getCategory().getName(), depFeed.getName());
                    //find Completed feeds executed since time
                    Set<? extends BatchJobExecution> jobs =  jobExecutionProvider.findJobsForFeedCompletedSince(depFeedSystemName, latest.getStartTime());
                    if(jobs != null){
                        for(BatchJobExecution job: jobs){
                            DateTime endTime = job.getEndTime();
                            Map<String,String> executionContext = job.getJobExecutionContextAsMap();
                            Map<String,Object>  map = new HashMap<>();
                            //filter the map
                            if(executionContext != null) {
                                //add those requested to the results map
                                for (Entry<String, String> entry : executionContext.entrySet()) {
                                    if (props == null || props.isEmpty() || props.contains(entry.getKey())) {
                                        map.put(entry.getKey(), entry.getValue());
                                    }
                                }
                            }
                            results.addFeedExecutionContext(depFeedSystemName,job.getJobExecutionId(),job.getStartTime(),endTime,map);


                        }
                    }

                }
            }
            
            return results;
        } else {
            throw new FeedNotFoundExcepton(feedId);
        }
    }

    /*
    @Override
    public Map<DateTime, Map<String, Object>> getAllResults(FeedOperationCriteria criteria, Set<String> props) {
        Map<DateTime, Map<String, Object>> results = new HashMap<DateTime, Map<String,Object>>();
       // List<FeedOperation> ops = find(criteria);
        
        for (FeedOperation op : ops) {
            DateTime time = op.getStopTime();
            Map<String, Object> map = results.get(time);
            
            for (Entry<String, Object> entry : op.getResults().entrySet()) {
                if (props.isEmpty() || props.contains(entry.getKey())) {
                    if (map == null) {
                        map = new HashMap<>();
                        results.put(time, map);
                    }
                    
                    map.put(entry.getKey(), entry.getValue());
                }
            }
        }
        
        return results;
    }


    private FeedOperationExecutedJobWrapper createOperation(ExecutedJob exec) {
        Long id = exec.getExecutionId();
        // TODO Inefficient
        ExecutedJob fullExec = this.jobRepo.findByExecutionId(id.toString());
        return new FeedOperationExecutedJobWrapper(fullExec);
    }
      */


    private FeedOperationBatchJobExecutionJobWrapper createOperation(BatchJobExecution exec) {
        return new FeedOperationBatchJobExecutionJobWrapper(exec);
    }



    private class Criteria extends AbstractMetadataCriteria<FeedOperationCriteria> implements FeedOperationCriteria, Predicate<ExecutedJob> {

        private Set<State> states = new HashSet<>();
        private Set<Feed.ID> feedIds = new HashSet<>();
        private DateTime startedBefore;
        private DateTime startedSince;
        private DateTime stoppedBefore;
        private DateTime stoppedSince;
        
        // TODO This is a temporary filtering solution.  Replace with an implementation that uses
        // the criteria to create SQL.
        @Override
        public boolean test(ExecutedJob job) {
            Feed.ID id = null;
            
            if (! feedIds.isEmpty()) {
                String[] jobName = job.getJobName().split("\\.");
                Feed feed = feedProvider.findBySystemName(jobName[0], jobName[1]);
                id = feed != null ? feed.getId() : null;
            }
            
            return 
                (states.isEmpty() || states.contains(asOperationState(job.getStatus()))) &&
                (feedIds.isEmpty() || (id != null && feedIds.contains(id))) &&
                (this.startedBefore == null || this.startedBefore.isAfter(job.getStartTime())) &&
                (this.startedSince == null || this.startedSince.isBefore(job.getStartTime())) &&
                (this.stoppedBefore == null || this.stoppedBefore.isAfter(job.getEndTime())) &&
                (this.stoppedSince == null || this.stoppedSince.isBefore(job.getEndTime()));
        }

        @Override
        public FeedOperationCriteria state(State... states) {
            this.states.addAll(Arrays.asList(states));
            return this;
        }

        @Override
        public FeedOperationCriteria feed(Feed.ID... feedIds) {
            this.feedIds.addAll(Arrays.asList(feedIds));
            return this;
        }

        @Override
        public FeedOperationCriteria startedSince(DateTime time) {
            this.startedSince = time;
            return this;
        }

        @Override
        public FeedOperationCriteria startedBefore(DateTime time) {
            this.startedBefore = time;
            return this;
        }

        @Override
        public FeedOperationCriteria startedBetween(DateTime after, DateTime before) {
            startedSince(after);
            startedBefore(before);
            return this;
        }

        @Override
        public FeedOperationCriteria stoppedSince(DateTime time) {
            this.stoppedSince = time;
            return this;
        }

        @Override
        public FeedOperationCriteria stoppedBefore(DateTime time) {
            this.stoppedBefore = time;
            return this;
        }

        @Override
        public FeedOperationCriteria stoppedBetween(DateTime after, DateTime before) {
            stoppedSince(after);
            stoppedBefore(before);
            return this;
        }
    }
}
