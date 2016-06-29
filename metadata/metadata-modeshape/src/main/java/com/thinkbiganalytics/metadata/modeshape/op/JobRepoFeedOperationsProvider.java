/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.op;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.joda.time.DateTime;

import com.thinkbiganalytics.jobrepo.query.model.ExecutedFeed;
import com.thinkbiganalytics.jobrepo.query.model.ExecutedJob;
import com.thinkbiganalytics.jobrepo.query.model.ExecutionStatus;
import com.thinkbiganalytics.jobrepo.repository.FeedRepository;
import com.thinkbiganalytics.jobrepo.repository.JobRepository;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.op.FeedOperation;
import com.thinkbiganalytics.metadata.api.op.FeedOperation.ID;
import com.thinkbiganalytics.metadata.api.op.FeedOperation.State;
import com.thinkbiganalytics.metadata.api.op.FeedOperationCriteria;
import com.thinkbiganalytics.metadata.api.op.FeedOperationsProvider;
import com.thinkbiganalytics.metadata.core.AbstractMetadataCriteria;
import com.thinkbiganalytics.metadata.modeshape.feed.JcrFeedProvider;
import com.thinkbiganalytics.metadata.modeshape.op.FeedOperationExecutedJobWrapper.OpId;

/**
 *
 * @author Sean Felten
 */
public class JobRepoFeedOperationsProvider implements FeedOperationsProvider {
    
    @Inject
    private JcrFeedProvider feedProvider;
    
    @Inject
    private FeedRepository feedRepo;
    
    @Inject
    private JobRepository jobRepo;
    
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
                return State.IN_PROGRESS;
            case STARTING:
                return State.IN_PROGRESS;
            case STOPPING:
                return State.IN_PROGRESS;
            case STOPPED:
                return State.CANCELED;
            case UNKNOWN:
                return State.IN_PROGRESS;
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

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.FeedOperationsProvider#getOperation(com.thinkbiganalytics.metadata.api.op.FeedOperation.ID)
     */
    @Override
    public FeedOperation getOperation(ID id) {
        OpId opId = (OpId) id;
        ExecutedJob exec = this.jobRepo.findByExecutionId(opId.toString());
        
        if (exec != null) {
            return new FeedOperationExecutedJobWrapper(exec);
        } else {
            return null;
        }
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.FeedOperationsProvider#find(com.thinkbiganalytics.metadata.api.op.FeedOperationCriteria)
     */
    @Override
    public List<FeedOperation> find(FeedOperationCriteria criteria) {
        // TODO Replace with more efficient, sql-based filtering...
        Criteria execCriteria = (Criteria) criteria;
        
        return this.jobRepo.findJobs(0, Integer.MAX_VALUE).stream()
                        .limit(execCriteria.getLimit())
                        .filter(execCriteria)
                        .map((exec) -> new FeedOperationExecutedJobWrapper(exec))
                        .collect(Collectors.toList());
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.FeedOperationsProvider#find(Feed.ID)
     */
    @Override
    public List<FeedOperation> find(Feed.ID feedId) {
        return find(feedId, Integer.MAX_VALUE);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.op.FeedOperationsProvider#find(Feed.ID, int)
     */
    @Override
    public List<FeedOperation> find(Feed.ID feedId, int limit) {
        return metadata.<List<FeedOperation>>read(() -> {
            Feed feed = this.feedProvider.getFeed(feedId);
            
            if (feed != null) {
                ExecutedFeed feedExec = this.feedRepo.findLastCompletedFeed(feed.getQualifiedName());
                
                if (feedExec != null) {
                    List<ExecutedJob> jobs = feedExec.getExecutedJobs();
                    
                    if (jobs.isEmpty()) {
                        return Collections.<FeedOperation>emptyList();
                    } else {
                        return feedExec.getExecutedJobs().stream()
                                        .limit(limit)
                                        .map((exec) -> new FeedOperationExecutedJobWrapper(exec))
                                        .collect(Collectors.toList());
                    }
                } else {
                    return Collections.<FeedOperation>emptyList();
                }
            } else {
                return Collections.<FeedOperation>emptyList();
            }
        });
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
                Feed feed = feedProvider.findBySystemName(job.getFeedName());
                id = feed != null ? feed.getId() : null;
            }
            
            return 
                (states.isEmpty() || states.contains(asOperationState(job.getStatus()))) &&
                (feedIds.isEmpty() || (id != null && feedIds.contains(id))) &&
                (this.startedBefore == null || this.startedBefore.isBefore(job.getStartTime())) &&
                (this.startedSince == null || this.startedSince.isAfter(job.getStartTime())) &&
                (this.stoppedBefore == null || this.stoppedBefore.isBefore(job.getEndTime())) &&
                (this.stoppedSince == null || this.stoppedSince.isAfter(job.getEndTime()));
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
