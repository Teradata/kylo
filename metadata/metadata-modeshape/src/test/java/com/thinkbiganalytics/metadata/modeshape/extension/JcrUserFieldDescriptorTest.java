package com.thinkbiganalytics.metadata.modeshape.extension;

/*-
 * #%L
 * thinkbig-metadata-modeshape
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

import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.category.CategoryProvider;
import com.thinkbiganalytics.metadata.api.extension.UserFieldDescriptor;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.JcrTestConfig;
import com.thinkbiganalytics.metadata.modeshape.ModeShapeEngineConfig;
import com.thinkbiganalytics.metadata.modeshape.feed.FeedTestConfig;
import com.thinkbiganalytics.metadata.modeshape.feed.FeedTestUtil;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.nodetype.PropertyDefinition;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ModeShapeEngineConfig.class, JcrTestConfig.class, FeedTestConfig.class})
@ComponentScan(basePackages = {"com.thinkbiganalytics.metadata.modeshape"})
public class JcrUserFieldDescriptorTest {

    @Inject
    private FeedTestUtil feedTestUtil;

    @Inject
    private JcrMetadataAccess metadataAccess;

    @Inject
    private FeedProvider feedProvider;

    @Inject
    private CategoryProvider categoryProvider;

    @Test
    public void testFeedCreateAndRemove() {
        Set<UserFieldDescriptor> descriptors = new HashSet<>();

        FeedManagerUserFieldDescriptor descriptor1 = new FeedManagerUserFieldDescriptor();
        descriptor1.setSystemName("property1");
        descriptor1.setDisplayName("Property1");
        descriptor1.setDescription(" desc for property1");
        descriptor1.setOrder(1);
        descriptor1.setRequired(true);

        descriptors.add(descriptor1);

        FeedManagerUserFieldDescriptor descriptor2 = new FeedManagerUserFieldDescriptor();
        descriptor2.setSystemName("property2");
        descriptor2.setDisplayName("Property2");
        descriptor2.setDescription(" desc for property2");
        descriptor2.setOrder(2);
        descriptor2.setRequired(false);

        descriptors.add(descriptor2);

        metadataAccess.commit(() -> {
            feedProvider.setUserFields(descriptors);
        }, MetadataAccess.ADMIN);

        Set<UserFieldDescriptor> feedManagerFieldDescriptors = metadataAccess.read(() -> {
            return transformFieldDescriptors(feedProvider.getUserFields());
        }, MetadataAccess.ADMIN);

        Assert.assertTrue(feedManagerFieldDescriptors.size() == 2);

        //now try to delete the first one
        feedManagerFieldDescriptors.removeIf(descriptor -> descriptor.getOrder() == 1);
        Assert.assertTrue(feedManagerFieldDescriptors.size() == 1);

        metadataAccess.commit(() -> {
            feedProvider.setUserFields(feedManagerFieldDescriptors);
        }, MetadataAccess.ADMIN);

        Set<UserFieldDescriptor> finalDescriptors = metadataAccess.read(() -> feedProvider.getUserFields(), MetadataAccess.ADMIN);
        Assert.assertTrue(finalDescriptors.size() == 1);

    }


    @Test
    public void testCategoryCreateAndRemove() {
        Set<UserFieldDescriptor> descriptors = new HashSet<>();

        FeedManagerUserFieldDescriptor descriptor1 = new FeedManagerUserFieldDescriptor();
        descriptor1.setSystemName("property1");
        descriptor1.setDisplayName("Property1");
        descriptor1.setDescription(" desc for property1");
        descriptor1.setOrder(1);
        descriptor1.setRequired(true);

        descriptors.add(descriptor1);

        FeedManagerUserFieldDescriptor descriptor2 = new FeedManagerUserFieldDescriptor();
        descriptor2.setSystemName("property2");
        descriptor2.setDisplayName("Property2");
        descriptor2.setDescription(" desc for property2");
        descriptor2.setOrder(2);
        descriptor2.setRequired(false);

        descriptors.add(descriptor2);

        metadataAccess.commit(() -> {
            categoryProvider.setUserFields(descriptors);
        }, MetadataAccess.ADMIN);

        Set<UserFieldDescriptor> feedManagerFieldDescriptors = metadataAccess.read(() -> {
            return transformFieldDescriptors(categoryProvider.getUserFields());
        }, MetadataAccess.ADMIN);

        Assert.assertTrue(feedManagerFieldDescriptors.size() == 2);

        //now try to delete the first one
        feedManagerFieldDescriptors.removeIf(descriptor -> descriptor.getOrder() == 1);
        Assert.assertTrue(feedManagerFieldDescriptors.size() == 1);

        metadataAccess.commit(() -> {
            categoryProvider.setUserFields(feedManagerFieldDescriptors);
        }, MetadataAccess.ADMIN);

        Set<UserFieldDescriptor> finalDescriptors = metadataAccess.read(() -> categoryProvider.getUserFields(), MetadataAccess.ADMIN);
        Assert.assertTrue(finalDescriptors.size() == 1);

    }


    private Set<UserFieldDescriptor> transformFieldDescriptors(Set<UserFieldDescriptor> descriptors) {
        return descriptors.stream().map(f ->
                                        {
                                            FeedManagerUserFieldDescriptor d = new FeedManagerUserFieldDescriptor();
                                            d.setSystemName(f.getSystemName());
                                            d.setDisplayName(f.getDisplayName());
                                            d.setDescription(f.getDescription());
                                            d.setOrder(f.getOrder());
                                            d.setRequired(f.isRequired());
                                            return d;
                                        }).collect(Collectors.toSet());
    }

    /**
     * Verify getting the description
     */
    @Test
    @SuppressWarnings("unchecked")
    public void getDescription() throws Exception {
        // Mock property node
        final Property description = Mockito.when(Mockito.mock(Property.class).getString()).thenReturn("Node for testing").getMock();

        final Node node = Mockito.mock(Node.class);
        Mockito.when(node.getProperty(JcrExtensibleType.DESCRIPTION))
            .thenReturn(description)
            .thenThrow(PathNotFoundException.class);

        // Test description
        final JcrUserFieldDescriptor userField = new JcrUserFieldDescriptor(node, Mockito.mock(PropertyDefinition.class));
        Assert.assertEquals("Node for testing", userField.getDescription());
        Assert.assertNull(userField.getDescription());
    }

    /**
     * Verify getting the display name
     */
    @Test
    @SuppressWarnings("unchecked")
    public void getDisplayName() throws Exception {
        // Mock property node
        final Property displayName = Mockito.when(Mockito.mock(Property.class).getString()).thenReturn("Test Node").getMock();

        final Node node = Mockito.mock(Node.class);
        Mockito.when(node.getProperty(JcrExtensibleType.NAME))
            .thenReturn(displayName)
            .thenThrow(PathNotFoundException.class);

        // Test display name
        final JcrUserFieldDescriptor userField = new JcrUserFieldDescriptor(node, Mockito.mock(PropertyDefinition.class));
        Assert.assertEquals("Test Node", userField.getDisplayName());
        Assert.assertNull(userField.getDisplayName());
    }

    /**
     * Verify getting the order
     */
    @Test
    @SuppressWarnings("unchecked")
    public void getOrder() throws Exception {
        // Mock property node
        final Property order = Mockito.when(Mockito.mock(Property.class).getString()).thenReturn("12").getMock();

        final Node node = Mockito.mock(Node.class);
        Mockito.when(node.getProperty(JcrUserFieldDescriptor.ORDER))
            .thenReturn(order)
            .thenThrow(PathNotFoundException.class);

        // Test order
        final JcrUserFieldDescriptor userField = new JcrUserFieldDescriptor(node, Mockito.mock(PropertyDefinition.class));
        Assert.assertEquals(12, userField.getOrder());
        Assert.assertEquals(0, userField.getOrder());
    }

    /**
     * Verify checking if required
     */
    @Test
    @SuppressWarnings("unchecked")
    public void isRequired() throws Exception {
        // Mock property node
        final Property falseProperty = Mockito.when(Mockito.mock(Property.class).getString()).thenReturn("false").getMock();
        final Property trueProperty = Mockito.when(Mockito.mock(Property.class).getString()).thenReturn("true").getMock();

        final Node node = Mockito.mock(Node.class);
        Mockito.when(node.getProperty(JcrUserFieldDescriptor.REQUIRED))
            .thenReturn(falseProperty)
            .thenReturn(trueProperty)
            .thenThrow(PathNotFoundException.class);

        // Test required
        final JcrUserFieldDescriptor userField = new JcrUserFieldDescriptor(node, Mockito.mock(PropertyDefinition.class));
        Assert.assertFalse(userField.isRequired());
        Assert.assertTrue(userField.isRequired());
        Assert.assertFalse(userField.isRequired());
    }

    /**
     * Verify getting the system name
     */
    @Test
    public void getSystemName() throws Exception {
        final PropertyDefinition property = Mockito.when(Mockito.mock(PropertyDefinition.class).getName())
            .thenReturn(JcrMetadataAccess.USR_PREFIX + ":testProp")
            .thenReturn(JcrMetadataAccess.USR_PREFIX + ":%E7%A2%BC%E6%A8%99%E6%BA%96%E8%90%AC%E5%9C%8B%E7%A2%BC%2F1.1%2F%3Fname%3D%2520")
            .getMock();
        final JcrUserFieldDescriptor userField = new JcrUserFieldDescriptor(Mockito.mock(Node.class), property);
        Assert.assertEquals("testProp", userField.getSystemName());
        Assert.assertEquals("碼標準萬國碼/1.1/?name=%20", userField.getSystemName());
    }


    private static class FeedManagerUserFieldDescriptor implements UserFieldDescriptor {

        private String description;
        private String displayName;
        private String systemName;
        private int order;
        private boolean required;

        @Nullable
        @Override
        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        @Nullable
        @Override
        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        @Nonnull
        @Override
        public String getSystemName() {
            return systemName;
        }

        public void setSystemName(String systemName) {
            this.systemName = systemName;
        }

        @Override
        public int getOrder() {
            return order;
        }

        public void setOrder(int order) {
            this.order = order;
        }

        @Override
        public boolean isRequired() {
            return required;
        }

        public void setRequired(boolean required) {
            this.required = required;
        }
    }
}
