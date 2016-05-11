package com.thinkbiganalytics.jobrepo.repository.dao;

import com.thinkbiganalytics.jobrepo.nifi.model.FlowFileComponent;
import com.thinkbiganalytics.jobrepo.nifi.model.NifiJobExecution;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Created by sr186054 on 2/26/16.
 */
public class NifiPipelineControllerDao implements InitializingBean {


  private static final String INSERT_NIFI_PIPELINE_CONTROLLER_JOB = "INSERT INTO NIFI_PIPELINE_CONTROLLER_JOB "
                                                                    + "(EVENT_ID,NIFI_EVENT_ID,FLOW_FILE_UUID, FEED_ID, FEED_NAME,JOB_INSTANCE_ID, JOB_EXECUTION_ID) "
                                                                    + "VALUES(?,?,?,?, ?, ?,?)";

  private static final String INSERT_NIFI_PIPELINE_CONTROLLER_STEP = "INSERT INTO NIFI_PIPELINE_CONTROLLER_STEP "
                                                                     + "(EVENT_ID,NIFI_EVENT_ID,COMPONENT_ID,JOB_EXECUTION_ID,STEP_EXECUTION_ID) "
                                                                     + "VALUES(?,?, ?,?, ?)";



  private static final String GET_MAX_EVENT_ID = "SELECT MAX(EVENT_ID) FROM NIFI_PIPELINE_CONTROLLER_STEP";

  public NifiPipelineControllerDao() {
  }

  protected JdbcOperations jdbcTemplate;

  public void saveJobExecution(NifiJobExecution jobExecution) {

    Object[]
        parameters =
        new Object[]{jobExecution.getFlowFile().getFirstEvent().getEventId(),
                     jobExecution.getFlowFile().getFirstEvent().getNifiEventId(), jobExecution.getFlowFile().getUuid(), null,
                     jobExecution.getFeedName(), jobExecution.getJobInstanceId(), jobExecution.getJobExecutionId()};
    getJdbcTemplate().update(
        INSERT_NIFI_PIPELINE_CONTROLLER_JOB,
        parameters,
        new int[]{Types.BIGINT, Types.BIGINT, Types.VARCHAR, Types.BIGINT, Types.VARCHAR, Types.BIGINT, Types.BIGINT});
  }

  public void saveStepExecution(FlowFileComponent component) {

    Object[]
        parameters =
        new Object[]{component.getFirstEvent().getEventId(), component.getFirstEvent().getNifiEventId(),
                     component.getComponentId(), component.getJobExecution().getJobExecutionId(), component.getStepExecutionId()};
    getJdbcTemplate().update(
        INSERT_NIFI_PIPELINE_CONTROLLER_STEP,
        parameters,
        new int[]{Types.BIGINT, Types.BIGINT, Types.VARCHAR, Types.BIGINT, Types.BIGINT});
  }


  /**
   * Returns the Maximum Event Id that was processed into pipeline controller
   */
  public Long getMaxEventId() {
    Long count = jdbcTemplate.queryForObject(GET_MAX_EVENT_ID, new RowMapper<Long>() {
      @Override
      public Long mapRow(ResultSet resultSet, int i) throws SQLException {
        return resultSet.getLong(1);
      }
    });
    return count;
  }

  public void setJdbcTemplate(JdbcOperations jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public JdbcOperations getJdbcTemplate() {
    return jdbcTemplate;
  }



  /**
   * /** CREATE TABLE NIFI_PIPELINE_CONTROLLER_STEP  ( EVENT_ID BIGINT NOT NULL PRIMARY KEY, NIFI_EVENT_ID BIGINT, COMPONENT_ID
   * VARCHAR(255), JOB_EXECUTION_ID BIGINT, STEP_EXECUTION_ID BIGINT ) ENGINE=InnoDB; <p> <p> CREATE TABLE
   * NIFI_PIPELINE_CONTROLLER_JOB  ( EVENT_ID BIGINT NOT NULL PRIMARY KEY, NIFI_EVENT_ID BIGINT, FLOW_FILE_UUID VARCHAR(255),
   * FEED_ID BIGINT, FEED_NAME VARCHAR(255), JOB_INSTANCE_ID BIGINT, JOB_EXECUTION_ID BIGINT ) ENGINE=InnoDB;
   */


  @Override
  public void afterPropertiesSet() throws Exception {

  }
}
