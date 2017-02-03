package com.thinkbiganalytics.metadata.jpa.support;

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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.thinkbiganalytics.metadata.jpa.jobrepo.job.QJpaBatchJobExecution;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Helper class to map simple filter strings to more complex filters/joins
 *
 * Created by sr186054 on 12/5/16.
 */
public class CommonFilterTranslations {

    public static final String jobExecutionFeedNameFilterKey = "jobInstance.feed.name";

    static final ImmutableMap<String, String> jobExecutionFilters =
        new ImmutableMap.Builder<String, String>()
            .put("feed", jobExecutionFeedNameFilterKey)
            .put("feedName", jobExecutionFeedNameFilterKey)
            .put("feedname", jobExecutionFeedNameFilterKey)
            .put("jobName", "jobInstance.jobName")
            .put("jobStartTime", "startTimeMillis")
            .put("jobEndTime", "endTimeMillis")
            .put("startTime", "startTimeMillis")
            .put("endTime", "endTimeMillis")
            .put("starttime", "startTimeMillis")
            .put("endtime", "endTimeMillis")
            .put("executionId","jobExecutionId")
            .put("executionid", "jobExecutionId")
            .put("job", "jobInstance.jobName").build();

    static final ImmutableList<String> dateToMillisFields =
        new ImmutableList.Builder<String>()
            .add("jobStartTime")
            .add("startTime")
            .add("starttime")
            .add("jobEndTime")
            .add("endTime")
            .add("endtime")
            .add("startTimeMillis")
            .add("endTimeMillis").build();


    static final ImmutableMap<Class<? extends EntityPathBase>, Map<String,String>> queryDslFilters = new ImmutableMap.Builder<Class<? extends EntityPathBase>, Map<String,String>>().put(QJpaBatchJobExecution.class,jobExecutionFilters).build();

    public static boolean isDateStoredAsMillisField(String filterField) {
        return dateToMillisFields.contains(filterField);
    }

    /**
     * Check to see if the incoming path,column exist as a filter that should be resolved to a more complex string
     * @param basePath
     * @param column
     * @return
     */
    public static boolean containsFilterMappings(EntityPathBase basePath, String column){
        return containsFilterMappings(basePath.getClass(),column);
    }

    /**
     * if the supplied column matches a common filter for the incoming {@code basePath} object it will resolve the column to the correct filter
     * otherwise it will return the column
     * @param basePath
     * @param column
     * @return
     */
   public static String resolvedFilter(EntityPathBase basePath, String column){
        if(containsFilterMappings(basePath,column)){
            return queryDslFilters.get(basePath.getClass()).get(column);
        }
        return column;
    }

    /**
     * If the incoming {@code column} matches as a key to a more complex filter this will return the more complex filter/column needed for the Join or Sort.  Otherwise it will return the {@code column} as the filter string
     * @param column
     * @return
     */
    public static String getResolvedJobExecutionFilter(String column){
        return getResolvedFilter(QJpaBatchJobExecution.class,column);
    }



    private static String getResolvedFilter(Class<? extends EntityPathBase> clazz, String column){
        if(containsFilterMappings(clazz,column)){
            return queryDslFilters.get(clazz).get(column);
        }
        return null;
    }


    private static boolean containsFilterMappings(Class<? extends  EntityPathBase> clazz, String column){
        return queryDslFilters.containsKey(clazz) && queryDslFilters.get(clazz).containsKey(column);
    }

    /**
     * Update the Pageable sort filter and resolve any filter strings if needed
     * @param base
     * @param pageable
     * @return
     */
    public static Pageable resolveSortFilters(EntityPathBase base,Pageable pageable){

        if(pageable != null && pageable.getSort() != null) {
            List<Sort.Order> sortList = Lists.newArrayList(pageable.getSort().iterator());
            boolean anyMatch = sortList.stream().anyMatch(order -> containsFilterMappings(base,order.getProperty()));
            //if there is a match reconstruct pageable
            if(anyMatch){
                List<Sort.Order> updatedSortOrder = sortList.stream().map(order -> new Sort.Order(order.getDirection(),resolvedFilter(base,order.getProperty()))).collect(Collectors.toList());
                Sort sort= new Sort(updatedSortOrder);
                Pageable updatedPageable = new PageRequest(pageable.getPageNumber(),pageable.getPageSize(),sort);
                return updatedPageable;
            }
        }
        return pageable;

    }

}
