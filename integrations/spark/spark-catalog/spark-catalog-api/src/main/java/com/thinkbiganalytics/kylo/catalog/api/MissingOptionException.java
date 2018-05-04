package com.thinkbiganalytics.kylo.catalog.api;

import javax.annotation.Nullable;

/**
 * Thrown to indicate that a required data set option was not defined.
 */
public class MissingOptionException extends KyloCatalogException {

    private static final long serialVersionUID = -453339716817390889L;

    /**
     * Construct a {@code MissingOptionException} with the specified message.
     */
    public MissingOptionException(@Nullable final String message) {
        super(message);
    }
}
