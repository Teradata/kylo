package com.thinkbiganalytics.jobrepo.query.support;

import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.MetaDataAccessException;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

/**
 * Taken from Spring Batch
 */
public enum DatabaseType {
  DERBY("Apache Derby"),
  DB2("DB2"),
  DB2ZOS("DB2ZOS"),
  HSQL("HSQL Database Engine"),
  SQLSERVER("Microsoft SQL Server"),
  MYSQL("MySQL"),
  ORACLE("Oracle"),
  POSTGRES("PostgreSQL"),
  SYBASE("Sybase"),
  H2("H2"),
  SQLITE("SQLite");

  private static final Map<String, DatabaseType> nameMap;
  private final String productName;

  private DatabaseType(String productName) {
    this.productName = productName;
  }

  public String getProductName() {
    return this.productName;
  }

  public static DatabaseType fromProductName(String productName) {
    if (!nameMap.containsKey(productName)) {
      throw new IllegalArgumentException("DatabaseType not found for product name: [" + productName + "]");
    } else {
      return (DatabaseType) nameMap.get(productName);
    }
  }

  public static DatabaseType fromMetaData(DataSource dataSource) throws MetaDataAccessException {
    String databaseProductName = JdbcUtils.extractDatabaseMetaData(dataSource, "getDatabaseProductName").toString();
    if (StringUtils.hasText(databaseProductName) && !databaseProductName.equals("DB2/Linux") && databaseProductName
        .startsWith("DB2")) {
      String databaseProductVersion = JdbcUtils.extractDatabaseMetaData(dataSource, "getDatabaseProductVersion").toString();
      if (!databaseProductVersion.startsWith("SQL")) {
        databaseProductName = "DB2ZOS";
      } else {
        databaseProductName = JdbcUtils.commonDatabaseName(databaseProductName);
      }
    } else {
      databaseProductName = JdbcUtils.commonDatabaseName(databaseProductName);
    }

    return fromProductName(databaseProductName);
  }

  static {
    nameMap = new HashMap();
    DatabaseType[] arr$ = values();
    int len$ = arr$.length;

    for (int i$ = 0; i$ < len$; ++i$) {
      DatabaseType type = arr$[i$];
      nameMap.put(type.getProductName(), type);
    }

  }
}
