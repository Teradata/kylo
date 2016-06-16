package com.thinkbiganalytics.metadata.migration.category;

import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.feedmgr.category.FeedManagerCategory;
import com.thinkbiganalytics.metadata.api.feedmgr.category.FeedManagerCategoryProvider;
import com.thinkbiganalytics.metadata.migration.support.Util;

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
public class CategoryDatabaseProvider {


    @Inject
    private JdbcTemplate jdbcTemplate;

    @Inject
    private FeedManagerCategoryProvider categoryProvider;


    public Map<Category.ID, FeedManagerCategory> queryCategoriesAsMap() {
        Map<Category.ID, FeedManagerCategory> categoryMap = new HashMap<>();
        List<FeedManagerCategory> categories = queryCategories();
        if (categories != null && !categories.isEmpty()) {
            for (FeedManagerCategory category : categories) {
                categoryMap.put(category.getId(), category);
            }
        }
        return categoryMap;
    }

    public List<FeedManagerCategory> queryCategories() {
        String query = "SELECT HEX(c.id) ID ,c.created_time,c.modified_time,c.description,c.name,c.display_name, "
                       + "    c.state, c.version, fmC.icon, fmC.icon_color "
                       + "FROM CATEGORY c "
                       + "inner join FM_CATEGORY fmC on fmC.id = c.id";

        List<FeedManagerCategory> categories = jdbcTemplate.query(query, new RowMapper<FeedManagerCategory>() {
            @Override
            public FeedManagerCategory mapRow(ResultSet rs, int i) throws SQLException {

                String id = Util.uuidNoSpacesToString(rs.getString("ID"));
                Timestamp created = rs.getTimestamp("CREATED_TIME");
                Timestamp updated = rs.getTimestamp("MODIFIED_TIME");
                String description = rs.getString("DESCRIPTION");
                String systemName = rs.getString("NAME");
                String displayName = rs.getString("DISPLAY_NAME");
                String state = rs.getString("STATE");
                Integer version = rs.getInt("VERSION");
                String icon = rs.getString("ICON");
                String iconColor = rs.getString("ICON_COLOR");

                FeedManagerCategoryDTO dto = new FeedManagerCategoryDTO();
                dto.setId(categoryProvider.resolveId(id));
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
                dto.setIconColor(iconColor);
                dto.setIcon(icon);
                return dto;


            }
        });

        return categories;
    }


}
