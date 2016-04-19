package com.thinkbiganalytics.nifi.rest.support;

import org.apache.nifi.web.api.dto.ProcessorDTO;
import org.apache.nifi.web.api.dto.TemplateDTO;

import java.util.List;

/**
 * Created by sr186054 on 1/21/16.
 */
public class NifiTemplateUtil {

    public static List<ProcessorDTO>getInputProcessorsForTemplate(TemplateDTO template ){
        List<String> sourceIds =  NifiConnectionUtil.getInputProcessorIds(template.getSnippet().getConnections());
        return NifiProcessUtil.findProcessorsByIds(template.getSnippet().getProcessors(), sourceIds);
    }

}
