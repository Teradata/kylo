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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by sr186054 on 7/25/17.
 */
@Service
public class JpaNifiFeedStatisticsProvider implements NifiFeedStatisticsProvider {

    private NifiFeedStatisticsRepository feedStatisticsRepository;

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
                feedStatisticsRepository.save(((JpaNifiFeedStats) nifiFeedStats));
            });
        }
    }

    @Override
    public NifiFeedStats findLatestStatsForFeed(String feedName) {
        return feedStatisticsRepository.findLatestForFeed(feedName);
    }

    public List<? extends NifiFeedStats> findFeedStats(boolean streamingOnly) {
        if (streamingOnly) {
            return feedStatisticsRepository.findStreamingFeedStats();
        } else {
            return feedStatisticsRepository.findFeedStats();
        }
    }
}
