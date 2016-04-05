/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa.sla;

import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.tool.hbm2ddl.SchemaExport;

/**
 *
 * @author Sean Felten
 */
public class GenerateDDL extends JpaObligationGroup {

    /**
     * @param args
     */
    public static void main(String[] args) {
//        StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
//                .
//                .applySetting("hibernate.dialect", "org.hibernate.dialect.H2Dialect")
//                .build();
//        MetadataSources metadata = new MetadataSources(registry);
//
//        // [...] adding annotated classes to metadata here...
//        metadata.addAnnotatedClass(...);
//
//        SchemaExport export = new SchemaExport( (MetadataImplementor) metadata.buildMetadata(), connection);
//        
//        export.create(true, true);    
    }

}
