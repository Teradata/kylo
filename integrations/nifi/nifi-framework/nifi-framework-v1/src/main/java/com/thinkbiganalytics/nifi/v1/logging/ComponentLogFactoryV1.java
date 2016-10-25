package com.thinkbiganalytics.nifi.v1.logging;

import com.thinkbiganalytics.nifi.logging.ComponentLogFactory;

import org.apache.nifi.logging.ComponentLog;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

/**
 * An implementation of {@link ComponentLogFactory} for NiFi v1.0.
 */
@Component
public class ComponentLogFactoryV1 implements ComponentLogFactory {

    @Nonnull
    @Override
    public ComponentLog getLog(@Nonnull ProcessorInitializationContext context) {
        return context.getLogger();
    }
}
