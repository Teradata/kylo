package com.thinkbiganalytics.nifi.provenance.v2.writer;

import com.thinkbiganalytics.nifi.provenance.v2.ProvenanceEventRecordSerializer;
import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.provenance.ProvenanceEventRecord;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.incrementer.DataFieldMaxValueIncrementer;
import org.springframework.jdbc.support.incrementer.MySQLMaxValueIncrementer;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Created by sr186054 on 2/25/16.
 * see mysql-schema.sql for db table scripts
 */
@Component
public class ProvenanceEventRecordDatabaseWriter extends AbstractProvenanceEventWriter implements InitializingBean {

    private DataFieldMaxValueIncrementer nifiProvenanceEventSequencer;

    @Autowired
    @Qualifier("jdbcThinkbigNifi")
    private JdbcTemplate jdbcTemplate;

    private ProvenanceEventRecordSerializer serializer;
    private static String NIFI_PROVENANCE_EVENT_TABLE = "NIFI_PROVENANCE_EVENT";

    public ProvenanceEventRecordDatabaseWriter() {
        serializer = new ProvenanceEventRecordSerializer();

    }

    @Override
    public void afterPropertiesSet() throws Exception {
        nifiProvenanceEventSequencer = new MySQLMaxValueIncrementer(jdbcTemplate.getDataSource(), "NIFI_PROVENANCE_EVENT_SEQ", "ID");
    }


    @Override
    /**
     * Write the Event to the Database Table
     */
    public Long writeEvent(ProvenanceEventRecord event) {
        System.out.println("persistProvenanceEventRecord!!! " + event);
        Long id = null;
        try {
            Long eventId = event.getEventId();
            if (eventId == null) {
                eventId = eventIdIncrementer.getAndIncrement();
            }
            String json = serializer.getAsJSON(event);
            String attributesJson = serializer.getAttributesAsJSON(event);
            String sql = "INSERT INTO " + NIFI_PROVENANCE_EVENT_TABLE + "(EVENT_ID,NIFI_EVENT_ID,EVENT_TYPE,FLOW_FILE_UUID,FLOW_FILE_ENTRY_DATE,EVENT_TIME,EVENT_DURATION,LINEAGE_START_DATE,COMPONENT_ID,COMPONENT_TYPE,DETAILS,PARENT_UUIDS,CHILD_UUIDS,ATTRIBUTES_JSON,JSON)" +
                    "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            Object[] params = new Object[]{id, eventId, event.getEventType().toString(), event.getFlowFileUuid(),
                    event.getFlowFileEntryDate(), event.getEventTime(), event.getEventDuration(), event.getLineageStartDate(), event.getComponentId(), event.getComponentType(),
                    event.getDetails(), StringUtils.join(event.getParentUuids(), ","), StringUtils.join(event.getChildUuids(), ","), attributesJson, json};

            int[] types = new int[]{Types.BIGINT, Types.BIGINT, Types.VARCHAR, Types.VARCHAR,
                    Types.BIGINT, Types.BIGINT, Types.BIGINT, Types.BIGINT, Types.VARCHAR, Types.VARCHAR,
                    Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR};
            jdbcTemplate.update(sql, params, types);
        } catch (org.springframework.dao.DataAccessException e) {
            e.printStackTrace();
        }
        return id;
    }

    @Override
    /**
     * NIFI works of in memory event ids that change.  get the max nifi event id... not our internal event id sequencer
     */
    public Long getMaxEventId() {

        String query = "SELECT MAX(NIFI_EVENT_ID) EVENT_ID FROM " + NIFI_PROVENANCE_EVENT_TABLE;
        Long eventId = jdbcTemplate.queryForObject(query, new RowMapper<Long>() {
            @Override
            public Long mapRow(ResultSet resultSet, int i) throws SQLException {
                return resultSet.getLong(1);
            }
        });
        return eventId;

    }

    @Override
    public void setMaxEventId(Long eventId) {
        eventIdIncrementer.setId(eventId);
    }
}
