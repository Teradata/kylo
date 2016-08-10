package com.thinkbiganalytics.metadata.api.extension;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A user-defined field on categories or feeds.
 */
public interface UserFieldDescriptor {

    /**
     * Gets a human-readable specification for this field.
     *
     * @return a human-readable specification
     */
    @Nullable
    String getDescription();

    /**
     * Gets a human-readable title for this field.
     *
     * @return a human-readable title
     */
    @Nullable
    String getDisplayName();

    /**
     * Gets the index of the display order for this field.
     *
     * <p>The first property will have a value of 0, with later fields increasing in value.</p>
     *
     * @return the display order index
     */
    int getOrder();

    /**
     * Indicates that this field is required.
     *
     * @return {@code true} if this field is required; {@code false} or {@code null} otherwise
     */
    boolean isRequired();

    /**
     * Gets the internal identifier for this field.
     *
     * @return the internal identifier
     */
    @Nonnull
    String getSystemName();
}
