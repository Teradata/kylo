package com.thinkbiganalytics.util;

/*-
 * #%L
 * thinkbig-nifi-core-processors
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

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ColumnSpecTest {

    /**
     * Verify the DDL for creating the column.
     */
    @Test
    public void toCreateSQL() {
        final ColumnSpec[] specs = ColumnSpec.createFromString("col1|string");
        Assert.assertNotNull(specs);
        Assert.assertEquals("`col1` string", specs[0].toCreateSQL());
    }

    /**
     * Verify the DDL for creating the partition.
     */
    @Test
    public void toPartitionSQL() {
        final ColumnSpec[] specs = ColumnSpec.createFromString("col1|string");
        Assert.assertNotNull(specs);
        Assert.assertEquals("`col1` string", specs[0].toPartitionSQL());
    }

    /**
     * Verify the join query.
     */
    @Test
    public void toPrimaryKeyJoinSQL() {
        final ColumnSpec[] specs = ColumnSpec.createFromString("col1|string|pk1|1|0|0\ncol2|int\ncol3|int|pk2|1|0|0");
        Assert.assertNotNull(specs);
        Assert.assertEquals("`a`.`col1` = `b`.`col1` AND `a`.`col3` = `b`.`col3`", ColumnSpec.toPrimaryKeyJoinSQL(specs, "a", "b"));
    }

    /**
     * Verify the primary key identifiers.
     */
    @Test
    public void toPrimaryKeys() {
        final ColumnSpec[] specs = ColumnSpec.createFromString("col1|string|pk1|1|0|0\ncol2|int\ncol3|int|pk2|1|0|0");
        Assert.assertNotNull(specs);
        Assert.assertArrayEquals(new String[]{"`col1`", "`col3`"}, ColumnSpec.toPrimaryKeys(specs));
    }

    @Test
    public void testFromString() throws IOException {
        ColumnSpec[] specs = ColumnSpec.createFromString("col1|string|my comment\ncol2|int\ncol3|string|foo description\ncol4|string");
        assertTrue("Expecting 4 parts", specs.length == 4);
        assertTrue(specs[0].getComment().equals("my comment"));

        ColumnSpec[] specs2 = ColumnSpec.createFromString("col1|string|my comment|1|0|0\ncol2|int\ncol3|string|foo description\ncol4|string||BAD|1|1");
        assertTrue("Expecting 4 parts", specs.length == 4);
        assertTrue(specs2[0].isPk());
        assertFalse(specs2[0].isCreateDt());
        assertFalse(specs2[0].isModifiedDt());

        assertFalse(specs2[1].isPk());
        assertFalse(specs2[1].isCreateDt());
        assertFalse(specs2[1].isModifiedDt());

        assertFalse(specs2[2].isPk());
        assertFalse(specs2[2].isCreateDt());
        assertFalse(specs2[2].isModifiedDt());

        assertFalse(specs2[3].isPk());
        assertTrue(specs2[3].isCreateDt());
        assertTrue(specs2[3].isModifiedDt());
    }


    @Test
    public void testSpecs() throws IOException {
        ColumnSpec[]
            specs =
            ColumnSpec.createFromString("col1|string|my comment\ncol2|int\ncol3|string|foo description\ncol4|string");

        StringBuffer sb = new StringBuffer();
        int i = specs.length;
        for (ColumnSpec spec : specs) {
            sb.append(spec.toCreateSQL());
            if (i-- > 1) {
                sb.append(", ");
            }
        }
        System.out.println(sb.toString());
        assertNotNull(sb.toString());
    }

    @Test
    public void testPartitionKeyAndQuery() {
        PartitionKey key1 = new PartitionKey("country", "string", "country");
        PartitionKey key2 = new PartitionKey("year", "int", "year(hired_date)");
        PartitionKey key3 = new PartitionKey("reg date month", "int", "month(reg date)");
        PartitionKey key4 = new PartitionKey("reg date day", "int", "day(`reg date`)");

        PartitionSpec spec = new PartitionSpec(key1, key2, key3, key4);

        String sql = spec.toDistinctSelectSQL("sourceSchema", "sourceTable", "11111111");
        System.out.println(sql);
        String expected = "select `country`, year(`hired_date`), month(`reg date`), day(`reg date`), count(0) as `tb_cnt` from `sourceSchema`.`sourceTable` where `processing_dttm` = \"11111111\" "
                          + "group by `country`, year(`hired_date`), month(`reg date`), day(`reg date`)";
        assertEquals(expected, sql);

        assertEquals("`country`", key1.getKeyForSql());
        assertEquals("`year`", key2.getKeyForSql());
        assertEquals("`reg date month`", key3.getKeyForSql());
        assertEquals("`reg date day`", key4.getKeyForSql());

    }
}
