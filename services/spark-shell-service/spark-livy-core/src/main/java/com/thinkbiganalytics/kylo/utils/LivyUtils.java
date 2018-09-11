package com.thinkbiganalytics.kylo.utils;

/*-
 * #%L
 * kylo-spark-livy-core
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

import com.thinkbiganalytics.kylo.spark.client.LivyClient;
import com.thinkbiganalytics.kylo.spark.exceptions.LivyCodeException;
import com.thinkbiganalytics.kylo.spark.livy.SparkLivyProcess;
import com.thinkbiganalytics.kylo.spark.model.Statement;
import com.thinkbiganalytics.kylo.spark.model.enums.StatementState;
import com.thinkbiganalytics.rest.JerseyRestClient;
import com.thinkbiganalytics.spark.shell.SparkShellProcess;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LivyUtils {
    private static final Logger logger = LoggerFactory.getLogger(LivyUtils.class);

    private LivyUtils() {} // private constructor

    // TODO: is there a better way to wait for a response than synchronous?  UI could poll?
    public static Statement getStatement(LivyClient livyClient, JerseyRestClient jerseyClient, SparkLivyProcess sparkLivyProcess, Integer stmtId) {
        Statement statement = null;
        do {
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            statement = livyClient.getStatement( jerseyClient, sparkLivyProcess, stmtId );

            if( statement.getState().equals(StatementState.error)) {
                // TODO: what about cancelled? or cancelling?
                throw new LivyCodeException("Unexpected error encountered in Statement='" + statement + "'");
            }
        } while (statement == null || !statement.getState().equals(StatementState.available));

        return statement;
    }


}
