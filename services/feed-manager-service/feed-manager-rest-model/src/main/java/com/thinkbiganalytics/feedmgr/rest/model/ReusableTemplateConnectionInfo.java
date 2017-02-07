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

/**
 */
public class ReusableTemplateConnectionInfo {

  //private String reusableTemplateFeedName;
  private String feedOutputPortName;
  private String reusableTemplateInputPortName;
  private String inputPortDisplayName;



  public String getFeedOutputPortName() {
    return feedOutputPortName;
  }

  public void setFeedOutputPortName(String feedOutputPortName) {
    this.feedOutputPortName = feedOutputPortName;
  }

  public String getReusableTemplateInputPortName() {
    return reusableTemplateInputPortName;
  }

  public void setReusableTemplateInputPortName(String reusableTemplateInputPortName) {
    this.reusableTemplateInputPortName = reusableTemplateInputPortName;
  }

 // public String getReusableTemplateFeedName() {
 //   return reusableTemplateFeedName;
 // }

//  public void setReusableTemplateFeedName(String reusableTemplateFeedName) {
 //   this.reusableTemplateFeedName = reusableTemplateFeedName;
 // }

  public String getInputPortDisplayName() {
    return inputPortDisplayName;
  }

  public void setInputPortDisplayName(String inputPortDisplayName) {
    this.inputPortDisplayName = inputPortDisplayName;
  }
}
