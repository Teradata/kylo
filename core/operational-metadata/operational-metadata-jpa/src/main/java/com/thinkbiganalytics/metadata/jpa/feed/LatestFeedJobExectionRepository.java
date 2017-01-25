package com.thinkbiganalytics.metadata.jpa.feed;

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

import com.thinkbiganalytics.metadata.api.feed.LatestFeedJobExecution;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeed;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Created by sr186054 on 12/6/16.
 */
public interface LatestFeedJobExectionRepository extends JpaRepository<JpaLatestFeedJobExecution, Long> {


    List<JpaLatestFeedJobExecution> findByFeedType(String feedType);

    @Query("select v from JpaLatestFeedJobExecution as v where v.feedType = 'CHECK'")
    List<JpaLatestFeedJobExecution> findCheckDataJobs();

}
