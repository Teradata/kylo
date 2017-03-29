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

import com.thinkbiganalytics.metadata.api.app.KyloVersion;
import com.thinkbiganalytics.metadata.api.app.KyloVersionProvider;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import javax.annotation.PostConstruct;

/**
 * Provider for accessing and updating the Kylo version
 */
public class JpaKyloVersionProvider implements KyloVersionProvider {

    private static final Logger log = LoggerFactory.getLogger(JpaKyloVersionProvider.class);


    private KyloVersionRepository kyloVersionRepository;

    private String currentVersion;

    private String buildTimestamp;


    @Autowired
    public JpaKyloVersionProvider(KyloVersionRepository kyloVersionRepository) {
        this.kyloVersionRepository = kyloVersionRepository;
    }

    @Override
    public boolean isUpToDate() {
        return getCurrentVersion().equals(getLatestVersion());
    }

    @Override
    public KyloVersion getCurrentVersion() {

        List<JpaKyloVersion> versions = kyloVersionRepository.findAll();
        if (versions != null && !versions.isEmpty()) {
            return versions.get(0);
        }
        return null;
    }

    @Override
    public KyloVersion updateToLatestVersion() {
        if (getLatestVersion() != null) {
            KyloVersion currentVersion = getLatestVersion();
            KyloVersion existingVersion = getCurrentVersion();
            if (existingVersion == null) {
                existingVersion = currentVersion;
                kyloVersionRepository.save((JpaKyloVersion) existingVersion);
            } else {
                if (!existingVersion.equals(currentVersion)) {
                    existingVersion.update(currentVersion);
                    kyloVersionRepository.save((JpaKyloVersion) existingVersion);
                }
            }
            return existingVersion;
        }
        return null;
    }
    
    @Override
    public KyloVersion getLatestVersion() {
        return parseVersionString(getCurrentVersionString());
    }

    private KyloVersion parseVersionString(String versionString) {

        if (versionString != null) {
            JpaKyloVersion kyloVersion = new JpaKyloVersion();
            //Major version ends before second period
            //i.e.  v 0.3.0    0.3
            int beforeIndex = StringUtils.ordinalIndexOf(versionString, ".", 2);
            String majorVersionString = StringUtils.substring(versionString, 0, beforeIndex);
            String minorVersion = StringUtils.substring(versionString, (beforeIndex + 1));
            kyloVersion.setMajorVersion(majorVersionString);
            kyloVersion.setMinorVersion(minorVersion);
            return kyloVersion;
        }
        return null;


    }


    @PostConstruct
    private void init() {
        getLatestVersion();
    }


    /**
     * Parse the version.txt file located in the classpath to obtain the current version installed
     */
    private String getCurrentVersionString() {
        if (currentVersion == null) {
            Properties prop = new Properties();
            String versionFile = "version.txt";

            InputStream in = this.getClass().getClassLoader()
                .getResourceAsStream(versionFile);
            URL url = this.getClass().getClassLoader().getResource(versionFile);
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
        }
        return currentVersion;

    }

    public String getBuildTimestamp() {
        return buildTimestamp;
    }
}
