package com.thinkbiganalytics.jobrepo.jpa;

import com.thinkbiganalytics.jpa.AbstractJpaProvider;

import org.springframework.beans.factory.annotation.Qualifier;

import java.io.Serializable;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public abstract class BaseJpaProvider<T, PK extends Serializable> extends AbstractJpaProvider<T, PK> {

    @PersistenceContext
    @Inject
    @Qualifier("jobRepositoryEntityManager")
    protected EntityManager entityManager;

    public EntityManager getEntityManager() {
        return entityManager;
    }

}