package com.thinkbiganalytics.metadata.jpa.feed;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

import java.io.Serializable;

import javax.persistence.EntityManager;

/**
 * Created by ru186002 on 05/04/2017.
 */
public class SecuredFeedRepositoryFactoryBean<R extends JpaRepository<T, I>, T,
    I extends Serializable> extends JpaRepositoryFactoryBean<R, T, I> {

    protected RepositoryFactorySupport createRepositoryFactory(EntityManager em) {
        return new SecuredFeedRepositoryFactory(em);
    }

}
