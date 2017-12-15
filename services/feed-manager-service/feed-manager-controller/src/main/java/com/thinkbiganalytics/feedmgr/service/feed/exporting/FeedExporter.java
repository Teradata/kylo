package com.thinkbiganalytics.feedmgr.service.feed.exporting;
/*-
 * #%L
 * thinkbig-feed-manager-controller
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

import com.thinkbiganalytics.feedmgr.rest.model.FeedDataTransformation;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.security.FeedServicesAccessControl;
import com.thinkbiganalytics.feedmgr.service.MetadataService;
import com.thinkbiganalytics.feedmgr.service.datasource.DatasourceModelTransform;
import com.thinkbiganalytics.feedmgr.service.feed.exporting.model.ExportFeed;
import com.thinkbiganalytics.feedmgr.service.feed.importing.model.ImportFeed;
import com.thinkbiganalytics.feedmgr.service.template.exporting.model.ExportTemplate;
import com.thinkbiganalytics.feedmgr.service.template.exporting.TemplateExporter;
import com.thinkbiganalytics.feedmgr.support.ZipFileUtil;
import com.thinkbiganalytics.json.ObjectMapperSerializer;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.security.FeedAccessControl;
import com.thinkbiganalytics.metadata.rest.model.data.Datasource;
import com.thinkbiganalytics.security.AccessController;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.NotFoundException;

/**
 * Created by sr186054 on 12/13/17.
 */
public class FeedExporter {

    @Inject
    private MetadataService metadataService;

    @Inject
    private MetadataAccess metadataAccess;

    @Inject
    private AccessController accessController;

    /**
     * Provides access to {@code Datasource} objects.
     */
    @Inject
    private DatasourceProvider datasourceProvider;

    /**
     * The {@code Datasource} transformer
     */
    @Inject
    private DatasourceModelTransform datasourceTransform;

    @Inject
    private TemplateExporter templateExporter;

    /**
     * Export a feed as a zip file
     *
     * @param feedId the id {@link Feed#getId()} of the feed to export
     * @return object containing the zip file with data about the feed.
     */
    public ExportFeed exportFeed(String feedId) throws IOException {
        this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.EXPORT_FEEDS);
        this.metadataService.checkFeedPermission(feedId, FeedAccessControl.EXPORT);

        // Prepare feed metadata
        final FeedMetadata feed = metadataService.getFeedById(feedId);

        if (feed == null) {
            //feed will not be found when user is allowed to export feeds but has no entity access to feed with feed id
            throw new NotFoundException("Feed not found for id " + feedId);
        }

        final List<Datasource> userDatasources = Optional.ofNullable(feed.getDataTransformation())
            .map(FeedDataTransformation::getDatasourceIds)
            .map(datasourceIds -> metadataAccess.read(
                () ->
                    datasourceIds.stream()
                        .map(datasourceProvider::resolve)
                        .map(datasourceProvider::getDatasource)
                        .map(domain -> datasourceTransform.toDatasource(domain, DatasourceModelTransform.Level.FULL))
                        .map(datasource -> {
                            // Clear sensitive fields
                            datasource.getDestinationForFeeds().clear();
                            datasource.getSourceForFeeds().clear();
                            return datasource;
                        })
                        .collect(Collectors.toList())
                 )
            )
            .orElse(null);
        if (userDatasources != null && !userDatasources.isEmpty()) {
            this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ACCESS_DATASOURCES);
            feed.setUserDatasources(userDatasources);
        }

        // Add feed json to template zip file
        final ExportTemplate exportTemplate = templateExporter.exportTemplateForFeedExport(feed.getTemplateId());
        final String feedJson = ObjectMapperSerializer.serialize(feed);

        final byte[] zipFile = ZipFileUtil.addToZip(exportTemplate.getFile(), feedJson, ImportFeed.FEED_JSON_FILE);
        return new ExportFeed(feed.getSystemFeedName() + ".feed.zip", zipFile);
    }

}
