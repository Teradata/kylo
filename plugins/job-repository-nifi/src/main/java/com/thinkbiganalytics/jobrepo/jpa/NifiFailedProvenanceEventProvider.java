package com.thinkbiganalytics.jobrepo.jpa;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.thinkbiganalytics.jobrepo.jpa.model.NifiFailedEvent;
import com.thinkbiganalytics.jobrepo.model.FailedProvenanceEvent;
import com.thinkbiganalytics.jobrepo.service.FailedProvenanceEventProvider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by sr186054 on 8/17/16.
 */
@Service
public class NifiFailedProvenanceEventProvider implements FailedProvenanceEventProvider {

    @Autowired
    private JPAQueryFactory factory;

    private NifiFailedEventRepository repository;

    @Autowired
    public NifiFailedProvenanceEventProvider(NifiFailedEventRepository repository) {
        this.repository = repository;
    }


    @Override
    public FailedProvenanceEvent create(FailedProvenanceEvent t) {
        return repository.save((NifiFailedEvent) t);
    }


}
