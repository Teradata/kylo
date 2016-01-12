/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.spark.spark_launcher;

import org.apache.tika.detect.MagicDetector;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.sax.PhoneExtractingContentHandler;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by matthutton on 1/9/16.
 */
public class TikaExample {


    public String parseExample() throws IOException, SAXException, TikaException, URISyntaxException {
        AutoDetectParser parser = new AutoDetectParser();
        BodyContentHandler handler2 = new BodyContentHandler();
        Metadata metadata = new Metadata();
        PhoneExtractingContentHandler handler = new PhoneExtractingContentHandler(handler2, metadata);
        try (InputStream stream = TikaExample.class.getResourceAsStream("employee.csv")) {
            Path target = Paths.get(TikaExample.class.getResource("employee.csv").toURI());
            System.out.println(" Probed: "+ Files.probeContentType(target));
            parser.parse(stream, handler, metadata);
            System.out.println("Metadata:"+metadata.toString());
            return handler.toString();
        }
    }

    public static void main(String[] args) throws Exception {
        TikaExample example = new TikaExample();
        String s = example.parseExample();
        System.out.println("s: "+s);
    }


}
