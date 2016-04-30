/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa;

import java.nio.file.Path;
import java.nio.file.Paths;

import javax.persistence.AttributeConverter;

/**
 * Converts Path objects to String columns.
 * @author Sean Felten
 */
public class PathAttributeConverter implements AttributeConverter<Path, String> {

    @Override
    public String convertToDatabaseColumn(Path path) {
        return path.toAbsolutePath().toString();
    }

    @Override
    public Path convertToEntityAttribute(String dbData) {
        return Paths.get(dbData);
    }


}
