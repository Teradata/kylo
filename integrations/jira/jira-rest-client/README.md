# Jira Rest Client

## About
A Restful client that connects to the JIRA REST api (https://docs.atlassian.com/jira/REST/latest/)

This project was created because the Atlassian Rest client uses many outdated jars which conflict with newer Java development(https://www.versioneye.com/java/com.atlassian.jira:jira-rest-java-client-core/3.0.0)

This project uses Jersey 2.x and Joda-time and can easily plugin kylo

## How to use
To use the client you need to pass in a `JiraRestClientConfig` object that defines your connection criteria and optional usage of any SSL certs.
You then supply this configuration to create the client.
You can use this client with or without Spring.  If you are using Sprint a `JiraClient` can be autowired in for you with just a properties file

### Usage without Spring

```java
JiraRestClientConfig config = new JiraRestClientConfig("/rest/api/latest/");
config.setUsername("USERNAME");
config.setPassword("PASSWORD");
config.setHttps(true);
config.setKeystoreOnClasspath(true);
config.setKeystorePath("/kylo_jira.jks");
config.setKeystorePassword("changeit");
config.setHost("bugs.thinkbiganalytics.com");

JiraJerseyClient jiraClient = new JiraJerseyClient(config);

Issue issue = jiraClient.getIssue("JIRA-1");
String summary = issue.getSummary();
```
NOTE: Details of these ClientConfig properties and what they mean can be found below

### Usage with Spring
If you are using Spring, there is a `jira-rest-client.xml` that defines the client bean so you can just autowire it in with a .properties file for the configuration data

1. Supply the connection properties
```
# connect to Jira
jira.host=bugs.thinkbiganalytics.com
jira.apiPath=/rest/api/latest/
jira.username=username
jira.password=password
jira.https=true
jira.keystorePath=/thinkbig_jira.jks
jira.keystorePassword=changeit
jira.keystoreOnClasspath=true
```
NOTE: Details of these connection properties and what they mean can be found below

2. Autowire in the Client

```java
@Autowired
JiraClient jiraClient;
public void doSomeJiraStuff(){
Issue issue = jiraClient.getIssue("JIRA-1");
}
```

## Client Operations
### Getting Jira Issues
To get the issue pass in the JIRA Issue key to the getIssue method

```java
@Autowired
JiraClient jiraClient;

public void doSomeJiraStuff(){
Issue issue = jiraClient.getIssue("JIRA-1");
String summary = issue.getSummary();
String description = issue.getDescription();
IssueType issueType = issue.getIssueType();
List<Comment> comments = issue.getComments();
Project project = issue.getProject();
}
```

### Creating Jira Issue
Below is how you create a new Issue.

```java
@Autowired
JiraClient jiraClient;

public void doSomeJiraStuff(){

    /**
     * Create a new Jira Issue
     * @param projectKey The String Key.  If you have issue  IT-1, then this is "IT"
     * @param summary
     * @param description
     * @param issueType  the String type (i.e. "Bug", "Task")
     * @param assigneeName  the jira username
     * @return
     * @throws JiraException
     */

  Issue issue =  jiraClient.createIssue(projectKey,summary,description,issueType,assigneeName);
}
```
NOTE: All fields are required for the issue to be created.  A JiraException will be thrown if these are not specified or are null.

### Getting valid IssueType Names for a Jira Project
Since JIRA allows for projects to specify their own Issue Types, You may need to check and see the valid issueType names before creating the issue +
```java
 List<String> issueTypes = jiraClient.getIssueTypeNamesForProject("MYPROJ");
```

### The JiraRestClient Configuration Properties
```
jira.host=The Path of the Jira Host
jira.apiPath=The JIRA URL for the correct version of JIRA you want to connect to
jira.username=username
jira.password=password
jira.https=true/false if you are using HTTPS or not.  If you are using HTTPS then the next 3 parameters are necessary
jira.keystorePath=The Path to the Keystore that contains the SSL certificate for the JIRA server.  This can be an absolute file refrerence on the server itself, or a reference to a file in the classpath.  If they keystore is in the classpath then start this with a "/" (i.e.  /myjirakeystore.jks
jira.keystoreOnClasspath=true/false.  See jira.keystorePath for this usage.
jira.keystorePassword=The Password for the keystore
```

### Generating the SSL Keystore for your Jira server connection
To generate the keystore for the Jira Server you need to do the following:
1.   Download the Cert file for the Jira Server.
    You can do this in Firefox -> click on the lock at the address bar -> details -> export-> (use the first x.509(PEM) ) and save that to a file.
2.   Generate a keystore from that cert file

```
 keytool -import -trustcacerts -alias YOUR_ALIAS -file NAME_OF_THE_CERT_FROM_STEP1.pem -keystore NAME_OF_YOUR_KEYSTORE.jks -keypass PASSWORD -storepass PASSWORD

```

**Below is a full example**
1.   Download cert file called **kylo-jira-cert.pem**
2.   Generate Keystore from that cert file resulting in the *thinkbig_jira.jks* output file.
```
 keytool -import -trustcacerts -alias kylo-jira -file kylo-jira-cert.pem -keystore kylo_jira.jks -keypass changeit -storepass changeit

 ```
