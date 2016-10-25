package com.thinkbiganalytics.metadata.jpa.jobrepo.nifi;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Created by sr186054 on 8/23/16.
 */
public interface NifiEventStepExecutionRepository extends JpaRepository<JpaNifiEventStepExecution, JpaNifiEventStepExecution.NifiEventStepExecutionPK> {

    @Query(value = "select nifiEventStep from JpaNifiEventStepExecution as nifiEventStep "
                   + "where nifiEventStep.stepExecution.stepExecutionId = :stepExecutionId ")
    JpaNifiEventStepExecution findByStepExecution(@Param("stepExecutionId") Long stepExecutionId);


}
