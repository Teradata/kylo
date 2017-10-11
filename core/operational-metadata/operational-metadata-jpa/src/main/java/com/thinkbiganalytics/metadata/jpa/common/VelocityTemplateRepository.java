package com.thinkbiganalytics.metadata.jpa.common;

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
import java.util.Set;

/**
 * Spring data repository for accessing {@link JpaOpsManagerFeed}
 */
public interface VelocityTemplateRepository extends JpaRepository<JpaVelocityTemplate, JpaVelocityTemplate.JpaVelocityTemplateId>, QueryDslPredicateExecutor<JpaVelocityTemplate> {

    @Query("select t from JpaVelocityTemplate as t where t.type = :type")
    List<JpaVelocityTemplate> findByType(@Param("type") String type);

    @Query("select t from JpaVelocityTemplate as t where t.systemName = :systemName")
    JpaVelocityTemplate findBySystemName(@Param("systemName") String systemName);


    @Query("select t from JpaVelocityTemplate as t where t.id  in(:ids)")
    List<JpaVelocityTemplate> findForIds(@Param("ids")Set<VelocityTemplate.ID> ids);

    @Query("select t from JpaVelocityTemplate as t where t.type = :type and t.isDefault = true")
    JpaVelocityTemplate findDefault(@Param("type")String type);

}
