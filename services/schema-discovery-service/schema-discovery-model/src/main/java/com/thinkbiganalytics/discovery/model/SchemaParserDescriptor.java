/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.discovery.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.thinkbiganalytics.policy.rest.model.BaseUiPolicyRule;

@JsonIgnoreProperties(ignoreUnknown = true)
/**
 * Model used to pass the parser properties
 */
public class SchemaParserDescriptor extends BaseUiPolicyRule {


}
