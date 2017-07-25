package com.thinkbiganalytics.alerts.rest;

import com.thinkbiganalytics.alerts.AlertConstants;
import com.thinkbiganalytics.alerts.api.AlertSummary;
import com.thinkbiganalytics.alerts.rest.model.Alert;
import com.thinkbiganalytics.alerts.rest.model.AlertChangeEvent;
import com.thinkbiganalytics.alerts.rest.model.AlertSummaryGrouped;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

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


    public  String alertSummaryDisplayName(AlertSummary alertSummary){
        String type = alertSummary.getType();
        String part = type;
        if(part.startsWith(AlertConstants.KYLO_ALERT_TYPE_PREFIX)) {
            part = StringUtils.substringAfter(part, AlertConstants.KYLO_ALERT_TYPE_PREFIX);
        }
        else {
            int idx = StringUtils.lastOrdinalIndexOf(part, "/", 2);
            part = StringUtils.substring(part, idx);
        }
        String[] parts = part.split("/");
        StringBuffer displayNameSb = new StringBuffer();
        return Arrays.asList(parts).stream().map(s -> StringUtils.capitalize(s)).collect(Collectors.joining(" "));
    }

    public Collection<AlertSummaryGrouped> groupAlertSummaries(List<AlertSummary> alertSummaries) {
        Map<String,AlertSummaryGrouped> group = new HashMap<>();
        alertSummaries.forEach(alertSummary -> {
            String key = alertSummary.getType()+":"+alertSummary.getSubtype();
            String displayName = alertSummaryDisplayName(alertSummary);
            group.computeIfAbsent(key,key1->new AlertSummaryGrouped(alertSummary.getType(),alertSummary.getSubtype(),displayName)).add(toModel(alertSummary.getLevel()),alertSummary.getCount(),alertSummary.getLastAlertTimestamp());
        });
        return group.values();
    }


    public com.thinkbiganalytics.alerts.rest.model.Alert toModel(com.thinkbiganalytics.alerts.api.Alert alert) {
        com.thinkbiganalytics.alerts.rest.model.Alert result = new com.thinkbiganalytics.alerts.rest.model.Alert();
        result.setId(alert.getId().toString());
        result.setActionable(alert.isActionable());
        result.setCreatedTime(alert.getCreatedTime());
        result.setLevel(toModel(alert.getLevel()));
        result.setState(toModel(alert.getState()));
        result.setType(alert.getType());
        result.setDescription(alert.getDescription());
        result.setCleared(alert.isCleared());
        result.setContent(alert.getContent()!= null ? alert.getContent().toString():null);
        result.setSubtype(alert.getSubtype());
        alert.getEvents().forEach(e -> result.getEvents().add(toModel(e)));
        return result;
    }

    public com.thinkbiganalytics.alerts.rest.model.AlertChangeEvent toModel(com.thinkbiganalytics.alerts.api.AlertChangeEvent event) {
        com.thinkbiganalytics.alerts.rest.model.AlertChangeEvent result = new com.thinkbiganalytics.alerts.rest.model.AlertChangeEvent();
        result.setCreatedTime(event.getChangeTime());
        result.setDescription(event.getDescription());
        result.setState(toModel(event.getState()));
        result.setUser(event.getUser() != null ? event.getUser().getName() : null);
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
