package com.thinkbiganalytics.metadata.modeshape.support;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.thinkbiganalytics.metadata.api.MissingUserPropertyException;
import com.thinkbiganalytics.metadata.api.extension.UserFieldDescriptor;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;

public class JcrPropertyUtilTest {

    /** Verify getting user properties. */
    @Test
    public void getUserProperties() throws Exception {
        // Mock properties
        final Property prop1 = Mockito.mock(Property.class);
        Mockito.when(prop1.getName()).thenReturn("*");

        final Property prop2 = Mockito.mock(Property.class);
        Mockito.when(prop2.getName()).thenReturn("usr:testProp");
        Mockito.when(prop2.getString()).thenReturn("one");

        final Property prop3 = Mockito.mock(Property.class);
        Mockito.when(prop3.getName()).thenReturn("usr:%E7%A2%BC%E6%A8%99%E6%BA%96%E8%90%AC%E5%9C%8B%E7%A2%BC%2F1.1%2F%3Fname%3D%2520");
        Mockito.when(prop3.getString()).thenReturn("two");

        // Mock node
        final Iterator<Property> delegateIter = ImmutableList.of(prop1, prop2, prop3).iterator();
        final PropertyIterator propIter = Mockito.mock(PropertyIterator.class);
        Mockito.when(propIter.hasNext()).thenAnswer(invocation -> delegateIter.hasNext());
        Mockito.when(propIter.nextProperty()).thenAnswer(invocation -> delegateIter.next());

        final Node node = Mockito.when(Mockito.mock(Node.class).getProperties()).thenReturn(propIter).getMock();

        // Test user properties
        final Map<String, String> userProps = JcrPropertyUtil.getUserProperties(node);
        Assert.assertEquals(2, userProps.size());
        Assert.assertEquals("one", userProps.get("testProp"));
        Assert.assertEquals("two", userProps.get("碼標準萬國碼/1.1/?name=%20"));
    }

    /** Verify setting user properties. */
    @Test
    public void setUserProperties() throws Exception {
        // Mock node
        final Property oldProp = Mockito.when(Mockito.mock(Property.class).getName()).thenReturn("usr:oldProp").getMock();

        final Iterator<Property> delegateIter = ImmutableList.of(oldProp).iterator();
        final PropertyIterator propIter = Mockito.mock(PropertyIterator.class);
        Mockito.when(propIter.hasNext()).thenAnswer(invocation -> delegateIter.hasNext());
        Mockito.when(propIter.nextProperty()).thenAnswer(invocation -> delegateIter.next());

        final Node node = Mockito.mock(Node.class);
        Mockito.when(node.getProperties()).thenReturn(propIter);

        // Mock field
        final UserFieldDescriptor field = Mockito.mock(UserFieldDescriptor.class);
        Mockito.when(field.getSystemName()).thenReturn("testProp");
        Mockito.when(field.isRequired()).thenReturn(true);

        // Test setting user properties
        final Map<String, String> properties = ImmutableMap.of("testProp", "one", "碼標準萬國碼/1.1/?name=%20", "two");

        JcrPropertyUtil.setUserProperties(node, Collections.singleton(field), properties);
        Mockito.verify(node).setProperty("usr:testProp", "one");
        Mockito.verify(node).setProperty("usr:%E7%A2%BC%E6%A8%99%E6%BA%96%E8%90%AC%E5%9C%8B%E7%A2%BC%2F1.1%2F%3Fname%3D%2520", "two");
        Mockito.verify(node).getProperties();
        Mockito.verify(oldProp).remove();
        Mockito.verifyNoMoreInteractions(node);
    }

    /** Verify exception if a required property is not set. */
    @Test(expected = MissingUserPropertyException.class)
    public void setUserPropertiesWithMissing() {
        // Mock field
        final UserFieldDescriptor field = Mockito.mock(UserFieldDescriptor.class);
        Mockito.when(field.getSystemName()).thenReturn("requiredProperty");
        Mockito.when(field.isRequired()).thenReturn(true);

        // Test required property
        JcrPropertyUtil.setUserProperties(Mockito.mock(Node.class), Collections.singleton(field), Collections.emptyMap());
    }
}
