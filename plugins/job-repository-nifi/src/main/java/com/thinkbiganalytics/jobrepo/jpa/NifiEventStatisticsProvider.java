package com.thinkbiganalytics.jobrepo.jpa;

import org.springframework.beans.factory.annotation.Qualifier;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Created by sr186054 on 8/17/16.
 */
public class NifiEventStatisticsProvider {

    @PersistenceContext
    @Inject
    @Qualifier("jobRepositoryEntityManager")
    protected EntityManager entityManager;

    public NifiEventSummaryStats create(NifiEventSummaryStats t) {
        this.entityManager.persist(t);
        return t;
    }

}
