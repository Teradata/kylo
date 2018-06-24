package com.thinkbiganalytics.schema;

/*-
 * #%L
 * kylo-schema-discovery-rdbms
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics, a Teradata Company
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

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

public class DefaultJdbcTableTest {

    /**
     * Verify extracting table information from a result set.
     */
    @Test
    public void fromResultSet() throws Exception {
        final ResultSet rs = Mockito.mock(ResultSet.class);
        Mockito.when(rs.getString(JdbcConstants.TABLE_CAT)).thenReturn("finance");
        Mockito.when(rs.getString(JdbcConstants.TABLE_SCHEM)).thenReturn("dbo");
        Mockito.when(rs.getString(JdbcConstants.TABLE_NAME)).thenReturn("people");
        Mockito.when(rs.getString(JdbcConstants.TABLE_TYPE)).thenReturn("TABLE");
        Mockito.when(rs.getString(JdbcConstants.REMARKS)).thenReturn("Finance People");

        final DefaultJdbcTable table = DefaultJdbcTable.fromResultSet(rs, Mockito.mock(DatabaseMetaData.class));
        Assert.assertEquals("finance", table.getCatalog());
        Assert.assertEquals("dbo", table.getSchema());
        Assert.assertEquals("people", table.getName());
        Assert.assertEquals("finance.dbo.people", table.getQualifiedIdentifier());
        Assert.assertEquals("TABLE", table.getType());
        Assert.assertEquals("Finance People", table.getRemarks());

        final DatabaseMetaData metaData = Mockito.mock(DatabaseMetaData.class);
        Mockito.when(metaData.getCatalogSeparator()).thenReturn(".");
        Mockito.when(metaData.getIdentifierQuoteString()).thenReturn("\"");

        table.setMetaData(metaData);
        Assert.assertEquals("\"finance\".\"dbo\".\"people\"", table.getQualifiedIdentifier());
    }

    /**
     * Verify generating the qualified identifier for a table.
     */
    @Test
    public void getQualifiedIdentifier() {
        final DefaultJdbcTable table = new DefaultJdbcTable("toys.\"products\"", "TABLE");
        table.setCatalog("toys");
        Assert.assertEquals("toys.toys.\"products\"", table.getQualifiedIdentifier());

        table.setCatalogSeparator(",");
        Assert.assertEquals("toys,toys.\"products\"", table.getQualifiedIdentifier());

        table.setIdentifierQuoteString(" ");
        Assert.assertEquals("toys,toys.\"products\"", table.getQualifiedIdentifier());

        table.setIdentifierQuoteString("`");
        Assert.assertEquals("`toys`,`toys.\"products\"`", table.getQualifiedIdentifier());

        table.setIdentifierQuoteString("\"");
        Assert.assertEquals("\"toys\",\"toys.\"\"products\"\"\"", table.getQualifiedIdentifier());
    }
}
