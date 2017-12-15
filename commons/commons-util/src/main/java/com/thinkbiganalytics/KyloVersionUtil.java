package com.thinkbiganalytics;

/*-
 * #%L
 * kylo-commons-util
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Objects;
import java.util.Properties;

/**
 * Check the Version of Kylo looking at the version.txt file found in the /conf folder of the Kylo install
 */
public class KyloVersionUtil {

    private static final Logger log = LoggerFactory.getLogger(KyloVersionUtil.class);

    public static KyloVersion getBuildVersion() {
        return parseVersion(getBuildVersionString());
    }

    public static String versionString = null;

    public static String buildTimestamp = null;

    public static boolean isUpToDate(KyloVersion dbVersion) {
        KyloVersion deployedVersion = getBuildVersion();
        return dbVersion != null && deployedVersion != null && deployedVersion.equals(dbVersion);
    }

    public static KyloVersion parseVersion(String versionString) {
        if (versionString != null) {
            String[] dotSplit = versionString.split("\\.");
            String[] dashSplit = dotSplit[dotSplit.length - 1].split("-");
            dotSplit[dotSplit.length - 1] = dashSplit[0];
            
            String tag = dashSplit.length > 1 ? dashSplit[1] : "";
            String major = "";
            String minor = "";
            String point = "";
            int majorIdx = 0;
            
            if (dotSplit[0].startsWith("0")) {
                major = dotSplit[0] + "." + dotSplit[1]; 
                majorIdx = 1;
            } else {
                major = dotSplit[0];
            }
            
            if (majorIdx + 1 < dotSplit.length) {
                minor = dotSplit[majorIdx + 1];
            }
            
            if (majorIdx + 2 < dotSplit.length) {
                point = dotSplit[majorIdx + 2];
            }
            
            return new Version(major, minor, point, tag);
        } else {
            return null;
        }
    }

    private static String getBuildVersionString() {
        if (StringUtils.isBlank(versionString)) {
            String currentVersion = null;
            Properties prop = new Properties();
            String versionFile = "version.txt";

            InputStream in = KyloVersionUtil.class.getClassLoader()
                .getResourceAsStream(versionFile);
            URL url = KyloVersionUtil.class.getClassLoader().getResource(versionFile);
            if (in != null) {
                try {
                    try {
                        log.info("finding version information from {} ", url.toURI().getPath().toString());
                    } catch (Exception e) {

                    }
                    prop.load(in);
                    currentVersion = prop.getProperty("version");
                    buildTimestamp = prop.getProperty("build.date");

                    log.info("loaded Kylo version file: {}  build Time: {}", currentVersion, buildTimestamp);
                } catch (IOException e) {

                }
            }
            versionString = currentVersion;
        }
        return versionString;

    }


    public static String getBuildTimestamp() {
        if (StringUtils.isBlank(buildTimestamp)) {
            getBuildVersionString();
        }
        return buildTimestamp;
    }


    public static class Version implements KyloVersion {

        private String majorVersion;
        private String minorVersion;
        private String pointVersion;
        private String tag;
        private String description;

        public Version(KyloVersion version) {
            this(version.getMajorVersion(), version.getMinorVersion(), version.getPointVersion(), version.getTag());
        }

        /**
         * create a new version with a supplied major and minor version
         */
        public Version(String major, String minor, String point, String tag) {
            this.majorVersion = major;
            this.minorVersion = minor;
            this.pointVersion = point;
            this.tag = tag;
            
            // Fix the case where the minor version contains a tag due to an old schema version.
            if (this.minorVersion.contains("-")) {
                String[] split = this.minorVersion.split("-");
                this.minorVersion = split[0];
                this.tag = split[1];
            }
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
            return this.majorVersion == null ? "" : this.majorVersion;
        }

        public void setMajorVersion(String majorVersion) {
            this.majorVersion = majorVersion;
        }

        public String getMinorVersion() {
            return this.minorVersion == null ? "" : this.minorVersion;
        }

        public void setMinorVersion(String minorVersion) {
            this.minorVersion = minorVersion;
        }
        
        public String getPointVersion() {
            return pointVersion == null ? "" : this.pointVersion;
        }

        public void setPointVersion(String pointVersion) {
            this.pointVersion = pointVersion;
        }

        public String getTag() {
            return tag == null ? "" : this.tag;
        }

        public void setTag(String tag) {
            this.tag = tag;
        }
        
        @Override
        public KyloVersion withoutTag() {
            return new Version(this.getMajorVersion(), this.getMinorVersion(), this.getPointVersion(), null);
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
                    throw new IllegalStateException("Cannot parse major version number", e);
                }
            } else {
                return null;
            }
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
            
            return Objects.equals(this.getMajorVersion(), that.getMajorVersion()) && 
                            Objects.equals(this.getMinorVersion(), that.getMinorVersion()) && 
                            Objects.equals(this.getPointVersion(), that.getPointVersion()) && 
                            Objects.equals(this.getTag(), that.getTag());
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.majorVersion, this.minorVersion, this.pointVersion, this.tag);
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return getMajorVersion() + "." + getMinorVersion() 
                + (Strings.isNullOrEmpty(getPointVersion()) ? "" : "." + getPointVersion())
                + (Strings.isNullOrEmpty(getTag()) ? "" : "-" + getTag());
        }

        /* (non-Javadoc)
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        @Override
        public int compareTo(KyloVersion o) {
            int result = 0;
            if ((result = getMajorVersion().compareTo(o.getMajorVersion())) != 0) return result;
            if ((result = getMinorVersion().compareTo(o.getMinorVersion())) != 0) return result;
            if ((result = getPointVersion().compareTo(o.getPointVersion())) != 0) return result;
            return getTag().compareTo(o.getTag());
        }

        /* (non-Javadoc)
         * @see com.thinkbiganalytics.KyloVersion#matches(java.lang.String, java.lang.String, java.lang.String)
         */
        @Override
        public boolean matches(String major, String minor, String point) {
            return Objects.equals(getMajorVersion(), major) && Objects.equals(getMinorVersion(), minor) && Objects.equals(getPointVersion(), point);
        }

        /* (non-Javadoc)
         * @see com.thinkbiganalytics.KyloVersion#matches(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
         */
        @Override
        public boolean matches(String major, String minor, String point, String tag) {
            return matches(major, minor, point) && Objects.equals(getTag(), tag);
        }
    }

}
