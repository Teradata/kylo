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


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.thinkbiganalytics.discovery.schema.QueryResultColumn;
import com.thinkbiganalytics.kylo.spark.model.Statement;
import com.thinkbiganalytics.kylo.spark.model.TestSerializing;
import com.thinkbiganalytics.spark.rest.model.TransformQueryResult;
import com.thinkbiganalytics.spark.rest.model.TransformResponse;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.utils.TestUtils;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestLivyRestModelTransformer {
    private static final Logger logger = LoggerFactory.getLogger(TestSerializing.class);


    @Test
    public void simpleTestWithSchema() throws IOException {
        final String json = TestUtils.getTestResourcesFileAsString("dataFrameStatementPostResponseWithSchema.json");

        //JSON from String to Object
        Statement statement = new ObjectMapper().readValue(json, Statement.class);
        logger.info("response={}", statement);

        TransformResponse response = LivyRestModelTransformer.toTransformResponse(statement, "testTableName");
        assertThat(response).hasFieldOrPropertyWithValue("status", TransformResponse.Status.SUCCESS );
        assertThat(response).hasFieldOrPropertyWithValue("progress", 1.0);

        TransformQueryResult tqr = response.getResults();
        assertThat(tqr).isNotNull();

        List<QueryResultColumn> cols = tqr.getColumns();
        assertThat(cols).isNotNull();
        QueryResultColumn dt0 = cols.get(0);
        assertThat(dt0).hasFieldOrPropertyWithValue("dataType", "integer");

        List<Object> row1 = Lists.newArrayList( 1, "Toyota Park", "Bridgeview", "IL", 0, "1527526048390");
        assertThat( tqr.getRows() ).contains(row1);
    }


}
