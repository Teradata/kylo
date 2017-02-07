/**
 *
 */
package com.thinkbiganalytics.metadata.rest;

/*-
 * #%L
 * thinkbig-metadata-rest-controller
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.google.common.base.Function;
import com.thinkbiganalytics.metadata.api.extension.ExtensibleType;
import com.thinkbiganalytics.metadata.api.extension.ExtensibleTypeBuilder;
import com.thinkbiganalytics.metadata.api.extension.ExtensibleTypeProvider;
import com.thinkbiganalytics.metadata.rest.model.extension.ExtensibleTypeDescriptor;
import com.thinkbiganalytics.metadata.rest.model.extension.FieldDescriptor;

/**
 * A helper class for converting extensible type rest models to domain models
 */
public class ExtensiblesModel {

    public static final Function<ExtensibleType, ExtensibleTypeDescriptor> DOMAIN_TO_TYPE
        = new Function<ExtensibleType, ExtensibleTypeDescriptor>() {
        @Override
        public ExtensibleTypeDescriptor apply(ExtensibleType domain) {
            ExtensibleTypeDescriptor typeDescr = new ExtensibleTypeDescriptor();

            typeDescr.setId(domain.getId().toString());
            typeDescr.setName(domain.getName());
            typeDescr.setSupertype(domain.getSupertype() != null ? domain.getSupertype().getName() : null);
            typeDescr.setCreatedTime(domain.getCreatedTime());
            typeDescr.setModifiedTime(domain.getModifiedTime());
            typeDescr.setDisplayName(domain.getDiplayName());
            typeDescr.setDescription(domain.getDesciption());

            for (com.thinkbiganalytics.metadata.api.extension.FieldDescriptor field : domain.getFieldDescriptors()) {
                FieldDescriptor fieldDescr = new FieldDescriptor();

                fieldDescr.setName(field.getName());
                fieldDescr.setDisplayName(field.getDisplayName());
                fieldDescr.setDescription(field.getDescription());
                fieldDescr.setCollection(field.isCollection());
                fieldDescr.setRequired(field.isRequired());

                typeDescr.addField(fieldDescr);
            }

            return typeDescr;
        }
    };

    /**
     * Create the API model ExtensibleType from the REST model ExtensibleTypeDescriptor
     *
     * @param typeDescr    the type descriptor given in the rest call
     * @param typeProvider the type provider
     * @return the extensible type
     */
    public static ExtensibleType createType(ExtensibleTypeDescriptor typeDescr, ExtensibleTypeProvider typeProvider) {
        ExtensibleTypeBuilder bldr = typeProvider.buildType(typeDescr.getName());
        build(typeDescr, bldr);
        return bldr.build();
    }

    /**
     * Create an extensible type using the REST model and an ID and provider
     *
     * @param typeDescr    the type descriptor given in the rest call
     * @param id           the id to use
     * @param typeProvider the type provider
     * @return the extensible type
     */
    public static ExtensibleType updateType(ExtensibleTypeDescriptor typeDescr, ExtensibleType.ID id, ExtensibleTypeProvider typeProvider) {
        ExtensibleTypeBuilder bldr = typeProvider.updateType(id);
        build(typeDescr, bldr);
        return bldr.build();
    }

    private static void build(ExtensibleTypeDescriptor typeDescr, ExtensibleTypeBuilder bldr) {
        bldr.description(typeDescr.getDescription());

        for (FieldDescriptor field : typeDescr.getFields()) {
            bldr.field(field.getName())
                .displayName(field.getDisplayName())
                .description(field.getDescription())
                .type(com.thinkbiganalytics.metadata.api.extension.FieldDescriptor.Type.valueOf(field.getType().name()))
                .collection(field.isCollection())
                .required(field.isRequired())
                .add();
        }
    }

}
