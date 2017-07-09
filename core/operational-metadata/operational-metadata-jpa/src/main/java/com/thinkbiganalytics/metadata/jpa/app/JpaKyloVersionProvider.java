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

import com.thinkbiganalytics.KyloVersionUtil;
import com.thinkbiganalytics.KyloVersion;
import com.thinkbiganalytics.metadata.api.app.KyloVersionProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

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
    
    private static final Sort SORT_ORDER = new Sort(new Sort.Order(Direction.DESC, "majorVersion"),
                                                    new Sort.Order(Direction.DESC, "minorVersion"),
                                                    new Sort.Order(Direction.DESC, "pointVersion"),
                                                    new Sort.Order(Direction.DESC, "tag") );


    private KyloVersionRepository kyloVersionRepository;

    private String currentVersion;

    private String buildTimestamp;


    @Autowired
    public JpaKyloVersionProvider(KyloVersionRepository kyloVersionRepository) {
        this.kyloVersionRepository = kyloVersionRepository;
    }

    @Override
    public boolean isUpToDate() {
        KyloVersion buildVer = KyloVersionUtil.getBuildVersion();
        KyloVersion currentVer = getCurrentVersion();
        return currentVer != null && buildVer.matches(currentVer.getMajorVersion(),
                                                        currentVer.getMinorVersion(),
                                                        currentVer.getPointVersion());
    }

    @Override
    public KyloVersion getCurrentVersion() {
        List<JpaKyloVersion> versions = kyloVersionRepository.findAll(SORT_ORDER);
        if (versions != null && !versions.isEmpty()) {
            return versions.get(0);
        }
        return null;
    }
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.app.KyloVersionProvider#setCurrentVersion(com.thinkbiganalytics.KyloVersion)
     */
    @Override
    public void setCurrentVersion(KyloVersion version) {
        JpaKyloVersion update = new JpaKyloVersion(version.getMajorVersion(), 
                                                   version.getMinorVersion(), 
                                                   version.getPointVersion(),
                                                   version.getTag());
        kyloVersionRepository.save(update);
    }
    
    @Override
    public KyloVersion getBuildVersion() {
        return KyloVersionUtil.getBuildVersion();
    }




    @PostConstruct
    private void init() {
        getBuildVersion();
    }



}
