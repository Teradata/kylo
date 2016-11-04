package com.thinkbiganalytics.metadata.api.op;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by sr186054 on 10/26/16.
 */
public class FeedDependencyDeltaResults {

    private String feedName;
    private String feedId;

    public FeedDependencyDeltaResults() {

    }

    public FeedDependencyDeltaResults(String feedId, String feedName) {
        this.feedId = feedId;
        this.feedName = feedName;
    }

    /**
     * An array of the dependentFeed system Names
     */
    private List<String> dependentFeedNames = new ArrayList<>();

    Map<String, List<FeedJobExecutionData>> feedJobExecutionContexts = new HashMap<>();

    /**
     * Map storing the dependent feedName and the latest completed Execution Context
     */
    Map<String, FeedJobExecutionData> latestFeedJobExecutionContext = new HashMap<>();


    /**
     * internal map to store jobexecution data
     */
    @JsonIgnore
    Map<Long, FeedJobExecutionData> jobExecutionDataMap = new HashMap<>();


    public void addFeedExecutionContext(String depFeedSystemName, Long jobExecutionId, DateTime startTime, DateTime endTime, Map<String, Object> executionContext) {
        if (!dependentFeedNames.contains(depFeedSystemName)) {
            dependentFeedNames.add(depFeedSystemName);
        }
        FeedJobExecutionData feedJobExecutionData = jobExecutionDataMap.get(jobExecutionId);
        if (feedJobExecutionData == null) {
            feedJobExecutionData = new FeedJobExecutionData(jobExecutionId, startTime, endTime, executionContext);
            feedJobExecutionContexts.computeIfAbsent(depFeedSystemName, feedName -> new ArrayList<>()).add(feedJobExecutionData);
            FeedJobExecutionData latest = latestFeedJobExecutionContext.get(depFeedSystemName);
            //update the latest pointer
            if (latest == null || (latest != null && endTime.isAfter(latest.getEndTime()))) {
                latestFeedJobExecutionContext.put(depFeedSystemName, feedJobExecutionData);
            }
        } else {
            feedJobExecutionData.getExecutionContext().putAll(executionContext);
        }

    }

    private void reduceExecutionContextToMatchingKeys(FeedJobExecutionData executionData, List<String> validKeys) {
        if (executionData != null && executionData.getExecutionContext() != null) {
            Map<String, Object> reducedMap = executionData.getExecutionContext().entrySet().stream().filter(e ->
                                                                                                                validKeys.stream()
                                                                                                                    .anyMatch(validKey -> e.getKey().toLowerCase().startsWith(validKey.toLowerCase())))
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue()));
            executionData.setExecutionContext(reducedMap);

        }

    }

    /**
     * reduce the excecution Context data that is in the Map matching where any key starts with the passed in list of validkeys
     */
    public void reduceExecutionContextToMatchingKeys(List<String> validKeys) {
        feedJobExecutionContexts.values().forEach(feedJobExecutionDatas -> {
            if (feedJobExecutionDatas != null) {
                feedJobExecutionDatas.stream().forEach(executionData -> reduceExecutionContextToMatchingKeys(executionData, validKeys));
            }
        });

        latestFeedJobExecutionContext.values().forEach(executionData -> {
            if (executionData != null) {
                reduceExecutionContextToMatchingKeys(executionData, validKeys);
            }
        });
    }


    public String getFeedName() {
        return feedName;
    }

    public void setFeedName(String feedName) {
        this.feedName = feedName;
    }

    public String getFeedId() {
        return feedId;
    }

    public void setFeedId(String feedId) {
        this.feedId = feedId;
    }

    public List<String> getDependentFeedNames() {
        if (dependentFeedNames == null) {
            dependentFeedNames = new ArrayList<>();
        }
        return dependentFeedNames;
    }

    public void setDependentFeedNames(List<String> dependentFeedNames) {
        this.dependentFeedNames = dependentFeedNames;
    }

    public Map<String, List<FeedJobExecutionData>> getFeedJobExecutionContexts() {
        return feedJobExecutionContexts;
    }

    public Map<String, FeedJobExecutionData> getLatestFeedJobExecutionContext() {
        return latestFeedJobExecutionContext;
    }


    public static class FeedJobExecutionData {

        private Long jobExecutionId;
        private DateTime startTime;
        private DateTime endTime;
        private Map<String, Object> executionContext;

        public FeedJobExecutionData() {

        }

        public FeedJobExecutionData(Long jobExecutionId, DateTime startTime, DateTime endTime, Map<String, Object> executionContext) {
            this.jobExecutionId = jobExecutionId;
            this.startTime = startTime;
            this.endTime = endTime;
            this.executionContext = executionContext;
        }

        public Long getJobExecutionId() {
            return jobExecutionId;
        }

        public void setJobExecutionId(Long jobExecutionId) {
            this.jobExecutionId = jobExecutionId;
        }

        public DateTime getStartTime() {
            return startTime;
        }

        public void setStartTime(DateTime startTime) {
            this.startTime = startTime;
        }

        public DateTime getEndTime() {
            return endTime;
        }

        public void setEndTime(DateTime endTime) {
            this.endTime = endTime;
        }

        public Map<String, Object> getExecutionContext() {
            return executionContext;
        }

        public void setExecutionContext(Map<String, Object> executionContext) {
            this.executionContext = executionContext;
        }
    }
}
