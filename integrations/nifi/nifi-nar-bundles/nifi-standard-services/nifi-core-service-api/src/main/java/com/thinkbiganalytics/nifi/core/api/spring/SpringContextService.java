package com.thinkbiganalytics.nifi.core.api.spring;

import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.controller.ControllerService;
import org.springframework.beans.BeansException;

/**
 * @author Sean Felten
 */
@Tags({"thinkbig", "spring", "bean", "context"})
@CapabilityDescription("Provides access to spring beans loaded from a spring configuration")
public interface SpringContextService extends ControllerService {

    <T> T getBean(Class<T> requiredType) throws BeansException;

    <T> T getBean(String name, Class<T> requiredType) throws BeansException;

}
