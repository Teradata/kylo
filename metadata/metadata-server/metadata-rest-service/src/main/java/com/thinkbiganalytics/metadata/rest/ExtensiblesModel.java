/**
 * 
 */
package com.thinkbiganalytics.metadata.rest;

import com.google.common.base.Function;
import com.thinkbiganalytics.metadata.api.extension.ExtensibleType;
import com.thinkbiganalytics.metadata.api.extension.ExtensibleTypeBuilder;
import com.thinkbiganalytics.metadata.api.extension.ExtensibleTypeProvider;
import com.thinkbiganalytics.metadata.rest.model.extension.ExtensibleTypeDescriptor;
import com.thinkbiganalytics.metadata.rest.model.extension.FieldDescriptor;

/**
 *
 * @author Sean Felten
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

    public static ExtensibleType createType(ExtensibleTypeDescriptor typeDescr, ExtensibleTypeProvider typeProvider) {
        ExtensibleTypeBuilder bldr = typeProvider.buildType(typeDescr.getName());
        build(typeDescr, bldr);
        return bldr.build();
    }
    
    public static ExtensibleType updateType(ExtensibleTypeDescriptor typeDescr, ExtensibleType.ID id, ExtensibleTypeProvider typeProvider) {
        ExtensibleTypeBuilder bldr = typeProvider.updateType(id);
        build(typeDescr, bldr);
        return bldr.build();
    }
    
    public static void build(ExtensibleTypeDescriptor typeDescr, ExtensibleTypeBuilder bldr) {
        bldr.description(typeDescr.getDescription());
        
        for (FieldDescriptor field : typeDescr.getFields()) {
//            FieldDescriptorBuilder fieldBldr = bldr.field(field.getName())
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
