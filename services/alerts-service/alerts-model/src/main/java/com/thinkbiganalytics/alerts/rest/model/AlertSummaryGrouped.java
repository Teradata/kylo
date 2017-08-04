package com.thinkbiganalytics.alerts.rest.model;

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

import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sr186054 on 7/22/17.
 */
public class AlertSummaryGrouped {

    private URI type;

    private String subtype;

    private String feedId;
    private String feedName;
    private String slaId;
    private String slaName;

    private Long count = 0L;

    private Map<Alert.Level,Long> levelCounts = new HashMap<>();

    private Long lastAlertTimestamp;

    private String typeDisplayName;

    private String groupDisplayName;



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


    public static class Builder {
        private URI type;

        private String subtype;

        private String feedId;
        private String feedName;
        private String slaId;
        private String slaName;
        private String typeDisplayName;

        public Builder type(URI type){
            this.type = type;
            return this;
        }
        public Builder typeString(String typeString){
            this.type = URI.create(typeString);
            return this;
        }

        public Builder subType(String subtype){
            this.subtype = subtype;
            return this;
        }

        public Builder feedId(String feedId){
            this.feedId = feedId;
            return this;
        }

        public Builder feedName(String feedName){
            this.feedName = feedName;
            return this;
        }

        public Builder slaId(String slaId){
            this.slaId = slaId;
            return this;
        }

        public Builder slaName(String slaName){
            this.slaName = slaName;
            return this;
        }

        public Builder typeDisplayName(String typeDisplayName){
            this.typeDisplayName = typeDisplayName;
            return this;
        }
        public AlertSummaryGrouped build(){
            AlertSummaryGrouped alertSummary = new AlertSummaryGrouped(type,subtype,typeDisplayName);
            alertSummary.setFeedName(this.feedName);
            alertSummary.setFeedId(this.feedId);
            alertSummary.setSlaId(this.slaId);
            alertSummary.setSlaName(this.slaName);
            if(StringUtils.isNotBlank(this.feedName)){
                alertSummary.setGroupDisplayName(this.feedName);
            }
            else if(StringUtils.isNotBlank(this.slaName)){
                alertSummary.setGroupDisplayName(this.slaName);
            }
            else if(StringUtils.isNotBlank(subtype)){
                alertSummary.setGroupDisplayName(subtype);
            }
            else {
                alertSummary.setGroupDisplayName("Other");
            }
            return alertSummary;

        }
    }


    public String getGroupDisplayName() {
        return groupDisplayName;
    }

    public void setGroupDisplayName(String groupDisplayName) {
        this.groupDisplayName = groupDisplayName;
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


    public String getFeedId() {
        return feedId;
    }

    public void setFeedId(String feedId) {
        this.feedId = feedId;
    }

    public String getFeedName() {
        return feedName;
    }

    public void setFeedName(String feedName) {
        this.feedName = feedName;
    }

    public String getSlaId() {
        return slaId;
    }

    public void setSlaId(String slaId) {
        this.slaId = slaId;
    }

    public String getSlaName() {
        return slaName;
    }

    public void setSlaName(String slaName) {
        this.slaName = slaName;
    }
}
