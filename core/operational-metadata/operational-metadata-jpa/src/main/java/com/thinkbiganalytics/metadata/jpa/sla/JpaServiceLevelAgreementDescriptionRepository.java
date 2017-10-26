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

import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeed;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAssessment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 */
public interface JpaServiceLevelAgreementDescriptionRepository extends JpaRepository<JpaServiceLevelAgreementDescription, ServiceLevelAgreementDescriptionId> {

    @Query("select distinct sla from JpaServiceLevelAgreementDescription sla join sla.feeds f where f.id = :id ")
    List<JpaServiceLevelAgreementDescription> findForFeed(@Param("id") OpsManagerFeed.ID feedId);

    @Query("select distinct sla from JpaServiceLevelAgreementDescription sla left join fetch sla.feeds where sla.slaId = :id")
    JpaServiceLevelAgreementDescription findByIdFetchFeeds(@Param("id") ServiceLevelAgreementDescriptionId id);

}
