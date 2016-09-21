package com.thinkbiganalytics.metadata.jpa.jobrepo.job;


import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by sr186054 on 8/31/16.
 */
public interface BatchJobParametersRepository extends JpaRepository<JpaBatchJobExecutionParameter, JpaBatchJobExecutionParameter.BatchJobExecutionParametersPK> {


}