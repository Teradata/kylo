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

import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;

/**
 * An set implementation that wraps a JCR multi-valued property and supports all access and modification operations.
 */
public class MultiValuePropertySet<E> extends MultiValuePropertyCollection<E> implements Set<E> {

    private static Set<Value> toValueSet(Property prop) {
        try {
            return Arrays.stream(prop.getValues()).collect(Collectors.toSet());
        } catch (ValueFormatException e) {
            throw new IllegalArgumentException("The property provided must be multi-valued: " + prop);
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Unable to construct a set from the property: " + prop, e);
        }
    }
    
    public MultiValuePropertySet(Node parent, String propertyName) {
        this(parent, propertyName, false);
    }
    
    public MultiValuePropertySet(Node parent, String propertyName, boolean weakRefs) {
        super(parent, propertyName, weakRefs, new HashSet<>());
    }
    
    public MultiValuePropertySet(Property prop) {
        super(prop, toValueSet(prop));
    }
    
    public MultiValuePropertySet(Property prop, Set<Value> values) {
        super(prop, values);
    }

}
