package com.thinkbiganalytics.schema;

/*-
 * #%L
 * thinkbig-schema-discovery-rdbms
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

import com.thinkbiganalytics.discovery.schema.Field;
import com.thinkbiganalytics.discovery.schema.TableSchema;
import com.thinkbiganalytics.kerberos.KerberosTicketConfiguration;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

public class DBSchemaParserTest {

    /**
     * Verify describing a MySQL table.
     */
    @Test
    public void describeTableMySql() throws Exception {
        // Mock data source
        final DataSource dataSource = Mockito.mock(DataSource.class);

        final Connection connection = Mockito.mock(Connection.class);
        Mockito.when(dataSource.getConnection()).thenReturn(connection);

        final DatabaseMetaData dbMetaData = Mockito.mock(DatabaseMetaData.class);
        Mockito.when(dbMetaData.supportsCatalogsInIndexDefinitions()).thenReturn(true);
        Mockito.when(connection.getMetaData()).thenReturn(dbMetaData);

        final ResultSet tablesResultSet = Mockito.mock(ResultSet.class);
        Mockito.when(tablesResultSet.next()).thenReturn(true);
        Mockito.when(tablesResultSet.getString(1)).thenReturn("mydb");
        Mockito.when(tablesResultSet.getString(3)).thenReturn("mytable");
        Mockito.when(dbMetaData.getTables(Mockito.eq("mydb"), Mockito.eq("%"), Mockito.eq("mytable"), Mockito.any(String[].class))).thenReturn(tablesResultSet);

        final ResultSet keysResultSet = Mockito.mock(ResultSet.class);
        Mockito.when(keysResultSet.next()).thenReturn(true, false);
        Mockito.when(keysResultSet.getString("COLUMN_NAME")).thenReturn("id");
        Mockito.when(keysResultSet.getString("TABLE_CAT")).thenReturn("mydb");
        Mockito.when(dbMetaData.getPrimaryKeys(null, "mydb", "mytable")).thenReturn(keysResultSet);

        final ResultSet columnsResultSet = Mockito.mock(ResultSet.class);
        Mockito.when(columnsResultSet.next()).thenReturn(true, true, false);
        Mockito.when(columnsResultSet.getString("TABLE_CAT")).thenReturn("mydb");
        Mockito.when(columnsResultSet.getString("COLUMN_NAME")).thenReturn("id", "mycol");
        Mockito.when(columnsResultSet.getInt("DATA_TYPE")).thenReturn(-5, 12);
        Mockito.when(columnsResultSet.getString("REMARKS")).thenReturn("primary key", "string column");
        Mockito.when(columnsResultSet.getString("IS_NULLABLE")).thenReturn("NO", "YES");
        Mockito.when(dbMetaData.getColumns("mydb", null, "mytable", null)).thenReturn(columnsResultSet);

        // Test describing table
        final DBSchemaParser parser = new DBSchemaParser(dataSource, new KerberosTicketConfiguration());
        final TableSchema schema = parser.describeTable("mydb", "mytable");
        Assert.assertNotNull("Expected schema but table was not found", schema);
        Assert.assertEquals("mytable", schema.getName());
        Assert.assertEquals("mydb", schema.getSchemaName());

        final List<Field> fields = schema.getFields();
        Assert.assertNotNull("Expected schema to have fields", fields);
        Assert.assertTrue("Expected 2 fields but found 0", fields.size() >= 1);
        Assert.assertEquals("id", fields.get(0).getName());
        Assert.assertEquals("BIGINT", fields.get(0).getNativeDataType());
        Assert.assertEquals("bigint", fields.get(0).getDerivedDataType());
        Assert.assertEquals("primary key", fields.get(0).getDescription());
        Assert.assertFalse("Expected id field to be non-nullable", fields.get(0).isNullable());
        Assert.assertTrue("Expected id field to be primary key", fields.get(0).isPrimaryKey());
        Assert.assertTrue("Expected 2 fields but found 1", fields.size() >= 2);
        Assert.assertEquals("mycol", fields.get(1).getName());
        Assert.assertEquals("VARCHAR", fields.get(1).getNativeDataType());
        Assert.assertEquals("string", fields.get(1).getDerivedDataType());
        Assert.assertEquals("string column", fields.get(1).getDescription());
        Assert.assertTrue("Expected id field to be nullable", fields.get(1).isNullable());
        Assert.assertFalse("Expected id field to be regular", fields.get(1).isPrimaryKey());
        Assert.assertEquals(2, fields.size());
    }

    /**
     * Verify describing a SQL Server table.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void describeTableSqlServer() throws Exception {
        // Mock data source
        final DataSource dataSource = Mockito.mock(DataSource.class);

        final Connection connection = Mockito.mock(Connection.class);
        Mockito.when(dataSource.getConnection()).thenReturn(connection);

        final DatabaseMetaData dbMetaData = Mockito.mock(DatabaseMetaData.class);
        Mockito.when(dbMetaData.getTables(Mockito.eq("mydb"), Mockito.eq("%"), Mockito.eq("mytable"), Mockito.any(String[].class))).thenThrow(SQLException.class);
        Mockito.when(dbMetaData.supportsCatalogsInIndexDefinitions()).thenReturn(true);
        Mockito.when(connection.getMetaData()).thenReturn(dbMetaData);

        final ResultSet tablesResultSet = Mockito.mock(ResultSet.class);
        Mockito.when(tablesResultSet.next()).thenReturn(true);
        Mockito.when(tablesResultSet.getString(1)).thenReturn("mycat");
        Mockito.when(tablesResultSet.getString(2)).thenReturn("mydb");
        Mockito.when(tablesResultSet.getString(3)).thenReturn("mytable");
        Mockito.when(dbMetaData.getTables(Mockito.isNull(String.class), Mockito.eq("mydb"), Mockito.eq("mytable"), Mockito.any(String[].class))).thenReturn(tablesResultSet);

        final ResultSet keysResultSet = Mockito.mock(ResultSet.class);
        Mockito.when(keysResultSet.next()).thenReturn(false);
        Mockito.when(dbMetaData.getPrimaryKeys(null, "mydb", "mytable")).thenReturn(keysResultSet);

        final ResultSet columnsResultSet = Mockito.mock(ResultSet.class);
        Mockito.when(columnsResultSet.next()).thenReturn(true, false);
        Mockito.when(columnsResultSet.getString("TABLE_CAT")).thenReturn("mydb");
        Mockito.when(columnsResultSet.getString("COLUMN_NAME")).thenReturn("mycol");
        Mockito.when(columnsResultSet.getInt("DATA_TYPE")).thenReturn(12);
        Mockito.when(columnsResultSet.getString("REMARKS")).thenReturn("string column");
        Mockito.when(columnsResultSet.getString("IS_NULLABLE")).thenReturn("YES");
        Mockito.when(dbMetaData.getColumns("mycat", "mydb", "mytable", null)).thenReturn(columnsResultSet);

        // Test describing table
        final DBSchemaParser parser = new DBSchemaParser(dataSource, new KerberosTicketConfiguration());
        final TableSchema schema = parser.describeTable("mydb", "mytable");
        Assert.assertNotNull("Expected schema but table was not found", schema);
        Assert.assertEquals("mytable", schema.getName());
        Assert.assertEquals("mydb", schema.getSchemaName());

        final List<Field> fields = schema.getFields();
        Assert.assertNotNull("Expected schema to have fields", fields);
        Assert.assertEquals(1, fields.size());
        Assert.assertEquals("mycol", fields.get(0).getName());
        Assert.assertEquals("VARCHAR", fields.get(0).getNativeDataType());
        Assert.assertEquals("string", fields.get(0).getDerivedDataType());
        Assert.assertEquals("string column", fields.get(0).getDescription());
        Assert.assertTrue("Expected id field to be nullable", fields.get(0).isNullable());
        Assert.assertFalse("Expected id field to be regular", fields.get(0).isPrimaryKey());

    }
}
