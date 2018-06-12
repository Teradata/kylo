package com.thinkbiganalytics.kylo.catalog.spark.sources;

/*-
 * #%L
 * Kylo Catalog Core
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics
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

import com.thinkbiganalytics.kylo.catalog.api.KyloCatalogClient;
import com.thinkbiganalytics.kylo.catalog.api.KyloCatalogConstants;
import com.thinkbiganalytics.kylo.catalog.api.MissingOptionException;
import com.thinkbiganalytics.kylo.catalog.spi.DataSetOptions;

import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.DataFrameWriter;
import org.apache.spark.sql.SQLConf;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.catalyst.TableIdentifier;
import org.apache.spark.sql.catalyst.analysis.Catalog;
import org.apache.spark.sql.catalyst.analysis.UnresolvedRelation;
import org.apache.spark.sql.catalyst.plans.logical.InsertIntoTable;
import org.apache.spark.sql.catalyst.plans.logical.LogicalPlan;
import org.apache.spark.sql.execution.QueryExecution;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.annotation.Nonnull;

@SuppressWarnings("unchecked")
public class AbstractHiveDataSetProviderTest {

    /**
     * Spark SQL data frame
     */
    private DataFrame dataFrame;

    /**
     * Spark SQL context
     */
    private SQLContext sqlContext;

    /**
     * Spark SQL writer
     */
    private DataFrameWriter writer;

    /**
     * Set up tests.
     */
    @Before
    public void beforeEach() {
        sqlContext = Mockito.mock(SQLContext.class);
        Mockito.when(sqlContext.conf()).thenReturn(new SQLConf());
    }

    /**
     * Verify reading from a Hive table.
     */
    @Test
    public void read() {
        // Create options
        final DataSetOptions options = new DataSetOptions();
        options.addJar("http://example.com/serde.jar");
        options.setOption(KyloCatalogConstants.PATH_OPTION, "default.mytable");

        // Test reading
        dataFrame = Mockito.mock(DataFrame.class);
        final MockHiveDataSetProvider provider = new MockHiveDataSetProvider();

        final DataFrame result = provider.read(Mockito.mock(KyloCatalogClient.class), options);
        Assert.assertEquals(dataFrame, result);
        Mockito.verify(sqlContext).sql("ADD JAR http://example.com/serde.jar");
    }

    /**
     * Verify exception if path not defined.
     */
    @Test(expected = MissingOptionException.class)
    public void readWithoutPath() {
        final MockHiveDataSetProvider provider = new MockHiveDataSetProvider();
        provider.read(Mockito.mock(KyloCatalogClient.class), new DataSetOptions());
    }

    /**
     * Verify writing to a Hive table.
     */
    @Test
    public void write() {
        // Mock writer
        writer = createDataFrameWriter(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                final InsertIntoTable plan = invocation.getArgumentAt(0, InsertIntoTable.class);
                final UnresolvedRelation relation = (UnresolvedRelation) plan.table();
                Assert.assertEquals("default", relation.tableIdentifier().database().get());
                Assert.assertEquals("mytable", relation.tableIdentifier().table());
                return null;
            }
        });

        // Create options
        final DataSetOptions options = new DataSetOptions();
        options.addJar("http://example.com/serde.jar");
        options.setOption(KyloCatalogConstants.PATH_OPTION, "default.mytable");

        // Test writing
        final MockHiveDataSetProvider provider = new MockHiveDataSetProvider();
        provider.write(Mockito.mock(KyloCatalogClient.class), options, Mockito.mock(DataFrame.class));
        Mockito.verify(sqlContext).sql("ADD JAR http://example.com/serde.jar");
        Mockito.verify(sqlContext).executePlan(Mockito.any(LogicalPlan.class));
    }

    /**
     * Verify exception when missing the table name.
     */
    @Test(expected = MissingOptionException.class)
    public void writeWithoutPath() {
        final MockHiveDataSetProvider provider = new MockHiveDataSetProvider();
        provider.write(Mockito.mock(KyloCatalogClient.class), new DataSetOptions(), Mockito.mock(DataFrame.class));
    }

    /**
     * Creates a {@code DataFrameWriter} that creates tables using the specified answer.
     */
    @Nonnull
    private DataFrameWriter createDataFrameWriter(@Nonnull final Answer<Void> executePlanAnswer) {
        final DataFrame df = Mockito.mock(DataFrame.class);
        Mockito.when(df.sqlContext()).thenReturn(sqlContext);

        final Catalog catalog = Mockito.mock(Catalog.class);
        Mockito.when(catalog.tableExists(Mockito.any(TableIdentifier.class))).thenReturn(false);
        Mockito.when(sqlContext.catalog()).thenReturn(catalog);

        final QueryExecution queryExecution = Mockito.mock(QueryExecution.class);
        Mockito.when(sqlContext.executePlan(Mockito.any(LogicalPlan.class))).then(new Answer<QueryExecution>() {
            @Override
            public QueryExecution answer(InvocationOnMock invocation) throws Throwable {
                executePlanAnswer.answer(invocation);
                return queryExecution;
            }
        });

        return new DataFrameWriter(df);
    }

    /**
     * Mock implementation of {@code AbstractHiveDataSetProvider}.
     */
    private class MockHiveDataSetProvider extends AbstractHiveDataSetProvider<DataFrame> {

        @Nonnull
        @Override
        protected DataFrameWriter getDataFrameWriter(@Nonnull final DataFrame dataSet, @Nonnull final DataSetOptions options) {
            return writer;
        }

        @Nonnull
        @Override
        protected DataFrame loadFromTable(@Nonnull final KyloCatalogClient<DataFrame> client, @Nonnull final String tableName) {
            return dataFrame;
        }

        @Override
        protected void sql(@Nonnull final KyloCatalogClient<DataFrame> client, @Nonnull final String query) {
            sqlContext.sql(query);
        }
    }
}
