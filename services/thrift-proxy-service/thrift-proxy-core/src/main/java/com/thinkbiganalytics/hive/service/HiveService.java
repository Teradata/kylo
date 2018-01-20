package com.thinkbiganalytics.hive.service;

/*-
 * #%L
 * thinkbig-thrift-proxy-core
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


import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.thinkbiganalytics.discovery.schema.QueryResult;
import com.thinkbiganalytics.discovery.schema.TableSchema;
import com.thinkbiganalytics.hive.util.HiveUtils;
import com.thinkbiganalytics.kerberos.KerberosTicketConfiguration;
import com.thinkbiganalytics.kerberos.KerberosUtil;
import com.thinkbiganalytics.schema.DBSchemaParser;
import com.thinkbiganalytics.schema.QueryRunner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.sql.DataSource;

/**
 * Executes Hive queries and retrieves Hive metadata.
 *
 * <p>Acquires a Kerberos ticket as needed to execute the Hive queries. The HTTP transport mode for Hive requires a Kerberos ticket whenever interacting with the Hive JDBC driver.</p>
 */
public class HiveService {

    private static final Logger log = LoggerFactory.getLogger(HiveService.class);
    private static final long DEFAULT_EXPIRY_DURATION = 4;
    private static final TimeUnit DEFAULT_EXPIRY_TIME_NIT = TimeUnit.HOURS;

    @Inject
    @Qualifier("hiveJdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    @Inject
    @Qualifier("kerberosHiveConfiguration")
    private KerberosTicketConfiguration kerberosHiveConfiguration;

    private DBSchemaParser schemaParser = null;

    /**
     * Maps current user to his Hive access cache
     */
    private LoadingCache<String, UserHiveAccessCache> perUserAccessCache;

    @Autowired
    private Environment env;

    @PostConstruct
    public void postConstruct() {
        long duration = env.getProperty("hive.userImpersonation.cache.expiry.duration") != null ? Long.valueOf(env.getProperty("hive.userImpersonation.cache.expiry.duration")) : DEFAULT_EXPIRY_DURATION;
        TimeUnit timeUnit = env.getProperty("hive.userImpersonation.cache.expiry.time-unit") != null ? TimeUnit.valueOf(env.getProperty("hive.userImpersonation.cache.expiry.time-unit")) : DEFAULT_EXPIRY_TIME_NIT;

        perUserAccessCache = CacheBuilder.newBuilder().expireAfterWrite(duration, timeUnit).build(new CacheLoader<String, UserHiveAccessCache>() {
            @Override
            public UserHiveAccessCache load(@Nonnull String user) throws Exception {
                return new UserHiveAccessCache(user);
            }
        });
    }

    /**
     * Caches Hive schemas and tables accessible by current user
     */
    class UserHiveAccessCache {
        /**
         * List of schemas to which user has access
         */
        private List<String> schemas;
        /**
         * Holds tables in each schema
         */
        private LoadingCache<String, List<String>> schemaTables = CacheBuilder.newBuilder().build(new CacheLoader<String, List<String>>() {
            @Override
            public List<String> load(@Nonnull String schemaName) throws Exception {
                return isSchemaAccessibleByUser(schemaName) ? loadTablesForImpersonatedUser(schemaName) : Collections.emptyList();
            }
        });

        UserHiveAccessCache(String user) {
            try {
                this.schemas = loadDatabasesForImpersonatedUser();
            } catch (Exception e) {
                this.schemas = Collections.emptyList();
                log.warn(String.format("Failed to load Hive databases for user %s. User may not have access to Hive. %s", user, e.getMessage()));
            }
        }

        private boolean isSchemaAccessibleByUser(@Nonnull String schemaName) {
            return schemas.contains(schemaName);
        }

        List<String> getTables(@Nonnull String schema) throws ExecutionException {
            return schemaTables.get(schema);
        }

        List<String> getSchemas() {
            return schemas;
        }

        /**
         * returns a list of all the scheam.tablename for a given schema
         */
        private List<String> loadTablesForImpersonatedUser(String schema) {
            QueryResult tables = query("show tables in `" + schema + "`");
            return tables.getRows().stream().flatMap(row -> row.entrySet().stream()).map(e -> schema + "." + e.getValue().toString()).collect(Collectors.toList());
        }
        /**
         * returns a list of all the scheam.tablename for a given schema
         */
        private List<String> loadDatabasesForImpersonatedUser() {
            QueryResult databases = query("show databases");
            return databases.getRows().stream().flatMap(row -> row.entrySet().stream()).map(e -> e.getValue().toString()).collect(Collectors.toList());
        }

    }

    private DataSource getDataSource() {
        return jdbcTemplate.getDataSource();
    }

    private DBSchemaParser getDBSchemaParser() {
        if (schemaParser == null) {
            schemaParser = new DBSchemaParser(getDataSource(), kerberosHiveConfiguration);
        }
        return schemaParser;
    }

    public List<String> getSchemaNames() {
        return KerberosUtil.runWithOrWithoutKerberos(() -> getDBSchemaParser().listSchemas(), kerberosHiveConfiguration);
    }

    public List<String> getTables(String schema) {
        return KerberosUtil.runWithOrWithoutKerberos(() -> getDBSchemaParser().listTables(schema), kerberosHiveConfiguration);
    }

    /**
     * @return cached list of tables in given schema to which user has access
     */
    public List<String> getTablesForImpersonatedUser(String schema) {
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            UserHiveAccessCache cache = this.perUserAccessCache.get(currentUser);
            return cache.getTables(schema);
        } catch (ExecutionException e) {
            throw new RuntimeException(String.format("Failed to get Hive tables in schema %s accessible by user %s", schema, currentUser));
        }
    }

    /**
     * @return cached list of databases to which user has access
     */
    private List<String> getDatabasesForImpersonatedUser() {
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            UserHiveAccessCache cache = this.perUserAccessCache.get(currentUser);
            return cache.getSchemas();
        } catch (ExecutionException e) {
            throw new RuntimeException(String.format("Failed to get Hive schemas accessible by user %s", currentUser));
        }
    }

    /**
     * returns a list of all the schema.tablename
     */
    public List<String> getAllTablesForImpersonatedUser() {
        long start = System.currentTimeMillis();
        List<String> allTables = new ArrayList<>();
        getDatabasesForImpersonatedUser().forEach(database -> allTables.addAll(getTablesForImpersonatedUser(database)));
        log.debug("time to get all tables " + (System.currentTimeMillis() - start) + " ms");
        return allTables;
    }

    public boolean testConnection() throws SQLException {
        return ((RefreshableDataSource) this.getDataSource()).testConnection();

    }

    /**
     * Describes the given Table
     */
    public TableSchema getTableSchema(String schema, String table) {
        return KerberosUtil.runWithOrWithoutKerberos(() -> getDBSchemaParser().describeTable(schema, table), kerberosHiveConfiguration);
    }

    public QueryResult browse(String schema, String table, String where, Integer limit) throws DataAccessException {

        if (where == null) {
            where = "";
        }
        String query = "SELECT * from " + HiveUtils.quoteIdentifier(schema, table) + " " + where + " LIMIT " + limit;
        return browse(query);
    }


    public QueryResult browse(String query) throws DataAccessException {
        return query(query);

    }

    public QueryResult query(String query) throws DataAccessException {
        return KerberosUtil.runWithOrWithoutKerberos(() -> {
            //  Setting in order to query complex formats like parquet
            jdbcTemplate.execute("set hive.optimize.index.filter=false");
            return new QueryRunner(jdbcTemplate).query(query);
        }, kerberosHiveConfiguration);
    }

    /**
     * Executes a single SQL update operation (such as insert, update, or delete).
     *
     * @param query the SQL to execute
     * @return the number of rows affected
     * @throws DataAccessException if there is any problem
     */
    public int update(@Nonnull final String query) {
        return KerberosUtil.runWithOrWithoutKerberos(() -> jdbcTemplate.update(query), kerberosHiveConfiguration);
    }


    public boolean isTableAccessibleByImpersonatedUser(String table) {
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        String schemaName = table.substring(0, table.indexOf("."));
        try {
            UserHiveAccessCache cache = this.perUserAccessCache.get(currentUser);
            List<String> tables = cache.getTables(schemaName);
            return tables != null && tables.contains(table);
        } catch (ExecutionException e) {
            throw new RuntimeException(String.format("Failed to determine if Hive table %s is accessible by user %s", table, currentUser));
        }
    }

    public void refreshHiveAccessCacheForImpersonatedUser() {
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        this.perUserAccessCache.refresh(currentUser);
    }

}
