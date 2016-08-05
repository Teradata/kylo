package com.thinkbiganalytics.metadata.api.extension;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * Provides access to {@link ExtensibleType} objects.
 */
public interface ExtensibleTypeProvider {

    ExtensibleType.ID resolve(Serializable ser);

    ExtensibleTypeBuilder buildType(String name);

    ExtensibleTypeBuilder updateType(ExtensibleType.ID id);

    ExtensibleType getType(ExtensibleType.ID id);

    ExtensibleType getType(String name);

    boolean deleteType(ExtensibleType.ID id);

    List<ExtensibleType> getTypes();

    List<ExtensibleType> getTypes(ExtensibleType type);

    Set<FieldDescriptor> getPropertyDescriptors(String nodeType);
}
