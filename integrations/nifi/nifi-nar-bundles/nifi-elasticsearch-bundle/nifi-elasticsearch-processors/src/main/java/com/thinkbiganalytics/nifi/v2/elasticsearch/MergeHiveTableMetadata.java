package com.thinkbiganalytics.nifi.v2.elasticsearch;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.IOUtils;
import org.apache.nifi.annotation.behavior.InputRequirement;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.logging.ProcessorLog;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.io.InputStreamCallback;
import org.apache.nifi.processor.io.OutputStreamCallback;
import org.apache.nifi.processor.util.StandardValidators;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

/**
 * This processor aggregates JSON metadata about a hive table so that a table and it's columns are in one JSON document
 */
@InputRequirement(InputRequirement.Requirement.INPUT_REQUIRED)
@Tags({"hive", "metadata", "thinkbig", "elasticsearch"})
@CapabilityDescription("Aggregate JSON across multiple documents into one document representing a Hive table (V2)")
public class MergeHiveTableMetadata extends AbstractProcessor {
    // relationships
    public static final Relationship REL_SUCCESS = new Relationship.Builder()
            .name("success")
            .description("Json objects that are successfully merged are transferred to this relationship")
            .build();

    public static final Relationship REL_FAILURE = new Relationship.Builder()
            .name("failure")
            .description(
                    "Json objects that are un-successfully merged are transferred to this relationship")
            .build();
    private final Set<Relationship> relationships;

    // properties
    public static final PropertyDescriptor DATABASE_NAME = new PropertyDescriptor.Builder()
            .name("Database Name Field")
            .description("The name of the hive database field")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .expressionLanguageSupported(true)
            .build();
    public static final PropertyDescriptor DATABASE_OWNER = new PropertyDescriptor.Builder()
            .name("Database Owner Field")
            .description("Database owner field name")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .expressionLanguageSupported(true)
            .build();
    public static final PropertyDescriptor TABLE_CREATE_TIME = new PropertyDescriptor.Builder()
            .name("Table Create Time Field")
            .description("Field representing the table create time")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .expressionLanguageSupported(true)
            .build();
    public static final PropertyDescriptor TABLE_NAME = new PropertyDescriptor.Builder()
            .name("Table Name Field")
            .description("Field holding the table name")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .expressionLanguageSupported(true)
            .build();
    public static final PropertyDescriptor TABLE_TYPE = new PropertyDescriptor.Builder()
            .name("Table Type Field")
            .description("Field representing what type of hive table it is")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .expressionLanguageSupported(true)
            .build();
    public static final PropertyDescriptor COLUMN_NAME = new PropertyDescriptor.Builder()
            .name("Column Name Field")
            .description("Field representing the column name")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .expressionLanguageSupported(true)
            .build();
    public static final PropertyDescriptor COLUMN_TYPE = new PropertyDescriptor.Builder()
            .name("Column Type Field")
            .description("Field representing what the column type is")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .expressionLanguageSupported(true)
            .build();

    private final List<PropertyDescriptor> propDescriptors;

    public MergeHiveTableMetadata() {
        final Set<Relationship> r = new HashSet<>();
        r.add(REL_SUCCESS);
        r.add(REL_FAILURE);
        relationships = Collections.unmodifiableSet(r);

        final List<PropertyDescriptor> pds = new ArrayList<>();
        pds.add(DATABASE_NAME);
        pds.add(DATABASE_OWNER);
        pds.add(TABLE_CREATE_TIME);
        pds.add(TABLE_NAME);
        pds.add(TABLE_TYPE);
        pds.add(COLUMN_NAME);
        pds.add(COLUMN_TYPE);
        propDescriptors = Collections.unmodifiableList(pds);
    }

    @Override
    public Set<Relationship> getRelationships() {
        return relationships;
    }

    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return propDescriptors;
    }

    @Override
    public void onTrigger(final ProcessContext context, final ProcessSession session) throws ProcessException {
        final ProcessorLog logger = getLogger();
        FlowFile incoming = session.get();
        FlowFile outgoing = (incoming == null ? session.create() : incoming);
        try {
              /* Configuration parameters for spark launcher */
            final String databaseNameField = context.getProperty(DATABASE_NAME).evaluateAttributeExpressions(outgoing).getValue();
            final String databaseOwnerField = context.getProperty(DATABASE_OWNER).evaluateAttributeExpressions(outgoing).getValue();
            final String tableCreateTimeField = context.getProperty(TABLE_CREATE_TIME).evaluateAttributeExpressions(outgoing).getValue();
            final String tableNameField = context.getProperty(TABLE_NAME).evaluateAttributeExpressions(outgoing).getValue();
            final String tableTypeField = context.getProperty(TABLE_TYPE).evaluateAttributeExpressions(outgoing).getValue();
            final String columnNameField = context.getProperty(COLUMN_NAME).evaluateAttributeExpressions(outgoing).getValue();
            final String columnTypeField = context.getProperty(COLUMN_TYPE).evaluateAttributeExpressions(outgoing).getValue();

            final StringBuffer sb = new StringBuffer();
            session.read(incoming, new InputStreamCallback() {

                @Override
                public void process(InputStream in) throws IOException {
                    sb.append(IOUtils.toString(in));
                }

            });

            logger.debug("The json that was received is: " + sb.toString());

            outgoing = session.write(outgoing, new OutputStreamCallback() {
                @Override
                public void process(final OutputStream out) throws IOException {
                    try {
                        JSONArray array = new JSONArray(sb.toString());
                        Map<String, Metadata> tables = new HashMap();

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject jsonObj = array.getJSONObject(i);
                            String databaseName = jsonObj.getString(databaseNameField);
                            String databaseOwner = jsonObj.getString(databaseOwnerField);
                            String tableName = jsonObj.getString(tableNameField);
                            String tableCreateTime = jsonObj.getString(tableCreateTimeField);
                            String tableType = jsonObj.getString(tableTypeField);
                            String columnName = jsonObj.getString(columnNameField);
                            String columnType = jsonObj.getString(columnTypeField);
                            String key = databaseName + tableName;

                            if (tables.containsKey(key)) {
                                Metadata meta = tables.get(key);
                                HiveColumn column = new HiveColumn();
                                column.setColumnName(columnName);
                                column.setColumnType(columnType);
                                meta.getHiveColumns().add(column);

                            } else {
                                Metadata meta = new Metadata();
                                meta.setDatabaseName(databaseName);
                                meta.setDatabaseOwner(databaseOwner);
                                meta.setTableCreateTime(tableCreateTime);
                                meta.setTableName(tableName);
                                meta.setTableType(tableType);
                                HiveColumn column = new HiveColumn();
                                column.setColumnName(columnName);
                                column.setColumnType(columnType);
                                meta.getHiveColumns().add(column);
                                tables.put(key, meta);
                            }
                        }
                        List<Metadata> tablesAsList = new ArrayList();
                        Iterator iter = tables.entrySet().iterator();
                        while (iter.hasNext()) {
                            Map.Entry pair = (Map.Entry) iter.next();
                            tablesAsList.add((Metadata) pair.getValue());
                        }
                        Gson gson = new Gson();
                        JsonElement element = gson.toJsonTree(tablesAsList, new TypeToken<List<Metadata>>() {
                        }.getType());
                        JsonArray jsonArray = element.getAsJsonArray();

                        out.write(jsonArray.toString().getBytes());
                    } catch (final Exception e) {
                        throw new ProcessException(e);
                    }
                }
            });

            logger.info("*** Completed with status ");
            session.transfer(outgoing, REL_SUCCESS);
        } catch (final Exception e) {
            e.printStackTrace();
            logger.error("Unable to execute merge hive json job", new Object[]{incoming, e});
            session.transfer(incoming, REL_FAILURE);
        }

    }

    private class Metadata {
        private String databaseName;
        private String databaseOwner;
        private String tableCreateTime;
        private String tableName;
        private String tableType;
        private List<HiveColumn> hiveColumns = new ArrayList();

        public List<HiveColumn> getHiveColumns() {
            return hiveColumns;
        }

        public void setHiveColumns(List<HiveColumn> hiveColumns) {
            this.hiveColumns = hiveColumns;
        }


        public String getDatabaseName() {
            return databaseName;
        }

        public void setDatabaseName(String databaseName) {
            this.databaseName = databaseName;
        }

        public String getDatabaseOwner() {
            return databaseOwner;
        }

        public void setDatabaseOwner(String databaseOwner) {
            this.databaseOwner = databaseOwner;
        }

        public String getTableCreateTime() {
            return tableCreateTime;
        }

        public void setTableCreateTime(String tableCreateTime) {
            this.tableCreateTime = tableCreateTime;
        }

        public String getTableName() {
            return tableName;
        }

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        public String getTableType() {
            return tableType;
        }

        public void setTableType(String tableType) {
            this.tableType = tableType;
        }
    }

    private class HiveColumn {
        private String columnName;
        private String columnType;

        public String getColumnType() {
            return columnType;
        }

        public void setColumnType(String columnType) {
            this.columnType = columnType;
        }

        public String getColumnName() {
            return columnName;
        }

        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }
    }
}
