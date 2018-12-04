package com.thinkbiganalytics.discovery.parsers.hadoop;

/*-
 * #%L
 * XMLFileSchemaParser.java
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

import com.thinkbiganalytics.discovery.model.DefaultHiveTableSettings;
import com.thinkbiganalytics.discovery.model.DefaultTableSettings;
import com.thinkbiganalytics.discovery.parser.FileSchemaParser;
import com.thinkbiganalytics.discovery.parser.SampleFileSparkScript;
import com.thinkbiganalytics.discovery.parser.SchemaParser;
import com.thinkbiganalytics.discovery.parsers.csv.CSVFileSchemaParser;
import com.thinkbiganalytics.discovery.schema.HiveTableSchema;
import com.thinkbiganalytics.discovery.schema.HiveTableSettings;
import com.thinkbiganalytics.discovery.schema.Schema;
import com.thinkbiganalytics.discovery.schema.TableSettings;
import com.thinkbiganalytics.discovery.util.TableSchemaType;
import com.thinkbiganalytics.policy.PolicyProperty;
import com.thinkbiganalytics.policy.PropertyLabelValue;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

@SchemaParser(name = "XML", allowSkipHeader = false, description = "Supports XML formatted files.", tags = {"XML"}, usesSpark = true,mimeTypes = "application/xml", sparkFormat = "xml")
public class XMLFileSchemaParser extends AbstractSparkFileSchemaParser implements FileSchemaParser {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(CSVFileSchemaParser.class);

    @PolicyProperty(name = "Row Tag", required = true, hint = "Specify root tag to extract from", value = ",",additionalProperties = {@PropertyLabelValue(label = "spark.option",value = "rowTag")})
    private String rowTag = "";

    @PolicyProperty(name = "Attribute Prefix", required = true, hint = "The prefix for attributes so that we can differentiating attributes and elements. This will be the prefix for field names", value = "_",additionalProperties = {@PropertyLabelValue(label = "spark.option",value = "attributePrefix")})
    private String attributePrefix = "_";

    @PolicyProperty(name = "Value Tag", required = true, hint = "The tag used for the value when there are attributes in the element having no child", value = "_",additionalProperties = {@PropertyLabelValue(label = "spark.option",value = "valueTag")})
    private String valueTag = "_VALUE";

    @Override
    public boolean tableSettingsRequireFileInspection(){
        return true;
    }

    @Override
    public TableSettings deriveTableSettings(TableSchemaType target) throws IOException {
        throw new UnsupportedOperationException(" XML table settings requires  the file to be inspected.  Please set the tableSettingsRequireFileInspection = true");
    }
    @Override
    public TableSettings parseTableSettings(InputStream is, Charset charset, TableSchemaType target) throws IOException {
        return toTableSettings(is,getSparkFileType(),target);
    }


    @Override
    public Schema parse(InputStream is, Charset charset, TableSchemaType target) throws IOException {
        File tempFile = null;
        HiveTableSchema schema = null;

        // We use Spark to derive the column types but then generate a Hive compatible schema which requires the XPaths for the Hive schema
        // Store the file so we can access it twice
        try {
            tempFile = streamToFile(is);

            LOG.debug("tempFile created {}", tempFile.getAbsolutePath());

            // Now build the serde and properties for the Hive schema
            HiveXMLSchemaHandler hiveParse = parseForHive(tempFile);
            HiveTableSettings tableSettings = toHiveTableSettings(hiveParse);
            String serde = tableSettings.getHiveFormat();
            // Set rowTag if it was derived by the SAX parse
            this.rowTag = hiveParse.getStartTag();
            LOG.debug("XML serde {}", serde);

            // Parse using Spark
            try (InputStream fis = new FileInputStream(tempFile)) {
                schema = (HiveTableSchema) getSparkParserService().doParse(fis, SparkFileType.XML, target, new XMLCommandBuilder(hiveParse.getStartTag(),attributePrefix, valueTag));
            }

            schema.setStructured(true);

            LOG.debug("XML Spark parser discovered {} fields", schema.getFields().size());

            schema.setHiveFormat(serde);
            String xmlStart = hiveParse.startTag + (hiveParse.startTagHasAttributes ? " " : ">");
            String xmlEnd = hiveParse.startTag;
            schema.setSerdeTableProperties(String.format("tblproperties ( \"xmlinput.start\" = \"<%s\", \"xmlinput.end\" = \"</%s>\")", xmlStart, xmlEnd));

            LOG.debug("properties", schema.getProperties());
        } catch (Exception e) {
            LOG.error("Failed to parse XML", e);
            if (e instanceof IOException) {
                throw (IOException) e;
            } else {
                throw new IOException("Failed to generate schema for XML", e);
            }
        } finally {
            if (tempFile != null) {
                tempFile.delete();
            }
        }
        return schema;
    }

    @Override
    public SparkFileType getSparkFileType() {
        return SparkFileType.XML;
    }


    private TableSettings toTableSettings(InputStream inputStream, SparkFileType fileType, TableSchemaType tableSchemaType) throws IOException {
        switch (tableSchemaType) {
            case HIVE:
               return toHiveTableSettings(inputStream);
            default:
                return new DefaultTableSettings();
        }
    }


    private HiveTableSettings toHiveTableSettings(InputStream is) throws IOException{
        File tempFile = null;
        try {
            tempFile = streamToFile(is);
            // Now build the serde and properties for the Hive schema
            HiveXMLSchemaHandler hiveParse = parseForHive(tempFile);
            return toHiveTableSettings(hiveParse);
        } catch (Exception e) {
            LOG.error("Failed to parse XML", e);
            if (e instanceof IOException) {
                throw (IOException) e;
            } else {
                throw new IOException("Failed to generate schema for XML", e);
            }
        } finally {
            if (tempFile != null) {
                tempFile.delete();
            }
        }

    }

    private HiveTableSettings toHiveTableSettings(HiveXMLSchemaHandler hiveXMLSchemaHandler){
        String paths = StringUtils.join(hiveXMLSchemaHandler.columnPaths.values(), ",");
        String serde = String.format("row format serde 'com.ibm.spss.hive.serde2.xml.XmlSerDe' with serdeproperties (%s) stored as inputformat 'com.ibm.spss.hive.serde2.xml.XmlInputFormat' "
                                     + "outputformat 'org.apache.hadoop.hive.ql.io.IgnoreKeyTextOutputFormat'", paths);

        LOG.debug("XML serde {}", serde);
        HiveTableSettings hiveTableSettings = new DefaultHiveTableSettings();
        hiveTableSettings.setHiveFormat(serde);
        hiveTableSettings.setStructured(true);
        return hiveTableSettings;
    }



    @Override
    public SampleFileSparkScript getSparkScript(InputStream is) throws IOException {
        File tempFile = streamToFile(is);
        if (StringUtils.isEmpty(rowTag)) {
            try {
                HiveXMLSchemaHandler hiveParse = parseForHive(tempFile);
                rowTag = hiveParse.getStartTag();
            } catch (Exception e) {
                throw new IOException(e);
            }
        }
        return getSparkParserService().getSparkScript(tempFile, getSparkFileType(), getSparkCommandBuilder());
    }

    public SampleFileSparkScript getSparkScript(List<String> files){

        return getSparkParserService().getSparkScript(files, getSparkCommandBuilder());
    }

    @Override
    public SparkCommandBuilder getSparkCommandBuilder() {
        XMLCommandBuilder xmlCommandBuilder = new XMLCommandBuilder(getRowTag(), getAttributePrefix(), getValueTag());
        xmlCommandBuilder.setDataframeVariable(dataFrameVariable);
        xmlCommandBuilder.setLimit(limit);
        return xmlCommandBuilder;
    }


    protected HiveXMLSchemaHandler parseForHive(File xmlFile) throws Exception {

        HiveXMLSchemaHandler handler = new HiveXMLSchemaHandler(rowTag);
        SAXParserFactory parserFactory = SAXParserFactory.newInstance();
        parserFactory.setNamespaceAware(false);
        SAXParser saxParser = parserFactory.newSAXParser();
        saxParser.parse(xmlFile, handler);
        return handler;
    }

    protected static File streamToFile(InputStream is) throws IOException {
        final File tempFile = File.createTempFile("kylo-parser", "xml");
        try (FileOutputStream out = new FileOutputStream(tempFile)) {
            IOUtils.copyLarge(is, out);
        }
        return tempFile;

    }

    /**
     * Build Spark script for parsing XML
     */
    static class XMLCommandBuilder extends AbstractSparkCommandBuilder {

        String xmlRowTag;

        String attributePrefix;

        String valueTag;

        XMLCommandBuilder(String rowTag, String attributePrefix, String valueTag) {
            this.xmlRowTag = rowTag;
            this.attributePrefix = attributePrefix;
            this.valueTag = valueTag;
        }

        @Override
        public String build(String pathToFile) {
            StringBuilder sb = new StringBuilder();

            sb.append("\nimport com.databricks.spark.xml._;\n");
            sb.append((dataframeVariable != null ? "var " + dataframeVariable + " = " : ""));
            sb.append(String.format("sqlContext.read"
                                    + ".format(\"com.databricks.spark.xml\").option(\"rowTag\",\"%s\")"
                                    + ".option(\"attributePrefix\",\"%s\").option(\"valueTag\",\"%s\")"
                                    + ".load(\"%s\")", xmlRowTag, attributePrefix, valueTag, pathToFile));
            return sb.toString();
        }

        @Override
        public String build(List<String> paths) {
            StringBuilder sb = new StringBuilder();

            sb.append("\nimport com.databricks.spark.xml._;\n");
            sb.append(unionDataFrames(paths,"sqlContext.read"
                                            + ".format(\"com.databricks.spark.xml\").option(\"rowTag\",\"%s\")"
                                            + ".option(\"attributePrefix\",\"%s\").option(\"valueTag\",\"%s\")"
                                            + ".load(\"%s\")\n",xmlRowTag, attributePrefix, valueTag));

            return sb.toString();
        }


    }

    public String getRowTag() {
        return rowTag;
    }

    public void setRowTag(String rowTag) {
        this.rowTag = rowTag;
    }

    public String getAttributePrefix() {
        return attributePrefix;
    }

    public String getValueTag() { return valueTag; }

    public void setValueTag(String valueTag) { this.valueTag = valueTag; }

    public void setAttributePrefix(String attributePrefix) {
        this.attributePrefix = attributePrefix;
    }

    static class HiveXMLSchemaHandler extends DefaultHandler {

        /**
         * Starting tag
         */
        private String startTag;

        /**
         * Whether the start tag has been visited
         */
        boolean startTagFound;

        /**
         * Whether the startTag has any attributes
         */
        boolean startTagHasAttributes;

        /**
         * Whether to continue processing tags or ignore
         */
        private boolean stopProcessing;

        /**
         * Tracks the element stack
         */
        private Stack<String> elementStack = new Stack<>();

        /**
         * Columns and the corresponding xpath expression
         */
        private Map<String, String> columnPaths = new HashMap<>();

        private String lastQName;

        public HiveXMLSchemaHandler(String startTag) {
            this.startTag = startTag;
        }

        public String getStartTag() {
            return startTag;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            // Skip processing until we find start tag
            if (!stopProcessing) {

                // If startTag is not specified grab the first element
                if (StringUtils.isEmpty(startTag) || startTag.equals(qName)) {
                    this.startTagFound = true;
                    this.startTag = qName;
                    this.startTagHasAttributes = attributes.getLength() > 0;
                }
                if (startTagFound) {
                    // Track last visible name to determine if we are dealing with nested elements or simple text
                    this.lastQName = qName;
                    elementStack.push(qName);
                    if (!columnPaths.containsKey(qName) && elementStack.size() < 2) {
                        storeAttributePaths(attributes);
                    }
                }
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {

            if (qName.equals(startTag)) {
                this.stopProcessing = true;
            }

            if (!stopProcessing && this.startTagFound) {
                // Only add unique names
                if (!columnPaths.containsKey(qName) && elementStack.size() <= 2) {
                    String ext = ((lastQName.equals(qName)) ? "/text()" : "/*");
                    String path = String.format("\"column.xpath.%s\" = \"%s%s\"", qName, currentXPath(), ext);
                    addPath(qName, path);
                }
                elementStack.pop();
            }
        }

        @Override
        public void startPrefixMapping(String prefix, String uri) throws SAXException {
            super.startPrefixMapping(prefix, uri);
        }

        @Override
        public void endPrefixMapping(String prefix) throws SAXException {
            super.endPrefixMapping(prefix);
        }

        public void endDocument() throws SAXException {
            // only process data
        }

        private String getAttributeName(String qName) {
            return "_" + qName;
        }

        private void addPath(String columnName, String path) {
            this.columnPaths.put(columnName, path);
        }

        private void storeAttributePaths(Attributes attributes) {
            String xPath = currentXPath();
            if (attributes != null && attributes.getLength() > 0) {
                for (int i = 0; i < attributes.getLength(); i++) {
                    String attributeName = getAttributeName(attributes.getQName(i));
                    String path = String.format("\"column.xpath.%s\" = \"%s/@%s\"", attributeName, xPath, attributes.getQName(i));
                    addPath(attributeName, path);
                }
            }
        }

        private String currentXPath() {
            int size = this.elementStack.size();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < size; i++) {
                sb.append("/" + this.elementStack.get(i));
            }
            return sb.toString();
        }

    }


}

