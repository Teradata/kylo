package com.thinkbiganalytics.feedmgr.nifi;


import com.thinkbiganalytics.feedmgr.MetadataFieldAnnotationFieldNameResolver;
import com.thinkbiganalytics.feedmgr.MetadataFields;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;
import com.thinkbiganalytics.annotations.AnnotatedFieldProperty;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by sr186054 on 1/25/16.
 */
public class PropertyExpressionResolver {
    @Autowired
    private  SpringEnvironmentProperties environmentProperties;

    public static String metadataPropertyPrefix = MetadataFieldAnnotationFieldNameResolver.metadataPropertyPrefix;
    public static String configPropertyPrefix = "config.";

    public   List<NifiProperty> resolvePropertyExpressions(FeedMetadata metadata) {
        List<NifiProperty> resolvedProperties = new ArrayList<>();
        if(metadata != null && metadata.getProperties() != null && !metadata.getProperties().isEmpty()){
            for(NifiProperty property:metadata.getProperties()){
                if(resolveExpression(metadata,property)){
                    resolvedProperties.add(property);
                }
            }
        }
        return resolvedProperties;
    }


    public  boolean resolveExpression(FeedMetadata metadata, NifiProperty property) {
        String value = property.getValue();
        StringBuffer sb = null;


        if (StringUtils.isNotBlank(value)) {
            Pattern variablePattern = Pattern.compile("\\$\\{(.*?)\\}");
            Matcher matchVariablePattern = variablePattern.matcher(value);
            while (matchVariablePattern.find()) {
                if(sb == null){
                    sb = new StringBuffer();
                }
                String group = matchVariablePattern.group();
                int groupCount = matchVariablePattern.groupCount();
                if(groupCount ==1) {

                    String variable = matchVariablePattern.group(1);
                    //lookup the variable
                    //first look at configuration properties
                    String resolvedValue = getConfigurationPropertyValue(variable);
                    if (resolvedValue != null) {
                        matchVariablePattern.appendReplacement(sb, resolvedValue);
                    }else {
                        try {
                                resolvedValue = getMetadataPropertyValue(metadata, variable);
                                matchVariablePattern.appendReplacement(sb, resolvedValue);

                        } catch (Exception e) {
                        }
                    }
                }
            }
            if(sb != null) {
                matchVariablePattern.appendTail(sb);
                property.setValue(StringUtils.trim(sb.toString()));
            }
        }
        return sb != null;

    }

    public  String getMetadataPropertyValue(FeedMetadata metadata, String variableName) throws Exception{
            String fieldPathName = StringUtils.substringAfter(variableName, metadataPropertyPrefix);
        Object obj = null;
        try {
             obj = BeanUtils.getProperty(metadata, fieldPathName);
        }catch(Exception e)
        {
        //    e.printStackTrace();
        }
            //check to see if the path has a Metadata annotation with a matching field
            String matchingProperty = MetadataFields.getInstance().getMatchingPropertyDescriptor(metadata, variableName);
            if(obj == null && matchingProperty != null){
                matchingProperty = StringUtils.substringAfter(matchingProperty, metadataPropertyPrefix);
                obj = BeanUtils.getProperty(metadata,matchingProperty);
            }
        if(obj != null) {
            return obj.toString();
        }
        else {
            return null;
        }
    }

    public  String getConfigurationPropertyValue(String propertyKey){
      return environmentProperties.getPropertyValueAsString(propertyKey);
    }

    public  List<AnnotatedFieldProperty> getMetadataProperties(){
        List<AnnotatedFieldProperty> properties = MetadataFields.getInstance().getProperties(FeedMetadata.class);
        return properties;
    }



    private  List<String> getFieldNames(List<Field> fields) {
        List<String> names = new ArrayList<>();
        if(fields != null) {
            for(Field field: fields){
                Class clazz = field.getDeclaringClass();
                names.add(field.getName());
            }
        }
        return names;
    }
}
