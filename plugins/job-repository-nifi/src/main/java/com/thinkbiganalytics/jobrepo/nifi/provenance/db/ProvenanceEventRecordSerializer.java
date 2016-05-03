package com.thinkbiganalytics.jobrepo.nifi.provenance.db;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.nifi.web.api.dto.provenance.ProvenanceEventDTO;

/**
 * Serialize the ProvenanceEventRecord
 * delegates to Jackson to turn it into a JSON object
 */
public class ProvenanceEventRecordSerializer {

    private ObjectMapper mapper;

    public ProvenanceEventRecordSerializer() {
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    }



    public String getAttributesAsJSON(ProvenanceEventDTO event) {
        String json = null;
        try {
            if (event.getAttributes() != null) {
                json = mapper.writeValueAsString(event.getAttributes());
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return json;
    }

    public String getAsJSON(ProvenanceEventDTO event) {
        String json = null;
        try {
            json = mapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return json;
    }
}
