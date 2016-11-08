package com.thinkbiganalytics.metadata.migration.template;

import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplate;
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
public class TemplateDatabaseProvider {


    @Inject
    private JdbcTemplate jdbcTemplate;

    @Inject
    private FeedManagerTemplateProvider templateProvider;


    public Map<FeedManagerTemplate.ID, FeedManagerTemplate> queryTemplatesAsMap() {
        Map<FeedManagerTemplate.ID, FeedManagerTemplate> map = new HashMap<>();
        List<FeedManagerTemplate> list = queryTemplates();
        if (list != null && !list.isEmpty()) {
            for (FeedManagerTemplate item : list) {
                map.put(item.getId(), item);
            }
        }
        return map;
    }

    public List<FeedManagerTemplate> queryTemplates() {
        String query = "SELECT HEX(t.ID) ID, t.created_time, t.modified_time, t.description, t.name, t.state,\n"
                       + "    t.is_define_table, t.is_data_transform, t.allow_preconditions, t.json,\n"
                       + "    t.nifi_template_id, t.icon, t.icon_color FROM thinkbig.FM_TEMPLATE t";

        List<FeedManagerTemplate> categories = jdbcTemplate.query(query, new RowMapper<FeedManagerTemplate>() {
            @Override
            public FeedManagerTemplate mapRow(ResultSet rs, int i) throws SQLException {

                String id = Util.uuidNoSpacesToString(rs.getString("ID"));
                Timestamp created = rs.getTimestamp("CREATED_TIME");
                Timestamp updated = rs.getTimestamp("MODIFIED_TIME");
                String description = rs.getString("DESCRIPTION");
                String name = rs.getString("NAME");
                boolean isDefineTable = BooleanUtils.toBoolean(rs.getString("IS_DEFINE_TABLE"));
                boolean isDataTransform = BooleanUtils.toBoolean(rs.getString("IS_DATA_TRANSFORM"));
                boolean isAllowPreconditions = BooleanUtils.toBoolean(rs.getString("ALLOW_PRECONDITIONS"));
                String json = rs.getString("JSON");
                String nifiTemplateId = rs.getString("NIFI_TEMPLATE_ID");
                String state = rs.getString("STATE");
                String icon = rs.getString("ICON");
                String iconColor = rs.getString("ICON_COLOR");

                FeedManagerTemplateDTO dto = new FeedManagerTemplateDTO();
                dto.setId(templateProvider.resolveId(id));
                if (created != null) {
                    dto.setCreatedTime(new DateTime(created.getTime()));
                }
                if (updated != null) {
                    dto.setModifiedTime(new DateTime(updated.getTime()));
                }
                dto.setDescription(description);
                dto.setName(name);
                dto.setState(FeedManagerTemplate.State.valueOf(state));
                dto.setDefineTable(isDefineTable);
                dto.setAllowPreconditions(isAllowPreconditions);
                dto.setDataTransformation(isDataTransform);
                dto.setJson(json);
                dto.setNifiTemplateId(nifiTemplateId);
                dto.setIconColor(iconColor);
                dto.setIcon(icon);
                return dto;


            }
        });

        return categories;
    }


}
