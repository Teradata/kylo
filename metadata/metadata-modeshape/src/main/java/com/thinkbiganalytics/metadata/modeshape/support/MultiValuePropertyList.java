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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;

/**
 * An list implementation that wraps a JCR multi-valued property and supports all access and modification operations.
 */
public class MultiValuePropertyList<E> extends MultiValuePropertyCollection<E> implements List<E> {
    
    private  MultiValuePropertyList<E> parentList;

    private static List<Value> toValueList(Property prop) {
        try {
            return Arrays.stream(prop.getValues()).collect(Collectors.toList());
        } catch (ValueFormatException e) {
            throw new IllegalArgumentException("The property provided must be multi-valued: " + prop);
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Unable to construct a list from the property: " + prop, e);
        }
    }
    
    public MultiValuePropertyList(Node parent, String propertyName) {
        this(parent, propertyName, false);
    }
    
    public MultiValuePropertyList(Node parent, String propertyName, boolean weakRefs) {
        super(parent, propertyName, weakRefs, new ArrayList<>());
    }
    
    public MultiValuePropertyList(Node parent, String propertyName, List<Value> list) {
        this(parent, propertyName, false, list);
    }
    
    public MultiValuePropertyList(Node parent, String propertyName, boolean weakRefs, List<Value> list) {
        super(parent, propertyName, weakRefs, list);
    }
    
    public MultiValuePropertyList(Node parent, String propertyName, List<Value> list, MultiValuePropertyList<E> parentList) {
        super(parent, propertyName, parentList.isWeakReferences(), list);
        this.parentList = parentList;
    }

    public MultiValuePropertyList(Property prop) {
        super(prop, toValueList(prop));
    }
    
    protected MultiValuePropertyList(Property prop, List<Value> list) {
        this(prop, list, null);
    }
    
    protected MultiValuePropertyList(Property prop, List<Value> list, MultiValuePropertyList<E> parentList) {
        super(prop, list);
        this.parentList = parentList;
    }

    /* (non-Javadoc)
     * @see java.util.List#addAll(int, java.util.Collection)
     */
    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        List<Value> values = c.stream()
                        .map(e -> JcrPropertyUtil.asValue(getValueFactory(), e))
                        .collect(Collectors.toList());
        boolean result = getValuesList().addAll(index, values);
        synchProperty();
        return result;
    }

    /* (non-Javadoc)
     * @see java.util.List#get(int)
     */
    @Override
    public E get(int index) {
        return asElement(getValuesList().get(index));
    }

    /* (non-Javadoc)
     * @see java.util.List#set(int, java.lang.Object)
     */
    @Override
    public E set(int index, E element) {
        Value oldValue = getValuesList().set(index, JcrPropertyUtil.asValue(getValueFactory(), element));
        synchProperty();
        return asElement(oldValue);
    }

    /* (non-Javadoc)
     * @see java.util.List#add(int, java.lang.Object)
     */
    @Override
    public void add(int index, E element) {
        getValuesList().add(index, JcrPropertyUtil.asValue(getValueFactory(), element));
        synchProperty();
    }

    /* (non-Javadoc)
     * @see java.util.List#remove(int)
     */
    @Override
    public E remove(int index) {
        Value oldValue = getValuesList().remove(index);
        synchProperty();
        return asElement(oldValue);
    }

    /* (non-Javadoc)
     * @see java.util.List#indexOf(java.lang.Object)
     */
    @Override
    public int indexOf(Object o) {
        Value value = JcrPropertyUtil.asValue(getValueFactory(), o);
        return getValuesList().indexOf(value);
    }

    /* (non-Javadoc)
     * @see java.util.List#lastIndexOf(java.lang.Object)
     */
    @Override
    public int lastIndexOf(Object o) {
        Value value = JcrPropertyUtil.asValue(getValueFactory(), o);
        return getValuesList().lastIndexOf(value);
    }

    /* (non-Javadoc)
     * @see java.util.List#listIterator()
     */
    @Override
    public ListIterator<E> listIterator() {
        return new ElementsListIterator(getValuesList().listIterator());
    }

    /* (non-Javadoc)
     * @see java.util.List#listIterator(int)
     */
    @Override
    public ListIterator<E> listIterator(int index) {
        return new ElementsListIterator(getValuesList().listIterator(index));
    }

    /* (non-Javadoc)
     * @see java.util.List#subList(int, int)
     */
    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        List<Value> sublist = getValuesList().subList(fromIndex, toIndex);
        if (getProperty() == null) {
            return new MultiValuePropertyList<>(getParent(), getPropertyName(), sublist, this);
        } else {
            return new MultiValuePropertyList<>(getProperty(), sublist, this);
        }
    }
    
    @Override
    protected void synchProperty() {
        if (this.parentList != null) {
            this.parentList.synchProperty();
        } else {
            super.synchProperty();
        }
    }
    
    protected List<Value> getValuesList() {
        return getValues();
    }
    
    
    protected class ElementsListIterator extends ElementsIterator implements ListIterator<E> {
        
        public ElementsListIterator(ListIterator<Value> valuesItr) {
            super(valuesItr);
        }

        /* (non-Javadoc)
         * @see java.util.ListIterator#hasPrevious()
         */
        @Override
        public boolean hasPrevious() {
            return valuesListIterator().hasPrevious();
        }

        /* (non-Javadoc)
         * @see java.util.ListIterator#previous()
         */
        @Override
        public E previous() {
            return asElement(valuesListIterator().previous());
        }

        /* (non-Javadoc)
         * @see java.util.ListIterator#nextIndex()
         */
        @Override
        public int nextIndex() {
            return valuesListIterator().nextIndex();
        }

        /* (non-Javadoc)
         * @see java.util.ListIterator#previousIndex()
         */
        @Override
        public int previousIndex() {
            return valuesListIterator().previousIndex();
        }

        /* (non-Javadoc)
         * @see java.util.ListIterator#set(java.lang.Object)
         */
        @Override
        public void set(E e) {
            Value value = JcrPropertyUtil.asValue(getValueFactory(), e);
            valuesListIterator().set(value);
            synchProperty();
        }

        /* (non-Javadoc)
         * @see java.util.ListIterator#add(java.lang.Object)
         */
        @Override
        public void add(E e) {
            Value value = JcrPropertyUtil.asValue(getValueFactory(), e);
            valuesListIterator().add(value);
            synchProperty();
        }
        
        protected ListIterator<Value> valuesListIterator() {
            return (ListIterator<Value>) super.valuesIterator();
        }
    }

}
