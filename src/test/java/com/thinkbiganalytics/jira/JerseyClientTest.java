package com.thinkbiganalytics.jira;

import com.thinkbiganalytics.jira.domain.Issue;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

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
        JiraJerseyClient client = new JiraJerseyClient(config);
        Map<String,String> params = new HashMap<String,String>();
        params.put("projectKeys","JRTT");
        params.put("expand", "projects.issuetypes.fields");


        Issue issue = null;
        try {
            issue = client.createIssue("JRTT","Test Summary for new Jira Client","Test Description","Task","scott.reisdorf");
            Assert.assertNotNull(issue);
            issue = client.createIssue("JRTT","Test Summary for new Jira Client","Test Description","Task","scott.reisdorf");
        } catch (JiraException e) {
            e.printStackTrace();
        }
        System.out.println(issue);
    }
}
