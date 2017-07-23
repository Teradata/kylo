package com.thinkbiganalytics.alerts.rest.model;

import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by sr186054 on 7/22/17.
 */
public class AlertSummaryGrouped {

    private URI type;

    private String subtype;

    private Long count = 0L;

    private Map<Alert.Level,Long> levelCounts = new HashMap<>();

    private Long lastAlertTimestamp;

    private String typeDisplayName;

    public AlertSummaryGrouped(){

    }
    public AlertSummaryGrouped(String type, String subtype, String typeDisplayName){
        this(URI.create(type),subtype,typeDisplayName);

    }
    public AlertSummaryGrouped(URI type, String subtype, String typeDisplayName){
        this.type = type;
        this.subtype = subtype;
        this.typeDisplayName = typeDisplayName;
    }

    public URI getType() {
        return type;
    }

    public void setType(URI type) {
        this.type = type;
    }

    public String getSubtype() {
        return subtype;
    }

    public void setSubtype(String subtype) {
        this.subtype = subtype;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public Map<Alert.Level, Long> getLevelCounts() {
        return levelCounts;
    }

    public void setLevelCounts(Map<Alert.Level, Long> levelCounts) {
        this.levelCounts = levelCounts;
    }

    public Long getLastAlertTimestamp() {
        return lastAlertTimestamp;
    }

    public void add(Alert.Level level, Long count, Long lastAlertTimestamp) {
        levelCounts.computeIfAbsent(level, c -> 0L);
        Long value = levelCounts.get(level)+count;
        levelCounts.put(level,value);
        this.count+=count;

        if(lastAlertTimestamp != null){
            if(this.lastAlertTimestamp == null){
                this.lastAlertTimestamp = lastAlertTimestamp;
            }
            else if(lastAlertTimestamp > this.lastAlertTimestamp){
                this.lastAlertTimestamp = lastAlertTimestamp;
            }
        }
    }

    public String getTypeDisplayName() {
        return typeDisplayName;
    }

    public void setTypeDisplayName(String typeDisplayName) {
        this.typeDisplayName = typeDisplayName;
    }
}
