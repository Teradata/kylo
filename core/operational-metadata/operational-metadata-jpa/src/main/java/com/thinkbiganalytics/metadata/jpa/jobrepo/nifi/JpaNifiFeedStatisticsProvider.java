package com.thinkbiganalytics.metadata.jpa.jobrepo.nifi;

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

import com.thinkbiganalytics.metadata.api.jobrepo.nifi.NifiFeedStatisticsProvider;
import com.thinkbiganalytics.metadata.api.jobrepo.nifi.NifiFeedStats;
import com.thinkbiganalytics.metadata.jpa.jobrepo.job.JpaBatchJobExecutionProvider;
import com.thinkbiganalytics.security.AccessController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;

import javax.inject.Inject;
import javax.validation.ConstraintViolationException;

/**
 * Created by sr186054 on 7/25/17.
 */
@Service
public class JpaNifiFeedStatisticsProvider implements NifiFeedStatisticsProvider {
    private static final Logger log = LoggerFactory.getLogger(JpaNifiFeedStatisticsProvider.class);

    private NifiFeedStatisticsRepository feedStatisticsRepository;

    @Inject
    private AccessController accessController;

    @Autowired
    public JpaNifiFeedStatisticsProvider(NifiFeedStatisticsRepository feedStatisticsRepository) {
        this.feedStatisticsRepository = feedStatisticsRepository;
    }

    /**
     * Marks these are the latest feed stats
     *
     * @param feedStatsList the stats to save
     */
    @Override
    public void saveLatestFeedStats(List<NifiFeedStats> feedStatsList) {
        if (feedStatsList != null) {
            feedStatsList.stream().forEach(nifiFeedStats -> {
                try {
                    feedStatisticsRepository.save(((JpaNifiFeedStats) nifiFeedStats));
                }catch (DataIntegrityViolationException e) {
                    log.warn("DataIntegrityViolationException violation when attempting to save {} ",nifiFeedStats);
                }
            });
        }
    }

    public void deleteFeedStats(String feedName){
        List<JpaNifiFeedStats> stats = feedStatisticsRepository.findForFeedWithoutAcl(feedName);
        if(stats != null && !stats.isEmpty()){
            feedStatisticsRepository.delete(stats);
        }
    }

    @Override
    public NifiFeedStats findLatestStatsForFeed(String feedName) {
        return accessController.isEntityAccessControlled() ? feedStatisticsRepository.findLatestForFeedWithAcl(feedName) : feedStatisticsRepository.findLatestForFeedWithoutAcl(feedName);
    }

    public NifiFeedStats findLatestStatsForFeedWithoutAccessControl(String feedName) {
        return feedStatisticsRepository.findLatestForFeedWithoutAcl(feedName);
    }

    public List<? extends NifiFeedStats> findFeedStats(boolean streamingOnly) {
        if (streamingOnly) {
            return accessController.isEntityAccessControlled() ? feedStatisticsRepository.findStreamingFeedStatsWithAcl() : feedStatisticsRepository.findStreamingFeedStatsWithoutAcl();
        } else {
            return accessController.isEntityAccessControlled() ? feedStatisticsRepository.findFeedStatsWithAcl() : feedStatisticsRepository.findFeedStatsWithoutAcl();
        }
    }
}
