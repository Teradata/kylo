package com.thinkbiganalytics.feedmgr.service;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;
import com.thinkbiganalytics.feedmgr.rest.model.UserField;
import com.thinkbiganalytics.feedmgr.rest.model.UserProperty;
import com.thinkbiganalytics.metadata.api.extension.UserFieldDescriptor;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Transforms user-defined properties between Feed Manager and Metadata formats.
 */
public final class UserPropertyTransform {

    /**
     * Transforms the specified Metadata user-defined properties to Feed Manager user-defined properties.
     *
     * @param userFields  the ordered set of Metadata user-defined properties
     * @param fieldValues the map of property names to values
     * @return the set of Feed Manager user-defined properties
     */
    @Nonnull
    public static Set<UserProperty> toFeedManagerProperties(@Nonnull final Set<UserFieldDescriptor> userFields, @Nonnull final Map<String, String> fieldValues) {
        // Map field names and order
        int order = 0;
        final Map<String, UserFieldDescriptor> userFieldMap = Maps.newHashMapWithExpectedSize(userFields.size());
        final Map<String, Integer> userFieldOrder = Maps.newHashMapWithExpectedSize(userFields.size());

        for (UserFieldDescriptor field : userFields) {
            userFieldMap.put(field.getSystemName(), field);
            userFieldOrder.put(field.getSystemName(), order++);
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
                    final UserFieldDescriptor field = userFieldMap.get(entry.getKey());
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
     * Transforms the specified {@link UserField} objects into {@link UserFieldDescriptor} objects.
     *
     * @param userFields the user-defined fields
     * @return equivalent user-defined field descriptors
     */
    @Nonnull
    public static Set<UserFieldDescriptor> toUserFieldDescriptors(@Nonnull final Set<UserField> userFields) {
        return userFields.stream().map(FeedManagerUserFieldDescriptor::new).collect(Collectors.toSet());
    }

    /**
     * Transforms the specified {@link UserFieldDescriptor} objects into {@link UserField} objects.
     *
     * @param descriptors the user-defined field descriptors
     * @return equivalent user-defined fields
     */
    @Nonnull
    public static Set<UserField> toUserFields(@Nonnull final Set<UserFieldDescriptor> descriptors) {
        return descriptors.stream().map(descriptor -> {
            final UserField field = new UserField();
            field.setDescription(descriptor.getDescription());
            field.setDisplayName(descriptor.getDisplayName());
            field.setOrder(descriptor.getOrder());
            field.setRequired(descriptor.isRequired());
            field.setSystemName(descriptor.getSystemName());
            return field;
        }).collect(Collectors.toSet());
    }

    /**
     * Instances of {@code UserPropertyTransform} should not be constructed.
     *
     * @throws UnsupportedOperationException always
     */
    private UserPropertyTransform() {
        throw new UnsupportedOperationException();
    }

    /**
     * A {@link UserFieldDescriptor} backed by a {@link UserField}.
     */
    private static class FeedManagerUserFieldDescriptor implements UserFieldDescriptor {

        /** Delegate user-defined field */
        @Nonnull
        private final UserField userField;

        /**
         * Constructs a {@code FeedManagerUserFieldDescriptor} that delegates to the specified user-defined field.
         *
         * @param userField the user-defined field to delegate to
         */
        FeedManagerUserFieldDescriptor(@Nonnull final UserField userField) {
            this.userField = userField;
        }

        @Nullable
        @Override
        public String getDescription() {
            return userField.getDescription();
        }

        @Nullable
        @Override
        public String getDisplayName() {
            return userField.getDisplayName();
        }

        @Override
        public int getOrder() {
            return (userField.getOrder() != null) ? userField.getOrder() : 0;
        }

        @Override
        public boolean isRequired() {
            return (userField.getRequired() != null) ? userField.getRequired() : false;
        }

        @Nonnull
        @Override
        public String getSystemName() {
            return userField.getSystemName();
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(getSystemName());
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(UserFieldDescriptor.class)
                    .add("name", getSystemName())
                    .toString();
        }
    }
}
