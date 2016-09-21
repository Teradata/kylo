package com.thinkbiganalytics.metadata.jpa.app;

import com.thinkbiganalytics.metadata.api.app.KyloVersion;
import com.thinkbiganalytics.metadata.api.app.KyloVersionProvider;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import javax.annotation.PostConstruct;

/**
 * Created by sr186054 on 9/19/16.
 */
@Service
public class JpaKyloVersionProvider implements KyloVersionProvider {

    private KyloVersionRepository kyloVersionRepository;

    private String currentVersion;

    private String buildTimestamp;


    @Autowired
    public JpaKyloVersionProvider(KyloVersionRepository kyloVersionRepository) {
        this.kyloVersionRepository = kyloVersionRepository;
    }

    @Override
    public KyloVersion getKyloVersion() {

        List<JpaKyloVersion> versions = kyloVersionRepository.findAll();
        if (versions != null && !versions.isEmpty()) {
            return versions.get(0);
        }
        return null;
    }

    @Override
    public KyloVersion updateToCurrentVersion() {
        if (getCurrentVersion() != null) {
            KyloVersion currentVersion = parseVersionString(getCurrentVersion());
            KyloVersion existingVersion = getKyloVersion();
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
        getCurrentVersion();
    }


    private String getCurrentVersion() {
        if (currentVersion == null) {
            Properties prop = new Properties();
            String versionFile = "version.txt";

            InputStream in = this.getClass().getClassLoader()
                .getResourceAsStream(versionFile);
            if (in != null) {
                try {
                    prop.load(in);
                    currentVersion = prop.getProperty("version");
                    buildTimestamp = prop.getProperty("build.date");
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
