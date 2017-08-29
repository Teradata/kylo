package com.thinkbiganalytics.feedmgr.rest.model.schema;

/*-
 * #%L
 * thinkbig-feed-manager-rest-model
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.thinkbiganalytics.discovery.model.DefaultField;
import com.thinkbiganalytics.discovery.model.DefaultTableSchema;
import com.thinkbiganalytics.discovery.schema.Field;
import com.thinkbiganalytics.discovery.schema.TableSchema;
import com.thinkbiganalytics.metadata.MetadataField;
import com.thinkbiganalytics.policy.rest.model.FieldPolicy;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TableSetup {

    private static final Logger log = LoggerFactory.getLogger(TableSetup.class);
    @MetadataField(description = "Nifi property name 'table_column_specs'")
    public String fieldStructure;
    @Deprecated
    //this is now referenced in the sourceTableSchema.name
    //${metadata.table.existingTableName} will still work, but it is advised to switch it to ${metadata.table.sourceTableSchema.name}
    public String existingTableName;
    @JsonSerialize(as = DefaultTableSchema.class)
    @JsonDeserialize(as = DefaultTableSchema.class)
    private TableSchema tableSchema;
    @JsonSerialize(as = DefaultTableSchema.class)
    @JsonDeserialize(as = DefaultTableSchema.class)
    private TableSchema sourceTableSchema;
    @JsonSerialize(as = DefaultTableSchema.class)
    @JsonDeserialize(as = DefaultTableSchema.class)
    private TableSchema feedTableSchema;
    private String method;
    private String description = "";
    private List<FieldPolicy> fieldPolicies;
    private List<PartitionField> partitions;
    private String tableType;
    @MetadataField
    private String incrementalDateField;
    @MetadataField(description = "Source Field to be used when incrementally querying Table Data ")
    private String sourceTableIncrementalDateField;
    private TableOptions options;
    @MetadataField(description = "Hive Row Format String for the Feed Table (example: ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' LINES TERMINATED BY '\\n' STORED AS\n  TEXTFILE ")
    private String feedFormat;
    @MetadataField(description = "Format of the Destination Table storage. Supported Values are: [STORED AS PARQUET, STORED AS ORC]")
    private String targetFormat;
    @MetadataField(description = "Destination Hive Table Properties string (i.e.  tblproperties(\"orc.compress\"=\"SNAPPY\") ")
    private String targetTblProperties;
    @MetadataField(description = "Strategy for merging data into the destination.  Supported Values are [Sync, Merge, Dedupe and Merge]")
    private String targetMergeStrategy;
    @MetadataField(description = "JSON array of FieldPolicy objects")
    private String fieldPoliciesJson;
    @MetadataField(description = "Nifi propety name 'elasticsearch.columns'")
    private String fieldIndexString;
    @MetadataField(description = "Nifi property name 'table_partition_specs'")
    private String partitionStructure;
    @MetadataField(description = "Nifi property name 'partition_specs'")
    private String partitionSpecs;
    @MetadataField(description = "List of destination (feed table) field names separated by a new line")
    private String fieldsString;
    @MetadataField(description = "List of source table field names separated by a new line")
    private String sourceFields;
    @MetadataField(description = "List of source table field names separated by a comma")
    private String sourceFieldsCommaString;
    @MetadataField(description = "Structure of the feed table")
    private String feedFieldStructure;
    @MetadataField(description = "List of fields that can be null separated by a comma")
    private String nullableFields;

    @MetadataField(description = "List of fields that are primary keys separated by a comma")
    private String primaryKeyFields;

    private Map<String,String> sourceTargetFieldMap;

    private Map<String,String> targetSourceFieldMap;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<FieldPolicy> getFieldPolicies() {
        return fieldPolicies;
    }

    public void setFieldPolicies(List<FieldPolicy> fieldPolicies) {
        this.fieldPolicies = fieldPolicies;
    }

    public List<PartitionField> getPartitions() {
        return partitions;
    }

    public void setPartitions(List<PartitionField> partitions) {
        this.partitions = partitions;
    }

    public String getPartitionStructure() {
        return partitionStructure;
    }

    public void setPartitionStructure(String partitionStructure) {
        this.partitionStructure = partitionStructure;
    }

    public String getFieldStructure() {
        return fieldStructure;
    }

    public void setFieldStructure(String fieldStructure) {
        this.fieldStructure = fieldStructure;
    }

    public String getFieldsString() {
        return fieldsString;
    }

    public void setFieldsString(String fieldsString) {
        this.fieldsString = fieldsString;
    }

    private void setStringBuffer(StringBuffer sb, String name, String separator) {
        if (StringUtils.isNotBlank(sb.toString())) {
            sb.append(separator);
        }
        sb.append(name);
    }

    private Field getFieldForName(String name) {
        if (tableSchema != null && tableSchema.getFields() != null) {
            return tableSchema.getFields().stream().filter(field -> field.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
        }
        return null;
    }

    /**
     * Ensure that the partition sourceFieldDataType matches the referencing source datatype
     * This is needed for the partitoins with the "val" as it needs to use that datatype
     */
    private void ensurePartitionSourceDataTypes() {
        if (partitions != null) {
            partitions.stream().forEach(partition -> {
                Field field = getFieldForName(partition.getSourceField());
                if (field != null) {
                    partition.setSourceDataType(field.getDataTypeWithPrecisionAndScale());
                }
            });
        }
    }

    @JsonIgnore
    public void simplifyFieldPoliciesForSerialization() {
        if (fieldPolicies != null) {
            getFieldPolicies().stream().forEach(fieldPolicy -> {
               if (fieldPolicy.getStandardization() != null) {
                    fieldPolicy.getStandardization().stream().forEach(policy -> policy.simplifyForSerialization());
                }
                if (fieldPolicy.getValidation() != null) {
                    fieldPolicy.getValidation().stream().forEach(policy -> policy.simplifyForSerialization());
                }

                boolean isPartitionColumn = getPartitions().stream().anyMatch(partitionArrayItem -> partitionArrayItem.getSourceField().equalsIgnoreCase(fieldPolicy.getFieldName()));
                fieldPolicy.setPartitionColumn(isPartitionColumn);
            });
        }
    }


    @JsonIgnore
    public void updateFieldStringData() {
        StringBuffer fieldsString = new StringBuffer();
        StringBuffer nullableFieldsString = new StringBuffer();
        StringBuffer primaryKeyFieldsString = new StringBuffer();
        if (tableSchema != null && tableSchema.getFields() != null) {
            for (Field field : tableSchema.getFields()) {
                setStringBuffer(fieldsString, field.getName(), "\n");
                if (field.isNullable()) {
                    setStringBuffer(nullableFieldsString, field.getName(), ",");
                }
                if (field.isPrimaryKey()) {
                    setStringBuffer(primaryKeyFieldsString, field.getName(), ",");
                }
            }
        }
        setFieldsString(fieldsString.toString());
        setNullableFields(nullableFieldsString.toString());
        setPrimaryKeyFields(primaryKeyFieldsString.toString());
    }

    /**
     * ensure the source names are set to some value
     */
    private void ensureSourceTableSchemaFieldNames() {
        if (sourceTableSchema != null && sourceTableSchema.getFields() != null) {
            long nullFields = sourceTableSchema.getFields().stream().filter(field -> StringUtils.isBlank(field.getName())).count();
            //if the source fields are all null and the counts match that from the dest table, reset the source to the dest names
            if (nullFields == sourceTableSchema.getFields().size() && tableSchema.getFields() != null && tableSchema.getFields().size() == sourceTableSchema.getFields().size()) {
                //reset the names to be that of the dest table?
                List<String> names = tableSchema.getFields().stream().map(f -> f.getName()).collect(Collectors.toList());
                for (int i = 0; i < tableSchema.getFields().size(); i++) {
                    Field f = sourceTableSchema.getFields().get(i);
                    if (f instanceof DefaultField) {
                        ((DefaultField) f).setName(names.get(i));
                    }
                }
            }
        }
    }

    @JsonIgnore
    public void updateSourceFieldsString() {
        StringBuffer sb = new StringBuffer();
        if (sourceTableSchema != null && sourceTableSchema.getFields() != null) {
            for (Field field : sourceTableSchema.getFields()) {
                setStringBuffer(sb, field.getName(), "\n");
            }
            setSourceFields(sb.toString());
        }

    }

    @JsonIgnore
    public void updateSourceFieldsCommaString() {
        StringBuffer sb = new StringBuffer();
        if (sourceTableSchema != null && sourceTableSchema.getFields() != null) {
            for (Field field : sourceTableSchema.getFields()) {
                setStringBuffer(sb, field.getName(), ",");
            }
            setSourceFieldsCommaString(sb.toString());
        }

    }

    @JsonIgnore
    public String getFieldStructure(String type, TableSchema schema) {
        StringBuffer sb = new StringBuffer();
        if (schema != null && schema.getFields() != null) {
            for (Field field : schema.getFields()) {
                if (StringUtils.isNotBlank(sb.toString())) {
                    sb.append("\n");
                }
                String otherName = "";
                if(type.equalsIgnoreCase("FEED")) {
                    otherName = getSourceTargetFieldMap().getOrDefault(field.getName(), "");
                }
                else {
                    otherName = getTargetSourceFieldMap().getOrDefault(field.getName(),"");
                }
                sb.append(field.asFieldStructure(otherName));

            }
        }
        return sb.toString();
    }


    @JsonIgnore
    public void updateFieldStructure() {
        setFieldStructure(getFieldStructure("TARGET",tableSchema));
    }

    @JsonIgnore
    public void updateFeedStructure() {
        setFeedFieldStructure(getFieldStructure("FEED",feedTableSchema));
    }

    @JsonIgnore
    public void updateFieldIndexString() {
        StringBuffer sb = new StringBuffer();
        if (tableSchema != null && tableSchema.getFields() != null && fieldPolicies != null) {
            int idx = 0;
            for (FieldPolicy field : fieldPolicies) {
                if (field.isIndex() && StringUtils.isNotBlank(sb.toString())) {
                    sb.append(",");
                }
                if (field.isIndex()) {
                    sb.append(tableSchema.getFields().get(idx).getName());
                }
                idx++;
            }
        }
        fieldIndexString = sb.toString();
    }

    @JsonIgnore
    public void updateFieldPolicyNames() {
        if (tableSchema != null && tableSchema.getFields() != null && fieldPolicies != null && fieldPolicies.size() == tableSchema.getFields().size()) {
            if(sourceTargetFieldMap == null){
                sourceTargetFieldMap = new HashMap<>();
            }
            if(targetSourceFieldMap == null){
                targetSourceFieldMap = new HashMap<>();
            }
            int idx = 0;
            for (FieldPolicy field : fieldPolicies) {
                field.setFieldName(tableSchema.getFields().get(idx).getName());
                sourceTargetFieldMap.put(field.getFeedFieldName(),field.getFieldName());
                targetSourceFieldMap.put(field.getFieldName(),field.getFeedFieldName());
                idx++;
            }
        }
    }


    @JsonIgnore
    public void updatePartitionStructure() {
        StringBuffer sb = new StringBuffer();
        if (partitions != null) {
            for (PartitionField field : partitions) {
                if (StringUtils.isNotBlank(sb.toString())) {
                    sb.append("\n");
                }
                sb.append(field.asPartitionStructure());

            }
        }
        setPartitionStructure(sb.toString());
    }


    @JsonIgnore
    public void updatePartitionSpecs() {
        StringBuffer sb = new StringBuffer();
        if (partitions != null) {
            for (PartitionField field : partitions) {
                if (StringUtils.isNotBlank(sb.toString())) {
                    sb.append("\n");
                }
                sb.append(field.asPartitionSpec());

            }
        }
        setPartitionSpecs(sb.toString());
    }

    @JsonIgnore
    private void updateFieldPolicyJson() {
        ObjectMapper mapper = new ObjectMapper();
        String json = "[]";
        try {
            simplifyFieldPoliciesForSerialization();
            json = mapper.writeValueAsString(getFieldPolicies());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        fieldPoliciesJson = json;

    }


    private void updateTargetTblProperties() {
        this.targetTblProperties = "";

        //build based upon compression options
        if (options != null && StringUtils.isNotBlank(options.getCompressionFormat()) && !"NONE".equalsIgnoreCase(options.getCompressionFormat())) {
            if ("STORED AS PARQUET".equalsIgnoreCase(getTargetFormat())) {
                this.targetTblProperties = "tblproperties(\"parquet.compression\"=\"" + options.getCompressionFormat() + "\")";
            } else if ("STORED AS ORC".equalsIgnoreCase(getTargetFormat())) {
                this.targetTblProperties = "tblproperties(\"orc.compress\"=\"" + options.getCompressionFormat() + "\")";
            } else {
                log.warn("Compression enabled with unsupported target format: {}", getTargetFormat());
            }
        }
    }

    public void ensureNotEmpty() {
        if (StringUtils.isBlank(sourceFields)) {
            sourceFields = "NA";
        }
        if (StringUtils.isBlank(sourceFieldsCommaString)) {
            sourceFieldsCommaString = "NA";
        }
        if (sourceTableSchema != null) {
            if (StringUtils.isBlank(sourceTableSchema.getName())) {
                sourceTableSchema.setName("NA");
            }
        }
    }

    public void updateMetadataFieldValues() {
        ensurePartitionSourceDataTypes();
        updatePartitionStructure();

        updateFieldStringData();
        ensureSourceTableSchemaFieldNames();
        updateSourceFieldsString();
        updateSourceFieldsCommaString();
        updateFieldIndexString();
        updatePartitionSpecs();
        updateFieldPolicyNames();
        updateFieldStructure();
        updateFeedStructure();
        updateFieldPolicyJson();
        updateTargetTblProperties();
        ensureNotEmpty();

    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getTableType() {
        return tableType;
    }

    public void setTableType(String tableType) {
        this.tableType = tableType;
    }

    public TableOptions getOptions() {
        return options;
    }

    public void setOptions(TableOptions options) {
        this.options = options;
    }


    public String getPartitionSpecs() {
        return partitionSpecs;
    }

    public void setPartitionSpecs(String partitionSpecs) {
        this.partitionSpecs = partitionSpecs;
    }

    public TableSchema getTableSchema() {
        return tableSchema;
    }

    public void setTableSchema(TableSchema tableSchema) {
        this.tableSchema = tableSchema;
    }

    public String getFieldIndexString() {
        return fieldIndexString;
    }

    public void setFieldIndexString(String fieldIndexString) {
        this.fieldIndexString = fieldIndexString;
    }

    public String getExistingTableName() {
        return existingTableName;
    }

    public void setExistingTableName(String existingTableName) {
        this.existingTableName = existingTableName;
    }

    public String getIncrementalDateField() {
        return incrementalDateField;
    }

    public void setIncrementalDateField(String incrementalDateField) {
        this.incrementalDateField = incrementalDateField;
    }

    public TableSchema getSourceTableSchema() {
        return sourceTableSchema;
    }

    public void setSourceTableSchema(TableSchema sourceTableSchema) {
        this.sourceTableSchema = sourceTableSchema;
    }

    public TableSchema getFeedTableSchema() {
        return feedTableSchema;
    }

    public void setFeedTableSchema(TableSchema feedTableSchema) {
        this.feedTableSchema = feedTableSchema;
    }

    public String getSourceFields() {
        return sourceFields;
    }

    public void setSourceFields(String sourceFields) {
        this.sourceFields = sourceFields;
    }

    public String getNullableFields() {
        return nullableFields;
    }

    public void setNullableFields(String nullableFields) {
        this.nullableFields = nullableFields;
    }

    public String getPrimaryKeyFields() {
        return primaryKeyFields;
    }

    public void setPrimaryKeyFields(String primaryKeyFields) {
        this.primaryKeyFields = primaryKeyFields;
    }

    public String getFieldPoliciesJson() {
        return fieldPoliciesJson;
    }

    public void setFieldPoliciesJson(String fieldPoliciesJson) {
        this.fieldPoliciesJson = fieldPoliciesJson;
    }


    public String getFeedFormat() {
        return feedFormat;
    }

    public void setFeedFormat(String feedFormat) {
        this.feedFormat = feedFormat;
    }

    public String getTargetFormat() {
        return targetFormat;
    }

    public void setTargetFormat(String targetFormat) {
        this.targetFormat = targetFormat;
    }

    public String getTargetTblProperties() {
        return targetTblProperties;
    }

    public void setTargetTblProperties(String targetTblProperties) {
        this.targetTblProperties = targetTblProperties;
    }

    public String getSourceTableIncrementalDateField() {
        return sourceTableIncrementalDateField;
    }

    public void setSourceTableIncrementalDateField(String sourceTableIncrementalDateField) {
        this.sourceTableIncrementalDateField = sourceTableIncrementalDateField;
    }

    public String getTargetMergeStrategy() {
        return targetMergeStrategy;
    }

    public void setTargetMergeStrategy(String targetMergeStrategy) {
        this.targetMergeStrategy = targetMergeStrategy;
    }

    public String getSourceFieldsCommaString() {
        return sourceFieldsCommaString;
    }

    public void setSourceFieldsCommaString(String sourceFieldsCommaString) {
        this.sourceFieldsCommaString = sourceFieldsCommaString;
    }

    public String getFeedFieldStructure() {
        return feedFieldStructure;
    }

    public void setFeedFieldStructure(String feedFieldStructure) {
        this.feedFieldStructure = feedFieldStructure;
    }

    public Map<String, String> getSourceTargetFieldMap() {
        return sourceTargetFieldMap;
    }

    public void setSourceTargetFieldMap(Map<String, String> sourceTargetFieldMap) {
        this.sourceTargetFieldMap = sourceTargetFieldMap;
    }

    public Map<String, String> getTargetSourceFieldMap() {
        return targetSourceFieldMap;
    }

    public void setTargetSourceFieldMap(Map<String, String> targetSourceFieldMap) {
        this.targetSourceFieldMap = targetSourceFieldMap;
    }
}
