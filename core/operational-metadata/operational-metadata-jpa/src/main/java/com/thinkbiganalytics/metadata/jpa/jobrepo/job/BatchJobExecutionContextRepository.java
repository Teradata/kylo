package com.thinkbiganalytics.metadata.jpa.jobrepo.job;


import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by sr186054 on 8/23/16.
 */
public interface BatchJobExecutionContextRepository
    extends JpaRepository<JpaBatchJobExecutionContext, Long> {


}
