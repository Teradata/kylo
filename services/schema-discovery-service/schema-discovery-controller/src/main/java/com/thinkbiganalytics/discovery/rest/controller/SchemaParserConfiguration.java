/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.discovery.rest.controller;

import com.thinkbiganalytics.policy.FieldPolicyItem;

import java.io.Serializable;

/**
 * Created by matthutton on 9/7/16.
 */
public interface SchemaParserConfiguration extends Serializable, FieldPolicyItem {

        String convertValue(String value);

}
