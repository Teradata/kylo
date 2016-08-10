package com.thinkbiganalytics.metadata.api.extension;

import javax.annotation.Nonnull;

/**
 * Builds a new type.
 */
public interface ExtensibleTypeBuilder {

    ExtensibleTypeBuilder supertype(ExtensibleType type);

    ExtensibleTypeBuilder displayName(String dispName);

    ExtensibleTypeBuilder description(String descr);

    ExtensibleTypeBuilder addField(String name, FieldDescriptor.Type type);

    FieldDescriptorBuilder field(String name);

    /**
     * Saves this type.
     *
     * @return this type
     */
    @Nonnull
    ExtensibleType build();
}
