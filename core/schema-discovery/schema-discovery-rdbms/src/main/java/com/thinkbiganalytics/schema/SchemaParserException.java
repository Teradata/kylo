package com.thinkbiganalytics.schema;

import org.springframework.dao.DataAccessException;

/**
 * Created by sr186054 on 8/11/17.
 */
public class SchemaParserException extends DataAccessException {

    public SchemaParserException(String msg) {
        super(msg);
    }

    public SchemaParserException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
