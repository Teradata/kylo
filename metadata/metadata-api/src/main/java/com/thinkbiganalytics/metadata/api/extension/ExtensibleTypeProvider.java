package com.thinkbiganalytics.metadata.api.extension;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * Provides access to {@link ExtensibleType} objects.
 */
public interface ExtensibleTypeProvider {

    ExtensibleType.ID resolve(Serializable ser);

    /**
     * Creates a builder for a new type with the specified name.
     *
     * @param name the type name
     * @return the type builder
     * @throws RuntimeException if a type with the same name already exists, or the repository is unavailable
     */
    @Nonnull
    ExtensibleTypeBuilder buildType(@Nonnull String name);

    /**
     * Creates a builder that overwrites the specified type.
     *
     * @param id the type id
     * @return the type builder
     * @throws RuntimeException if the type does not exist, or the repository is unavailable
     */
    @Nonnull
    ExtensibleTypeBuilder updateType(@Nonnull ExtensibleType.ID id);

    ExtensibleType getType(ExtensibleType.ID id);

    ExtensibleType getType(String name);

    boolean deleteType(ExtensibleType.ID id);

    List<ExtensibleType> getTypes();

    List<ExtensibleType> getTypes(ExtensibleType type);

    Set<FieldDescriptor> getPropertyDescriptors(String nodeType);
}
