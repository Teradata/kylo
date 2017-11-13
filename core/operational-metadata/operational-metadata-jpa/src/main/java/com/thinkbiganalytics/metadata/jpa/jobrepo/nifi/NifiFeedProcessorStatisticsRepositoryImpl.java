package com.thinkbiganalytics.metadata.jpa.jobrepo.nifi;
/*-
 * #%L
 * kylo-operational-metadata-jpa
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

import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.ParameterMode;
import javax.persistence.PersistenceContext;
import javax.persistence.StoredProcedureQuery;

/**
 * Call the stored proc.
 * NOTE: this is a Spring Data repository corresponding to the NifiFeedProcessorStatisticsRepository class.  The name of this class needs to match this explicitly.  {repoName}Impl
 * @see NifiFeedProcessorStatisticsRepository
 */
@Repository
public class NifiFeedProcessorStatisticsRepositoryImpl implements NifiFeedProcessorStatisticsRepositoryCustom {

    @PersistenceContext
    private EntityManager em;


    @Override
    public String compactFeedProcessorStats() {
        StoredProcedureQuery query = em.createStoredProcedureQuery("compact_feed_processor_stats");
        query.registerStoredProcedureParameter("res", String.class, ParameterMode.OUT);
        query.execute();
        String result = (String) query.getOutputParameterValue("res");
        return result;
    }
}
