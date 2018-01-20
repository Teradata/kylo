package com.thinkbiganalytics.ui.api.template;
/*-
 * #%L
 * kylo-ui-api
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
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.thinkbiganalytics.annotations.AnnotatedFieldProperty;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Custom deserializer for AnnotatedFieldProperty
 */
public class AnnotatedFieldPropertyListDeserializer extends JsonDeserializer<List<AnnotatedFieldProperty>> {

    @Override
    public List<AnnotatedFieldProperty> deserialize(JsonParser jsonParser, DeserializationContext arg1) throws IOException, JsonProcessingException {
        List<AnnotatedFieldProperty> list = new ArrayList<>();
        ObjectCodec oc = jsonParser.getCodec();
        JsonNode node = oc.readTree(jsonParser);
        if(node.isArray()) {
            for (JsonNode node1 : node) {
                String name = findStringValue(node1, "name", "");
                String description = findStringValue(node1, "description", "");
                AnnotatedFieldProperty annotatedFieldProperty = new AnnotatedFieldProperty();
                annotatedFieldProperty.setName(name);
                annotatedFieldProperty.setDescription(description);
                list.add(annotatedFieldProperty);
            }
        }
        return list;
    }

    public String findStringValue(JsonNode node, String field,String defaultValue){
        JsonNode n = node.findValue(field);
        if(n != null && n.isTextual()){
            return n.asText(defaultValue);
        }
        else {
            return defaultValue;
        }
    }
}