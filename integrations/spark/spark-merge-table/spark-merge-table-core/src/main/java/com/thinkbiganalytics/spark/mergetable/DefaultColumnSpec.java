/**
 * 
 */
package com.thinkbiganalytics.spark.mergetable;

/*-
 * #%L
 * thinkbig-nifi-core-processors
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

import com.google.common.io.LineReader;
import com.thinkbiganalytics.hive.util.HiveUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DefaultColumnSpec implements ColumnSpec, Serializable {
    
    private static final long serialVersionUID = 1L;

    private String name;
    private String comment;
    private String dataType;
    private boolean primaryKey;
    private boolean createDate;
    private boolean modifiedDate;
    private String otherColumnName;

    public DefaultColumnSpec(String name, String dataType, String comment) {
        this(name, dataType, comment, false, false, false);
    }

    public DefaultColumnSpec(String name, String dataType, String comment, boolean pk, boolean createDt, boolean modifiedDt) {
        this(name, dataType, comment, pk, createDt, modifiedDt,null);
    }

    public DefaultColumnSpec(DefaultColumnSpec other) {
        this.name = other.name;
        this.comment = other.comment;
        this.dataType = other.dataType;
        this.primaryKey = other.primaryKey;
        this.createDate = other.createDate;
        this.modifiedDate = other.modifiedDate;
        this.otherColumnName = other.otherColumnName;
    }

    public DefaultColumnSpec(String name, String dataType, String comment, boolean pk, boolean createDt, boolean modifiedDt, String otherColumnName) {
        super();
        Validate.notEmpty(name);

        this.name = name.toLowerCase().trim();
        this.dataType = (StringUtils.isEmpty(dataType) ? "string" : dataType);
        this.comment = comment;
        this.primaryKey = pk;
        this.createDate = createDt;
        this.modifiedDate = modifiedDt;
        this.otherColumnName = otherColumnName != null ? otherColumnName.toLowerCase() : "";
        validate();
    }

    public static String toPrimaryKeyJoinSQL(List<ColumnSpec> specs, String leftTableAlias, String rightTableAlias) {
        final String safeLeftTable = HiveUtils.quoteIdentifier(leftTableAlias);
        final String safeRightTable = HiveUtils.quoteIdentifier(rightTableAlias);
        final List<String> conditions = new ArrayList<>();
        
        for (ColumnSpec spec : specs) {
            if (spec.isPrimaryKey()) {
                String column = HiveUtils.quoteIdentifier(spec.getName());
                String condtion = safeLeftTable + "." + column + " = " + safeRightTable + "." + column;
                conditions.add(condtion);
            }
        }
        
        return StringUtils.join(conditions, " AND ");
    }

    public static List<String> toPrimaryKeys(List<ColumnSpec> specs) {
        final List<String> keys = new ArrayList<>();
        
        for (ColumnSpec spec : specs) {
            if (spec.isPrimaryKey()) {
                String key = HiveUtils.quoteIdentifier(spec.getName());
                keys.add(key);
            }
        }
        
        return keys;
    }
    
    /**
     * Method for defining a column specification as a pipe-delimited format: column|data type|comment (optional)|primaryKey|created|modified.
     */
    public static ColumnSpec createFromString(String specString) {
        Validate.notEmpty(specString);
        
        String[] parts = specString.split("\\|");
        
        Validate.notEmpty(parts);
        
        int len = parts.length;
        String columnName = "";
        String comment = "";
        String dataType = "string";
        boolean pk = false;
        boolean modifiedDt = false;
        boolean createDt = false;
        String otherName = "";
        
        switch (len) {
            default:
            case 7:
                otherName = parts[6];
            case 6:
                modifiedDt = "1".equals(parts[5].trim());
            case 5:
                createDt = "1".equals(parts[4].trim());
            case 4:
                pk = "1".equals(parts[3].trim());
            case 3:
                comment = parts[2];
            case 2:
                dataType = parts[1];
            case 1:
                columnName = parts[0];
        }
        
        return new DefaultColumnSpec(columnName, dataType, comment, pk, createDt, modifiedDt,otherName);
    }

    /**
     * Method for defining a column specification as a pipe-delimited format:  column|data type|comment (optional)|primaryKey|created|modified. Each row separated by a newline
     */
    public static Set<ColumnSpec> createFromStrings(String specString) {
        Validate.notEmpty(specString);
        
        try {
            Set<ColumnSpec> specs = new HashSet<>();
            LineReader lineReader = new LineReader( new StringReader(specString));
            String line = lineReader.readLine();

            while (line != null) {
                specs.add(createFromString(line));
                line = lineReader.readLine();
            }

            return specs;
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse column specs[" + specString + "]", e);
        }
    }

    private final void validate() {
        Validate.notEmpty(name);
        Validate.notEmpty(dataType);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(boolean pk) {
        this.primaryKey = pk;
    }

    public boolean isCreateDate() {
        return createDate;
    }

    public void setCreateDate(boolean createDt) {
        this.createDate = createDt;
    }

    public boolean isModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(boolean modifiedDt) {
        this.modifiedDate = modifiedDt;
    }

    public String toCreateSQL() {
        return toCreateSQL(false);
    }

    public String toCreateSQL(boolean strings) {
        StringBuffer sb = new StringBuffer();
        sb.append(HiveUtils.quoteIdentifier(name)).append(' ');
        sb.append((strings ? "string" : HiveUtils.quoteDataType(dataType)));
        if (!StringUtils.isEmpty(comment)) {
            sb.append(" COMMENT ").append(HiveUtils.quoteString(comment));
        }
        return sb.toString();
    }

    public String toPartitionSQL() {
        return HiveUtils.quoteIdentifier(name) + " " + dataType;
    }

    public String getOtherColumnName() {
        return otherColumnName;
    }
}
