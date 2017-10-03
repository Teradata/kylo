package com.thinkbiganalytics.feedmgr.service;

/*-
 * #%L
 * thinkbig-feed-manager-core
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;
import com.thinkbiganalytics.feedmgr.rest.model.UserField;
import com.thinkbiganalytics.feedmgr.rest.model.UserProperty;
import com.thinkbiganalytics.metadata.api.extension.UserFieldDescriptor;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Transforms user-defined properties between Feed Manager and Metadata formats.
 */
public final class UserPropertyTransform {

    /**
     * Instances of {@code UserPropertyTransform} should not be constructed.
     *
     * @throws UnsupportedOperationException always
     */
    private UserPropertyTransform() {
        throw new UnsupportedOperationException();
    }

    /**
     * Transforms the specified Metadata user-defined properties to Feed Manager user-defined properties.
     *
     * @param properties map of property names to values
     * @param userFields the user-defined field descriptors
     * @return the set of Feed Manager user-defined properties
     */
    @Nonnull
    @SafeVarargs
    public static Set<UserProperty> toUserProperties(@Nonnull final Map<String, String> properties, @Nonnull final Set<UserFieldDescriptor>... userFields) {
        // Map field names and order
        int order = 0;
        int size = Arrays.stream(userFields).collect(Collectors.summingInt(Set::size));
        final Map<String, UserFieldDescriptor> userFieldMap = Maps.newHashMapWithExpectedSize(size);
        final Map<String, Integer> userFieldOrder = Maps.newHashMapWithExpectedSize(size);

        for (final Set<UserFieldDescriptor> set : userFields) {
            final Iterator<UserFieldDescriptor> sorted = set.stream().sorted((f1, f2) -> Integer.compare(f1.getOrder(), f2.getOrder())).iterator();
            while (sorted.hasNext()) {
                final UserFieldDescriptor field = sorted.next();
                if (!userFieldMap.containsKey(field.getSystemName())) {
                    userFieldMap.put(field.getSystemName(), field);
                    userFieldOrder.put(field.getSystemName(), order++);
                }
            }
        }

        // Convert from Metadata to Feed Manager format
        final Stream<UserProperty> newProperties = userFieldMap.values().stream()
            .filter(field -> !properties.containsKey(field.getSystemName()))
            .map(field -> {
                final UserProperty property = new UserProperty();
                property.setDescription(field.getDescription());
                property.setDisplayName(field.getDisplayName());
                property.setLocked(true);
                property.setOrder(userFieldOrder.get(field.getSystemName()));
                property.setRequired(field.isRequired());
                property.setSystemName(field.getSystemName());
                return property;
            });

        final Stream<UserProperty> existingProperties = properties.entrySet().stream()
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
            });

        return Stream.concat(newProperties, existingProperties).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Transforms the specified Feed Manager user-defined properties to Metadata user-defined properties.
     *
     * @param userProperties the Feed Manager user-defined properties
     * @return a map of property names to values
     */
    @Nonnull
    public static Map<String, String> toMetadataProperties(@Nonnull final Set<UserProperty> userProperties) {
        return userProperties.stream()
            .filter(property -> property.getValue() != null)
            .collect(Collectors.toMap(UserProperty::getSystemName, UserProperty::getValue));
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
     * A {@link UserFieldDescriptor} backed by a {@link UserField}.
     */
    private static class FeedManagerUserFieldDescriptor implements UserFieldDescriptor {

        /**
         * Delegate user-defined field
         */
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
            return (userField.isRequired() != null) ? userField.isRequired() : false;
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
