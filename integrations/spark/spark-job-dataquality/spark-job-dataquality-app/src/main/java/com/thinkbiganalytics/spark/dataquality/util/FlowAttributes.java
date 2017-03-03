package com.thinkbiganalytics.spark.dataquality.util;

import java.io.FileNotFoundException;

/*-
 * #%L
 * kylo-spark-job-dataquality-app
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

import java.io.FileReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper Class for storing attributes relating to the flow.
 */
public class FlowAttributes {
    private static final Logger log = LoggerFactory.getLogger(FlowAttributes.class);

    // Stores the flow attributes
    private Map<String, String> attributeMap = new HashMap<String, String>();

    public boolean containsAttribute(String attribute) {
        return attributeMap.containsKey(attribute);
    }

    public Map<String, String> getAttributes() {
        return attributeMap;
    }

    public void addAttribute(String attribute, String value) {
        attributeMap.put(attribute, value);
    }

    public int count() {
        return attributeMap.size();
    }

    /**
     * Returns the value of the attribute passed in. If the attribute does not exist then an
     * exception is thrown
     * 
     * @param attribute Name of the attribute
     * @return String value of the attribute
     * @throws MissingAttributeException
     */
    public String getAttributeValue(String attribute) throws MissingAttributeException {
        String val = attributeMap.get(attribute);

        if (val == null) {
            String msg = "Attribute: " + attribute + " does not exist";
            log.error(msg);
            throw new MissingAttributeException(msg);
        }

        return val;
    }
    
    /**
     * Returns the value of the attribute passed in. If the attribute does not exist, then the
     * passed in default value is used
     * 
     * @param attribute Name of the attribute
     * @param defaultValue Value used if attribute does not exist
     * @return String value of the attribute
     */
    public String getAttributeValue(String attribute, String defaultValue) {
        if (!attributeMap.containsKey(attribute)) {
            addAttribute(attribute, defaultValue);
            return defaultValue;
        }
        else {
            return attributeMap.get(attribute);
        }
    }
    
    /**
     * Converts the string value of an attribute to data type long
     * 
     * @param attribute Name of the attribute
     * @return Long value of the attribute. If the value is not converted, Long.MIN_VALUE is
     *         returned
     * @throws MissingAttributeException
     */
    public long getAttributeValueLong(String attribute) throws MissingAttributeException {
        String val = getAttributeValue(attribute);

        return convertStringtoLong(attribute, val);
    }
    
    /**
     * Converts the string value of an attribute to data type long
     * 
     * @param attribute Name of the attribute
     * @return Long value of the attribute. If the value is not converted, Long.MIN_VALUE is
     *         returned
     * @throws MissingAttributeException
     */
    public long getAttributeValueLong(String attribute, String defaultValue) {
        String val = getAttributeValue(attribute, defaultValue);

        return convertStringtoLong(attribute, val);
    }

    /**
     * This method takes a location of a JSON file and parses it and sets the attributes as
     * key/value pairs
     * 
     * @param attributesJsonPath
     */
    public void setAttributes(String attributesJsonPath) {

        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(new FileReader(attributesJsonPath));
            JSONObject jsonObject = (JSONObject) obj;

            for (Iterator<?> iterator = jsonObject.keySet().iterator(); iterator.hasNext();) {
                String attribute = (String) iterator.next();
                String value = (String) jsonObject.get(attribute);

                addAttribute(attribute, value);
            }

        } catch (ParseException e ) {
            log.error("Error while parsing JSON file. Check " + attributesJsonPath, e);
        } catch (FileNotFoundException e ) {
            log.error("File not found. Checing path " + attributesJsonPath, e);
        } catch (Exception e) {
            log.error("Error setting attributes. Check JSON in path: " + attributesJsonPath, e);
        }

    }
    
    private long convertStringtoLong(String attrName, String val) {
        long longVal = Long.MIN_VALUE;
        try {
            longVal = Long.parseLong(val);
        } catch (NumberFormatException e) {
            log.error("Error while converting attribute: " + attrName
                      +
                      " to long", e);
            throw e;
        }

        return longVal;
    }
}
