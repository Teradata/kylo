package com.thinkbiganalytics.discovery.rest.controller;

import java.util.List;

public class SparkFilesScript {

    String parserDescriptor;
    List<String> files;
    String dataFrameVariable;

    public SparkFilesScript(){

    }

    public String getParserDescriptor() {
        return parserDescriptor;
    }

    public void setParserDescriptor(String parserDescriptor) {
        this.parserDescriptor = parserDescriptor;
    }

    public List<String> getFiles() {
        return files;
    }

    public void setFiles(List<String> files) {
        this.files = files;
    }

    public String getDataFrameVariable() {
        return dataFrameVariable;
    }

    public void setDataFrameVariable(String dataFrameVariable) {
        this.dataFrameVariable = dataFrameVariable;
    }
}
