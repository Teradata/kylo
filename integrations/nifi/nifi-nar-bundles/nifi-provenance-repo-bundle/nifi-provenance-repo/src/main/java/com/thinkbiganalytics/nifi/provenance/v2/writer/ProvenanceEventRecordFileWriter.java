package com.thinkbiganalytics.nifi.provenance.v2.writer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Files;
import org.apache.nifi.provenance.ProvenanceEventRecord;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Write events to a file
 */
public class ProvenanceEventRecordFileWriter extends AbstractProvenanceEventWriter {


    public static final String filePath = "/tmp/provenance-events.json";
    public static final String idPath = "/tmp/provenance-event-id.json";
    private ObjectMapper mapper;

    public ProvenanceEventRecordFileWriter() {
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    }

    @Override
    public Long getMaxEventId() {
        return getFileId();
    }

    private synchronized Long getFileId() {
        File f = new File(idPath);
        if (f.exists()) {
            try {
                String ln = Files.readFirstLine(f, StandardCharsets.UTF_8);
                if (ln != null && !ln.equalsIgnoreCase("")) {
                    ln = ln.trim();
                    try {
                        return new Long(ln);
                    } catch (Exception e) {

                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public void setMaxEventId(Long eventId) {

    }

    @Override
    public Long writeEvent(ProvenanceEventRecord event) {
        String json = null;
        try {
            json = mapper.writeValueAsString(event);
            writeToFile(filePath, json);
            overwriteFile(idPath, "" + event.getEventId());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;

    }

    private synchronized void writeToFile(String path, String str) {
        try {
            FileWriter writer = new FileWriter(path, true);
            //check if the file is empty
            BufferedReader br = new BufferedReader(new FileReader(path));
            boolean isEmpty = true;
            if (br.readLine() != null) {
                isEmpty = false;
            }
            if (!isEmpty) {
                writer.write(",\n");
            }
            writer.write(str);
            writer.flush();
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private synchronized void overwriteFile(String path, String str) {
        try {
            FileWriter writer = new FileWriter(path, false);
            //check if the file is empty
            BufferedReader br = new BufferedReader(new FileReader(path));
            writer.write(str);
            writer.flush();
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
