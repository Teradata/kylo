/**
 * 
 */
package com.thinkbiganalytics.metadata.rest.model.data;

import java.util.List;

/**
 *
 * @author Sean Felten
 */
public class HiveTablePartition {

    private String name;
    private List<String> values;
    private String formula;

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
