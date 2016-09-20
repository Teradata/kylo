package com.thinkbiganalytics.metadata.jpa.app;

import com.thinkbiganalytics.jpa.AbstractAuditedEntity;
import com.thinkbiganalytics.metadata.api.app.KyloVersion;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by sr186054 on 9/19/16.
 */
@Entity
@Table(name = "KYLO_VERSION")
public class JpaKyloVersion extends AbstractAuditedEntity implements KyloVersion, Serializable {

    @EmbeddedId
    private KyloVersionId kyloVersionId;


    @Column(name = "MAJOR_VERSION", insertable = false, updatable = false)
    private String majorVersion;


    @Column(name = "MINOR_VERSION", insertable = false, updatable = false)
    private String minorVersion;

    @Column
    private String description;


    public JpaKyloVersion() {
        this.kyloVersionId = new KyloVersionId();
    }


    public JpaKyloVersion(String majorVersion, String minorVersion) {
        this.kyloVersionId = new KyloVersionId(majorVersion, minorVersion);
    }


    public KyloVersion update(KyloVersion v) {
        setMajorVersion(v.getMajorVersion());
        setMinorVersion(v.getMinorVersion());
        return this;
    }


    @Override
    public String getVersion() {
        return this.kyloVersionId.getVersion();
    }


    public String getMajorVersion() {
        return this.kyloVersionId.getMajorVersion();
    }

    public void setMajorVersion(String majorVersion) {
        this.kyloVersionId.setMajorVersion(majorVersion);
    }

    public String getMinorVersion() {
        return this.kyloVersionId.getMinorVersion();
    }

    public void setMinorVersion(String minorVersion) {
        this.kyloVersionId.setMinorVersion(minorVersion);
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


    @Embeddable
    public static class KyloVersionId implements Serializable {

        @Column(name = "MAJOR_VERSION")
        private String majorVersion;

        @Column(name = "MINOR_VERSION")
        private String minorVersion;

        public String getVersion() {
            return majorVersion + "." + minorVersion;
        }

        public KyloVersionId() {

        }

        public KyloVersionId(String majorVersion, String minorVersion) {
            this.majorVersion = majorVersion;
            this.minorVersion = minorVersion;
        }

        public String getMajorVersion() {
            return majorVersion;
        }

        public void setMajorVersion(String majorVersion) {
            this.majorVersion = majorVersion;
        }

        public String getMinorVersion() {
            return minorVersion;
        }

        public void setMinorVersion(String minorVersion) {
            this.minorVersion = minorVersion;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            KyloVersionId that = (KyloVersionId) o;

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


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        JpaKyloVersion that = (JpaKyloVersion) o;

        return !(kyloVersionId != null ? !kyloVersionId.equals(that.kyloVersionId) : that.kyloVersionId != null);

    }

    @Override
    public int hashCode() {
        return kyloVersionId != null ? kyloVersionId.hashCode() : 0;
    }
}
