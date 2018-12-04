package com.thinkbiganalytics.kylo.spark.model;

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


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.thinkbiganalytics.kylo.spark.model.enums.SessionKind;
import com.thinkbiganalytics.kylo.spark.model.enums.SessionState;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import test.utils.TestUtils;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class TestSerializing {
    private static final Logger logger = LoggerFactory.getLogger(TestSerializing.class);

    @Test
    public void testSessionsGet() throws JsonProcessingException {
        SessionsGet sg = new SessionsGet.Builder().from(1).size(2).build();

        String sgJson = new ObjectMapper().writeValueAsString(sg);

        logger.debug("{}", sgJson);
        assertThat(sgJson).isEqualToIgnoringCase("{\"from\":1,\"size\":2}");
    }

    @Test
    public void testSessionsPost() throws JsonProcessingException {
        SessionsPost sp = new SessionsPost.Builder().kind("spark").proxyUser("dladmin").build();
        String sgJson = new ObjectMapper().writeValueAsString(sp);
        logger.debug("{}", sgJson);
        assertThat(sgJson).isEqualToIgnoringCase("{\"kind\":\"spark\",\"proxyUser\":\"dladmin\"}");

        sp = new SessionsPost.Builder().kind("spark").proxyUser("").build();
        sgJson = new ObjectMapper().writeValueAsString(sp);
        logger.debug("{}", sgJson);
        assertThat(sgJson).isEqualToIgnoringCase("{\"kind\":\"spark\",\"proxyUser\":\"\"}");

        sp = new SessionsPost.Builder().kind("spark").build();
        sgJson = new ObjectMapper().writeValueAsString(sp);
        logger.debug("{}", sgJson);
        assertThat(sgJson).isEqualToIgnoringCase("{\"kind\":\"spark\"}");

        sp = new SessionsPost.Builder()
                .kind("spark")
                .proxyUser("dladmin")
                .conf(ImmutableMap.of("spark.ivy.jars", "$HOME/.m2/repository"))
                .build();
        sgJson = new ObjectMapper().writeValueAsString(sp);
        logger.debug("{}", sgJson);
        assertThat(sgJson).isEqualToIgnoringCase("{\"kind\":\"spark\",\"proxyUser\":\"dladmin\",\"conf\":{\"spark.ivy.jars\":\"$HOME/.m2/repository\"}}");

        sp = new SessionsPost.Builder().kind("spark").jars(Lists.newArrayList("file:///jar1", "file:///jar2")).build();
        sgJson = new ObjectMapper().writeValueAsString(sp);
        logger.debug("{}", sgJson);
        assertThat(sgJson).isEqualToIgnoringCase("{\"kind\":\"spark\",\"jars\":[\"file:///jar1\",\"file:///jar2\"]}");
    }

    @Test
    public void testSessionGetResponse() throws IOException {
        final String json = "{\"from\":0,\"total\":1,\"sessions\":[{\"id\":0,\"appId\":null,\"owner\":null,\"proxyUser\":null,\"state\":\"starting\",\"kind\":\"shared\",\"appInfo\":{\"driverLogUrl\":null,\"sparkUiUrl\":null},\"log\":[\"stdout: \",\"\\nstderr: \"]}]}";

        //JSON from String to Object
        SessionsGetResponse sessions = new ObjectMapper().readValue(json, SessionsGetResponse.class);
        logger.info("session={}", sessions);

        assertThat(sessions).hasFieldOrPropertyWithValue("from", 0);
        assertThat(sessions).hasFieldOrPropertyWithValue("total", 1);
    }

    @Test
    public void testSessionFromJson() throws IOException {
        final String json = "{\"id\":0," +
                "\"appId\":null," +
                "\"owner\":null," +
                "\"proxyUser\":null," +
                "\"state\":\"starting\"," +
                "\"kind\":\"shared\"," +
                "\"appInfo\":{\"driverLogUrl\":\"hdfs://\",\"sparkUiUrl\":\"http://\"}," +
                "\"log\":[\"stdout: \",\"\\nstderr: \"]}";

        //JSON from String to Object
        Session session = new ObjectMapper().readValue(json, Session.class);
        logger.info("session={}", session);

        assertThat(session).hasFieldOrPropertyWithValue("id", 0);
        assertThat(session).hasFieldOrPropertyWithValue("appId", null);
        assertThat(session).hasFieldOrPropertyWithValue("size", null);
        assertThat(session).hasFieldOrPropertyWithValue("owner", null);
        assertThat(session).hasFieldOrPropertyWithValue("proxyUser", null);
        assertThat(session).hasFieldOrPropertyWithValue("state", SessionState.starting);
        assertThat(session).hasFieldOrPropertyWithValue("kind", SessionKind.shared);
        assertThat(session).hasFieldOrPropertyWithValue("appInfo",
                ImmutableMap.of("driverLogUrl", "hdfs://", "sparkUiUrl", "http://"));
        assertThat(session).hasFieldOrPropertyWithValue("log", Lists.newArrayList("stdout: ", "\nstderr: "));

    }

    @Test
    public void testStatementsPostResponse() throws IOException {
        final String json = TestUtils.getTestResourcesFileAsString("dataFrameStatementPostResponse.json");

        //JSON from String to Object
        Statement response = new ObjectMapper().readValue(json, Statement.class);
        logger.info("response={}", response);
    }


    @Test
    public void testStatementOutputResponse() throws IOException {
        final String json = TestUtils.getTestResourcesFileAsString("dataFrameFromJsonMagick.json");

        //JSON from String to Object
        StatementOutputResponse response = new ObjectMapper().readValue(json, StatementOutputResponse.class);
        logger.info("response={}", response);

        Object data = response.getData();
        logger.info("classType={}", data.getClass());
    }


}
