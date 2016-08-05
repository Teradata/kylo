package com.thinkbiganalytics.feedmgr.service;

import com.google.common.collect.Maps;
import com.thinkbiganalytics.feedmgr.rest.model.UserProperty;
import com.thinkbiganalytics.metadata.api.extension.FieldDescriptor;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

/**
 * Transforms user-defined properties between Feed Manager and Metadata formats.
 */
public final class UserPropertyTransform {

    /**
     * Transforms the specified Metadata user-defined properties to Feed Manager user-defined properties.
     *
     * @param userFields the ordered set of Metadata user-defined properties
     * @param fieldValues the map of property names to values
     * @return the set of Feed Manager user-defined properties
     */
    @Nonnull
    public static Set<UserProperty> toFeedManagerProperties(@Nonnull final Set<FieldDescriptor> userFields, @Nonnull final Map<String, String> fieldValues) {
        // Map field names and order
        int order = 0;
        final Map<String, FieldDescriptor> userFieldMap = Maps.newHashMapWithExpectedSize(userFields.size());
        final Map<String, Integer> userFieldOrder = Maps.newHashMapWithExpectedSize(userFields.size());

        for (FieldDescriptor field : userFields) {
            userFieldMap.put(field.getName(), field);
            userFieldOrder.put(field.getName(), order++);
        }

        // Convert from Metadata to Feed Manager format
        return fieldValues.entrySet().stream()
                .map(entry -> {
                    // Create the Feed Manager property
                    final UserProperty property = new UserProperty();
                    property.setLocked(false);
                    property.setSystemName(entry.getKey());
                    property.setValue(entry.getValue());

                    // Set additional Metadata attributes
                    final FieldDescriptor field = userFieldMap.get(entry.getKey());
                    if (field != null) {
                        property.setDescription(field.getDescription());
                        property.setDisplayName(field.getDisplayName());
                        property.setLocked(true);
                        property.setOrder(userFieldOrder.get(entry.getKey()));
                        property.setRequired(field.isRequired());
                    }

                    // Return the Feed Manager property
                    return property;
                })
                .collect(Collectors.toSet());
    }

    /**
     * Transforms the specified Feed Manager user-defined properties to Metadata user-defined properties.
     *
     * @param userProperties the Feed Manager user-defined properties
     * @return a map of property names to values
     */
    @Nonnull
    public static Map<String, String> toMetadataProperties(@Nonnull final Set<UserProperty> userProperties) {
        return userProperties.stream().collect(Collectors.toMap(UserProperty::getSystemName, UserProperty::getValue));
    }

    /**
     * Instances of {@code UserPropertyTransform} should not be constructed.
     *
     * @throws UnsupportedOperationException always
     */
    private UserPropertyTransform() {
        throw new UnsupportedOperationException();
    }
}
