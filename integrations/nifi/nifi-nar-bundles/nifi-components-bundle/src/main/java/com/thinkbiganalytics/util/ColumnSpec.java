/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.util;

import com.google.common.io.LineReader;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Vector;

public class ColumnSpec {

    private String name;

    private String comment;

    private String dataType;

    public ColumnSpec(String name, String dataType, String comment) {
        super();
        Validate.notEmpty(name);

        this.name = name.toLowerCase().trim();
        this.dataType = (StringUtils.isEmpty(dataType) ? "string" : dataType);
        this.comment = comment;
        validate();
    }

    public void validate() {
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

    public String toCreateSQL() {
        return toCreateSQL(false);
    }

    public String toCreateSQL(boolean strings) {
        StringBuffer sb = new StringBuffer();
        sb.append("`").append(name).append("` ");
        sb.append((strings ? "string" : dataType));
        if (!StringUtils.isEmpty(comment)) {
            sb.append(" COMMENT ").append("'").append(comment).append("'");
        }
        return sb.toString();
    }

    public String toPartitionSQL() {
        StringBuffer sb = new StringBuffer();
        sb.append("`").append(name).append("` ");
        sb.append(dataType);
        return sb.toString();
    }

    public String toAlterSQL() {
        throw new UnsupportedOperationException();
    }

    /**
     * Method  for defining a column specification as a pipe-delimited format:  column|data type|comment (optional). Each row separated by a newline
     */
    public static ColumnSpec[] createFromString(String specString) {
        if (StringUtils.isEmpty(specString)) return null;
        List<ColumnSpec> specs = new Vector<>();
        try {
            LineReader lineReader = new LineReader(
                    new StringReader(specString));

            String line;
            while ((line = lineReader.readLine()) != null) {
                String[] parts = line.split("\\|");
                int len = parts.length;
                if (len > 0) {
                    String columnName = "";
                    String comment = "";
                    String dataType = "string";
                    switch (len) {
                        default:
                        case 3:
                            comment = parts[2];
                        case 2:
                            dataType = parts[1];
                        case 1:
                            columnName = parts[0];
                    }
                    specs.add(new ColumnSpec(columnName, dataType, comment));
                }
            }
            return specs.toArray(new ColumnSpec[0]);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse column specs[" + specString + "]", e);
        }

    }
}
