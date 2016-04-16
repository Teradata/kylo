package com.thinkbiganalytics.jobrepo.query.support;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by sr186054 on 8/13/15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class QueryColumnFilter implements ColumnFilterItem {
    protected String name;
    protected Object value;
    private String operator;
    private String dataType = "string";
    private String sqlConditionBeforeOperator;

    private String tableAlias;

    private String queryName;
    boolean applyToWhereClause = true;


    @Override
    public void setApplyToWhereClause(boolean applyToWhereClause) {
        this.applyToWhereClause = applyToWhereClause;
    }

    @JsonIgnore
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");


    public QueryColumnFilter() {

    }

    public QueryColumnFilter(@JsonProperty("name") String name, @JsonProperty("value") Object value) {
        this.name = name;
        this.value = value;
        this.operator = "=";
    }

    public QueryColumnFilter(@JsonProperty("name") String name, @JsonProperty("value") Object value, @JsonProperty("operator") String operator) {
        this.name = name;
        this.value = value;
        this.operator = operator;
    }

    @Override
    public String getSqlConditionBeforeOperator() {
        if (StringUtils.isNotBlank(sqlConditionBeforeOperator)) {
            return sqlConditionBeforeOperator;
        } else {
            return getQueryName();
        }
    }

    @Override
    public void setSqlConditionBeforeOperator(String sqlConditionBeforeOperator) {
        this.sqlConditionBeforeOperator = sqlConditionBeforeOperator;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public String getOperator() {
        return operator;
    }

    @Override
    public void setOperator(String operator) {
        this.operator = operator;
    }

    @JsonIgnore
    private Object convertToDataType(String str) {
        Object item = null;
        try {
            if (StringUtils.isNotBlank(str)) {
                if ("string".equalsIgnoreCase(dataType)) {
                    item = str;
                }
                if ("integer".equalsIgnoreCase(dataType)) {
                    item = Integer.parseInt(str);
                } else if ("long".equalsIgnoreCase(dataType)) {
                    item = Long.parseLong(str);
                } else if ("double".equalsIgnoreCase(dataType)) {
                    item = Double.parseDouble(str);
                } else if ("float".equalsIgnoreCase(dataType)) {
                    item = Float.parseFloat(str);
                } else if ("date".equalsIgnoreCase(dataType)) {
                    item = simpleDateFormat.parse(str);
                } else {
                    item = str;
                }
            }
        } catch (ParseException parseException) {

        } catch (NumberFormatException nfe) {

        }
        return item;

    }

    @Override
    @JsonIgnore
    public Object getSqlValue() {
        if (this.isCollection()) {
            return getValueAsCollection();
        } else {
            Object value = convertToDataType(getStringValue());
            return value;
        }
    }

    @Override
    @JsonIgnore
    public String getStringValue() {
        if (getValue() != null) {
            return getValue().toString();
        }
        return null;
    }


    @Override
    @JsonIgnore
    public Collection<?> getValueAsCollection() {
        Collection<? extends Object> collection = null;
        String dataType = getDataType();
        if (getStringValue() != null) {
            Collection<String> stringCollection = CollectionUtils.arrayToList(getStringValue().split(","));
            if ("string".equalsIgnoreCase(dataType)) {
                collection = stringCollection;
            } else {
                List<Object> list = new ArrayList<Object>();
                for (String number : stringCollection) {
                    Object item = convertToDataType(number);
                    if (item != null) {
                        list.add(item);
                    }
                }
                collection = list;
            }
        }
        return collection;
    }

    @Override
    @JsonIgnore
    public boolean isCollection() {
        return (getStringValue() != null && getStringValue().contains(","));
    }


    @Override
    public String getDataType() {
        return dataType;
    }

    @Override
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    @Override
    public boolean isApplyToWhereClause() {
        return applyToWhereClause && StringUtils.isNotBlank(name) && !(name.startsWith(INTERNAL_QUERY_FLAG_PREFIX));
    }

    @Override
    public String getQueryName() {
        if (StringUtils.isBlank(queryName)) {
            return name;
        } else {
            return queryName;
        }
    }

    @Override
    public void setQueryName(String queryName) {
        this.queryName = queryName;
    }

    @Override
    public String getTableAlias() {
        return tableAlias;
    }

    @Override
    public void setTableAlias(String tableAlias) {
        this.tableAlias = tableAlias;
    }
}
