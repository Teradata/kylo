package com.thinkbiganalytics.jobrepo.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

/**
 * Created by sr186054 on 8/31/16.
 */
public interface NifiJobParametersRepository extends JpaRepository<NifiJobExecutionParameters, NifiJobExecutionParameters.NifiJobExecutionParametersPK>,
                                                     QueryDslPredicateExecutor<NifiJobExecutionParameters.NifiJobExecutionParametersPK> {


}