package com.thinkbiganalytics.feedmgr.rest.model;

/**
 * Created by sr186054 on 7/15/16.
 */
public class ImportOptions {

    public enum IMPORT_CONNECTING_FLOW {
        YES, NO, NOT_SET
    }

    private String categorySystemName;
    private boolean createReusableFlow;
    private boolean overwrite;
    private IMPORT_CONNECTING_FLOW importConnectingFlow;

    public void setCategorySystemName(String categorySystemName) {
        this.categorySystemName = categorySystemName;
    }

    public String getCategorySystemName() {
        return categorySystemName;
    }

    public boolean isCreateReusableFlow() {
        return createReusableFlow;
    }

    public void setCreateReusableFlow(boolean createReusableFlow) {
        this.createReusableFlow = createReusableFlow;
    }

    public boolean isOverwrite() {
        return overwrite;
    }

    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

    public IMPORT_CONNECTING_FLOW getImportConnectingFlow() {
        return importConnectingFlow;
    }

    public void setImportConnectingFlow(IMPORT_CONNECTING_FLOW importConnectingFlow) {
        this.importConnectingFlow = importConnectingFlow;
    }
}