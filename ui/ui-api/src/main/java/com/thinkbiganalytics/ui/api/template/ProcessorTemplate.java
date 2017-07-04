package com.thinkbiganalytics.ui.api.template;

import java.util.List;

import javax.annotation.Nullable;

/**
 * Created by sr186054 on 7/4/17.
 */
public interface ProcessorTemplate {


    /**
     * Return an array of the NiFi processor class name (i.e. com.thinkbiganalytics.nifi.GetTableData)
     * @return Return an array of the NiFi processor class name (i.e. com.thinkbiganalytics.nifi.GetTableData)
     */
   List getProcessorTypes();

    /**
     * the url for the template
     * @return
     */
    @Nullable
    default String getStepperTemplateUrl()  {
        return null;
    };


    @Nullable
    default String getFeedDetailsTemplateUrl() {
        return null;
    }

}
