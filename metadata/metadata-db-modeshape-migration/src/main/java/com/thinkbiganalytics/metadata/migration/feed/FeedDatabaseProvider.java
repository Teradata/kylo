package com.thinkbiganalytics.metadata.migration.feed;

import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feedmgr.category.FeedManagerCategoryProvider;
import com.thinkbiganalytics.metadata.api.feedmgr.feed.FeedManagerFeed;
import com.thinkbiganalytics.metadata.api.feedmgr.feed.FeedManagerFeedProvider;
import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplateProvider;
import com.thinkbiganalytics.metadata.migration.support.Util;

import org.apache.commons.lang3.BooleanUtils;
import org.joda.time.DateTime;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

/**
 * Created by sr186054 on 6/15/16.
 */
public class FeedDatabaseProvider {


    @Inject
    private JdbcTemplate jdbcTemplate;

    @Inject
    private FeedManagerFeedProvider feedProvider;

    @Inject
    private FeedManagerCategoryProvider categoryProvider;

    @Inject
    private FeedManagerTemplateProvider templateProvider;

    public Map<Feed.ID, FeedManagerFeed> queryFeedsAsMap() {
        Map<Feed.ID, FeedManagerFeed> map = new HashMap<>();
        List<FeedManagerFeed> list = queryFeeds();
        if (list != null && !list.isEmpty()) {
            for (FeedManagerFeed item : list) {
                map.put(item.getId(), item);
            }
        }
        return map;
    }


    public List<FeedManagerFeed> queryFeeds() {

        String query = "SELECT HEX(feed.id) ID, feed.created_time, feed.modified_time, feed.description, feed.display_name, "
                       + "feed.initialized, feed.name, feed.state,  feed.version, HEX(feed.category_id) CATEGORY_ID, "
                       + "fmFeed.json, HEX(fmFeed.template_id) TEMPLATE_ID, fmFeed.nifi_process_group_id "
                       + "FROM  FM_FEED as fmFeed inner join "
                       + "FEED as feed on feed.id = fmFeed.id";

        return jdbcTemplate.query(query, new RowMapper<FeedManagerFeed>() {
            @Override
            public FeedManagerFeed mapRow(ResultSet rs, int i) throws SQLException {
                String id = Util.uuidNoSpacesToString(rs.getString("ID"));
                Timestamp created = rs.getTimestamp("CREATED_TIME");
                Timestamp updated = rs.getTimestamp("MODIFIED_TIME");
                String description = rs.getString("DESCRIPTION");
                String systemName = rs.getString("NAME");
                String displayName = rs.getString("DISPLAY_NAME");
                boolean isInitialized = BooleanUtils.toBoolean(rs.getString("INITIALIZED"));
                String state = rs.getString("STATE");

                Integer version = rs.getInt("VERSION");
                String categoryId = Util.uuidNoSpacesToString(rs.getString("CATEGORY_ID"));
                String templateId = Util.uuidNoSpacesToString(rs.getString("TEMPLATE_ID"));
                String json = rs.getString("JSON");
                String nifiProcessGroupId = rs.getString("NIFI_PROCESS_GROUP_ID");

                FeedManagerFeedDTO dto = new FeedManagerFeedDTO();
                dto.setId(feedProvider.resolveId(id));
                if (created != null) {
                    dto.setCreatedTime(new DateTime(created.getTime()));
                }
                if (updated != null) {
                    dto.setModifiedTime(new DateTime(updated.getTime()));
                }
                dto.setDescription(description);
                dto.setName(systemName);
                dto.setDisplayName(displayName);
                dto.setVersion(version);
                dto.setState(Feed.State.valueOf(state));
                dto.setCategoryId(categoryProvider.resolveId(categoryId));
                dto.setTemplateId(templateProvider.resolveId(templateId));
                dto.setJson(json);
                dto.setNifiProcessGroupId(nifiProcessGroupId);
                return dto;
            }
        });


    }


}
