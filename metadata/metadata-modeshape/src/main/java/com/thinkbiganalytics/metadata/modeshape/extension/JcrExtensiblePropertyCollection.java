package com.thinkbiganalytics.metadata.modeshape.extension;

import java.util.Collection;

/**
 * Created by sr186054 on 8/4/16. Wrapper object that allows Extensible entity properties marked as *multiple (collection) to be saved into the system allowing the backend to determine the correct JCR
 * type in the collection
 */
public class JcrExtensiblePropertyCollection {

    public static int COLLECTION_TYPE = -10;

    private int collectionType;
    private Collection collection;

    public JcrExtensiblePropertyCollection(int collectionType, Collection collection) {
        this.collectionType = collectionType;
        this.collection = collection;
    }

    public int getCollectionType() {
        return collectionType;
    }

    public void setCollectionType(int collectionType) {
        this.collectionType = collectionType;
    }

    public Collection getCollection() {
        return collection;
    }

    public void setCollection(Collection collection) {
        this.collection = collection;
    }
}
