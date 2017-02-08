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

import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.nodetype.PropertyDefinition;

public class JcrUserFieldDescriptorTest {

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
}
