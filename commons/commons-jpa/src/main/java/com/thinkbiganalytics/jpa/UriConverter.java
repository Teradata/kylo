/**
 * 
 */
package com.thinkbiganalytics.jpa;

import java.net.URI;

import javax.persistence.AttributeConverter;

/**
 * Converts URIs to/from strings.
 * 
 * @author Sean Felten
 */
public class UriConverter implements AttributeConverter<URI, String> {

    /* (non-Javadoc)
     * @see javax.persistence.AttributeConverter#convertToDatabaseColumn(java.lang.Object)
     */
    @Override
    public String convertToDatabaseColumn(URI attribute) {
        if (attribute != null) {
            return attribute.toASCIIString();
        } else {
            return null;
        }
    }

    /* (non-Javadoc)
     * @see javax.persistence.AttributeConverter#convertToEntityAttribute(java.lang.Object)
     */
    @Override
    public URI convertToEntityAttribute(String dbData) {
        if (dbData != null) {
            return URI.create(dbData);
        } else {
            return null;
        }
    }

}
