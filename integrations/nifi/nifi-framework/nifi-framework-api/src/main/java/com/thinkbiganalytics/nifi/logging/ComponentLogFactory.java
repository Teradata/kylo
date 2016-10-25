package com.thinkbiganalytics.nifi.logging;

import org.apache.nifi.logging.ComponentLog;
import org.apache.nifi.processor.ProcessorInitializationContext;

import javax.annotation.Nonnull;

/**
 * Produces {@link ComponentLog} objects from various contexts.
 */
public interface ComponentLogFactory {

    /**
     * Returns a {@link ComponentLog} for the specified processor initialization context.
     *
     * @param context the processor initialization context
     * @return the component log
     */
    @Nonnull
    ComponentLog getLog(@Nonnull ProcessorInitializationContext context);
}
