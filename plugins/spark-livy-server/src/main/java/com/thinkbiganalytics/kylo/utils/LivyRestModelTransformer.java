package com.thinkbiganalytics.kylo.utils;

/*-
 * #%L
 * kylo-spark-livy-server
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


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.thinkbiganalytics.discovery.model.DefaultQueryResultColumn;
import com.thinkbiganalytics.discovery.schema.QueryResultColumn;
import com.thinkbiganalytics.json.ObjectMapperSerializer;
import com.thinkbiganalytics.kylo.spark.client.model.LivyServer;
import com.thinkbiganalytics.kylo.spark.client.model.enums.LivyServerStatus;
import com.thinkbiganalytics.kylo.spark.client.model.enums.LivySessionStatus;
import com.thinkbiganalytics.kylo.spark.exceptions.LivyCodeException;
import com.thinkbiganalytics.kylo.spark.exceptions.LivyDeserializationException;
import com.thinkbiganalytics.kylo.spark.exceptions.LivyUserException;
import com.thinkbiganalytics.kylo.spark.livy.SparkLivySaveException;
import com.thinkbiganalytics.kylo.spark.model.Statement;
import com.thinkbiganalytics.kylo.spark.model.StatementOutputResponse;
import com.thinkbiganalytics.kylo.spark.model.enums.SessionState;
import com.thinkbiganalytics.kylo.spark.model.enums.StatementOutputStatus;
import com.thinkbiganalytics.kylo.spark.model.enums.StatementState;
import com.thinkbiganalytics.kylo.spark.rest.model.job.SparkJobResponse;
import com.thinkbiganalytics.kylo.spark.rest.model.job.SparkJobResult;
import com.thinkbiganalytics.spark.dataprofiler.model.MetricType;
import com.thinkbiganalytics.spark.dataprofiler.output.OutputRow;
import com.thinkbiganalytics.spark.rest.model.DataSources;
import com.thinkbiganalytics.spark.rest.model.SaveResponse;
import com.thinkbiganalytics.spark.rest.model.ServerStatusResponse;
import com.thinkbiganalytics.spark.rest.model.TransformQueryResult;
import com.thinkbiganalytics.spark.rest.model.TransformResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.ws.rs.WebApplicationException;

public class LivyRestModelTransformer {

    private static final XLogger logger = XLoggerFactory.getXLogger(LivyRestModelTransformer.class);

    private static final ObjectMapper mapper = new ObjectMapper();

    // time based expiry to clear out ids not cleared because the UI failed to fetch results
    //    built here as a static cache; then Spring creates a bean of it to be injected in other places
    //    TODO: turn this class into a spring service
    public static Cache<String, Integer> statementIdCache = CacheBuilder.newBuilder()
        .expireAfterAccess(24, TimeUnit.HOURS)
        .maximumSize(100)
        .build();

    /**
     * Instances of {@code LivyRestModelTransformer} should not be constructed.
     *
     * @throws UnsupportedOperationException always
     */
    private LivyRestModelTransformer() {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    public static SparkJobResponse toJobResponse(@Nonnull final String id, @Nonnull final Statement statement) {
        final SparkJobResponse response = new SparkJobResponse();
        response.setId(id);
        response.setStatus(StatementStateTranslator.translate(statement.getState()));

        if (response.getStatus() == TransformResponse.Status.SUCCESS) {
            final JsonNode data = statement.getOutput().getData();
            if (data != null) {
                final String json = data.get("application/json").asText();
                response.setResult(ObjectMapperSerializer.deserialize(json, SparkJobResult.class));
            }
        }

        return response;
    }

    public static TransformResponse toTransformResponse(Statement statement, String transformId) {
        TransformResponse transformResponse = prepTransformResponse(statement, transformId);

        if (transformResponse.getStatus() == TransformResponse.Status.SUCCESS) {
            String code = statement.getCode().trim();
            if (code.endsWith("dfResultsAsJson")) {
                transformResponse.setResults(toTransformQueryResultWithSchema(transformResponse, statement.getOutput()));
            } else if (code.endsWith("dfProf")) {
                List<OutputRow> rows = toTransformResponseProfileStats(statement.getOutput());
                transformResponse.setProfile(toTransformResponseProfileStats(statement.getOutput()));
                transformResponse.setActualCols(1);
                Integer actualRows = rows.stream()
                    .filter(metric -> metric.getMetricType().equals(MetricType.TOTAL_COUNT.toString()))
                    .map(metric -> Integer.valueOf(metric.getMetricValue()))
                    .findFirst().orElse(1);
                transformResponse.setActualRows(actualRows);
                transformResponse.setResults(emptyResult());
            } else if (code.endsWith("transformAsStr")) {
                /* expects that 'statement' contains a payload of TransformResponse in JSON format */
                TransformResponse tr = serializeStatementOutputResponse(checkCodeWasWellFormed(statement.getOutput()), TransformResponse.class);
                statementIdCache.put(tr.getTable(), statement.getId());
                return tr;
            } else {
                logger.error("Exception Processing Result: ", new LivyCodeException("Unsupported result type requested of Livy.  Results not recognized"));
                throw new LivyUserException("livy.unsupported_result_type");
            } // end if
        }

        return transformResponse;
    }


    private static TransformQueryResult toTransformQueryResultWithSchema(TransformResponse transformResponse, StatementOutputResponse sor) {
        logger.entry(sor);
        checkCodeWasWellFormed(sor);

        TransformQueryResult tqr = new TransformQueryResult();
        transformResponse.setResults(tqr);

        tqr.setColumns(Lists.newArrayList());

        JsonNode data = sor.getData();
        if (data != null) {
            JsonNode appJson = data.get("application/json");
            String payload = appJson.asText();

            ArrayNode json;
            try {
                json = (ArrayNode) mapper.readTree(payload);
            } catch (IOException e) {
                logger.error("An unexpected IOException occurred", new LivyDeserializationException("could not deserialize JSON returned from Livy"));
                throw logger.throwing(new LivyUserException("livy.unexpected_error"));
            } // end try/catch

            // array contains three objects (dfRows, actualCols, actualRows )
            transformResponse.setActualCols(json.get(1).asInt());
            transformResponse.setActualRows(json.get(2).asInt());
            json = (ArrayNode) json.get(0);

            int numRows = 0;

            Iterator<JsonNode> rowIter = json.elements();
            List<List<Object>> rowData = Lists.newArrayList();
            while (rowIter.hasNext()) {
                JsonNode row = rowIter.next();
                if (numRows++ == 0) {
                    String schemaPayload = row.asText();

                    ObjectNode schemaObj;
                    try {
                        schemaObj = (ObjectNode) mapper.readTree(schemaPayload);
                    } catch (IOException e) {
                        logger.error("Unexpected error deserializing results", new LivyDeserializationException("Unable to deserialize dataFrame schema as serialized by Livy"));
                        throw logger.throwing(new LivyUserException("livy.unexpected_error"));
                    } // end try/catch

                    //  build column metadata
                    logger.debug("build column metadata");
                    String type = schemaObj.get("type").asText();
                    if (type.equals("struct")) {
                        ArrayNode fields = (ArrayNode) schemaObj.get("fields");

                        Iterator<JsonNode> colObjsIter = fields.elements();

                        int colIdx = 0;
                        while (colObjsIter.hasNext()) {
                            ObjectNode colObj = (ObjectNode) colObjsIter.next();
                            final JsonNode dataType = colObj.get("type");
                            JsonNode metadata = colObj.get("metadata");
                            String name = colObj.get("name").asText();
                            String nullable = colObj.get("nullable").asText();  // "true"|"false"

                            QueryResultColumn qrc = new DefaultQueryResultColumn();
                            qrc.setDisplayName(name);
                            qrc.setField(name);
                            qrc.setHiveColumnLabel(name);  // not used, but still be expected to be unique
                            qrc.setIndex(colIdx++);
                            // dataType is always empty if %json of dataframe directly:: https://www.mail-archive.com/user@livy.incubator.apache.org/msg00262.html
                            qrc.setDataType(convertDataFrameDataType(dataType));
                            qrc.setComment(metadata.asText());
                            tqr.getColumns().add(qrc);
                        }
                    } // will there be types other than "struct"?
                    continue;
                } // end schema extraction

                // get row data
                logger.debug("build row data");
                ArrayNode valueRows = (ArrayNode) row;

                Iterator<JsonNode> valuesIter = valueRows.elements();
                while (valuesIter.hasNext()) {
                    ArrayNode valueNode = (ArrayNode) valuesIter.next();
                    Iterator<JsonNode> valueNodes = valueNode.elements();
                    List<Object> newValues = Lists.newArrayListWithCapacity(tqr.getColumns().size());
                    while (valueNodes.hasNext()) {
                        JsonNode value = valueNodes.next();
                        // extract values according to how jackson deserialized it
                        if (value.isObject()) {
                            // spark treats an array as a struct with a single field "values" ...
                            //   Maps and structs can't contain arrays so
                            ArrayNode valuesArray = (ArrayNode) value.get("values");

                            if (valuesArray != null && valuesArray.isArray()) {
                                Iterator<JsonNode> arrIter = valuesArray.iterator();
                                List<Object> arrVals = Lists.newArrayListWithExpectedSize(valuesArray.size());
                                while (arrIter.hasNext()) {
                                    JsonNode valNode = arrIter.next();
                                    if (valNode.isNumber()) {
                                        arrVals.add(valNode.numberValue());
                                    } else {
                                        arrVals.add(valNode.asText());
                                    } // end if
                                } // end while
                                newValues.add(arrVals.toArray());
                            } else {
                                Map<String, Object> result = null;
                                try {
                                    result = mapper.convertValue(value, Map.class);
                                } catch (Exception e) {
                                    // column value must be a struct or other complex type that we don't handle special..
                                    newValues.add(value.toString());
                                }
                                newValues.add(result);
                            } // end if
                        } else if (value.isNumber()) {
                            // easy peasy.. it's just a number
                            newValues.add(value.numberValue());
                        } else if (value.isNull()) {
                            newValues.add(null);
                        } else if (value.isValueNode()) {
                            // value Nodes we just get the raw text..
                            newValues.add(value.asText());
                        } else {
                            // default = treat it as string..
                            newValues.add(value.toString());
                        } // end if
                    } // end while
                    rowData.add(newValues);
                } // end of valueRows
            } // end sor.data
            logger.trace("rowData={}", rowData);
            tqr.setRows(rowData);
            //tqr.setValidationResults(null);
        } // end if data!=null

        return logger.exit(tqr);
    }

    private static String convertDataFrameDataType(JsonNode dataType) {
        if (dataType.isObject()) {
            String type = dataType.get("type").asText();
            if (type.equals("udt")) {
                if (dataType.get("class").asText().equals("org.apache.spark.mllib.linalg.VectorUDT")) {
                    // TODO: null check
                    ArrayNode fields = (ArrayNode) dataType.get("sqlType").get("fields");
                    Iterator<JsonNode> fieldsIter = fields.elements();
                    while (fieldsIter.hasNext()) {
                        ObjectNode fieldDescriptors = (ObjectNode) fieldsIter.next();
                        if (fieldDescriptors.get("name").asText().equals("values")) {
                            ObjectNode fdObj = (ObjectNode) fieldDescriptors.get("type");
                            return new StringBuilder(fdObj.get("type").asText())
                                .append("<")
                                .append(fdObj.get("elementType").asText())
                                .append(">")
                                .toString();
                        }
                    }
                    return "Unknown UDT";
                } else {
                    if (dataType.get("class") != null) {
                        logger.error("UDT error encountered", new LivyDeserializationException("don't know how to deserialize UDT types for class = "
                                                                                               + dataType.get("class").asText()));
                    } else {
                        logger.error("UDT error encountered", new LivyDeserializationException("don't know how to deserialize UDT type of unspecified class"));
                    } // end if
                    throw new LivyUserException("livy.unexpected_error");
                } // end if
            } else if (type.equals("map")) {
                return new StringBuilder(dataType.get("type").asText())
                    .append("<")
                    .append(dataType.get("keyType").asText())
                    .append(",")
                    .append(dataType.get("valueType").asText())
                    .append(">")
                    .toString();
            } else if (type.equals("struct")) {
                ArrayNode fields = (ArrayNode) dataType.get("fields");
                Iterator<JsonNode> nodes = fields.elements();
                StringBuilder sb = new StringBuilder("struct<");
                // assumes min of 1 field in struct
                while (nodes.hasNext()) {
                    ObjectNode node = (ObjectNode) nodes.next();
                    String sfName = node.get("name").asText();
                    String sfType = node.get("type").asText();
                    sb.append(sfName)
                        .append(":")
                        .append(sfType)
                        .append(",");
                }
                sb.deleteCharAt(sb.length() - 1);
                return sb.toString();
            } else {
                // can there be other types?
                return "Unknown Type";
            } // end if
        } else {
            return dataType.asText();
        } // end if
    }


    private static List<OutputRow> toTransformResponseProfileStats(StatementOutputResponse sor) {
        checkCodeWasWellFormed(sor);

        JsonNode data = sor.getData();
        ArrayNode json = (ArrayNode) data.get("application/json");
        final List<OutputRow> profileResults = Lists.newArrayList();

        Iterator<JsonNode> rowIter = json.elements();
        while (rowIter.hasNext()) {
            JsonNode row = rowIter.next();
            String columnName = row.get("columnName").asText();
            String metricType = row.get("metricType").asText();
            String metricValue = row.get("metricValue").asText();
            OutputRow outputRow = new OutputRow(columnName, metricType, metricValue);
            profileResults.add(outputRow);
        } // end rowIter.next

        return profileResults;
    }

    public static SaveResponse toSaveResponse(Statement statement) {

        StatementOutputResponse sor = statement.getOutput();

        checkCodeWasWellFormed(sor);

        if (statement.getState() != StatementState.available) {
            SaveResponse response = new SaveResponse();
            response.setStatus(StatementStateTranslator.translateToSaveResponse(statement.getState()));
            return response;
        }

        SaveResponse saveResponse = serializeStatementOutputResponse(sor, SaveResponse.class);
        if (saveResponse.getStatus() == SaveResponse.Status.ERROR) {
            throw new SparkLivySaveException(saveResponse.getMessage(), saveResponse.getId());
        }
        return saveResponse;
    }

    private static TransformResponse prepTransformResponse(Statement statement, String transformId) {
        TransformResponse response = new TransformResponse();

        TransformResponse.Status status = StatementStateTranslator.translate(statement.getState());
        response.setStatus(status);
        response.setProgress(statement.getProgress());
        if (StringUtils.isNotEmpty(transformId)) {
            response.setTable(transformId);
        } else {
            // generate a new id if we don't have one yet...
            statementIdCache.put(ScalaScriptService.newTableName(), statement.getId());
        }
        return response;
    }


    public static DataSources toDataSources(Statement statement) {
        StatementOutputResponse sor = statement.getOutput();
        checkCodeWasWellFormed(sor);

        return serializeStatementOutputResponse(sor, DataSources.class);
    }


    public static URI toUri(Statement statement) {
        StatementOutputResponse sor = statement.getOutput();
        checkCodeWasWellFormed(sor);

        return serializeStatementOutputResponse(sor, URI.class);
    }

    public static ServerStatusResponse toServerStatusResponse(LivyServer livyServer, Integer sessionId) {
        LivyServerStatus livyServerStatus = livyServer.getLivyServerStatus();
        LivySessionStatus livySessionStatus = null;
        SessionState sessionState = livyServer.getLivySessionState(sessionId);
        if (sessionState == null) {
            // don't know about session, could compare id to high water to see if dropped
            if (sessionId <= livyServer.getSessionIdHighWaterMark()) {
                livySessionStatus = LivySessionStatus.completed;
            } else {
                throw new WebApplicationException("No session with that id was created on the server", 404);
            }
        } else if (SessionState.FINAL_STATES.contains(sessionState)) {
            livySessionStatus = LivySessionStatus.completed;
        } else if (SessionState.READY_STATES.contains(sessionState)) {
            livySessionStatus = LivySessionStatus.ready;
        } else if (livyServerStatus == LivyServerStatus.http_error) {
            livySessionStatus = LivySessionStatus.http_error;
        }

        ServerStatusResponse.ServerStatus serverStatus = ServerStatusResponse.ServerStatus.valueOf(livyServerStatus.toString());

        ServerStatusResponse.SessionStatus sessionStatus = ServerStatusResponse.SessionStatus.valueOf(livySessionStatus.toString());

        return ServerStatusResponse.newInstance(serverStatus, sessionId.toString(), sessionStatus);
    }


    private static TransformQueryResult emptyResult() {
        TransformQueryResult tqr = new TransformQueryResult();
        tqr.setColumns(Lists.newArrayList());
        tqr.setRows(Lists.newArrayList());
        return tqr;
    }

    private static <T extends Object> T serializeStatementOutputResponse(StatementOutputResponse sor, Class<T> clazz) {
        String errMsg = String.format("Unable to deserialize JSON returned from Livy into class '%s'", clazz.getSimpleName());

        JsonNode data = sor.getData();
        if (data != null) {
            JsonNode json = data.get("application/json");
            String jsonString = json.asText();
            try {
                return mapper.readValue(jsonString, clazz);
            } catch (IOException e) {
                logger.error("Deserialization error occured", new LivyDeserializationException(errMsg));
            } // end try/catch
        } else {
            logger.error("Deserialization error occured", new LivyDeserializationException(errMsg));
        }
        throw new LivyUserException("livy.unexpected_error");
    }

    private static StatementOutputResponse checkCodeWasWellFormed(StatementOutputResponse statementOutputResponse) {
        if (statementOutputResponse != null && statementOutputResponse.getStatus() != StatementOutputStatus.ok) {
            String msg = String.format("Malformed code sent to Livy.  ErrorType='%s', Error='%s', Traceback='%s'",
                                       statementOutputResponse.getEname(),
                                       statementOutputResponse.getEvalue(),
                                       statementOutputResponse.getTraceback());
            logger.error("Exception Processing Query: ", new LivyCodeException(msg));
            throw new LivyUserException("livy.syntax");
        }
        return statementOutputResponse;
    }

}
