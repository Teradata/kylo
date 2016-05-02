package com.thinkbiganalytics.feedmgr.rest.model;

/**
 * Created by sr186054 on 4/28/16.
 */
public class ReusableTemplateConnectionInfo {

  private String reusableTemplateFeedName;
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

  public String getReusableTemplateFeedName() {
    return reusableTemplateFeedName;
  }

  public void setReusableTemplateFeedName(String reusableTemplateFeedName) {
    this.reusableTemplateFeedName = reusableTemplateFeedName;
  }

  public String getInputPortDisplayName() {
    return inputPortDisplayName;
  }

  public void setInputPortDisplayName(String inputPortDisplayName) {
    this.inputPortDisplayName = inputPortDisplayName;
  }
}
