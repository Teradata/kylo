package com.thinkbiganalytics.jobrepo.query.support;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Iterables;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by sr186054 on 8/12/15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class QueryColumnFilterSqlString extends QueryColumnFilter implements ColumnFilter {

    private String sqlString;


    List<ColumnFilterItem> filters = new ArrayList<ColumnFilterItem>();

    public QueryColumnFilterSqlString() {

    }

    public QueryColumnFilterSqlString(@JsonProperty("name") String name, @JsonProperty("filters") List<ColumnFilterItem> filters) {
        this.name = name;
        this.filters = filters;
    }


    public QueryColumnFilterSqlString(String sqlStr) {
        this.value = sqlStr;
        this.name = "";
    }

    public QueryColumnFilterSqlString(@JsonProperty("name") String name, @JsonProperty("value") Object value) {
        super(name, value, "=");
    }

    public QueryColumnFilterSqlString(@JsonProperty("name") String name, @JsonProperty("value") Object value, @JsonProperty("operator") String operator) {
        super(name, value, operator);
    }

    public QueryColumnFilterSqlString(QueryColumnFilter filter) {
        this.name = filter.getName();
        this.value = filter.getValue();
    }

    @Override
    public List<ColumnFilterItem> getFilters() {
        return filters;
    }

    @Override
    public ColumnFilterItem getFirst() {
        if (getFilters() != null && !getFilters().isEmpty()) {
            return getFilters().get(0);
        }
        return this;
    }

    @Override
    @JsonIgnore
    public String getOperatorValueByName(String name) {
        ColumnFilterItem nameValue = getByName(name);
        if (nameValue != null) {
            return nameValue.getOperator();
        } else {
            return "=";
        }
    }

    @Override
    @JsonIgnore
    public ColumnFilterItem getByName(String name) {
        ColumnFilterItem nameValue = Iterables.tryFind(this.filters, new ColumnFilterFindByName(name)).orNull();
        return nameValue;
    }

    @Override
    @JsonIgnore
    public Object getValueForName(String name) {
        if (this.name.equalsIgnoreCase(name)) {
            return this.getValue();
        } else {
            ColumnFilterItem nameValue = getByName(name);
            Object val = null;
            if (nameValue != null) {
                val = nameValue.getValue();
            }
            return val;
        }
    }

    @Override
    @JsonIgnore
    public String getValueAsStringForName(String name) {
        ColumnFilterItem nameValue = getByName(name);
        String val = null;
        if (nameValue != null) {
            val = nameValue.getStringValue();
        }
        return val;
    }

    @Override
    @JsonIgnore
    public Collection<String> getValueAsCollection(String name) {
        String list = getValueAsStringForName(name);
        if (list != null) {
            return CollectionUtils.arrayToList(list.split(","));
        }
        return new ArrayList<String>();
    }

    @Override
    @JsonIgnore
    public boolean isSqlString() {
        return StringUtils.isBlank(name) || StringUtils.isNotBlank(sqlString);
    }

    @Override
    @JsonIgnore
    public String getNameOrFirstFilterName() {
        return getFirst().getName();
    }


    @Override
    public String getSqlString() {
        if (StringUtils.isBlank(sqlString)) {
            return this.getStringValue();
        }
        return sqlString;
    }

    @Override
    public void setSqlString(String sqlString) {
        this.sqlString = sqlString;
    }
}
