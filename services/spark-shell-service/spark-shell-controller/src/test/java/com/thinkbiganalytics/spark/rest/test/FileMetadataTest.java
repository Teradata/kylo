package com.thinkbiganalytics.spark.rest.test;

/*-
 * #%L
 * kylo-spark-shell-controller
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


import com.google.common.util.concurrent.Uninterruptibles;
import com.thinkbiganalytics.discovery.model.DefaultQueryResultColumn;
import com.thinkbiganalytics.discovery.schema.QueryResultColumn;
import com.thinkbiganalytics.spark.rest.filemetadata.FileMetadataTransformResponseModifier;
import com.thinkbiganalytics.spark.rest.filemetadata.tasks.FileMetadataTaskService;
import com.thinkbiganalytics.spark.rest.filemetadata.tasks.FileMetadataCompletionTask;
import com.thinkbiganalytics.spark.rest.controller.SparkShellUserProcessService;
import com.thinkbiganalytics.spark.rest.model.FileMetadataResponse;
import com.thinkbiganalytics.spark.rest.model.ModifiedTransformResponse;
import com.thinkbiganalytics.spark.rest.model.TransformQueryResult;
import com.thinkbiganalytics.spark.rest.model.TransformRequest;
import com.thinkbiganalytics.spark.rest.model.TransformResponse;
import com.thinkbiganalytics.spark.shell.SparkShellProcess;
import com.thinkbiganalytics.spark.shell.SparkShellRestClient;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nonnull;

public class FileMetadataTest {


    private static final Logger log = LoggerFactory.getLogger(FileMetadataTest.class);

    @Mock
    private FileMetadataTaskService trackerService;

    @Mock
    private SparkShellUserProcessService sparkShellUserProcessService;

    @Mock
    private SparkShellProcess sparkShellProcess;

    @Mock
    private SparkShellRestClient restClient;

    private Map<String,AtomicInteger> requestTracker = new HashMap<>();
    private boolean isComplete = false;



    private void setup(){
        this.trackerService = Mockito.spy(new FileMetadataTaskService());
        this.sparkShellUserProcessService = new MockSparkShellUserProcessService("tester");
        trackerService.setSparkShellUserProcessService(this.sparkShellUserProcessService);
        restClient = Mockito.mock(SparkShellRestClient.class);
        trackerService.setRestClient(restClient);


        Mockito.doReturn("tester").when(this.trackerService).getUsername();


       // Mockito.doCallRealMethod().when(this.trackerService).runScript(Mockito.any(ModifiedTransformResponse.class),Mockito.any());

        //Mockito.doCallRealMethod().when(this.trackerService).get(Mockito.anyString());

        //Mockito.doCallRealMethod().when(this.trackerService).removeFromCache(Mockito.anyString());

        Mockito.when(restClient.transform(Mockito.any(),  Mockito.any())).then(new Answer<Object>() {

            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                TransformRequest r = invocationOnMock.getArgumentAt(1,TransformRequest.class);
                TransformResponse response = new TransformResponse();
                response.setStatus(TransformResponse.Status.PENDING);
                response.setTable(UUID.randomUUID().toString());
                requestTracker.put(response.getTable(),new AtomicInteger(3));
                return response;
            }
        });

        Mockito.when(restClient.getTransformResult(Mockito.any(),  Mockito.any())).then(new Answer<Object>() {

            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                String tableId = invocationOnMock.getArgumentAt(1,String.class);
                AtomicInteger i = requestTracker.get(tableId);
                if(i != null){
                    int i1 = i.decrementAndGet();
                    if(i1 == 0){
                        TransformResponse response = new TransformResponse();
                        response.setStatus(TransformResponse.Status.SUCCESS);
                        response.setTable(tableId);
                        response.setResults(headerResult());
                        //add in the successful results
                       // requestTracker.remove(tableId);

                        return Optional.of(response);
                    }else {
                        //return pending
                        TransformResponse response = new TransformResponse();
                        response.setTable(tableId);
                        response.setStatus(TransformResponse.Status.PENDING);
                        return Optional.of(response);
                    }
                }

                FileMetadataCompletionTask finalResult = trackerService.get(tableId);
                    if(finalResult != null){
                        return Optional.of(finalResult.getModifiedTransformResponse());
                    }
                    else {
                        //return pending
                        TransformResponse response = new TransformResponse();
                        response.setStatus(TransformResponse.Status.PENDING);
                        response.setTable(tableId);
                        requestTracker.put(response.getTable(), new AtomicInteger(3));
                        return Optional.of(response);
                    }

        };

    });
    }

    public TransformQueryResult headerResult(){
        TransformQueryResult result = new TransformQueryResult();
        List<QueryResultColumn> columnList  = new ArrayList<>();

        columnList.add(newColumn("mimeType"));
        columnList.add(newColumn("resource"));
        columnList.add(newColumn("schema"));
        result.setColumns(columnList);
        List<List<Object>> rows = new ArrayList<>();
        Map<String,String>props = new HashMap<>();
        props.put("col1","string");
        props.put("col2","int");
        rows.add(newSchemaHeaderRow("application/parquet","file://0001.parquet",props));
        rows.add(newSchemaHeaderRow("application/parquet","file://0002.parquet",props));
        result.setRows(rows);
        return result;

    }


    public List<Object> newSchemaHeaderRow(String mimeType, String file,
                                          Map<String,String> properties){
        List<Object> row = new LinkedList<>();
        row.add(mimeType);
        row.add(file);
        row.add(properties);
        return row;
    }


    public QueryResultColumn newColumn(String field){
        QueryResultColumn column = new DefaultQueryResultColumn();
        column.setField(field);
        column.setHiveColumnLabel(field);
        return column;
    }

    public List<Object> row(String mimeType,String delimiter, Integer headerCount, String file, String encoding, Map<String,String> properties){
        List<Object> row = new LinkedList<>();
        row.add(mimeType);
        row.add(delimiter);
        row.add(headerCount);
        row.add(file);
        row.add(encoding);
        row.add(properties);
        return row;
    }

    public List<Object> newtRow(String mimeType,String file){
        return row(mimeType,"",0,file,"ISO",new HashMap<>());
    }


    @Test
    public void testChainedResult(){
        setup();

        TransformResponse initialResponse = new TransformResponse();
        initialResponse.setStatus(TransformResponse.Status.SUCCESS);
        TransformQueryResult result = new TransformQueryResult();
        List<QueryResultColumn> columnList  = new ArrayList<>();

        columnList.add(newColumn("mimeType"));
        columnList.add(newColumn("delimiter"));
        columnList.add(newColumn("headerCount"));
        columnList.add(newColumn("resource"));
        columnList.add(newColumn("encoding"));
        result.setColumns(columnList);

        List<List<Object>> rows = new ArrayList<>();
        rows.add(newtRow("application/parquet","file://my/parquet001.parquet"));
        rows.add(newtRow("application/parquet","file://my/parquet002.parquet"));
        rows.add(newtRow("application/parquet","file://my/parquet003.parquet"));
        rows.add(newtRow("application/avro","file://my/avro001.avro"));
        rows.add(newtRow("application/avro","file://my/avro002.avro"));
        rows.add(newtRow("text/csv","file://my/test001.csv"));
        rows.add(newtRow("text/csv","file://my/test002.csv"));
        result.setRows(rows);
        initialResponse.setResults(result);
        initialResponse.setTable(UUID.randomUUID().toString());

        FileMetadataTransformResponseModifier fileMetadataResult = new FileMetadataTransformResponseModifier(trackerService);
        ModifiedTransformResponse<FileMetadataResponse> m =fileMetadataResult.modify(initialResponse);
        FileMetadataResponse response = m.getResults();
        int retryCount = 0;
        long start = System.currentTimeMillis();
        boolean process = response == null;
        while(process){
            Uninterruptibles.sleepUninterruptibly(1000, TimeUnit.MILLISECONDS);
            response = m.getResults();
            if(response != null){
               process = false;
            }
            retryCount +=1;
            if(retryCount >40){
                process = false;
            }
        }
        long stop = System.currentTimeMillis();
        log.info("Time to get chained response {} ms, Retry Attempts: {}", (stop -start),retryCount);
        Assert.assertEquals(3,response.getDatasets().size());
        Assert.assertEquals(2, response.getDatasets().get("file://my/test001.csv").getFiles().size());
        Assert.assertEquals(3, response.getDatasets().get("file://my/parquet001.parquet").getFiles().size());
        Assert.assertEquals(2, response.getDatasets().get("file://my/avro001.avro").getFiles().size());




    }


    public class MockSparkShellUserProcessService extends SparkShellUserProcessService {

        private String username;
        public MockSparkShellUserProcessService(String username){
            this.username  = username;
        }

        @Nonnull
        @Override
        protected SparkShellProcess getSparkShellProcess() throws Exception {
            return sparkShellProcess;
        }

        @Override
        protected SparkShellProcess getSparkShellProcess(String username) throws Exception {
            return sparkShellProcess;
        }
    }

}
