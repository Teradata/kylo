package com.thinkbiganalytics.metadata.jpa.jobrepo.step;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by sr186054 on 8/23/16.
 */
public interface BatchStepExecutionContextRepository
    extends JpaRepository<JpaBatchStepExecutionContext, Long> {


}
