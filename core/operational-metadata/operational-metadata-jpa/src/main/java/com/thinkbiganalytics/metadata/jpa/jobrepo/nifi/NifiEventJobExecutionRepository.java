package com.thinkbiganalytics.metadata.jpa.jobrepo.nifi;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

/**
 * Created by sr186054 on 8/23/16.
 */
public interface NifiEventJobExecutionRepository extends JpaRepository<JpaNifiEventJobExecution, JpaNifiEventJobExecution.NifiEventJobExecutionPK> {

    @Query(value = "select nifiEventJob from JpaNifiEventJobExecution as nifiEventJob "
                   + "where nifiEventJob.flowFileId in(:flowFileIds) ")
    List<JpaNifiEventJobExecution> findByFlowFileIds(@Param("flowFileId") Set<String> flowFileIds);


}
