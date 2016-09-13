package com.thinkbiganalytics.jobrepo.jpa;

import com.thinkbiganalytics.jobrepo.jpa.model.NifiEventJobExecution;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

/**
 * Created by sr186054 on 8/23/16.
 */
public interface NifiEventJobExecutionRepository extends JpaRepository<NifiEventJobExecution, NifiEventJobExecution.NifiEventJobExecutionPK> {

    @Query(value = "select nifiEventJob from NifiEventJobExecution as nifiEventJob "
                   + "where nifiEventJob.flowFileId in(:flowFileIds) ")
    List<NifiEventJobExecution> findByFlowFileIds(@Param("flowFileId") Set<String> flowFileIds);


}
