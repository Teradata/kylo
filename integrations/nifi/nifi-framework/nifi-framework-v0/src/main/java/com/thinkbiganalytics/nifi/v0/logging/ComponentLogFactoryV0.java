package com.thinkbiganalytics.nifi.v0.logging;

import com.thinkbiganalytics.nifi.logging.ComponentLogFactory;

import org.apache.nifi.logging.ComponentLog;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

/**
 * An implementation of {@link ComponentLogFactory} for NiFi v0.6.
 */
@Component
public class ComponentLogFactoryV0 implements ComponentLogFactory {

    @Nonnull
    @Override
    public ComponentLog getLog(@Nonnull ProcessorInitializationContext context) {
        return context.getLogger();
    }
}
