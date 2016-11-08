package com.thinkbiganalytics.metadata.api.feedmgr.template;

import com.thinkbiganalytics.metadata.api.MetadataException;

/**
 * Created by sr186054 on 11/7/16.
 */
public class TemplateDeletionException extends MetadataException {

    private static final long serialVersionUID = 1L;

    String templateName;
    String templateId;


    public TemplateDeletionException(String templateName, String templateId, String message) {
        super("Unable to delete the template with the name " + templateName + ", id: " + templateId + " " + (message != null ? message : ""));
        this.templateName = templateName;
        this.templateId = templateId;
    }
}
