package com.thinkbiganalytics.nifi.activemq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTOHolder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.incrementer.DataFieldMaxValueIncrementer;
import org.springframework.stereotype.Component;

import java.sql.Types;
import java.util.List;

/**
 * Created by sr186054 on 2/25/16. see mysql-schema.sql for db table scripts
 */
@Component
public class ProvenanceEventReceiverDatabaseWriter implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(ProvenanceEventReceiverDatabaseWriter.class);
    private DataFieldMaxValueIncrementer nifiProvenanceEventSequencer;

    @Autowired
    @Qualifier("jdbcThinkbigNifi")
    private JdbcTemplate jdbcTemplate;

    private static String NIFI_PROVENANCE_EVENT_TABLE = "NIFI_PROVENANCE_EVENT_JSON";

    @Override
    public void afterPropertiesSet() throws Exception {
    }


    public void writeEvent(ProvenanceEventRecordDTOHolder holder) throws Exception {
        holder.getEvents().stream().forEach(e -> {
            try {
                writeEvent(e);
            } catch (Exception e1) {
                logger.error("An error occurred writing to the temporary H2 Database for event {}, {}", e, e1.getMessage(), e1);
            }
        });
    }

    /**
     * Write the Event to the Database Table
     */
    public void writeEvent(ProvenanceEventRecordDTO event) throws Exception {
        logger.info("Writing event to temp table: " + event);
        Long eventId = event.getEventId();
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(event);
        String
            sql =
            "INSERT INTO " + NIFI_PROVENANCE_EVENT_TABLE
            + "(NIFI_EVENT_ID, JSON)"
            +
            "VALUES(?,?)";
        Object[] params = new Object[]{eventId, json};

        int[] types = new int[]{Types.BIGINT, Types.VARCHAR};
        jdbcTemplate.update(sql, params, types);
    }

    /**
     * Select the Events from the Database Table
     */
    public List<ProvenanceEventRecordDTO> getEvents() throws Exception {
        logger.info("Getting the temporary events!!! ");

        List<ProvenanceEventRecordDTO> events = this.jdbcTemplate.query(
            "SELECT NIFI_EVENT_ID, JSON from " + NIFI_PROVENANCE_EVENT_TABLE + " order by NIFI_EVENT_ID ASC",
            (rs, rowNum) -> {
                //long eventId = rs.getLong("NIFI_EVENT_ID");
                String json = rs.getString("JSON");
                ProvenanceEventRecordDTO event = null;
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    event = mapper.readValue(json, ProvenanceEventRecordDTO.class);
                } catch (Exception ee) {
                    logger.error("Error marshalling the JSON back to the DTO", ee);
                }
                return event;
            });

        return events;
    }

    /**
     * Clear the Events from the Database Table
     */
    public void clearEvents() throws Exception {
        logger.info("Clearing the temporary events!!! ");
        String sql = "DELETE FROM " + NIFI_PROVENANCE_EVENT_TABLE;
        jdbcTemplate.update(sql);
    }

    public void createTables() throws Exception {
        logger.info("Creating table for temporary storage ");
        String sql = "CREATE TABLE IF NOT EXISTS " + NIFI_PROVENANCE_EVENT_TABLE + "(" +
                     " NIFI_EVENT_ID BIGINT  NOT NULL, JSON TEXT);";

        jdbcTemplate.update(sql);
    }
}