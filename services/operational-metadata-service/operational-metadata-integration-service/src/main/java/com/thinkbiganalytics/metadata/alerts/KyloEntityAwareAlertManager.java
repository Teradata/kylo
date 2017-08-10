/**
 *
 */
package com.thinkbiganalytics.metadata.alerts;

/*-
 * #%L
 * thinkbig-alerts-default
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

import com.google.common.collect.ImmutableMap;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.thinkbiganalytics.alerts.api.AlertCriteria;
import com.thinkbiganalytics.alerts.spi.AlertSource;
import com.thinkbiganalytics.alerts.spi.defaults.DefaultAlertCriteria;
import com.thinkbiganalytics.alerts.spi.defaults.DefaultAlertManager;
import com.thinkbiganalytics.alerts.spi.defaults.KyloEntityAwareAlertCriteria;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.jpa.alerts.JpaAlertRepository;
import com.thinkbiganalytics.metadata.jpa.sla.QJpaServiceLevelAgreementDescription;
import com.thinkbiganalytics.metadata.jpa.support.CommonFilterTranslations;

import javax.inject.Inject;

/**
 *
 */
public class KyloEntityAwareAlertManager extends DefaultAlertManager {

    @Inject
    private JPAQueryFactory queryFactory;

    @Inject
    private MetadataAccess metadataAccess;

    private JpaAlertRepository repository;

    private AlertSource.ID identifier = new  KyloEntityAwareAlertManagerAlertManagerId();

    public static final ImmutableMap<String, String> alertSlaFilters =
        new ImmutableMap.Builder<String, String>()
            .put("sla", "name")
            .put("slaId", "slaId.uuid")
            .put("slaDescription", "description").build();


    public KyloEntityAwareAlertManager(JpaAlertRepository repo) {
        super(repo);
        CommonFilterTranslations.addFilterTranslations(QJpaServiceLevelAgreementDescription.class, alertSlaFilters);
    }

    @Override
    public ID getId() {
        return identifier;
    }

    public KyloEntityAwareAlertCriteria criteria() {
        return new KyloEntityAwareAlertCriteria(queryFactory);
    }


    protected DefaultAlertCriteria ensureAlertCriteriaType(AlertCriteria criteria){
        if(criteria == null){
            return criteria();
        }
        else if(criteria instanceof DefaultAlertCriteria && !(criteria instanceof KyloEntityAwareAlertCriteria)){
            KyloEntityAwareAlertCriteria kyloEntityAwareAlertCriteria = criteria();
            ((DefaultAlertCriteria)criteria).transfer(kyloEntityAwareAlertCriteria);
            return kyloEntityAwareAlertCriteria;
        }
        return (DefaultAlertCriteria)criteria;

    }

    public static class KyloEntityAwareAlertManagerAlertManagerId implements ID {


        private static final long serialVersionUID = 7691516770322504702L;

        private String idValue = KyloEntityAwareAlertManager.class.getSimpleName();


        public KyloEntityAwareAlertManagerAlertManagerId() {
        }


        public String getIdValue() {
            return idValue;
        }

        @Override
        public String toString() {
            return idValue;
        }

    }


}
