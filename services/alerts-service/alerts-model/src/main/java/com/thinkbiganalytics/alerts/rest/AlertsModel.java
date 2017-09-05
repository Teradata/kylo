package com.thinkbiganalytics.alerts.rest;

/*-
 * #%L
 * thinkbig-alerts-model
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

import com.thinkbiganalytics.alerts.AlertConstants;
import com.thinkbiganalytics.alerts.api.AlertSummary;
import com.thinkbiganalytics.alerts.api.EntityAlert;
import com.thinkbiganalytics.alerts.api.SourceAlert;
import com.thinkbiganalytics.alerts.rest.model.Alert;
import com.thinkbiganalytics.alerts.rest.model.AlertSummaryGrouped;
import com.thinkbiganalytics.json.ObjectMapperSerializer;
import com.thinkbiganalytics.metadata.api.alerts.EntityAwareAlertSummary;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by sr186054 on 7/22/17.
 */
@Component
public class AlertsModel {

    private static final Logger log = LoggerFactory.getLogger(AlertsModel.class);

    /**
     *
     * @param alertSummary
     * @return
     */
    public String alertTypeDisplayName(AlertSummary alertSummary) {
        String type = alertSummary.getType();
        return alertTypeDisplayName(type);
    }


    public String alertTypeDisplayName(String type) {
        String part = type;
        if (part.startsWith(AlertConstants.KYLO_ALERT_TYPE_PREFIX+"/alert")) {
            part = StringUtils.substringAfter(part, AlertConstants.KYLO_ALERT_TYPE_PREFIX+"/alert");
        }
         else if (part.startsWith(AlertConstants.KYLO_ALERT_TYPE_PREFIX)) {
            part = StringUtils.substringAfter(part, AlertConstants.KYLO_ALERT_TYPE_PREFIX);
        } else {
            int idx = StringUtils.lastOrdinalIndexOf(part, "/", 2);
            part = StringUtils.substring(part, idx);
        }
        String[] parts = part.split("/");
        return Arrays.asList(parts).stream().map(s -> StringUtils.capitalize(s)).collect(Collectors.joining(" "));
    }

    public Collection<AlertSummaryGrouped> groupAlertSummaries(List<AlertSummary> alertSummaries) {
        Map<String, AlertSummaryGrouped> group = new HashMap<>();
        alertSummaries.forEach(alertSummary -> {

            String key = alertSummary.getType() + ":" + alertSummary.getSubtype();

            String displayName = alertTypeDisplayName(alertSummary);
            if (alertSummary instanceof EntityAwareAlertSummary) {
                EntityAwareAlertSummary entityAwareAlertSummary = (EntityAwareAlertSummary) alertSummary;
                key = entityAwareAlertSummary.getGroupByKey();
                group.computeIfAbsent(key, key1 -> new AlertSummaryGrouped.Builder().typeString(alertSummary.getType())
                    .typeDisplayName(displayName)
                    .subType(entityAwareAlertSummary.getSubtype())
                    .feedId(entityAwareAlertSummary.getFeedId() != null ? entityAwareAlertSummary.getFeedId().toString() : null)
                    .feedName(entityAwareAlertSummary.getFeedName())
                    .slaId(entityAwareAlertSummary.getSlaId() != null ? entityAwareAlertSummary.getSlaId().toString() : null)
                    .slaName(entityAwareAlertSummary.getSlaName()).build()).add(toModel(alertSummary.getLevel()), alertSummary.getCount(), alertSummary.getLastAlertTimestamp());
            } else {
                group.computeIfAbsent(key, key1 -> new AlertSummaryGrouped.Builder().typeString(alertSummary.getType())
                    .typeDisplayName(displayName)
                    .subType(alertSummary.getSubtype())
                    .build()).add(toModel(alertSummary.getLevel()), alertSummary.getCount(), alertSummary.getLastAlertTimestamp());
            }
        });
        return group.values();
    }


    public com.thinkbiganalytics.alerts.rest.model.Alert toModel(com.thinkbiganalytics.alerts.api.Alert alert) {
        com.thinkbiganalytics.alerts.api.Alert baseAlert = alert;
        try {
            if (Proxy.isProxyClass(alert.getClass())) {
                SourceAlert sourceAlert = (SourceAlert) Proxy.getInvocationHandler(alert);
                if (sourceAlert != null) {
                    baseAlert = sourceAlert.getWrappedAlert();
                }
            }
        }catch (Exception e){
            //unable to get base alert from proxy.  log the exception but continue
            log.error("Unable to get base alert from wrapped proxy for : {}, {} ",alert,e.getMessage(),e);

        }
        com.thinkbiganalytics.alerts.rest.model.Alert result = new com.thinkbiganalytics.alerts.rest.model.Alert();
        result.setId(alert.getId().toString());
        result.setActionable(alert.isActionable());
        result.setCreatedTime(alert.getCreatedTime());
        result.setLevel(toModel(alert.getLevel()));
        result.setState(toModel(alert.getState()));
        result.setType(alert.getType());
        result.setDescription(alert.getDescription());
        result.setCleared(alert.isCleared());
        result.setContent(alert.getContent() != null ? alert.getContent().toString() : null);
        result.setSubtype(alert.getSubtype());
        alert.getEvents().forEach(e -> result.getEvents().add(toModel(e)));
        if(baseAlert instanceof EntityAlert){
            result.setEntityId(((EntityAlert)baseAlert).getEntityId() != null ? ((EntityAlert)baseAlert).getEntityId().toString(): null);
            result.setEntityType(((EntityAlert)baseAlert).getEntityType());
        }
        return result;
    }

    public com.thinkbiganalytics.alerts.rest.model.AlertChangeEvent toModel(com.thinkbiganalytics.alerts.api.AlertChangeEvent event) {
        com.thinkbiganalytics.alerts.rest.model.AlertChangeEvent result = new com.thinkbiganalytics.alerts.rest.model.AlertChangeEvent();
        result.setCreatedTime(event.getChangeTime());
        result.setDescription(event.getDescription());
        result.setState(toModel(event.getState()));
        result.setUser(event.getUser() != null ? event.getUser().getName() : null);
        try {
        result.setContent(event.getContent() != null ? ObjectMapperSerializer.serialize(event.getContent()) : null);
        }catch (Exception e){

        }
        return result;
    }

    public Alert.Level toModel(com.thinkbiganalytics.alerts.api.Alert.Level level) {
        // Currently identical
        return Alert.Level.valueOf(level.name());
    }

    public Alert.State toModel(com.thinkbiganalytics.alerts.api.Alert.State state) {
        // Currently identical
        return Alert.State.valueOf(state.name());
    }

    public com.thinkbiganalytics.alerts.api.Alert.State toDomain(Alert.State state) {
        return com.thinkbiganalytics.alerts.api.Alert.State.valueOf(state.name());
    }

    public com.thinkbiganalytics.alerts.api.Alert.Level toDomain(Alert.Level level) {
        // Currently identical
        return com.thinkbiganalytics.alerts.api.Alert.Level.valueOf(level.name());
    }

}
