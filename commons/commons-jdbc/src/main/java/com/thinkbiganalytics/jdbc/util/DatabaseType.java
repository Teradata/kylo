package com.thinkbiganalytics.jdbc.util;

/*-
 * #%L
 * thinkbig-commons-jdbc
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

import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.MetaDataAccessException;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

/**
 * Taken from Spring Batch
 *
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
