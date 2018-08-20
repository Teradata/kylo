/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.support;

/*-
 * #%L
 * kylo-metadata-modeshape
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics, a Teradata Company
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

import static org.assertj.core.api.Assertions.assertThat;

import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.JcrPropertyTestConfig;
import com.thinkbiganalytics.metadata.modeshape.JcrTestConfig;
import com.thinkbiganalytics.metadata.modeshape.ModeShapeEngineConfig;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ModeShapeEngineConfig.class, JcrTestConfig.class, JcrPropertyTestConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class MultiValuePropertyListTest {

    @Inject
    private MetadataAccess metadata;

    @Before
    public void setup() {
        metadata.commit(() -> {
            Session session = JcrMetadataAccess.getActiveSession();
            Node testFolder = session.getRootNode().addNode("testFolder");
            Node testNode = testFolder.addNode("test");
            testNode.setProperty("prop1", new String[] { "a",  "b", "c" });
        }, MetadataAccess.SERVICE);
    }
    
    @Test
    public void testSize() {
        metadata.read(() -> {
            List<String> list = getList("prop1");
            
            assertThat(list.isEmpty()).isFalse();
            assertThat(list.size()).isEqualTo(3);
        }, MetadataAccess.SERVICE);
    }
    
    @Test
    public void testContains() {
        metadata.read(() -> {
            List<String> list = getList("prop1");
            
            assertThat(list.contains("a")).isTrue();
            assertThat(list.contains("b")).isTrue();
            assertThat(list.contains("c")).isTrue();
            assertThat(list.containsAll(Arrays.asList("a",  "b", "c"))).isTrue();
        }, MetadataAccess.SERVICE);
    }
    
    @Test
    public void testIndexOf() {
        metadata.read(() -> {
            List<String> list = getList("prop1");
            int bIdx = list.indexOf("b");
            int xIdx = list.indexOf("x");
            
            assertThat(bIdx).isEqualTo(1);
            assertThat(xIdx).isEqualTo(-1);
        }, MetadataAccess.SERVICE);
    }
    
    @Test
    public void testLastIndexOf() {
        metadata.read(() -> {
            List<String> list = getList("prop1");
            int bIdx = list.lastIndexOf("b");
            int xIdx = list.lastIndexOf("x");
            
            assertThat(bIdx).isEqualTo(1);
            assertThat(xIdx).isEqualTo(-1);
        }, MetadataAccess.SERVICE);
    }
    
    @Test
    public void testToArray() {
        metadata.read(() -> {
            List<String> list = getList("prop1");
            String[] arrInit = new String[list.size()];
            String[] arr1 = list.toArray(arrInit);
            Object[] arr2 = list.toArray();
            
            assertThat(arr1).hasSize(3);
            assertThat(arr2).hasSize(3);
            assertThat(arr1).isSameAs(arrInit);
            assertThat(arr2).isNotSameAs(arrInit);
            assertThat(arr1).contains("a",  "b", "c");
            assertThat(arr2).contains("a",  "b", "c");
        }, MetadataAccess.SERVICE);
    }
    
    @Test
    public void testIterator() {
        metadata.read(() -> {
            List<String> list = getList("prop1");
            Iterator<String> itr = list.iterator();
            
            assertThat(itr.hasNext()).isTrue();
            assertThat(itr.next()).isEqualTo("a");
            assertThat(itr.hasNext()).isTrue();
            assertThat(itr.next()).isEqualTo("b");
            assertThat(itr.hasNext()).isTrue();
            assertThat(itr.next()).isEqualTo("c");
            assertThat(itr.hasNext()).isFalse();
        }, MetadataAccess.SERVICE);
    }
    
    @Test
    public void testAdd() {
        metadata.commit(() -> {
            List<String> list = getList("prop1");
            boolean result = list.add("x");
            result |= list.add("y");
            
            assertThat(result).isTrue();
        }, MetadataAccess.SERVICE);
        
        metadata.read(() -> {
            List<String> list = getList("prop1");
            
            assertThat(list).contains("a", "b", "c", "x", "y");
        }, MetadataAccess.SERVICE);
    }
    
    @Test
    public void testAddAll() {
        metadata.commit(() -> {
            List<String> list = getList("prop1");
            boolean result = list.addAll(Arrays.asList("x", "y", "z"));
            
            assertThat(result).isTrue();
        }, MetadataAccess.SERVICE);
        
        metadata.read(() -> {
            List<String> list = getList("prop1");
            
            assertThat(list).contains("a", "b", "c", "x", "y", "z");
        }, MetadataAccess.SERVICE);
    }
    
    @Test
    public void testAddIdx() {
        metadata.commit(() -> {
            List<String> list = getList("prop1");
            list.add(1, "x");
            list.add(1, "y");
        }, MetadataAccess.SERVICE);
        
        metadata.read(() -> {
            List<String> list = getList("prop1");
            
            assertThat(list).contains("a", "y", "x", "b", "c");
        }, MetadataAccess.SERVICE);
    }
    
    @Test
    public void testAddAllIdx() {
        metadata.commit(() -> {
            List<String> list = getList("prop1");
            boolean result = list.addAll(1, Arrays.asList("x", "y", "z"));
            
            assertThat(result).isTrue();
        }, MetadataAccess.SERVICE);
        
        metadata.read(() -> {
            List<String> list = getList("prop1");
            
            assertThat(list).contains("a", "x", "y", "z", "b", "c");
        }, MetadataAccess.SERVICE);
    }
    
    @Test
    public void testRemove() {
        metadata.commit(() -> {
            List<String> list = getList("prop1");
            boolean result = list.remove("x");
            
            assertThat(result).isFalse();
            
            result |= list.remove("a");
            result |= list.remove("c");
            
            assertThat(result).isTrue();
        }, MetadataAccess.SERVICE);
        
        metadata.read(() -> {
            List<String> list = getList("prop1");
            
            assertThat(list).contains("b");
        }, MetadataAccess.SERVICE);
    }
    
    @Test
    public void testRemoveAll() {
        metadata.commit(() -> {
            List<String> list = getList("prop1");
            boolean result = list.removeAll(Arrays.asList("x", "a", "c"));
            
            assertThat(result).isTrue();
        }, MetadataAccess.SERVICE);
        
        metadata.read(() -> {
            List<String> list = getList("prop1");
            
            assertThat(list).contains("b");
        }, MetadataAccess.SERVICE);
    }
    
    @Test
    public void testRemoveIdx() {
        metadata.commit(() -> {
            List<String> list = getList("prop1");
            String oldElem = list.remove(1);
            
            assertThat(oldElem).isEqualTo("b");
        }, MetadataAccess.SERVICE);
        
        metadata.read(() -> {
            List<String> list = getList("prop1");
            
            assertThat(list).contains("a", "c");
        }, MetadataAccess.SERVICE);
    }
    
    @Test
    public void testSetIdx() {
        metadata.commit(() -> {
            List<String> list = getList("prop1");
            String oldElem = list.set(1, "x");
            
            assertThat(oldElem).isEqualTo("b");
        }, MetadataAccess.SERVICE);
        
        metadata.read(() -> {
            List<String> list = getList("prop1");
            
            assertThat(list).contains("a", "x", "c");
        }, MetadataAccess.SERVICE);
    }
    
    @Test
    public void testSublist() {
        metadata.commit(() -> {
            List<String> list = getList("prop1");
            List<String> sublist = list.subList(1, list.size());
            
            assertThat(list).contains("b", "c");
            
            sublist.set(0, "x");
        }, MetadataAccess.SERVICE);
        
        metadata.read(() -> {
            List<String> list = getList("prop1");
            
            assertThat(list).contains("a", "x", "c");
        }, MetadataAccess.SERVICE);
    }

    
    
    
    
    protected List<String> getList(String name) throws PathNotFoundException, RepositoryException {
        Property prop1 = getProperty(name);
        return new MultiValuePropertyList<>(prop1);
    }
    
    protected Property getProperty(String name) throws PathNotFoundException, RepositoryException {
        Session session = JcrMetadataAccess.getActiveSession();
        Node testNode = session.getNode("/testFolder/test");
        Property prop1 = testNode.getProperty(name);
        return prop1;
    }
    
}
