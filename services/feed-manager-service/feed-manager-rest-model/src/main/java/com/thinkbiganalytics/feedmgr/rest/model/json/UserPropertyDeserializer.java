package com.thinkbiganalytics.feedmgr.rest.model.json;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.thinkbiganalytics.feedmgr.rest.model.UserProperty;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class UserPropertyDeserializer extends StdDeserializer<Set<UserProperty>> {

    public UserPropertyDeserializer() {
        super(Object.class);
    }

    @Override
    public Set<UserProperty> deserialize(final JsonParser p, final DeserializationContext ctxt) throws IOException {
        final ObjectCodec codec = (p.getCodec() != null) ? p.getCodec() : new ObjectMapper();
        final JsonNode node = codec.readTree(p);
        final Set<UserProperty> userProperties;

        if (node.isArray()) {
            userProperties = new HashSet<>(node.size());
            for (final JsonNode child : node) {
                userProperties.add(codec.treeToValue(child, UserProperty.class));
            }
        } else if (node.isObject()) {
            final Iterator<Map.Entry<String, JsonNode>> iter = node.fields();
            userProperties = new HashSet<>(node.size());
            while (iter.hasNext()) {
                final Map.Entry<String, JsonNode> element = iter.next();
                final UserProperty userProperty = new UserProperty();
                userProperty.setSystemName(element.getKey());
                userProperty.setValue(element.getValue().asText());
                userProperties.add(userProperty);
            }
        } else {
            throw new JsonParseException("TODO", p.getCurrentLocation());  // TODO
        }

        return userProperties;
    }
}
