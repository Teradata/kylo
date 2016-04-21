package com.thinkbiganalytics.jobrepo.repository.dao;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thinkbiganalytics.jobrepo.nifi.model.ProvenanceEventRecordDTO;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;

/**
 * Created by sr186054 on 2/26/16.
 *
 * This is not being used now
 */
@Named
public class NifiDao implements InitializingBean {

  private static String NIFI_PROVENANCE_EVENT_TABLE = "NIFI_PROVENANCE_EVENT";

  private ObjectMapper mapper;

  public NifiDao() {
    mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  @Autowired
  @Qualifier("jdbcThinkbigNifi")
  protected JdbcTemplate jdbcTemplate;


  /**
   * Returns the events between and including 2 event ids;
   */
  public List<ProvenanceEventRecordDTO> getEvents(Long startEventId, Long endingEventId) {
    System.out.println("getLastRecordedEventId!!! ");
    List<ProvenanceEventRecordDTO> list = new ArrayList<>();
    String query = "";
    if (startEventId != null && endingEventId != null) {
      query =
          "SELECT EVENT_ID,JSON FROM " + NIFI_PROVENANCE_EVENT_TABLE + " WHERE EVENT_ID >=" + startEventId + " AND EVENT_ID <="
          + endingEventId + " ORDER BY EVENT_ID ";
    } else if (startEventId == null && endingEventId != null) {
      query =
          "SELECT EVENT_ID,JSON FROM " + NIFI_PROVENANCE_EVENT_TABLE + " WHERE EVENT_ID <=" + endingEventId
          + " ORDER BY EVENT_ID ";
    } else if (startEventId != null && endingEventId == null) {
      query =
          "SELECT EVENT_ID,JSON FROM " + NIFI_PROVENANCE_EVENT_TABLE + " WHERE EVENT_ID >=" + startEventId
          + " ORDER BY EVENT_ID ";
    }
    if (StringUtils.isNotBlank(query)) {
      list = jdbcTemplate.query(query, new RowMapper<ProvenanceEventRecordDTO>() {
        @Override
        public ProvenanceEventRecordDTO mapRow(ResultSet rs, int i) throws SQLException {
          Long id = rs.getLong("EVENT_ID");
          String jsonResult = rs.getString("JSON");
          if (StringUtils.isNotBlank(jsonResult)) {
            try {
              ProvenanceEventRecordDTO eventRecord = mapper.readValue(jsonResult, ProvenanceEventRecordDTO.class);
              eventRecord.setNifiEventId(eventRecord.getEventId());
              eventRecord.setEventId(id);
              return eventRecord;
            } catch (IOException e) {
              e.printStackTrace();
            }

          }
          return null;
        }
      });
    }

    return list;
  }

  /**
   * Returns the Maximum Event Id that was captured for Nifi
   */
  public Long getMaxEventId() {
    String query = "SELECT MAX(EVENT_ID) EVENT_ID FROM " + NIFI_PROVENANCE_EVENT_TABLE;
    Long count = jdbcTemplate.queryForObject(query, new RowMapper<Long>() {
      @Override
      public Long mapRow(ResultSet resultSet, int i) throws SQLException {
        return resultSet.getLong(1);
      }
    });
    return count;
  }


  @Override
  public void afterPropertiesSet() throws Exception {

  }
}
