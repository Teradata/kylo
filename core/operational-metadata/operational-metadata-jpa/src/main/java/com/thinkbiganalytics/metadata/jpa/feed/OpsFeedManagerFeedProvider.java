package com.thinkbiganalytics.metadata.jpa.feed;

import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeed;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeedProvider;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecutionProvider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by sr186054 on 9/15/16.
 */
@Service
public class OpsFeedManagerFeedProvider implements OpsManagerFeedProvider {

    private OpsManagerFeedRepository repository;

    @Inject
    BatchJobExecutionProvider batchJobExecutionProvider;


    @Autowired
    public OpsFeedManagerFeedProvider(OpsManagerFeedRepository repository) {
        this.repository = repository;
    }

    @Override
    public OpsManagerFeed.ID resolveId(Serializable id) {
        if (id instanceof JpaOpsManagerFeed.OpsManagerFeedId) {
            return (JpaOpsManagerFeed.OpsManagerFeedId) id;
        } else {
            return new JpaOpsManagerFeed.OpsManagerFeedId(id);
        }
    }

    @Override
    public OpsManagerFeed findByName(String name) {
        return repository.findByName(name);
    }


    public OpsManagerFeed findById(OpsManagerFeed.ID id) {
        return repository.findOne(id);
    }

    public List<? extends OpsManagerFeed> findByFeedIds(List<OpsManagerFeed.ID> ids) {
        if (ids != null && !ids.isEmpty()) {
            return repository.findByFeedIds(ids);
        }
        return null;
    }

    public void save(List<? extends OpsManagerFeed> feeds) {
        repository.save((List<JpaOpsManagerFeed>) feeds);
    }

    @Override
    public OpsManagerFeed save(OpsManagerFeed.ID feedManagerId, String systemName) {
        OpsManagerFeed feed = findById(feedManagerId);
        if (feed == null) {
            feed = new JpaOpsManagerFeed();
            ((JpaOpsManagerFeed) feed).setName(systemName);
            ((JpaOpsManagerFeed) feed).setId((JpaOpsManagerFeed.OpsManagerFeedId) feedManagerId);
            repository.save((JpaOpsManagerFeed) feed);
        }
        return feed;
    }

    @Override
    public void delete(OpsManagerFeed.ID id) {
        OpsManagerFeed feed = findById(id);
        if (feed != null) {
            repository.delete(feed.getId());
        }
    }

    public boolean isFeedRunning(OpsManagerFeed.ID id) {
        OpsManagerFeed feed = findById(id);
        if (feed != null) {
            return batchJobExecutionProvider.isFeedRunning(feed.getName());
        }
        return false;
    }
}
