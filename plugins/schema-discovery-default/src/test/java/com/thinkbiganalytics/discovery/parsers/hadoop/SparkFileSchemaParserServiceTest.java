package com.thinkbiganalytics.discovery.parsers.hadoop;

/*-
 * #%L
 * thinkbig-schema-discovery-default
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

import com.thinkbiganalytics.discovery.model.DefaultQueryResultColumn;
import com.thinkbiganalytics.discovery.parser.SampleFileSparkScript;
import com.thinkbiganalytics.discovery.schema.Field;
import com.thinkbiganalytics.discovery.schema.QueryResultColumn;
import com.thinkbiganalytics.discovery.schema.Schema;
import com.thinkbiganalytics.discovery.util.TableSchemaType;
import com.thinkbiganalytics.spark.rest.model.TransformQueryResult;
import com.thinkbiganalytics.spark.rest.model.TransformResponse;
import com.thinkbiganalytics.spark.shell.SparkShellProcessManager;
import com.thinkbiganalytics.spark.shell.SparkShellRestClient;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.Whitebox;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test Parsing of schema from the SparkSchemaParser Service
 */
public class SparkFileSchemaParserServiceTest {


    private List<QueryResultColumn> decimalColumns() {
        List<QueryResultColumn> columns = new ArrayList<>();
        columns.add(newColumn("decimalColumn", "decimal(17,12)"));
        columns.add(newColumn("stringColumn", "string"));
        return columns;
    }

    private List<QueryResultColumn> nonDecimalColumns() {
        List<QueryResultColumn> columns = new ArrayList<>();
        columns.add(newColumn("intColumn", "int"));
        columns.add(newColumn("stringColumn", "string"));
        return columns;
    }

    private QueryResultColumn newColumn(String name, String dataType) {
        QueryResultColumn column = new DefaultQueryResultColumn();
        column.setField(name);
        column.setDisplayName(name);
        column.setTableName("table");
        column.setDataType(dataType);
        column.setDatabaseName("database");
        return column;
    }

    private TransformResponse transformResponse(List<QueryResultColumn> columns) {
        TransformResponse transformResponse = new TransformResponse();
        transformResponse.setStatus(TransformResponse.Status.SUCCESS);
        TransformQueryResult result = new TransformQueryResult();
        result.setColumns(columns);
        transformResponse.setResults(result);
        return transformResponse;
    }

    private Schema parseQueryResult(List<QueryResultColumn> columns, SparkFileType sparkFileType, TableSchemaType tableSchemaType) throws Exception {
        final SparkShellRestClient restClient = Mockito.mock(SparkShellRestClient.class);
        final SparkShellProcessManager sparkShellProcessManager = Mockito.mock(SparkShellProcessManager.class);

        SparkFileSchemaParserService service = Mockito.mock(SparkFileSchemaParserService.class);
        Whitebox.setInternalState(service, "shellProcessManager", sparkShellProcessManager);
        Whitebox.setInternalState(service, "restClient", restClient);

        Mockito.when(service.doParse(Mockito.any(InputStream.class), Mockito.any(), Mockito.any(), Mockito.any())).thenCallRealMethod();
        Mockito.when(sparkShellProcessManager.getSystemProcess()).thenReturn(null);
        Mockito.when(restClient.transform(Mockito.any(), Mockito.any())).thenReturn(transformResponse(columns));
        byte[] b = new byte[]{};
        InputStream inputStream = new ByteArrayInputStream(b);

        Schema schema = service.doParse(inputStream, sparkFileType, tableSchemaType, new DefaultSparkCommandBuilder(sparkFileType.name()));
        return schema;
    }

    /**
     * Test to ensure the column types that have precision,scale get parsed correctly to the field.precisionScale property
     */
    @org.junit.Test
    public void testDecimalParsing() {
        try {
            Schema decimalSchema = parseQueryResult(decimalColumns(), SparkFileType.PARQUET, TableSchemaType.HIVE);
            assertNotNull(decimalSchema);
            Field decimalField = decimalSchema.getFields().stream().filter(field -> field.getName().equalsIgnoreCase("decimalColumn")).findFirst().orElse(null);
            assertNotNull(decimalField);
            assertEquals("decimal", decimalField.getDerivedDataType());
            assertEquals("17,12", decimalField.getPrecisionScale());
            assertEquals("decimal(17,12)", decimalField.getDataTypeWithPrecisionAndScale());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Test to ensure standard columns without precision work
     */
    @org.junit.Test
    public void testParsing() {
        try {
            Schema schema = parseQueryResult(nonDecimalColumns(), SparkFileType.AVRO, TableSchemaType.HIVE);
            assertNotNull(schema);
            schema.getFields().stream().forEach(field -> {
                assertNotNull(field);
                assertTrue(StringUtils.isBlank(field.getPrecisionScale()));
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private AbstractSparkFileSchemaParser setup(AbstractSparkFileSchemaParser schemaParser) throws Exception {
        final SparkShellRestClient restClient = Mockito.mock(SparkShellRestClient.class);
        final SparkShellProcessManager sparkShellProcessManager = Mockito.mock(SparkShellProcessManager.class);

        SparkFileSchemaParserService service = Mockito.mock(SparkFileSchemaParserService.class);
        Whitebox.setInternalState(service, "shellProcessManager", sparkShellProcessManager);
        Whitebox.setInternalState(service, "restClient", restClient);

        Mockito.when(service.getSparkScript(Mockito.any(List.class), Mockito.any())).thenCallRealMethod();
        Mockito.when(service.doParse(Mockito.any(InputStream.class), Mockito.any(), Mockito.any(), Mockito.any())).thenCallRealMethod();
        Mockito.when(schemaParser.getSparkParserService()).thenReturn(service);
     //   Mockito.when(schemaParser.getSparkScript(Mockito.anyList())).thenCallRealMethod();
     //   Mockito.when(schemaParser.getSparkCommandBuilder()).thenCallRealMethod();
         return schemaParser;
    }

    @org.junit.Test
    public void testScript(){
        //String  file1 ="file:///opt/kylo/setup/data/sample-data/avro/userdata1.avro";
       // String file2 = "file:///opt/kylo/setup/data/sample-data/avro/userdata1.avro";

        String  file1 ="file:///opt/kylo/setup/data/sample-data/csv/userdata1.csv";
        String file2 = "file:///opt/kylo/setup/data/sample-data/csv/userdata2.csv";
        String file3 = "file:///opt/kylo/setup/data/sample-data/csv/userdata3.csv";
        List<String>files = new ArrayList<>();
        files.add(file1);
        files.add(file2);
        files.add(file3);

        AvroFileSchemaParser avroFileSchemaParser = Mockito.spy(AvroFileSchemaParser.class);
        avroFileSchemaParser.setDataFrameVariable("df");

        OrcFileSchemaParser orcFileSchemaParser = Mockito.spy(OrcFileSchemaParser.class);
        orcFileSchemaParser.setLimit(-1);
        XMLFileSchemaParser xmlFileSchemaParser = Mockito.spy(XMLFileSchemaParser.class);
        xmlFileSchemaParser.setRowTag("row1");
        SparkCSVFileSchemaParser csvFileSchemaParser = Mockito.spy(SparkCSVFileSchemaParser.class);
        csvFileSchemaParser.setDataFrameVariable("df");
      //  orcFileSchemaParser.setDataFrameVariable("df");
try {
    SampleFileSparkScript sparkScript = setup(csvFileSchemaParser).getSparkScript(files);
    System.out.println(sparkScript.getScript());
}catch (Exception e){
e.printStackTrace();
}

    }

}
