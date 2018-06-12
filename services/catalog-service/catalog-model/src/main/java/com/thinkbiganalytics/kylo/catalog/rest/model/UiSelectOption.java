package com.thinkbiganalytics.kylo.catalog.rest.model;

public class UiSelectOption {
    private String label;
    private String value;

    public UiSelectOption() {}

    public UiSelectOption(UiSelectOption other) {
        label = other.label;
        value = other.value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
