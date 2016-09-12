package com.thinkbiganalytics.jobrepo.jpa;

import com.thinkbiganalytics.jobrepo.jpa.model.BatchStepExecutionContext;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by sr186054 on 8/23/16.
 */
public interface BatchStepExecutionContextRepository
    extends JpaRepository<BatchStepExecutionContext, Long> {


}
