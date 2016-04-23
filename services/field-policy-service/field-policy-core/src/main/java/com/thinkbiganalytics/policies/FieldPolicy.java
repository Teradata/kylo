/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.policies;

import com.thinkbiganalytics.policies.standardization.StandardizationPolicy;
import com.thinkbiganalytics.policies.validation.Validator;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class FieldPolicy implements Serializable {

  private String table;
  private String field;

  /**
   * Will preserve the value and show the row as valid even if it fails to conform to the target schema type.
   */
  private boolean skipSchemaValidation;
  private boolean nullable = true;
  private List<Validator> validators;
  private List<StandardizationPolicy> policies;
  /**
   * Whether attempts to automatically discover the type of field data such as email, credit card, etc.
   */
  private boolean typeDiscovery;

  /**
   * Whether to scan large strings for occurrences of data that should be protected
   */
  private int piiScan;

  protected FieldPolicy() {
  }

  public List<Validator> getValidators() {
    return validators;
  }

  protected void setValidators(List<Validator> validators) {
    this.validators = Collections.unmodifiableList(validators);
  }

  public List<StandardizationPolicy> getStandardizationPolicies() {
    return policies;
  }

  protected void setStandardizationPolicies(List<StandardizationPolicy> policies) {
    this.policies = Collections.unmodifiableList(policies);
  }

  public String getTable() {
    return table;
  }

  protected void setTable(String table) {
    this.table = table;
  }

  public String getField() {
    return field;
  }

  protected void setField(String field) {
    this.field = field;
  }

  /**
   * Will preserve the value and show the row as valid even if it fails to conform to the target schema type.
   */
  public Boolean shouldSkipSchemaValidation() {
    return skipSchemaValidation;
  }

  protected void setSkipSchemaValidation(boolean skipSchemaValidation) {
    this.skipSchemaValidation = skipSchemaValidation;
  }

  public boolean isNullable() {
    return nullable;
  }

  protected void setNullable(boolean nullable) {
    this.nullable = nullable;
  }

  public boolean shouldTypeDiscovery() {
    return typeDiscovery;
  }

  protected void setTypeDiscovery(boolean typeDiscovery) {
    this.typeDiscovery = typeDiscovery;
  }

  public String toString() {
    return "FieldPolicy validators [" + (validators == null ? "NULL" : validators.size()) + "] policies [" + (policies == null
                                                                                                              ? "NULL"
                                                                                                              : policies.size())
           + "]";
  }

  public static void main(String[] args) {

  }
}
