/**
 * 
 */
package com.thinkbiganalytics.metadata.rest.model.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;

/**
 *
 * @author Sean Felten
 */
public class HiveTablePartition {

    private String name;
    private String formula;
    private List<String> values = new ArrayList<>();

    public HiveTablePartition() {
    }

    public HiveTablePartition(String name, String formula, List<String> values) {
        super();
        this.name = name;
        this.formula = formula;
        this.values = new ArrayList<>(values);
    }

    public HiveTablePartition(String name, String formula, String value, String... more) {
        super();
        this.name = name;
        this.formula = formula;
        this.values = Lists.asList(value, more);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    public String getFormula() {
        return formula;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

}
