package com.thinkbiganalytics.spark.conf;

/*-
 * #%L
 * Spark Shell Core
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics, a Teradata Company
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

import com.thinkbiganalytics.kylo.spark.rest.model.job.SparkJobRequest;
import com.thinkbiganalytics.kylo.spark.rest.model.job.SparkJobResponse;
import com.thinkbiganalytics.spark.conf.model.SparkShellProperties;
import com.thinkbiganalytics.spark.rest.model.DataSources;
import com.thinkbiganalytics.spark.rest.model.KyloCatalogReadRequest;
import com.thinkbiganalytics.spark.rest.model.SaveRequest;
import com.thinkbiganalytics.spark.rest.model.SaveResponse;
import com.thinkbiganalytics.spark.rest.model.ServerStatusResponse;
import com.thinkbiganalytics.spark.rest.model.TransformRequest;
import com.thinkbiganalytics.spark.rest.model.TransformResponse;
import com.thinkbiganalytics.spark.shell.ServerProcessManager;
import com.thinkbiganalytics.spark.shell.SparkShellProcess;
import com.thinkbiganalytics.spark.shell.SparkShellProcessManager;
import com.thinkbiganalytics.spark.shell.SparkShellRestClient;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.ws.rs.core.Response;

/**
 * Dummy configs to let kyloUpgrade process build an application context
 */
@Configuration
@Profile("kyloUpgrade")
public class SparkUpgradeConfig {

    @Bean
    public SparkShellProcessManager processManagerForUpgrade() {
        return new ServerProcessManager(new SparkShellProperties());
    }

    @Bean
    public SparkShellRestClient restClient() {
        return new SparkShellRestClient() {
            @Override
            public SparkJobResponse createJob(@Nonnull SparkShellProcess process, @Nonnull SparkJobRequest request) {
                return null;
            }

            @Nonnull
            @Override
            public Optional<Response> downloadQuery(@Nonnull SparkShellProcess process, @Nonnull String queryId, @Nonnull String saveId) {
                return Optional.empty();
            }

            @Nonnull
            @Override
            public Optional<Response> downloadTransform(@Nonnull SparkShellProcess process, @Nonnull String transformId, @Nonnull String saveId) {
                return Optional.empty();
            }

            @Nonnull
            @Override
            public DataSources getDataSources(@Nonnull SparkShellProcess process) {
                return null;
            }

            @Nonnull
            @Override
            public Optional<SparkJobResponse> getJobResult(@Nonnull SparkShellProcess process, @Nonnull String id) {
                return Optional.empty();
            }

            @Nonnull
            @Override
            public Optional<TransformResponse> getQueryResult(@Nonnull SparkShellProcess process, @Nonnull String id) {
                return Optional.empty();
            }

            @Nonnull
            @Override
            public Optional<TransformResponse> getTransformResult(@Nonnull SparkShellProcess process, @Nonnull String table) {
                return Optional.empty();
            }

            @Nonnull
            @Override
            public Optional<SaveResponse> getQuerySave(@Nonnull SparkShellProcess process, @Nonnull String queryId, @Nonnull String saveId) {
                return Optional.empty();
            }

            @Nonnull
            @Override
            public Optional<SaveResponse> getTransformSave(@Nonnull SparkShellProcess process, @Nonnull String transformId, @Nonnull String saveId) {
                return Optional.empty();
            }

            @Nonnull
            @Override
            public TransformResponse query(@Nonnull SparkShellProcess process, @Nonnull TransformRequest request) {
                return null;
            }

            @Nonnull
            @Override
            public SaveResponse saveQuery(@Nonnull SparkShellProcess process, @Nonnull String id, @Nonnull SaveRequest request) {
                return null;
            }

            @Nonnull
            @Override
            public SaveResponse saveTransform(@Nonnull SparkShellProcess process, @Nonnull String id, @Nonnull SaveRequest request) {
                return null;
            }

            @Nonnull
            @Override
            public TransformResponse transform(@Nonnull SparkShellProcess process, @Nonnull TransformRequest request) {
                return null;
            }

            @Nonnull
            @Override
            public TransformResponse kyloCatalogTransform(@Nonnull SparkShellProcess process, @Nonnull KyloCatalogReadRequest request) {
                return null;
            }

            @Nonnull
            @Override
            public ServerStatusResponse serverStatus(SparkShellProcess sparkShellProcess) {
                return null;
            }
        };
    }

}
