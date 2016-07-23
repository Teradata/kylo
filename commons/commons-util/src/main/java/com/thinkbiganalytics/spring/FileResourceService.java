package com.thinkbiganalytics.spring;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by sr186054 on 7/12/16.
 */
public class FileResourceService implements ApplicationContextAware {

    ApplicationContext context;

    public ApplicationContext getContext() {
        return context;
    }

    @Override
    public void setApplicationContext(ApplicationContext context)
        throws BeansException {
        this.context = context;
    }

    public Resource getResource(String resource) {
        return context.getResource(resource);
    }

    public String getResourceAsString(String resourceLocation) {
        try {
            Resource resource = getResource(resourceLocation);
            if (resource != null) {
                InputStream is = resource.getInputStream();
                return IOUtils.toString(is);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }


}
