package com.thinkbiganalytics.feedmgr.rest.model;

public class RemoteProcessGroupInputPort {


    private String templateName;
    private String inputPortName;
    private boolean selected;
    private boolean existing;


    public RemoteProcessGroupInputPort() {

    }

    public RemoteProcessGroupInputPort(String templateName, String inputPortName) {
        this.templateName = templateName;
        this.inputPortName = inputPortName;
    }

    public RemoteProcessGroupInputPort(String templateName, String inputPortName, boolean selected) {
       this(templateName,inputPortName);
        this.selected = selected;
    }

    public RemoteProcessGroupInputPort(String templateName, String inputPortName, boolean selected, boolean existing) {
        this.templateName = templateName;
        this.inputPortName = inputPortName;
        this.selected = selected;
        this.existing = existing;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getInputPortName() {
        return inputPortName;
    }

    public void setInputPortName(String inputPortName) {
        this.inputPortName = inputPortName;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isExisting() {
        return existing;
    }

    public void setExisting(boolean existing) {
        this.existing = existing;
    }
}
