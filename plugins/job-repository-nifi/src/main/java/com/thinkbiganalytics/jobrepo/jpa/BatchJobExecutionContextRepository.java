package com.thinkbiganalytics.jobrepo.jpa;

import com.thinkbiganalytics.jobrepo.jpa.model.BatchJobExecutionContext;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by sr186054 on 8/23/16.
 */
public interface BatchJobExecutionContextRepository
    extends JpaRepository<BatchJobExecutionContext, Long> {


}
