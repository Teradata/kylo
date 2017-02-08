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

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {JiraSpringTestConfig.class})
@Ignore("Ignore Jira tests")
public class JiraClientComponentTest {

    @Autowired
    JiraClient jiraClient;


    @Test
    public void testJiraClient() {

        try {
            ExecutorService executorService = Executors.newFixedThreadPool(1);
            Set<Callable<Issue>> callables = new HashSet<Callable<Issue>>();

            for (int i = 0; i < 1; i++) {
                callables.add(new Callable<Issue>() {
                    public Issue call() throws Exception {
                        return jiraClient.getIssue("JRTT-927");
                    }
                });
            }

            List<Future<Issue>> futures = executorService.invokeAll(callables);

            for (Future<Issue> future : futures) {
                System.out.println("future.get = " + future.get().getKey() + " - " + future.get().getSummary());
            }

            executorService.shutdown();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testCreateIssue() {
        String summary = "Test Jira Issue Summary on " + System.currentTimeMillis();
        try {
            Issue issue = jiraClient.createIssue("JRTT", summary, "Desc", "Task", "scott.reisdorf");
            Assert.assertNotNull(issue);
            summary = "Test2 Jira Issue Summary on " + System.currentTimeMillis();
            issue = jiraClient.createIssue("JRTT", summary, "Desc2", "Task", "scott.reisdorf");
            Assert.assertNotNull(issue);
            summary = "Test3 Jira Issue Summary on " + System.currentTimeMillis();
            issue = jiraClient.createIssue("JRTT", summary, "Desc3", "Task", "scott.reisdorf");
            Assert.assertNotNull(issue);
        } catch (JiraException e) {
            e.printStackTrace();
        }
    }

}
