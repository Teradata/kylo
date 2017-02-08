package com.thinkbiganalytics.jira;

/*-
 * #%L
 * thinkbig-jira-rest-client
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

import com.thinkbiganalytics.jira.domain.Issue;

import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 */
@Ignore("Ignore Jira tests")
public class JerseyClientTest {


    @Test
    public void testJerseyClient() {
        JiraRestClientConfig config = new JiraRestClientConfig("/rest/api/latest/");
        config.setUsername("USERNAME");
        config.setPassword("PASSWORD");
        config.setHttps(true);
        config.setKeystoreOnClasspath(true);
        config.setKeystorePath("/kylo_jira.jks");
        config.setKeystorePassword("changeit");
        config.setHost("bugs.thinkbiganalytics.com");
        final JiraJerseyClient client = new JiraJerseyClient(config);
        Map<String, String> params = new HashMap<String, String>();
        params.put("projectKeys", "JRTT");
        params.put("expand", "projects.issuetypes.fields");

        try {
            //  final Issue  issue1 = client.createIssue("JRTT","Jira Client Pooling Test 1 ","Test Description","Task","scott.reisdorf");

            //  final Issue  issue2 = client.createIssue("JRTT","Jira Client Pooling Test 2","Test Description","Task","scott.reisdorf");

            ExecutorService executorService = Executors.newFixedThreadPool(10);
            Set<Callable<Issue>> callables = new HashSet<Callable<Issue>>();

            for (int i = 0; i < 10; i++) {
                callables.add(new Callable<Issue>() {
                    public Issue call() throws Exception {
                        return client.getIssue("JRTT-927");
                    }
                });
            }

            /*



            callables.add(new Callable<Issue>() {
                public Issue call() throws Exception {
                    return client.getIssue(issue2.getKey());
                }
            });

            callables.add(new Callable<Issue>() {
                public Issue call() throws Exception {
                    Issue issue = client.createIssue("JRTT", "Jira Client Pooling Test 3", "Test Description", "Task", "scott.reisdorf");
                    return client.getIssue(issue.getKey());
                }
            });
            callables.add(new Callable<Issue>() {
                public Issue call() throws Exception {
                    return client.getIssue(issue1.getKey());
                }
            });
*/
            List<Future<Issue>> futures = executorService.invokeAll(callables);

            for (Future<Issue> future : futures) {
                System.out.println("future.get = " + future.get().getKey() + " - " + future.get().getSummary());
            }

            executorService.shutdown();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

