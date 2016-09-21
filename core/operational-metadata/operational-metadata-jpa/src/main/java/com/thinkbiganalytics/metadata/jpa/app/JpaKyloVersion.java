package com.thinkbiganalytics.metadata.jpa.app;

import com.thinkbiganalytics.jpa.AbstractAuditedEntity;
import com.thinkbiganalytics.metadata.api.app.KyloVersion;

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
            return Float.parseFloat(getMajorVersion());
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
