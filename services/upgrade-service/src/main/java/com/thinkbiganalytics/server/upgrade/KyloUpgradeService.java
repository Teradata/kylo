package com.thinkbiganalytics.server.upgrade;

/*-
 * #%L
 * kylo-operational-metadata-upgrade-service
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

import com.google.common.io.Resources;
import com.thinkbiganalytics.KyloVersion;
import com.thinkbiganalytics.KyloVersionUtil;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.app.KyloVersionProvider;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

public class KyloUpgradeService {

    private static final String UPGRADE_VERSIONS_FILE = "upgrade-versions.txt";

    private static final Logger log = LoggerFactory.getLogger(KyloUpgradeService.class);

    @Inject
    private MetadataAccess metadataAccess;
    @Inject
    private KyloVersionProvider versionProvider;
    @Inject
    private Optional<List<UpgradeState>> upgradeActions;
    
    private KyloVersion buildVersion;
    private boolean freshInstall = false;
    private List<KyloVersion> upgradeSequence;
    
    public static void main(String... args) {
        try {
            if (args.length < 2) {
                throw new IllegalArgumentException("Current source directory and build version required as arguments");
            }
            String resourcesDir = args[0];
            Resource versionsResource = new FileSystemResource(new File(resourcesDir, UPGRADE_VERSIONS_FILE));
            KyloVersion projectVersion = KyloVersionUtil.parseVersion(args[1]).withoutTag();

            List<KyloVersion> upgradeVersions = Stream.concat(readUpgradeVersions(versionsResource).stream(), Stream.of(projectVersion))
                            .sorted()
                            .distinct()
                            .collect(Collectors.toList());
            writeUpgradeVersions(upgradeVersions, versionsResource.getFile());
        } catch (Exception e) {
            throw new UpgradeException("Failed to update versions file: " + UPGRADE_VERSIONS_FILE, e);
        }
    }
    
    /**
     * Reads the list of versions from the specified resource.
     * @param versionsResource the versions resource
     * @return a list of KyloVersions
     * @throws IOException 
     */
    private static List<KyloVersion> readUpgradeVersions(Resource versionsResource) throws IOException {
        if (! versionsResource.exists()) {
            throw new UpgradeException("Kylo versions file does not exist: " + versionsResource.getURI());
        }
        
        return Resources.readLines(versionsResource.getURL(), Charset.forName("UTF-8")).stream()
            .filter(v -> StringUtils.isNotBlank(v))
            .map(KyloVersionUtil::parseVersion)
            .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Writers out the list of versions to the specified file.
     * @param versions the versions
     * @param versionsFile the file
     * @throws FileNotFoundException thrown if the destination file does not exist
     * @throws UnsupportedEncodingException 
     */
    private static void writeUpgradeVersions(List<KyloVersion> versions, File versionsFile) throws FileNotFoundException, UnsupportedEncodingException {
        try (PrintWriter writer = new PrintWriter(versionsFile, "UTF-8")) {
            versions.forEach(writer::println);
        }
    }

    /**
     * Upgrades to the next Kylo version relative to the current version.
     * @return true if after this call upgrades are complete and Kylo is up-to-date.
     */
    public boolean upgradeNext() {
        boolean isComplete = false;
        
        if (this.upgradeActions.isPresent()) {
            // Determine the next version from the current version.  If this is
            // a fresh install then the build version will be returned.  If
            // there are no versions later than the current one (we are up-to-date)
            // then nextVerion will be null.
            KyloVersion nextVersion = metadataAccess.read(() -> {
                return getNextVersion();
            }, MetadataAccess.SERVICE);
            
            if (nextVersion != null) {
                // Invoke any upgrade actions targeted for this next version in its own transaction.
                this.upgradeActions.get().stream()
                    .filter(action -> action.isTargetVersion(nextVersion))
                    .forEach(action -> {
                        metadataAccess.commit(() -> {
                            action.upgradeTo(nextVersion);
                        }, MetadataAccess.SERVICE);
                    });
                
                // Update this version it to be the current version.
                metadataAccess.commit(() -> {
                    versionProvider.setCurrentVersion(nextVersion);
                }, MetadataAccess.SERVICE);
                
                log.info("=================================");
                log.info("Finished upgrade through v{}", nextVersion);
                
                isComplete = nextVersion.equals(this.buildVersion);
//                
                // If upgrades are complete and this was starting from a fresh install then invoke any
                // fresh install actions.
                if (isComplete && this.freshInstall) {
                    metadataAccess.commit(() -> {
                        this.upgradeActions.get().stream()
                            .filter(a -> a.isTargetFreshInstall())
                            .forEach(a -> a.upgradeTo(this.buildVersion));
                        
                        log.info("=================================");
                        log.info("Finished fresh install updates");
                    }, MetadataAccess.SERVICE);
                }
                
                return isComplete;
            } else {
                log.info("Nothing left to upgrade - Kylo is up-to-date");
                return true;
            }
        } else {
            isComplete = metadataAccess.commit(() -> {
                versionProvider.setCurrentVersion(this.buildVersion);
                return true;
            }, MetadataAccess.SERVICE);
            
            log.info("=================================");
            log.info("Finished upgrade through v{}", this.buildVersion);
        }
        
        return isComplete;
    }
    
    @PostConstruct
    public void init() throws IOException {
        ClassPathResource versionsResource = new ClassPathResource(UPGRADE_VERSIONS_FILE, KyloUpgradeService.class.getClassLoader());
        KyloVersion current = versionProvider.getCurrentVersion();
        
        this.freshInstall = current == null;
        this.buildVersion = KyloVersionUtil.getBuildVersion();
        this.upgradeSequence = readUpgradeVersions(versionsResource);
    }

    private KyloVersion getNextVersion() {
        KyloVersion current = versionProvider.getCurrentVersion();
        // If there are no recorded versions then this is a fresh install so the next version is the actual build version.
        if (current == null) {
            return this.buildVersion;
        } else {
            int idx = IntStream.range(0, upgradeSequence.size())
                .filter(i -> 
                        upgradeSequence.get(i).matches(current.getMajorVersion(), current.getMinorVersion(), current.getPointVersion()) ||
                        upgradeSequence.get(i).compareTo(current) > 0)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("The current Kylo version is unrecognized: " + current));
            
            // If the current version is not the last one in the upgrade sequence then return it, otherwise return null.
            if (upgradeSequence.get(idx).compareTo(current) > 0) {
                return upgradeSequence.get(idx);
            } else if (idx < upgradeSequence.size() - 1) {
                return upgradeSequence.get(idx + 1);
            } else {
                return null;
            }
        }
    }
}
