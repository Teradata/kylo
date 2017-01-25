package com.thinkbiganalytics.metadata.jpa.app;

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

import com.thinkbiganalytics.jpa.AbstractAuditedEntity;
import com.thinkbiganalytics.metadata.api.app.KyloVersion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by sr186054 on 9/19/16.
 */
@Entity
@Table(name = "KYLO_VERSION")
public class JpaKyloVersion extends AbstractAuditedEntity implements KyloVersion, Serializable {

    private static final Logger log = LoggerFactory.getLogger(JpaKyloVersion.class);

    @Id
    @GeneratedValue
    private java.util.UUID id;


    @Column(name = "MAJOR_VERSION")
    private String majorVersion;


    @Column(name = "MINOR_VERSION")
    private String minorVersion;

    @Column
    private String description;


    public JpaKyloVersion() {

    }


    public JpaKyloVersion(String majorVersion, String minorVersion) {
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
    }

    public UUID getId() {
        return id;
    }

    public KyloVersion update(KyloVersion v) {
        setMajorVersion(v.getMajorVersion());
        setMinorVersion(v.getMinorVersion());
        return this;
    }


    @Override
    public String getVersion() {
        return majorVersion + "." + minorVersion;
    }

    public String getMajorVersion() {
        return this.majorVersion;
    }

    public void setMajorVersion(String majorVersion) {
        this.majorVersion = majorVersion;
    }

    public String getMinorVersion() {
        return this.minorVersion;
    }

    public void setMinorVersion(String minorVersion) {
        this.minorVersion = minorVersion;
    }

    @Override
    public Float getMajorVersionNumber() {
        if (getMajorVersion() != null) {
            try {
                return Float.parseFloat(getMajorVersion());
            } catch (NumberFormatException e) {
                log.error("error parsing Kylo Major Version of {} to a Float", getMajorVersion());
            }
        }
        return null;
    }


    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        JpaKyloVersion that = (JpaKyloVersion) o;

        if (majorVersion != null ? !majorVersion.equals(that.majorVersion) : that.majorVersion != null) {
            return false;
        }
        return !(minorVersion != null ? !minorVersion.equals(that.minorVersion) : that.minorVersion != null);

    }

    @Override
    public int hashCode() {
        int result = majorVersion != null ? majorVersion.hashCode() : 0;
        result = 31 * result + (minorVersion != null ? minorVersion.hashCode() : 0);
        return result;
    }
}
