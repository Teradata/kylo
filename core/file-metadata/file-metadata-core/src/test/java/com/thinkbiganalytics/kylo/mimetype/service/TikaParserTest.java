package com.thinkbiganalytics.kylo.mimetype.service;

/*-
 * #%L
 * kylo-file-metadata-core
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

import com.thinkbiganalytics.kylo.metadata.file.FileMetadata;
import com.thinkbiganalytics.kylo.metadata.file.service.FileMetadataService;

import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;


public class TikaParserTest {

    private class FileContext {

        private String name;
        private InputStream inputStream;

        public FileContext(String name, InputStream inputStream) {
            this.name = name;
            this.inputStream = inputStream;
        }

        public String getName() {
            return name;
        }

        public InputStream getInputStream() {
            return inputStream;
        }
    }

    private FileContext getFile(String fileName) throws Exception {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(fileName);
        return new FileContext(fileName, inputStream);
    }

    @Test
    public void testCsv() throws Exception {

    String  file = "MOCK_DATA.commasep.txt";
        FileMetadata type = FileMetadataService.detectFromStream(getFile(file).getInputStream(), file);
        Assert.assertEquals("text/csv", type.getMimeType());
        Assert.assertEquals(",", type.getProperties().get("delimiter"));


        file = "MOCK_DATA.tab_unix.txt";
        type = FileMetadataService.detectFromStream(getFile(file).getInputStream(), file);
        Assert.assertEquals("text/csv", type.getMimeType());
        Assert.assertEquals("\t", type.getProperties().get("delimiter"));

        file = "MOCK_DATA.pipe.txt";
        type = FileMetadataService.detectFromStream(getFile(file).getInputStream(), file);
        Assert.assertEquals("text/csv", type.getMimeType());
        Assert.assertEquals("|", type.getProperties().get("delimiter"));

        file = "MOCK_DATA.plus_unix.txt";
        type = FileMetadataService.detectFromStream(getFile(file).getInputStream(), file);
        Assert.assertEquals("text/csv", type.getMimeType());
        Assert.assertEquals("+", type.getProperties().get("delimiter"));

    }

    @Test
    public void testXml() throws  Exception {
        String file = "test.xml";
        FileMetadata type = FileMetadataService.detectFromStream(getFile(file).getInputStream(), file);
        Assert.assertEquals("application/xml", type.getMimeType());
        Assert.assertEquals("catalog", type.getProperties().get("rowTag"));
    }


        @Test
    public void test() throws Exception {

        String file = "test.xml";
        FileMetadata type = FileMetadataService.detectFromStream(getFile(file).getInputStream(), file);
        Assert.assertEquals("application/xml", type.getMimeType());
        Assert.assertEquals("catalog", type.getProperties().get("rowTag"));

        file = "test2.xml";
        type = FileMetadataService.detectFromStream(getFile(file).getInputStream(), file);
        Assert.assertEquals("application/xml", type.getMimeType());
        Assert.assertEquals("some-books", type.getProperties().get("rowTag"));

        file = "MOCK_DATA.commasep.txt";
        type = FileMetadataService.detectFromStream(getFile(file).getInputStream(), file);
        Assert.assertEquals("text/csv", type.getMimeType());
        Assert.assertEquals(",", type.getProperties().get("delimiter"));

        file = "MOCK_DATA.pipe.txt";
        type = FileMetadataService.detectFromStream(getFile(file).getInputStream(), file);
        Assert.assertEquals("text/csv", type.getMimeType());
        Assert.assertEquals("|", type.getProperties().get("delimiter"));

        file = "test.parquet";
        type = FileMetadataService.detectFromStream(getFile(file).getInputStream(), file);
        Assert.assertEquals("application/parquet", type.getMimeType());

        file = "books1.json";
        type = FileMetadataService.detectFromStream(getFile(file).getInputStream(), file);
        Assert.assertEquals("application/json", type.getMimeType());

        file = "userdata1.avro";
        type = FileMetadataService.detectFromStream(getFile(file).getInputStream(), file);
        Assert.assertEquals("application/avro", type.getMimeType());

        file = "userdata1.orva";
        type = FileMetadataService.detectFromStream(getFile(file).getInputStream(), file);
        Assert.assertEquals("application/avro", type.getMimeType());

        file = "userdata1_orc";
        type = FileMetadataService.detectFromStream(getFile(file).getInputStream(), file);
        Assert.assertEquals("application/orc", type.getMimeType());


    }

}
