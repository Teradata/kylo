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
import com.thinkbiganalytics.alerts.api.Alert;
import com.thinkbiganalytics.alerts.api.AlertCriteria;
import com.thinkbiganalytics.alerts.api.EntityAlert;
import com.thinkbiganalytics.alerts.spi.AlertManager;
import com.thinkbiganalytics.alerts.spi.AlertSource;
import com.thinkbiganalytics.alerts.spi.EntityIdentificationAlertContent;
import com.thinkbiganalytics.alerts.spi.defaults.DefaultAlertCriteria;
import com.thinkbiganalytics.alerts.spi.defaults.DefaultAlertManager;
import com.thinkbiganalytics.alerts.spi.defaults.KyloEntityAwareAlertCriteria;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.event.MetadataChange;
import com.thinkbiganalytics.metadata.api.event.MetadataEventListener;
import com.thinkbiganalytics.metadata.api.event.MetadataEventService;
import com.thinkbiganalytics.metadata.api.event.feed.FeedChangeEvent;
import com.thinkbiganalytics.metadata.api.event.sla.ServiceLevelAgreementEvent;
import com.thinkbiganalytics.metadata.jpa.alerts.JpaAlert;
import com.thinkbiganalytics.metadata.jpa.alerts.JpaAlertChangeEvent;
import com.thinkbiganalytics.metadata.jpa.alerts.JpaAlertRepository;
import com.thinkbiganalytics.metadata.jpa.sla.QJpaServiceLevelAgreementDescription;
import com.thinkbiganalytics.metadata.jpa.support.CommonFilterTranslations;
import com.thinkbiganalytics.security.AccessController;
import com.thinkbiganalytics.security.role.SecurityRole;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 *
 */
public class KyloEntityAwareAlertManager extends DefaultAlertManager {

    private static final Logger log = LoggerFactory.getLogger(KyloEntityAwareAlertManager.class);

    @Inject
    private JPAQueryFactory queryFactory;

    @Inject
    private AccessController controller;


    private FeedDeletedListener feedDeletedListener = new FeedDeletedListener();

    private SlaDeletedListener slaDeletedListener = new SlaDeletedListener();

    @Inject
    private MetadataAccess metadataAccess;

    @Inject
    private MetadataEventService metadataEventService;

    private JpaAlertRepository repository;

    private AlertSource.ID identifier = new KyloEntityAwareAlertManagerAlertManagerId();

    public static final ImmutableMap<String, String> alertSlaFilters =
        new ImmutableMap.Builder<String, String>()
            .put("sla", "name")
            .put("slaId", "slaId.uuid")
            .put("slaDescription", "description").build();


    public KyloEntityAwareAlertManager(JpaAlertRepository repo) {
        super(repo);
        CommonFilterTranslations.addFilterTranslations(QJpaServiceLevelAgreementDescription.class, alertSlaFilters);
    }

    @PostConstruct
    private void setupListeners() {
        metadataEventService.addListener(feedDeletedListener);
        metadataEventService.addListener(slaDeletedListener);
    }

    @Override
    public ID getId() {
        return identifier;
    }

    public KyloEntityAwareAlertCriteria criteria() {
        return new KyloEntityAwareAlertCriteria(queryFactory, controller);
    }


    protected KyloEntityAwareAlertCriteria ensureAlertCriteriaType(AlertCriteria criteria) {
        if (criteria == null) {
            return criteria();
        } else if (criteria instanceof DefaultAlertCriteria && !(criteria instanceof KyloEntityAwareAlertCriteria)) {
            KyloEntityAwareAlertCriteria kyloEntityAwareAlertCriteria = criteria();
            ((DefaultAlertCriteria) criteria).transfer(kyloEntityAwareAlertCriteria);
            return kyloEntityAwareAlertCriteria;
        }
        return (KyloEntityAwareAlertCriteria) criteria;

    }

    @Override
    public Optional<Alert> getAlert(Alert.ID id) {
        return super.getAlert(id);
    }

    @Override
    protected Alert asValue(Alert alert) {
        return new ImmutableEntityAlert(alert, this);
    }


    public Iterator<Alert> entityDeleted(AlertCriteria criteria, String message) {

        log.info("Query for Entity Alerts data");
        List<Alert> handledAlerts = this.metadataAccess.commit(() -> {
            List<Alert> alerts = new ArrayList<>();
            criteria.state(Alert.State.UNHANDLED);
            KyloEntityAwareAlertCriteria critImpl = ensureAlertCriteriaType(criteria);
            critImpl.createEntityQuery().fetch().stream().forEach(jpaAlert -> {
                JpaAlertChangeEvent event = new JpaAlertChangeEvent(Alert.State.HANDLED, MetadataAccess.SERVICE, message, null);
                jpaAlert.addEvent(event);
                //hide it
                jpaAlert.setCleared(true);
                alerts.add(asValue(jpaAlert));
            });
            return alerts;
        }, MetadataAccess.SERVICE);
        return handledAlerts.iterator();
    }

    public static class KyloEntityAwareAlertManagerAlertManagerId implements ID {


        private static final long serialVersionUID = 7691516770322504702L;

        private String idValue = KyloEntityAwareAlertManager.class.getSimpleName().hashCode() + "";


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

    protected static class ImmutableEntityAlert extends ImmutableAlert implements EntityAlert {

        private String entityId;
        private String entityType;

        public ImmutableEntityAlert(Alert alert, AlertManager mgr) {
            super(alert, mgr);
            if (alert instanceof JpaAlert) {
                entityId = ((JpaAlert) alert).getEntityId() != null ? ((JpaAlert) alert).getEntityId().toString() : null;
                entityType = ((JpaAlert) alert).getEntityType();
            }

        }

        @Override
        public Serializable getEntityId() {
            return entityId;
        }

        @Override
        public String getEntityType() {
            return entityType;
        }
    }

    private class FeedDeletedListener implements MetadataEventListener<FeedChangeEvent> {

        @Override
        public void notify(FeedChangeEvent event) {
            if (event.getData().getChange() == MetadataChange.ChangeType.DELETE) {
                KyloEntityAwareAlertCriteria criteria = criteria().entityCriteria(new EntityIdentificationAlertContent(event.getData().getFeedId().toString(), SecurityRole.ENTITY_TYPE.FEED));
                entityDeleted(criteria, "The Feed " + event.getData().getFeedName() + " has been deleted");
                updateLastUpdatedTime();
            }
        }
    }

    private class SlaDeletedListener implements MetadataEventListener<ServiceLevelAgreementEvent> {

        @Override
        public void notify(ServiceLevelAgreementEvent event) {
            if (event.getData().getChange() == MetadataChange.ChangeType.DELETE) {
                KyloEntityAwareAlertCriteria criteria = criteria().entityCriteria(new EntityIdentificationAlertContent(event.getData().getId().toString(), SecurityRole.ENTITY_TYPE.SLA));
                entityDeleted(criteria, "The SLA " + event.getData().getName() + " has been deleted");
                updateLastUpdatedTime();
            }
        }
    }


}
