package com.thinkbiganalytics.kylo.spark.client;

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


import com.thinkbiganalytics.kylo.spark.livy.SparkLivyProcess;
import com.thinkbiganalytics.kylo.spark.model.Session;
import com.thinkbiganalytics.kylo.spark.model.SessionsGetResponse;
import com.thinkbiganalytics.kylo.spark.model.SessionsPost;
import com.thinkbiganalytics.kylo.spark.model.Statement;
import com.thinkbiganalytics.kylo.spark.model.StatementsPost;
import com.thinkbiganalytics.rest.JerseyRestClient;
import com.thinkbiganalytics.spark.shell.SparkShellProcess;

public interface LivyClient {

    Statement postStatement(JerseyRestClient client, SparkLivyProcess sparkLivyProcess, StatementsPost sp);

    Statement getStatement(JerseyRestClient client,  SparkLivyProcess sparkLivyProcess, Integer statementId);

    Statement pollStatement(JerseyRestClient jerseyClient, SparkLivyProcess sparkLivyProcess, Integer stmtId);

    Statement pollStatement(JerseyRestClient jerseyClient, SparkLivyProcess sparkLivyProcess, Integer stmtId, Long wait);

    SessionsGetResponse getSessions(JerseyRestClient client);

    Session postSessions(JerseyRestClient client, SessionsPost sessionsPost);

    Session getSession(JerseyRestClient client, SparkLivyProcess sparkLivyProcess);
}
