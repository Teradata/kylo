package com.thinkbiganalytics.metadata.jpa.feed;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

/**
 * Created by ru186002 on 05/04/2017.
 */
@NoRepositoryBean
interface SecuredFeedRepository<T, ID extends Serializable>
    extends JpaRepository<T, ID>, QueryDslPredicateExecutor<T> {

}
