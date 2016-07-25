# Sevice Level Agreement (SLA) framework

Components
===
 - Metric (com.thinkbiganalytics.metadata.sla.api.Metric) annotated with @ServiceLevelAgreementMetric
   - A class that defines inputs that will be used when assessing the SLA
 - Metric Assessor (com.thinkbiganalytics.metadata.sla.spi.MetricAssessor)
    - A class that accepts a Metric and uses that input to make an assessment
 - SLA Action Configuration (com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementActionConfiguration)
   - A class that defines the inputs which will be sent to its corresponding ServiceLevelAgreementAction classes when the SLA is violated
 - SLA Action (com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementAction)
   - A class that defines what should happen when an SLA is violated
 

Metric (com.thinkbiganalytics.metadata.sla.api.Metric)
---
Any metric class you wish to be used when creating Service Level Agreements needs to be annotated with the @ServiceLevelAgreementMetric annotation and each of the properties requiring User input need to be annotated with the @PolicyProperty annotation.
 - The @PolicyProperty describe how the inputs should be rendered in the User interface.
 - The Constructor of this Metric class needs to have the @PolicyPropertyRef annotation on the incoming parameters for the framework to correctly construct the object
 
```java
@ServiceLevelAgreementMetric(name = "Feed Processed Data by a certain time",
                             description = "Ensure a Feed processes data by a specified time")
public class FeedOnTimeArrivalMetric implements Metric {
    @PolicyProperty(name = "FeedName", 
                    type = PolicyProperty.PROPERTY_TYPE.currentFeed,
                    hidden = true)
    private String feedName;

    @PolicyProperty(name = "ExpectedDeliveryTime",
                    displayName = "Expected Delivery Time",
                    type = PolicyProperty.PROPERTY_TYPE.string,
                    hint = "Cron Expression for when you expect to receive this data",
                    required = true)
    private String cronString;
    //...
        public FeedOnTimeArrivalMetric(@PolicyPropertyRef(name = "FeedName") String feedName,
                                   @PolicyPropertyRef(name = "ExpectedDeliveryTime") String cronString,
                                   @PolicyPropertyRef(name = "NoLaterThanTime") Integer lateTime,
                                   @PolicyPropertyRef(name = "NoLaterThanUnits") String lateUnits,
                                   @PolicyPropertyRef(name = "AsOfTime") Integer asOfTime,
                                   @PolicyPropertyRef(name = "AsOfUnits") String asOfUnits) throws ParseException {
        this.feedName = feedName;
        this.cronString = cronString;
        //...
```

Metric Assessor
---
The Assessor class needs to implement the following 2 methods:
```java
 public boolean accepts(Metric metric)
 public void assess(M metric, MetricAssessmentBuilder<D> builder);
```

the *accept(Metric)* method tells what Metrics the Assessor will assess.  Below is an example of the FeedOnTimeArrivalMetric
```java
@Override
    public boolean accepts(Metric metric) {
        return metric instanceof FeedOnTimeArrivalMetric;
    }
```
The *assess(Metric,MetricAssessmentBuilder)* method does the assessment and adds a AssessmentResult to the MetricAssessmentBuilder

```java
public class FeedOnTimeArrivalMetricAssessor implements MetricAssessor<FeedOnTimeArrivalMetric, Serializable> {
   public void assess(FeedOnTimeArrivalMetric metric, MetricAssessmentBuilder builder) {
   // Do assessment work
   //make your assessments and write result to the builder
   
     if (isHoliday) {
            LOG.debug("No data expected for feed {} due to a holiday", feedName);
            builder.message("No data expected for feed " + feedName + " due to a holiday")
                    .result(AssessmentResult.SUCCESS);
        } else if (lastFeedTime == null ) {
            LOG.debug("Feed with the specified name {} not found", feedName);
            builder.message("Feed with the specified name "+feedName+" not found ")
                    .result(AssessmentResult.WARNING);
        }  else if (lastFeedTime.isAfter(expectedTime) && lastFeedTime.isBefore(lateTime)) {
            LOG.debug("Data for feed {} arrived on {}, which was before late time: ", feedName, lastFeedTime, lateTime);

            builder.message("Data for feed " + feedName + " arrived on " + lastFeedTime + ", which was before late time: " + lateTime)
                    .result(AssessmentResult.SUCCESS);
        }
        else if(DateTime.now().isBefore(lateTime)) {
            return;
        }
        else {
            LOG.debug("Data for feed {} has not arrived before the late time: ", feedName, lateTime);

            builder.message("Data for feed " + feedName + " has not arrived before the late time: " + lateTime + "\n The last successful feed was on " + lastFeedTime)
                    .result(AssessmentResult.FAILURE);
        }

```

It then needs register with the Assessor using Spring.  Use the example below:

```java
@Bean(name = "onTimeAssessor")
    public MetricAssessor<? extends Metric, Serializable> onTimeMetricAssessor(@Qualifier("slaAssessor") ServiceLevelAssessor slaAssessor) {
        FeedOnTimeArrivalMetricAssessor metricAssr = new FeedOnTimeArrivalMetricAssessor();
        slaAssessor.registerMetricAssessor(metricAssr);
        return metricAssr;
    }
```
    


SLA Action Configuration
---
Action Configuration objects describe what fields need to be captured to alert when an SLA is violated
Any ServiceLevelAgreementActionConfiguration class you wish to be used when creating Service Level Agreements needs to be annotated with the @ServiceLevelAgreementActionConfig annotation and each of the properties requiring User input need to be annotated with the @PolicyProperty annotation.
 - The @PolicyProperty describe how the inputs should be rendered in the User interface.
 - The Constructor of this ServiceLevelAgreementActionConfiguration class needs to have the @PolicyPropertyRef annotation on the incoming parameters for the framework to correctly construct the object
 
```java

@ServiceLevelAgreementActionConfig(
    name = "Jira",
    description = "Create a Jira when the SLA is violated",
    actionClasses = {JiraServiceLevelAgreementAction.class}
)
public class JiraServiceLevelAgreementActionConfiguration extends BaseServiceLevelAgreementActionConfiguration {

    @PolicyProperty(name = "JiraProjectKey",
                    displayName = "Jira Project Key",
                    hint = "The Jira Project Key ",
                    required = true)
    private String projectKey;
    @PolicyProperty(name = "JiraIssueType",
                    displayName = "Issue Type",
                    hint = "The Jira Type ",
                    value = "Bug", type = PolicyProperty.PROPERTY_TYPE.select,
                    labelValues = {@PropertyLabelValue(label = "Bug", value = "Bug"),
                                   @PropertyLabelValue(label = "Task", value = "Task")},
                    required = true)
    private String issueType;
    @PolicyProperty(name = "JiraAssignee", displayName = "Assignee", hint = "Who should get assigned this Jira? ", required = true)
    private String assignee;

    public JiraServiceLevelAgreementActionConfiguration() {

    }

    public JiraServiceLevelAgreementActionConfiguration(@PolicyPropertyRef(name = "JiraProjectKey") String projectKey, @PolicyPropertyRef(name = "JiraIssueType") String issueType,
                                                        @PolicyPropertyRef(name = "JiraAssignee") String assignee) {
        this.projectKey = projectKey;
        this.issueType = issueType;
        this.assignee = assignee;
    }

```

SLA Action
---
The Action Class performs the response when the SLA is violated
This classes can either be bound by Spring, or need to have a default noarg constructor

```java

public class JiraServiceLevelAgreementAction implements ServiceLevelAgreementAction<JiraServiceLevelAgreementActionConfiguration> {

    @Override
    public boolean respond(JiraServiceLevelAgreementActionConfiguration actionConfiguration, Alert a) {
        String desc = ServiceLevelAssessmentAlertUtil.getDescription(a);
        String projectKey = actionConfiguration.getProjectKey();
        String issueType = actionConfiguration.getIssueType();

        ///do call to JIRA
        return true;
    }
}

```