package com.thinkbiganalytics.feedmgr.rest.model.schema;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.thinkbiganalytics.db.model.schema.Field;
import com.thinkbiganalytics.db.model.schema.TableSchema;
import com.thinkbiganalytics.feedmgr.metadata.MetadataField;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Created by sr186054 on 2/12/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TableSetup {

    private TableSchema tableSchema;

    private TableSchema sourceTableSchema;

    private String method;

    private String description = "";

    private List<FieldPolicy> fieldPolicies;

    private List<PartitionField> partitions;

    private String tableType;

    @MetadataField
    private String incrementalDateField;

    private TableOptions options;

    private String recordFormat;

    @MetadataField
    private String fieldIndexString;

    @MetadataField
    private String partitionStructure;

    @MetadataField
    private String partitionSpecs;

    @MetadataField
    public String fieldStructure;

    @MetadataField(description = "List of destination (feed table) field names separated by a new line")
    private String fieldsString;

    @MetadataField(description = "List of source table field names separated by a new line")
    private String sourceFields;

    @Deprecated
    //this is now referenced in the sourceTableSchema.name
    //${metadata.table.existingTableName} will still work, but it is advised to switch it to ${metadata.table.sourceTableSchema.name}
    public String existingTableName;


    @MetadataField(description = "List of fields that can be null separated by a comma")
    private String nullableFields;

    @MetadataField(description = "List of fields that are primary keys separated by a comma")
    private String primaryKeyFields;


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

    public void setFieldStructure(String fieldStructure) {
        this.fieldStructure = fieldStructure;
    }

    public String getFieldStructure() {
        return fieldStructure;
    }

    public String getFieldsString() {
        return fieldsString;
    }

    public void setFieldsString(String fieldsString) {
        this.fieldsString = fieldsString;
    }

    private void setStringBuffer(StringBuffer sb, String name,String separator){
        if(StringUtils.isNotBlank(sb.toString())){
            sb.append(separator);
        }
        sb.append(name);
    }



    @JsonIgnore
    public void updateFieldStringData(){
        StringBuffer fieldsString = new StringBuffer();
        StringBuffer nullableFieldsString = new StringBuffer();
        StringBuffer primaryKeyFieldsString = new StringBuffer();
        if(tableSchema != null && tableSchema.getFields() != null) {
            for(Field field: tableSchema.getFields()){
                setStringBuffer(fieldsString,field.getName(),"\n");
                if(field.getNullable()){
                    setStringBuffer(nullableFieldsString,field.getName(),",");
                }
                if(field.getPrimaryKey()){
                    setStringBuffer(primaryKeyFieldsString,field.getName(),",");
                }
            }
        }
        setFieldsString(fieldsString.toString());
        setNullableFields(nullableFieldsString.toString());
        setPrimaryKeyFields(primaryKeyFieldsString.toString());
    }


    @JsonIgnore
    public void updateSourceFieldsString(){
        StringBuffer sb = new StringBuffer();
        if(sourceTableSchema != null && sourceTableSchema.getFields() != null) {
            for(Field field: sourceTableSchema.getFields()){
                setStringBuffer(sb,field.getName(),"\n");

            }
        }
        setSourceFields(sb.toString());
    }

    @JsonIgnore
    public void updateFieldStructure(){
        StringBuffer sb = new StringBuffer();
        if(tableSchema != null && tableSchema.getFields() != null) {
            for(Field field: tableSchema.getFields()){
                if(StringUtils.isNotBlank(sb.toString())){
                    sb.append("\n");
                }
                sb.append(field.asFieldStructure());

            }
        }
        setFieldStructure(sb.toString());
    }

    @JsonIgnore
    public void updateFieldIndexString(){
        StringBuffer sb = new StringBuffer();
        if(tableSchema != null && tableSchema.getFields() != null && fieldPolicies != null) {
            int idx = 0;
            for(FieldPolicy field: fieldPolicies){
                if(field.isIndex() && StringUtils.isNotBlank(sb.toString())){
                    sb.append(",");
                }
                if(field.isIndex()) {
                    sb.append(tableSchema.getFields().get(idx).getName());
                }
                idx++;
            }
        }
        fieldIndexString = sb.toString();
    }

    @JsonIgnore
    public void updateFieldPolicyNames(){
        if(tableSchema != null && tableSchema.getFields() != null && fieldPolicies != null) {
            int idx = 0;
            for(FieldPolicy field: fieldPolicies){
                field.setFieldName(tableSchema.getFields().get(idx).getName());
            }
        }
    }


    @JsonIgnore
    public void updatePartitionStructure(){
        StringBuffer sb = new StringBuffer();
        if(partitions != null) {
            for(PartitionField field: partitions){
                if(StringUtils.isNotBlank(sb.toString())){
                    sb.append("\n");
                }
                sb.append(field.asPartitionStructure());

            }
        }
        setPartitionStructure(sb.toString());
    }


    @JsonIgnore
    public void updatePartitionSpecs(){
        StringBuffer sb = new StringBuffer();
        if(partitions != null) {
            for(PartitionField field: partitions){
                if(StringUtils.isNotBlank(sb.toString())){
                    sb.append("\n");
                }
                sb.append(field.asPartitionSpec());

            }
        }
        setPartitionSpecs(sb.toString());
    }

    public void updateMetadataFieldValues(){
        updatePartitionStructure();
        updateFieldStructure();
        updateFieldStringData();
        updateSourceFieldsString();
        updateFieldIndexString();
        updatePartitionSpecs();
        updateFieldPolicyNames();

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

    public String getRecordFormat() {
        return recordFormat;
    }

    public void setRecordFormat(String recordFormat) {
        this.recordFormat = recordFormat;
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
}
