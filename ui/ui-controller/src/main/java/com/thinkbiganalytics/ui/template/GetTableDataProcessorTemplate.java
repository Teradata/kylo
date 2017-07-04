package com.thinkbiganalytics.ui.template;

import com.google.common.collect.Lists;
import com.thinkbiganalytics.ui.api.template.ProcessorTemplate;

import org.springframework.stereotype.Component;

import java.util.List;

import javax.annotation.Nullable;

/**
 * Created by sr186054 on 7/4/17.
 */
@Component
public class GetTableDataProcessorTemplate implements ProcessorTemplate {

    @Override
    public List getProcessorTypes() {
        return Lists.newArrayList(new String[]{"com.thinkbiganalytics.nifi.GetTableData","com.thinkbiganalytics.nifi.v2.ingest.GetTableData"});
    }

    @Nullable
    @Override
    public String getStepperTemplateUrl() {
        return "js/plugin/processor-templates/GetTableData/get-table-data-create.html";
    }

    @Nullable
    @Override
    public String getFeedDetailsTemplateUrl() {
        return "js/plugin/processor-templates/GetTableData/get-table-data-edit.html";
    }
}
