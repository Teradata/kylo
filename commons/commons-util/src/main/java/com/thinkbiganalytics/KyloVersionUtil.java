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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

/**
 * Check the Version of Kylo looking at the version.txt file found in the /conf folder of the Kylo install
 */
public class KyloVersionUtil {

    private static final Logger log = LoggerFactory.getLogger(KyloVersionUtil.class);

    public static KyloVersion getLatestVersion() {
        return parseVersionString(getCurrentVersionString());
    }

    public static String versionString = null;

    public static String buildTimestamp = null;

    public static boolean isUpToDate(KyloVersion dbVersion) {
        KyloVersion deployedVersion = getLatestVersion();
        return dbVersion != null && deployedVersion != null && deployedVersion.equals(dbVersion);
    }

    private static KyloVersion parseVersionString(String versionString) {

        if (versionString != null) {

            //Major version ends before second period
            //i.e.  v 0.3.0    0.3
            int beforeIndex = StringUtils.ordinalIndexOf(versionString, ".", 2);
            String majorVersionString = StringUtils.substring(versionString, 0, beforeIndex);
            String minorVersion = StringUtils.substring(versionString, (beforeIndex + 1));
            Version kyloVersion = new Version(majorVersionString, minorVersion);
            return kyloVersion;
        }
        return null;


    }

    private static String getCurrentVersionString() {
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


    public String getBuildTimestamp() {
        if (StringUtils.isBlank(buildTimestamp)) {
            getCurrentVersionString();
        }
        return buildTimestamp;
    }


    public static class Version implements KyloVersion {

        private String majorVersion;
        private String minorVersion;
        private String description;

        public Version(KyloVersion version) {
            this(version.getMajorVersion(), version.getMinorVersion());
        }

        /**
         * create a new version with a supplied major and minor version
         */
        public Version(String majorVersion, String minorVersion) {
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

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return getMajorVersion() + "." + getMinorVersion();
        }
    }

}
