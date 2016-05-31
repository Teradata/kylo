package com.thinkbiganalytics.nifi.feedmgr;


import com.thinkbiganalytics.nifi.rest.model.NifiProperty;
import org.apache.commons.lang3.StringUtils;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Auto Inject Property Values stored in the application.properties file
 * 2 use cases are supported
 * 1) store properties in the file starting with the prefix defined in the "PropertyExpressionResolver class"  default = config.
 * 2) store properties in the file starting with "nifi.<PROCESSORTYPE>.<PROPERTY_KEY>   where PROCESSORTYPE and PROPERTY_KEY are all lowercase and the spaces are substituted with underscore
 *
 */
public class ConfigurationPropertyReplacer {

    public static String getProcessorPropertyConfigName(NifiProperty property ) {
        String processorTypeName = "nifi."+(StringUtils.substringAfterLast(property.getProcessorType(), ".") + "." + property.getKey()).toLowerCase().trim().replaceAll(" +", "_");
        return processorTypeName;
    }

    /**
     *
     * @param property
     * @return
     */
    public static boolean resolveStaticConfigurationProperty(NifiProperty property, Map<String,Object> configProperties){
        String value = property.getValue();
        StringBuffer sb = null;

        if(configProperties != null && !configProperties.isEmpty()) {


            if (StringUtils.isNotBlank(value)) {
                Pattern variablePattern = Pattern.compile("\\$\\{(.*?)\\}");
                Matcher matchVariablePattern = variablePattern.matcher(value);
                while (matchVariablePattern.find()) {
                    if (sb == null) {
                        sb = new StringBuffer();
                    }
                    String group = matchVariablePattern.group();
                    int groupCount = matchVariablePattern.groupCount();
                    if (groupCount == 1) {

                        String variable = matchVariablePattern.group(1);
                        //lookup the variable
                        //first look at configuration properties
                        Object resolvedValue = configProperties.get(variable);
                        if (resolvedValue != null) {
                            matchVariablePattern.appendReplacement(sb, resolvedValue.toString());
                        }
                    }
                }
                if (sb != null) {
                    matchVariablePattern.appendTail(sb);
                }
            }
        }

                if(sb == null) {
                    String key = getProcessorPropertyConfigName(property);

                    Object resolvedValue =  configProperties != null ? configProperties.get(key) : null;
                    if (resolvedValue != null) {
                        sb = new StringBuffer();
                        sb.append(resolvedValue.toString());
                    }

                }
                if(sb != null){
                    property.setValue(StringUtils.trim(sb.toString()));
                }


        return sb != null;
    }

}
