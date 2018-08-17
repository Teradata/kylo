package com.thinkbiganalytics.schema;

import com.thinkbiganalytics.discovery.schema.JdbcSchemaParser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Provider factory that returns instances of {@link JdbcSchemaParser}.
 */
@Component
public class JdbcSchemaParserProvider {

    /**
     * List of available JDBC schema parsers
     */
    @Nonnull
    private final List<JdbcSchemaParser> parsers;

    /**
     * Constructs a {@code JdbcSchemaParserProvider}.
     */
    @Autowired
    public JdbcSchemaParserProvider(@Nonnull final List<JdbcSchemaParser> parsers) {
        this.parsers = parsers;
    }

    /**
     * Gets the schema parser for the specified JDBC URL.
     *
     * @param url JDBC URL
     * @return the schema parser
     * @throws SQLException if a database access error occurs or no schema parsers accept the URL
     */
    @Nonnull
    public JdbcSchemaParser getSchemaParser(@Nonnull final String url) throws SQLException {
        for (final JdbcSchemaParser parser : parsers) {
            if (parser.acceptsURL(url)) {
                return parser;
            }
        }
        throw new SQLException("No schema parsers accept URL: " + url);
    }
}
