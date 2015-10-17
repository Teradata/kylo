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
        Issue issue = jiraClient.getIssue("TDB-2");
        Assert.assertEquals("TDB-2",issue.getKey());
            System.out.println("ISSUE "+issue);
        } catch (JiraException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCreateIssue(){
        String summary = "Test Jira Issue Summary on "+System.currentTimeMillis();
        try {
          Issue issue =  jiraClient.createIssue("JRTT",summary,"Desc","Task","scott.reisdorf");
           Assert.assertNotNull(issue);
            summary = "Test2 Jira Issue Summary on "+System.currentTimeMillis();
          issue =  jiraClient.createIssue("JRTT",summary,"Desc2","Task","scott.reisdorf");
            Assert.assertNotNull(issue);
            summary = "Test3 Jira Issue Summary on "+System.currentTimeMillis();
            issue =  jiraClient.createIssue("JRTT",summary,"Desc3","Task","scott.reisdorf");
            Assert.assertNotNull(issue);
        } catch (JiraException e) {
            e.printStackTrace();
        }
    }

}
