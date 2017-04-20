package com.thinkbiganalytics.feedmgr.rest.model;

/*-
 * #%L
 * thinkbig-feed-manager-rest-model
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

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UploadProgress {
    private boolean complete;
    private String key;
    private Integer totalParts = 0;

    private Set<String> sections = new HashSet<>();

    private Set<String> completedSections = new HashSet<>();

    private Integer percentComplete = 0;
    private DateTime lastSectionCompleteTime;

    private List<UploadProgressMessage> messages;

    public UploadProgress() {
        this.completedSections = new HashSet<>();
    }

    public UploadProgress(String key) {
        this();
        this.key = key;
    }

    public UploadProgress(String key, Set<String> sections) {
        this.key = key;
        this.sections = sections;
        this.totalParts = sections.size();
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public List<UploadProgressMessage> getMessages() {
        if(messages == null){
            messages = new ArrayList<>();
        }
        return messages;
    }

    public UploadProgressMessage getMessage(String messageKey) {
        return getMessages().stream().filter(uploadProgressMessage -> uploadProgressMessage.getMessageKey().equalsIgnoreCase(messageKey)).findFirst().orElse(null);
    }

    public void setMessages(List<UploadProgressMessage> messages) {
        this.messages = messages;
    }

    public Integer getTotalParts() {
        return totalParts;
    }

    public void setTotalParts(Integer totalParts) {
        this.totalParts = totalParts;
    }



    private Integer percent(Integer completedSections){
       return  Math.round(((float)completedSections / getTotalParts()) *100);
    }

    public void calculatePercentComplete(){
        if(getTotalParts() >0) {
            Integer percent  =percent(getCompletedSections().size());
            if(percent > percentComplete){
                percentComplete = percent;
            }
        }
        else {
            percentComplete = 0;
        }
    }

    /**
     *
     * @return time in millis since the last time the percentage was increased
     */
    public Long timeSinceLastSectionComplete(){
        if(lastSectionCompleteTime != null) {
            return DateTime.now().getMillis() - lastSectionCompleteTime.getMillis();
        }
        return 0L;
    }

    /**
     * Complete a section.
     * @param section
     */
    public void completeSection(String section){
        completedSections.add(section);
        lastSectionCompleteTime = DateTime.now();
        calculatePercentComplete();
    }

    private Set<String> getCompletedSections(){
        return completedSections;
    }

    public void setSections(Set<String> sections) {
        this.sections = sections;
        this.totalParts = sections.size();
    }

    public Integer getPercentComplete() {
        return percentComplete;
    }

    public void setPercentComplete(Integer percentComplete) {
        this.percentComplete = percentComplete;
    }

    /**
     * update the percent complete for longer processing sections to simulate a more unified progress bar
     */
    public void checkAndIncrementPercentage(){
        Long timeSince = timeSinceLastSectionComplete();
        int maxThreshold = 97;
        int incrementAmount = 1;
        if(timeSince > 500){
            Integer currentPercentage = getPercentComplete();
            Integer nextPercentage = percent(getCompletedSections().size() +1);

            if(currentPercentage !=0 && currentPercentage< nextPercentage && currentPercentage < maxThreshold){
                //slow down the increment if we are close to the end
                setPercentComplete(currentPercentage+incrementAmount);
            }
        }
    }
}
