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
import com.thinkbiganalytics.KyloVersion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Entity mapped to the Database table representing the current Kylo version deployed
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

    /**
     * create a new version with a supplied major and minor version
     */
    public JpaKyloVersion(String majorVersion, String minorVersion) {
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
    }

    /**
     * update this version to the new passed in version
     *
     * @return the newly updated version
     */
    public KyloVersion update(KyloVersion v) {
        setMajorVersion(v.getMajorVersion());
        setMinorVersion(v.getMinorVersion());
        return this;
    }


    /**
     * Return the major.minor version string
     *
     * @return the major.minor version string
     */
    @Override
    public String getVersion() {
        return majorVersion + "." + minorVersion;
    }

    /**
     * Return the major version of Kylo
     *
     * @return the major version
     */
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

    /**
     * @return the major version number
     */
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
        if (o == null || !(o instanceof KyloVersion)) {
            return false;
        }

        KyloVersion that = (KyloVersion) o;

        if (majorVersion != null ? !majorVersion.equals(that.getMajorVersion()) : that.getMajorVersion() != null) {
            return false;
        }
        return !(minorVersion != null ? !minorVersion.equals(that.getMinorVersion()) : that.getMinorVersion() != null);

    }

    @Override
    public int hashCode() {
        int result = majorVersion != null ? majorVersion.hashCode() : 0;
        result = 31 * result + (minorVersion != null ? minorVersion.hashCode() : 0);
        return result;
    }
}
