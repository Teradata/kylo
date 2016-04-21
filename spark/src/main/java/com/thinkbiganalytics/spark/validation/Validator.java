/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.spark.validation;

import java.io.Serializable;

/**
 * Created by matthutton on 1/20/16.
 */
public interface Validator<T> extends Serializable{

    boolean validate(T value);
}
