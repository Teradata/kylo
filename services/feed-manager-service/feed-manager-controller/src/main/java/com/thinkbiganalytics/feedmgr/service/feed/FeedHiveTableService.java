package com.thinkbiganalytics.feedmgr.service.feed;

/*-
 * #%L
 * kylo-feed-manager-controller
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

import com.thinkbiganalytics.discovery.schema.Field;
import com.thinkbiganalytics.discovery.schema.TableSchema;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.rest.model.schema.TableSetup;
import com.thinkbiganalytics.hive.service.HiveService;
import com.thinkbiganalytics.hive.util.HiveUtils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataAccessException;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

/**
 * Operates on Hive tables used by feeds.
 */
public class FeedHiveTableService {

    /**
     * Hive thrift service
     */
    @Nonnull
    private final HiveService hiveService;

    /**
     * Constructs a {@code FeedHiveTableService}.
     *
     * @param hiveService the Hive thrift service
     */
    public FeedHiveTableService(@Nonnull final HiveService hiveService) {
        this.hiveService = hiveService;
    }

    /**
     * Updates the specified column specification.
     *
     * @param feed          the feed to update
     * @param oldColumnName the old column name
     * @param newColumn     the new column specification
     * @throws DataAccessException if there is any problem
     */
    public void changeColumn(@Nonnull final FeedMetadata feed, @Nonnull final String oldColumnName, @Nonnull final Field newColumn) {
        final StringBuilder query = new StringBuilder();
        query.append("ALTER TABLE ").append(HiveUtils.quoteIdentifier(feed.getSystemCategoryName())).append('.').append(HiveUtils.quoteIdentifier(feed.getSystemFeedName()))
            .append(" CHANGE COLUMN ").append(HiveUtils.quoteIdentifier(oldColumnName)).append(' ').append(HiveUtils.quoteIdentifier(newColumn.getName()))
            .append(' ').append(newColumn.getDerivedDataType());
        if (newColumn.getDescription() != null) {
            query.append(" COMMENT ").append(HiveUtils.quoteString(newColumn.getDescription()));
        }
        hiveService.update(query.toString());
    }

    /**
     * Updates the column descriptions in the Hive metastore for the specified feed.
     *
     * @param feed the feed to update
     * @throws DataAccessException if there is any problem
     */
    public void updateColumnDescriptions(@Nonnull final FeedMetadata feed) {
        final List<Field> feedFields = Optional.ofNullable(feed.getTable()).map(TableSetup::getTableSchema).map(TableSchema::getFields).orElse(null);
        if (feedFields != null && !feedFields.isEmpty()) {
            final TableSchema hiveSchema = hiveService.getTableSchema(feed.getSystemCategoryName(), feed.getSystemFeedName());
            if (hiveSchema != null) {
                final Map<String, Field> hiveFieldMap = hiveSchema.getFields().stream().collect(Collectors.toMap(field -> field.getName().toLowerCase(), Function.identity()));
                feedFields.stream()
                    .filter(feedField -> {
                        final Field hiveField = hiveFieldMap.get(feedField.getName().toLowerCase());
                        return hiveField != null && (StringUtils.isNotEmpty(feedField.getDescription()) || StringUtils.isNotEmpty(hiveField.getDescription()))
                               && !Objects.equals(feedField.getDescription(), hiveField.getDescription());
                    })
                    .forEach(feedField -> changeColumn(feed, feedField.getName(), feedField));
            }
        }
    }
}
