package com.thinkbiganalytics.metadata.api.extension;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * Describes an object which may be extended with additional fields or subtypes at runtime.
 */
public interface ExtensibleType {

    interface ID extends Serializable {}

    ID getId();

    ExtensibleType getSupertype();

    String getName();

    String getDiplayName();

    String getDesciption();

    DateTime getCreatedTime();

    DateTime getModifiedTime();

    Set<FieldDescriptor> getFieldDescriptors();

    FieldDescriptor getFieldDescriptor(String name);
}
