package com.thinkbiganalytics.jira;

import com.thinkbiganalytics.jira.domain.Issue;

import org.glassfish.jersey.internal.RuntimeDelegateImpl;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by sr186054 on 10/15/15.
 */
@Ignore("Ignore Jira tests")
public class JerseyClientTest {


    @Test
    public void testJerseyClient(){
        JiraRestClientConfig config = new JiraRestClientConfig("/rest/api/latest/");
        config.setUsername("USERNAME");
        config.setPassword("PASSWORD");
        config.setHttps(true);
        config.setKeystoreOnClasspath(true);
        config.setKeystorePath("/thinkbig_jira.jks");
        config.setKeystorePassword("changeit");
        config.setHost("bugs.thinkbiganalytics.com");
        final JiraJerseyClient client = new JiraJerseyClient(config);
        Map<String,String> params = new HashMap<String,String>();
        params.put("projectKeys","JRTT");
        params.put("expand", "projects.issuetypes.fields");


        try {
         //  final Issue  issue1 = client.createIssue("JRTT","Jira Client Pooling Test 1 ","Test Description","Task","scott.reisdorf");

         //  final Issue  issue2 = client.createIssue("JRTT","Jira Client Pooling Test 2","Test Description","Task","scott.reisdorf");

            ExecutorService executorService = Executors.newFixedThreadPool(10);
            Set<Callable<Issue>> callables = new HashSet<Callable<Issue>>();

            for(int i= 0; i<10; i++) {
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

            for(Future<Issue> future : futures){
                System.out.println("future.get = " + future.get().getKey()+" - "+future.get().getSummary());
            }

            executorService.shutdown();




        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

