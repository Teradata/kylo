package com.thinkbiganalytics.spark.service;

/*-
 * #%L
 * kylo-spark-shell-client-app
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

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;
import org.apache.spark.sql.sources.DataSourceRegister;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * Service to aid in locating spark resources.
 */
public class SparkUtilityService {

    private static final XLogger log = XLoggerFactory.getXLogger(SparkUtilityService.class);

    /**
     * Spark locator service
     */
    @Resource
    public SparkLocatorService sparkLocatorService;

    @Resource
    public List<String> downloadsDatasourceExcludes;

    @Resource
    public List<String> tablesDatasourceExcludes;


    /**
     * called by SparkShellController or Livy
     * @return
     */
    public Map<String,List<String>> getDataSources() {
        log.entry();

        FluentIterable<String> fi = FluentIterable.from(sparkLocatorService.getDataSources())
                .transform(new Function<DataSourceRegister, String>() {
                    @Nullable
                    @Override
                    public String apply(@Nullable final DataSourceRegister input) {
                        return input != null ? input.shortName() : null;
                    }
                });

        final List<String> tableExcludes = tablesDatasourceExcludes;
        final List<String> tableSources = fi.filter(new Predicate<String>() {
            @Override
            public boolean apply(@Nullable String input) {
                return ! tableExcludes.contains(input);
            }
        }).toList();

        final List<String> downloadExcludes = downloadsDatasourceExcludes;
        final List<String> downloadSources = fi.filter(new Predicate<String>() {
            @Override
            public boolean apply(@Nullable String input) {
                return ! downloadExcludes.contains(input);
            }
        }).toList();

        Map<String,List<String>> result = ImmutableMap.of("tables", tableSources, "downloads", downloadSources);

        return log.exit(result);
    }

}
