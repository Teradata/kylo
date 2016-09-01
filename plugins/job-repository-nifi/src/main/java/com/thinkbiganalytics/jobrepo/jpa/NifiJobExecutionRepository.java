package com.thinkbiganalytics.jobrepo.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

/**
 * Created by sr186054 on 8/23/16.
 */
public interface NifiJobExecutionRepository extends JpaRepository<NifiJobExecution, Long>, QueryDslPredicateExecutor<Long> {


}
