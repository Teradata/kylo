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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

/**
 * Common DatabaseType defining some of the major databases and validationQuery strings
 */
public enum DatabaseType {
    DERBY("Apache Derby", "jdbc:derby:", "select 1"),
    DB2("DB2", "jdbc:db2", "select 1 from sysibm.sysdummy1"),
    FIREBIRD("Firebird", "jdbc:firebird", "select 1 from rdb$database"),
    H2("H2", "jdbc:h2", "select 1"),
    HSQL("HSQL Database Engine", "jdbc:hsqldb", "select 1 from INFORMATION_SCHEMA.SYSTEM_USERS"),
    MYSQL("MySQL", "jdbc:mysql", "select 1"),
    ORACLE("Oracle", "jdbc:oracle", "select 1 from dual"),
    POSTGRES("PostgreSQL", "jdbc:postgressql", "select 1"),
    SQLITE("SQLite", "jdbc:sqlite", "select 1"),
    SQLSERVER("Microsoft SQL Server", "jdbc:sqlserver", "select 1"),
    SYBASE("Sybase", "jdbc:sybase", "select 1"),
    TERADATA("Teradata", "jdbc:teradata", "select 1");


    private static final Map<String, DatabaseType> databaseProductNameMap;

    /**
     * lookup map based upon the jdbc connectoin string identifier
     */
    private static final Map<String, DatabaseType> jdbcConnectionStringMap;

    /**
     * Build up the lookup maps
     */
    static {
        databaseProductNameMap = new HashMap<>();
        jdbcConnectionStringMap = new HashMap<>();
        DatabaseType[] databaseTypes = values();
        for (DatabaseType dbType : databaseTypes) {
            databaseProductNameMap.put(dbType.getProductName(), dbType);
            for (String jdbcConnectionId : dbType.getJdbcConnectionStringIdentifiers()) {
                jdbcConnectionStringMap.put(jdbcConnectionId, dbType);
            }
        }
    }

    /**
     * The database name obtained from the connection.getDatabaseProductName
     */
    private final String productName;
    /**
     * unique string of jdbc connection to identify the database
     */
    private final String[] jdbcConnectionStringIdentifiers;
    /**
     * The validation Query needed to reconnect
     */
    private final String validationQuery;

    private DatabaseType(String productName, String jdbcUrlIdentifier, String validationQuery) {
        this(productName, new String[]{jdbcUrlIdentifier}, validationQuery);
    }

    private DatabaseType(String productName, String[] jdbcConnectionStringIdentifiers, String validationQuery) {
        this.productName = productName;
        this.jdbcConnectionStringIdentifiers = jdbcConnectionStringIdentifiers;
        this.validationQuery = validationQuery;
    }

    /**
     * Return the DatabaseType from the known Database Product name.
     *
     * @return the DatabaseType matching the product name
     */
    public static DatabaseType fromProductName(String productName) throws IllegalArgumentException {
        if (!databaseProductNameMap.containsKey(productName)) {
            throw new IllegalArgumentException("DatabaseType not found for product name: " + productName);
        } else {
            return databaseProductNameMap.get(productName);
        }
    }

    /**
     * Return the DatabaseType containing the first match to the jdbc connection string
     *
     * @param connectionString a jdbc url connection string
     * @return the DatabaseType matching the connection String
     */
    public static DatabaseType fromJdbcConnectionString(final String connectionString) throws IllegalArgumentException {
        final String lowerCaseConnectionString = connectionString.toLowerCase();

        for (final Map.Entry<String, DatabaseType> entry : jdbcConnectionStringMap.entrySet()) {
            if (lowerCaseConnectionString.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        throw new IllegalArgumentException("DatabaseType not found for jdbc connection String: " + connectionString);
    }

    /**
     * Parse the Database ProductName from the DataSource and return the matching DatabaseType
     */
    public static DatabaseType fromMetaData(DataSource dataSource) throws MetaDataAccessException {
        String databaseProductName = JdbcUtils.extractDatabaseMetaData(dataSource, "getDatabaseProductName").toString();
        databaseProductName = JdbcUtils.commonDatabaseName(databaseProductName);
        try {
            return fromProductName(databaseProductName);
        } catch (IllegalArgumentException e) {
            throw new MetaDataAccessException(e.getMessage());
        }
    }

    /**
     * Parse the Database ProductName from the Connection and return the matching DatabaseType
     */
    public static DatabaseType fromMetaData(Connection connection) throws MetaDataAccessException {
        String databaseProductName = null;
        try {
            databaseProductName = connection.getMetaData().getDatabaseProductName();
        } catch (SQLException e) {
            throw new MetaDataAccessException(e.getMessage());
        }
        if (databaseProductName != null) {
            databaseProductName = JdbcUtils.commonDatabaseName(databaseProductName);
            try {
                return fromProductName(databaseProductName);
            } catch (IllegalArgumentException e) {
                throw new MetaDataAccessException(e.getMessage());
            }
        } else {
            throw new MetaDataAccessException("Database Type not found for connection");
        }
    }

    /**
     * Get the database product name
     *
     * @return the database product name
     */
    public String getProductName() {
        return this.productName;
    }

    /**
     * get the array of connection identifiers for this database type
     *
     * @return the array of unique jdbc connection identifiers
     */
    public String[] getJdbcConnectionStringIdentifiers() {
        return this.jdbcConnectionStringIdentifiers;
    }

    /**
     * get the validation query for this database
     *
     * @return the validation query for the database
     */
    public String getValidationQuery() {
        return validationQuery;
    }
}
