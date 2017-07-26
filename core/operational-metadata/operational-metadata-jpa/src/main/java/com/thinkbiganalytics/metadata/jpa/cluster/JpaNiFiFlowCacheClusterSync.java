package com.thinkbiganalytics.metadata.jpa.cluster;

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

import com.thinkbiganalytics.DateTimeUtil;
import com.thinkbiganalytics.metadata.api.cluster.NiFiFlowCacheClusterSync;
import com.thinkbiganalytics.metadata.api.cluster.NiFiFlowCacheClusterUpdateItem;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "NIFI_FLOW_CACHE_CLUSTER_SYNC")
public class JpaNiFiFlowCacheClusterSync implements NiFiFlowCacheClusterSync {

    @EmbeddedId
    private NiFiFlowCacheKey niFiFlowCacheKey;

    @Column(name = "KYLO_CLUSTER_ADDRESS",insertable = false,updatable = false)
    private String clusterAddress;

    @Column(name = "UPDATE_KEY",insertable = false,updatable = false)
    private String updateKey;


    @ManyToOne(targetEntity = JpaNiFiFlowCacheClusterUpdateItem.class, fetch=FetchType.LAZY)
    @JoinColumn(name = "UPDATE_KEY", nullable = false,insertable = false,updatable = false)
    private NiFiFlowCacheClusterUpdateItem updateItem;

    @Column(name = "UPDATE_APPLIED", length = 1)
    @org.hibernate.annotations.Type(type = "yes_no")
    private boolean updateApplied;

    @Column(name = "INITIATING_CLUSTER", length = 1)
    @org.hibernate.annotations.Type(type = "yes_no")
    private boolean initiatingCluster;

    @Type(type = "com.thinkbiganalytics.jpa.PersistentDateTimeAsMillisLong")
    @Column(name = "CREATED_TIME")
    private DateTime createdTime;

    @Type(type = "com.thinkbiganalytics.jpa.PersistentDateTimeAsMillisLong")
    @Column(name = "MODIFIED_TIME")
    private DateTime modifiedTime;

    public JpaNiFiFlowCacheClusterSync(){

    }

    public JpaNiFiFlowCacheClusterSync(String clusterAddress, String updateKey, boolean updateApplied){
        this.setNiFiFlowCacheKey(new JpaNiFiFlowCacheClusterSync.NiFiFlowCacheKey(clusterAddress, updateKey));
        this.updateApplied = updateApplied;
        this.createdTime = DateTimeUtil.getNowUTCTime();
        this.modifiedTime = this.createdTime;
    }

    public NiFiFlowCacheKey getNiFiFlowCacheKey() {
        return niFiFlowCacheKey;
    }

    public void setNiFiFlowCacheKey(NiFiFlowCacheKey niFiFlowCacheKey) {
        this.niFiFlowCacheKey = niFiFlowCacheKey;
    }

    @Override
    public NiFiFlowCacheClusterUpdateItem getUpdateItem() {
        return updateItem;
    }

    public void setUpdateItem(NiFiFlowCacheClusterUpdateItem updateItem) {
        this.updateItem = updateItem;
    }

    public boolean isInitiatingCluster() {
        return initiatingCluster;
    }

    public void setInitiatingCluster(boolean initiatingCluster) {
        this.initiatingCluster = initiatingCluster;
    }

    @Override
    public boolean isUpdateApplied() {
        return updateApplied;
    }

    public void setUpdateApplied(boolean updateApplied) {
        this.updateApplied = updateApplied;
    }


    public String getClusterAddress() {
        return getNiFiFlowCacheKey().getClusterAddress();
    }

    public String getUpdateKey() {
        return getNiFiFlowCacheKey().getUpdateKey();
    }


    public DateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(DateTime createdTime) {
        this.createdTime = createdTime;
    }

    public DateTime getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(DateTime modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    @Embeddable
    public static class NiFiFlowCacheKey implements Serializable {

        @Column(name = "KYLO_CLUSTER_ADDRESS")
        private String clusterAddress;

        @Column(name = "UPDATE_KEY")
        private String updateKey;

        public NiFiFlowCacheKey(String clusterAddress, String updateKey) {
            this.clusterAddress = clusterAddress;
            this.updateKey = updateKey;
        }

        public NiFiFlowCacheKey() {}

        public String getClusterAddress() {
            return clusterAddress;
        }

        public String getUpdateKey() {
            return updateKey;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            NiFiFlowCacheKey that = (NiFiFlowCacheKey) o;

            if (clusterAddress != null ? !clusterAddress.equals(that.clusterAddress) : that.clusterAddress != null) {
                return false;
            }
            return updateKey != null ? updateKey.equals(that.updateKey) : that.updateKey == null;
        }

        @Override
        public int hashCode() {
            int result = clusterAddress != null ? clusterAddress.hashCode() : 0;
            result = 31 * result + (updateKey != null ? updateKey.hashCode() : 0);
            return result;
        }
    }



}
