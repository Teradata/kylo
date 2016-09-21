package com.thinkbiganalytics.metadata.jpa.jobrepo;

import org.springframework.batch.core.repository.ExecutionContextSerializer;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * The Serializer is autowired in via the spring-batch-common module TODO: This will be removed once Spring Batch is removed from the queries Created by sr186054 on 9/1/16.
 */
public class ExecutionContextSerializationHelper {

    @Autowired
    ExecutionContextSerializer serializer;

    public ExecutionContext toExecutionContext(Map<String, String> context) {
        ExecutionContext executionContext = new ExecutionContext();
        if (context != null) {
            context.entrySet().stream().forEach(e -> {
                executionContext.putString(e.getKey(), e.getValue());
            });
        }
        return executionContext;
    }


    /**
     * Taken from spring batch JdbcExecutionContextDao class
     */
    public String serializeContext(ExecutionContext ctx) {
        HashMap m = new HashMap();
        Iterator out = ctx.entrySet().iterator();

        while (out.hasNext()) {
            Map.Entry results = (Map.Entry) out.next();
            m.put(results.getKey(), results.getValue());
        }
        return serializeContext(m);
    }

    public String serializeStringMapContext(Map<String, String> map) {
        Map<String, Object> objectMap = Collections.<String, Object>unmodifiableMap(map);
        return serializeContext(objectMap);
    }

    public String serializeContext(Map<String, Object> map) {
        ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        String results1 = "";

        try {
            this.serializer.serialize(map, out1);
            results1 = new String(out1.toByteArray(), "ISO-8859-1");
            return results1;
        } catch (IOException var6) {
            throw new IllegalArgumentException("Could not serialize the execution context", var6);
        }
    }
}
