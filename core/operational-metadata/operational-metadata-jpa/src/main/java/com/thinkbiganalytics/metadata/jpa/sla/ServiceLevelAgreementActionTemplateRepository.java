package com.thinkbiganalytics.metadata.jpa.sla;

/*-
 * #%L
 * thinkbig-operational-metadata-jpa
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.thinkbiganalytics.common.velocity.model.VelocityTemplate;
import com.thinkbiganalytics.metadata.jpa.feed.JpaOpsManagerFeed;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Spring data repository for accessing {@link JpaOpsManagerFeed}
 */
public interface ServiceLevelAgreementActionTemplateRepository extends JpaRepository<JpaServiceLevelAgreementActionTemplate, JpaServiceLevelAgreementActionTemplate.ServiceLevelAgreementActionTemplateId>, QueryDslPredicateExecutor<JpaServiceLevelAgreementActionTemplate> {

    @Query("select t from JpaServiceLevelAgreementActionTemplate as t "
           + " join t.velocityTemplate as v "
           + " join fetch t.serviceLevelAgreementDescription "
           + " where v.id = :velocityTemplateId")
    List<JpaServiceLevelAgreementActionTemplate> findByVelocityTemplate(@Param("velocityTemplateId")VelocityTemplate.ID velocityTemplateId);


    @Query("select t from JpaServiceLevelAgreementActionTemplate as t "
           + " join t.serviceLevelAgreementDescription s "
           + " where s.slaId = :slaId")
    List<JpaServiceLevelAgreementActionTemplate> findByServiceLevelAgreement(@Param("slaId")ServiceLevelAgreementDescriptionId slaId);




}
