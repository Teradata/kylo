package com.thinkbiganalytics.metadata.api.extension;

import javax.annotation.Nonnull;

/**
 * Builds a new field.
 */
public interface FieldDescriptorBuilder {

    FieldDescriptorBuilder name(String name);

    FieldDescriptorBuilder type(FieldDescriptor.Type type);

    FieldDescriptorBuilder displayName(String name);

    FieldDescriptorBuilder description(String descr);

    FieldDescriptorBuilder collection(boolean flag);

    FieldDescriptorBuilder required(boolean flag);

    /**
     * Adds the specified metadata property to this field.
     *
     * @param name the property name
     * @param value the property value
     * @return this field
     */
    @Nonnull
    FieldDescriptorBuilder property(@Nonnull String name, @Nonnull String value);

    ExtensibleTypeBuilder add();
}
