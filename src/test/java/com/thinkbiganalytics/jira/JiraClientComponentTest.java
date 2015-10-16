package com.thinkbiganalytics.jira;

import com.thinkbiganalytics.jira.domain.Issue;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by mk186074 on 10/14/15.
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
            String summary = "Test Jira Issue Summary on "+System.currentTimeMillis();
        Issue issue = jiraClient.getIssue("TDB-2");
        Assert.assertEquals("TDB-2",issue.getKey());
            System.out.println("ISSUE "+issue);
        } catch (JiraException e) {
            e.printStackTrace();
        }
    }

}
