package com.thinkbiganalytics.feedmgr.service.feed.datasource;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

/**
 * Created by sr186054 on 5/5/16.
 */
public class NifiFeedDatasourceFactory {

    private static Class<? extends NifiProcessorToFeedSource> getTransformerClass(final String nifiProcessorType){

        Set<Class<?>>
                transfomers =new Reflections("com.thinkbiganalytics.feedmgr.service.feed.datasource").getTypesAnnotatedWith(NifiFeedSourceProcessor.class);

           Class<?> transformer = Iterables.tryFind(transfomers, new Predicate<Class<?>>() {
                @Override
                public boolean apply(Class<?> aClass) {
                    NifiFeedSourceProcessor nifiFeedSourceProcessor = (NifiFeedSourceProcessor) aClass.getAnnotation(NifiFeedSourceProcessor.class);
                    return nifiFeedSourceProcessor.nifiProcessorType().equalsIgnoreCase(nifiProcessorType);
                }
            }).orNull();
        if(transformer != null && transformer.isAssignableFrom(NifiProcessorToFeedSource.class)){
return (Class<? extends NifiProcessorToFeedSource>)transformer;
        }
        return null;
    }

    public static  Datasource transform(FeedMetadata metadata){
        String inputProcessorType = metadata.getInputProcessorType();
        Class<? extends NifiProcessorToFeedSource> transformerClass = getTransformerClass(inputProcessorType);
        if(transformerClass != null){
            try {
                NifiProcessorToFeedSource transformer = ConstructorUtils.invokeConstructor(transformerClass, metadata);
                return transformer.transform();
            } catch (NoSuchMethodException |IllegalAccessException | InvocationTargetException | InstantiationException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
