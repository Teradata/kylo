package com.thinkbiganalytics.metadata.jpa.sla;

import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAssessment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Created by sr186054 on 9/14/16.
 */
public interface JpaServiceLevelAssessmentRepository extends JpaRepository<JpaServiceLevelAssessment, JpaServiceLevelAssessment.ID> {

    @Query(" select assessment from JpaServiceLevelAssessment assessment where assessment.slaId = :id "
           + "and assessment.createdTime = (select max(assessment2.createdTime) "
           + "                              from JpaServiceLevelAssessment as assessment2 "
           + "                              where assessment2.slaId = :id)")
    List<JpaServiceLevelAssessment> findLatestAssessments(@Param("id") String id);

    @Query(" select assessment from JpaServiceLevelAssessment assessment where assessment.slaId = :id "
           + "and assessment.createdTime = (select max(assessment2.createdTime) "
           + "                              from JpaServiceLevelAssessment as assessment2 "
           + "                              where assessment2.slaId = :id"
           + "                              and assessment2.id != :assessmentId)")
    List<JpaServiceLevelAssessment> findLatestAssessmentsNotEqualTo(@Param("id") String id, @Param("assessmentId") ServiceLevelAssessment.ID assessmentId);

}
