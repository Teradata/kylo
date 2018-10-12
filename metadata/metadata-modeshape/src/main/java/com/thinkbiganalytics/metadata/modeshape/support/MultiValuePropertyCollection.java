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

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFactory;

/**
 * An abstract collection that wraps a JCR multi-valued property and supports all access and modification operations.
 */
public abstract class MultiValuePropertyCollection<E> implements Collection<E> {

    private final Node parent;
    private final String propertyName;
    private final boolean weakReferences;
    private Property property;
    private Collection<Value> values;
    
    protected MultiValuePropertyCollection(Node parent, String propertyName, boolean weakRefs, Collection<Value> values) {
        this.parent = parent;
        this.propertyName = propertyName;
        this.weakReferences = weakRefs;
        this.property = null;
        this.values = values;
    }
    
    protected MultiValuePropertyCollection(Property prop, Collection<Value> values) {
        try {
            if (prop != null && ! prop.isMultiple()) {
                throw new IllegalArgumentException("The property provided must be multi-valued: " + prop);
            }
            
            this.parent = prop.getParent();
            this.propertyName = prop.getName();
            this.weakReferences = prop.getType() == PropertyType.WEAKREFERENCE;
            this.property = prop;
            this.values = values;
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Unable to construct collection on property: " + prop, e);
        }
    }


    /* (non-Javadoc)
     * @see java.util.Collection#size()
     */
    @Override
    public int size() {
        return getValues().size();
    }

    /* (non-Javadoc)
     * @see java.util.Collection#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    /* (non-Javadoc)
     * @see java.util.Collection#contains(java.lang.Object)
     */
    @Override
    public boolean contains(Object o) {
        return elementsStream().anyMatch(o::equals);
    }

    /* (non-Javadoc)
     * @see java.util.Collection#iterator()
     */
    @Override
    public Iterator<E> iterator() {
        return new ElementsIterator(getValues().iterator());
    }

    /* (non-Javadoc)
     * @see java.util.Collection#toArray()
     */
    @Override
    public Object[] toArray() {
        return elementsStream().toArray(Object[]::new);
    }

    /* (non-Javadoc)
     * @see java.util.Collection#toArray(java.lang.Object[])
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        return elementsStream().toArray(s -> a.length == s ? a : (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), s));
    }

    /* (non-Javadoc)
     * @see java.util.Collection#add(java.lang.Object)
     */
    @Override
    public boolean add(E e) {
        boolean added = addElement(e);
        
        if (added) {
            synchProperty();
        }
        
        return added;
    }

    /* (non-Javadoc)
     * @see java.util.Collection#remove(java.lang.Object)
     */
    @Override
    public boolean remove(Object o) {
        ValueFactory factory = getValueFactory();
        boolean removed = getValues().remove(JcrPropertyUtil.asValue(factory, o, isWeakReferences()));
        
        if (removed) {
            synchProperty();
        }
        
        return removed;
    }

    /* (non-Javadoc)
     * @see java.util.Collection#containsAll(java.util.Collection)
     */
    @Override
    public boolean containsAll(Collection<?> c) {
        return elementsStream().allMatch(val -> c.contains(val));
    }

    /* (non-Javadoc)
     * @see java.util.Collection#addAll(java.util.Collection)
     */
    @Override
    public boolean addAll(Collection<? extends E> c) {
        boolean added = c.stream().reduce(false, (r, o) -> addElement(o) || r, (r1, r2) -> r1 || r2);
        
        if (added) {
            synchProperty();
        }
        return added;
    }

    /* (non-Javadoc)
     * @see java.util.Collection#removeAll(java.util.Collection)
     */
    @Override
    public boolean removeAll(Collection<?> c) {
        boolean removed = c.stream().reduce(false, (r, o) -> removeElement(o)|| r, (r1, r2) -> r1 || r2);
        
        if (removed) {
            synchProperty();
        }
        return removed;
    }

    /* (non-Javadoc)
     * @see java.util.Collection#retainAll(java.util.Collection)
     */
    @Override
    public boolean retainAll(Collection<?> c) {
        Set<E> set = elementsStream()
            .filter(v -> c.contains(v))
            .collect(Collectors.toSet());
        return removeAll(set);
    }

    /* (non-Javadoc)
     * @see java.util.Collection#clear()
     */
    @Override
    public void clear() {
        getValues().clear();
        synchProperty();
    }
    
    protected Property getProperty() {
        return this.property;
    }
    
    protected void setProperty(Property property) {
        this.property = property;
    }
    
    protected Node getParent() {
        return parent;
    }
    
    protected String getPropertyName() {
        return propertyName;
    }
    
    protected boolean isWeakReferences() {
        return weakReferences;
    }

    @SuppressWarnings("unchecked")
    protected E asElement(Value value) {
        try {
            return (E) JcrPropertyUtil.asValue(value, getSession());
        } catch (AccessDeniedException e) {
            throw new MetadataRepositoryException("Access denied to value: " + value, e);
        }
    }

    protected <C extends Collection<Value>> C getValues() {
        return (C) this.values;
    }

    protected boolean addElement(E e) {
        ValueFactory factory = getValueFactory();
        return getValues().add(JcrPropertyUtil.asValue(factory, e, isWeakReferences()));
    }

    protected boolean removeElement(Object e) {
        ValueFactory factory = getValueFactory();
        return getValues().remove(JcrPropertyUtil.asValue(factory, e, isWeakReferences()));
    }

    protected void synchProperty() {
        try {
            if (getProperty() == null) {
                Property prop = getParent().setProperty(getPropertyName(), new Value[0]);
                setProperty(prop);
            }
            
            getProperty().setValue(getValues().toArray(new Value[getValues().size()]));
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Unable to update values of property: " + getPropertyName(), e);
        }
    }

    protected ValueFactory getValueFactory() {
        try {
            return getSession().getValueFactory();
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Unable to obtain ValueFactory from property" + getPropertyName(), e);
        }
    }
    
    protected Session getSession() {
        try {
            return getParent().getSession();
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Unable to obtain session from property" + getPropertyName(), e);
        }
    }

    protected Stream<E> elementsStream() {
        return getValues().stream().map(v -> {
            try {
                return JcrPropertyUtil.asValue(v, getParent().getSession());
            } catch (RepositoryException e) {
                throw new MetadataRepositoryException("Failed to access the contents of the value: " + v, e);
            }
        });
    }
    
    
    protected class ElementsIterator implements Iterator<E> {
        
        private Iterator<Value> valuesIterator;
        
        public ElementsIterator(Iterator<Value> valuesItr) {
            this.valuesIterator = valuesItr;
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#hasNext()
         */
        @Override
        public boolean hasNext() {
            return valuesIterator().hasNext();
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#next()
         */
        @Override
        public E next() {
            return asElement(valuesIterator().next());
        }
        
        /* (non-Javadoc)
         * @see java.util.Iterator#remove()
         */
        @Override
        public void remove() {
            valuesIterator().remove();
            synchProperty();
        }
        
        /* (non-Javadoc)
         * @see java.util.Iterator#forEachRemaining(java.util.function.Consumer)
         */
        @Override
        public void forEachRemaining(Consumer<? super E> action) {
            valuesIterator().forEachRemaining(value -> action.accept(asElement(value)));
        }
        
        protected Iterator<Value> valuesIterator() {
            return this.valuesIterator;
        }
    }
    
}
