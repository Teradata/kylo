package com.thinkbiganalytics.servicemonitor.db.dao;

import com.thinkbiganalytics.servicemonitor.model.DefaultServiceComponent;
import com.thinkbiganalytics.servicemonitor.model.DefaultServiceStatusResponse;
import com.thinkbiganalytics.servicemonitor.model.ServiceComponent;
import com.thinkbiganalytics.servicemonitor.model.ServiceStatusResponse;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.DatabaseMetaDataCallback;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.MetaDataAccessException;
import org.springframework.util.Assert;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Named;

/**
 * Created by sr186054 on 9/30/15.
 */
@Named
public class DatabaseServiceCheckDao implements InitializingBean {


  @Autowired
  private JdbcTemplate jdbcTemplate;

  public JdbcTemplate getJdbcTemplate() {
    return jdbcTemplate;
  }

  public void afterPropertiesSet() throws Exception {
    Assert.notNull(this.jdbcTemplate);
  }

  public ServiceStatusResponse healthCheck() {
    String serviceName = "database";
    String componentName = "Kylo Database";
    ServiceComponent component = null;

    Map<String, Object> properties = new HashMap<>();

    try {
      properties =
          (Map<String, Object>) JdbcUtils.extractDatabaseMetaData(jdbcTemplate.getDataSource(), new DatabaseMetaDataCallback() {

            public Object processMetaData(DatabaseMetaData metadata) throws SQLException, MetaDataAccessException {
              Map<String, Object> properties = new HashMap<>();
              properties.put("database", metadata.getDatabaseProductName());
              properties.put("databaseVersion", metadata.getDatabaseMajorVersion() + "." + metadata.getDatabaseMinorVersion());
              properties.put("url", metadata.getURL());
              return properties;
            }
          });
      String dbVersion = properties.get("database") + " " + properties.get("databaseVersion");
      component =
          new DefaultServiceComponent.Builder(dbVersion, ServiceComponent.STATE.UP)
              .message(properties.get("database") + " is up.").properties(properties).build();
    } catch (MetaDataAccessException e) {
      component = new DefaultServiceComponent.Builder(componentName, ServiceComponent.STATE.DOWN).exception(e).build();
    }

    return new DefaultServiceStatusResponse(serviceName, Arrays.asList(component));

  }
}
