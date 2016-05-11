package com.thinkbiganalytics.jobrepo.nifi.provenance.db;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thinkbiganalytics.json.ObjectMapperSerializer;
import org.apache.nifi.web.api.dto.provenance.ProvenanceEventDTO;

/**
 * Serialize the ProvenanceEventRecord
 * delegates to Jackson to turn it into a JSON object
 */
public class ProvenanceEventRecordSerializer {



    public  static String getAttributesAsJSON(ProvenanceEventDTO event) {
        String json = null;

            if (event.getAttributes() != null) {
                json = ObjectMapperSerializer.serialize(event.getAttributes());
            }

        return json;
    }

    public static String getAsJSON(ProvenanceEventDTO event) {
        String json = ObjectMapperSerializer.serialize(event);
        return json;
    }

    public static ProvenanceEventDTO deserialize(String json){
        return ObjectMapperSerializer.deserialize(json,ProvenanceEventDTO.class);
    }
}
