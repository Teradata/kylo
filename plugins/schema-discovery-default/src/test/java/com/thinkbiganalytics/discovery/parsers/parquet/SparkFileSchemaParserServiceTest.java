package com.thinkbiganalytics.discovery.parsers.parquet;

import com.thinkbiganalytics.discovery.model.DefaultQueryResult;
import com.thinkbiganalytics.discovery.model.DefaultQueryResultColumn;
import com.thinkbiganalytics.discovery.parsers.hadoop.SparkFileSchemaParserService;
import com.thinkbiganalytics.discovery.schema.Field;
import com.thinkbiganalytics.discovery.schema.QueryResult;
import com.thinkbiganalytics.discovery.schema.QueryResultColumn;
import com.thinkbiganalytics.discovery.schema.Schema;
import com.thinkbiganalytics.discovery.util.TableSchemaType;
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
        columns.add(newColumn("decimalColumn", "decimal(10,2)"));
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
        QueryResult result = new DefaultQueryResult("query");
        result.setColumns(columns);
        transformResponse.setResults(result);
        return transformResponse;
    }

    private Schema parseQueryResult(List<QueryResultColumn> columns, SparkFileSchemaParserService.SparkFileType sparkFileType, TableSchemaType tableSchemaType) throws Exception {
        final SparkShellRestClient restClient = Mockito.mock(SparkShellRestClient.class);
        final SparkShellProcessManager sparkShellProcessManager = Mockito.mock(SparkShellProcessManager.class);

        SparkFileSchemaParserService service = Mockito.mock(SparkFileSchemaParserService.class);
        Whitebox.setInternalState(service, "shellProcessManager", sparkShellProcessManager);
        Whitebox.setInternalState(service, "restClient", restClient);

        Mockito.when(service.doParse(Mockito.any(InputStream.class), Mockito.any(), Mockito.any())).thenCallRealMethod();
        Mockito.when(sparkShellProcessManager.getSystemProcess()).thenReturn(null);
        Mockito.when(restClient.transform(Mockito.any(), Mockito.any())).thenReturn(transformResponse(columns));
        byte[] b = new byte[]{};
        InputStream inputStream = new ByteArrayInputStream(b);

        Schema schema = service.doParse(inputStream, sparkFileType, tableSchemaType);
        return schema;
    }

    /**
     * Test to ensure the column types that have precision,scale get parsed correctly to the field.precisionScale property
     */
    @Test
    public void testDecimalParsing() {
        try {
            Schema decimalSchema = parseQueryResult(decimalColumns(), SparkFileSchemaParserService.SparkFileType.PARQUET, TableSchemaType.HIVE);
            assertNotNull(decimalSchema);
            Field decimalField = decimalSchema.getFields().stream().filter(field -> field.getName().equalsIgnoreCase("decimalColumn")).findFirst().orElse(null);
            assertNotNull(decimalField);
            assertEquals("decimal", decimalField.getDerivedDataType());
            assertEquals("10,2", decimalField.getPrecisionScale());
            assertEquals("decimal(10,2)", decimalField.getDataTypeWithPrecisionAndScale());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Test to ensure standard columns without precision work
     */
    @Test
    public void testParsing() {
        try {
            Schema schema = parseQueryResult(nonDecimalColumns(), SparkFileSchemaParserService.SparkFileType.AVRO, TableSchemaType.HIVE);
            assertNotNull(schema);
            schema.getFields().stream().forEach(field -> {
                assertNotNull(field);
                assertTrue(StringUtils.isBlank(field.getPrecisionScale()));
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
