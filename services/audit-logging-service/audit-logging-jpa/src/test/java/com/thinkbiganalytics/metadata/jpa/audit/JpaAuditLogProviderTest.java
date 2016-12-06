/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa.audit;

import static org.assertj.core.api.Assertions.assertThat;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.audit.AuditLogEntry;
import com.thinkbiganalytics.metadata.persistence.MetadataPersistenceConfig;
import com.thinkbiganalytics.security.UsernamePrincipal;
import com.thinkbiganalytics.testing.jpa.TestPersistenceConfiguration;

/**
 *
 * @author Sean Felten
 */
@TestPropertySource(locations = "classpath:test-jpa-application.properties")
@SpringApplicationConfiguration(classes = { MetadataPersistenceConfig.class, TestPersistenceConfiguration.class, AuditLogProviderConfig.class })
public class JpaAuditLogProviderTest extends AbstractTestNGSpringContextTests {
    
    private static final Principal ADMIN = new UsernamePrincipal("admin");
    private static final Principal USER = new UsernamePrincipal("user");

    @Inject
    private JpaAuditLogProvider provider;
    
    @Inject
    private MetadataAccess metadataAccess;
    
    @Test
    public void testAddAdminLogs() {
        List<AuditLogEntry.ID> ids = this.metadataAccess.commit(() -> { 
            AuditLogEntry log1 = provider.createEntry(ADMIN, "simple", "Admin: Simple, non-entity entry");
            AuditLogEntry log2 = provider.createEntry(ADMIN, "entity", "Admin: Entity entry", UUID.randomUUID().toString());
            return Arrays.asList(log1.getId(), log2.getId());
        }, MetadataAccess.SERVICE);
        
        assertThat(ids).hasSize(2);
    }
    
    @Test
    public void testAddUserLogs() {
        List<AuditLogEntry.ID> ids = this.metadataAccess.commit(() -> { 
            AuditLogEntry log1 = provider.createEntry(USER, "simple", "User: Simple, non-entity entry");
            AuditLogEntry log2 = provider.createEntry(USER, "simple", "User: Simple, non-entity entry");
            AuditLogEntry log3 = provider.createEntry(USER, "entity", "User: Entity entry", UUID.randomUUID().toString());
            return Arrays.asList(log1.getId(), log2.getId(), log3.getId());
        }, MetadataAccess.SERVICE);
        
        assertThat(ids).hasSize(3);
    }
    
    @Test(dependsOnMethods={ "testAddAdminLogs", "testAddUserLogs" })
    public void testListAll() {
        List<AuditLogEntry.ID> ids = this.metadataAccess.read(() -> { 
            return provider.list().stream()
                            .map(AuditLogEntry::getId)
                            .collect(Collectors.toList());
        }, MetadataAccess.SERVICE);
        
        assertThat(ids).hasSize(5);
    }
    
    @Test(dependsOnMethods={ "testAddAdminLogs", "testAddUserLogs" })
    public void testList3() {
        List<AuditLogEntry.ID> ids = this.metadataAccess.read(() -> { 
            return provider.list(3).stream()
                            .map(AuditLogEntry::getId)
                            .collect(Collectors.toList());
        }, MetadataAccess.SERVICE);
        
        assertThat(ids).hasSize(3);
    }
    
    @Test(dependsOnMethods={ "testAddAdminLogs", "testAddUserLogs" })
    public void testFindByAdmin() {
        List<AuditLogEntry.ID> ids = this.metadataAccess.read(() -> { 
            return provider.findByUser(ADMIN).stream()
                            .map(AuditLogEntry::getId)
                            .collect(Collectors.toList());
        }, MetadataAccess.SERVICE);
        
        assertThat(ids).hasSize(2);
    }
    
    @Test(dependsOnMethods={ "testAddAdminLogs", "testAddUserLogs" })
    public void testFindByUser() {
        List<AuditLogEntry.ID> ids = this.metadataAccess.read(() -> { 
            return provider.findByUser(USER).stream()
                            .map(AuditLogEntry::getId)
                            .collect(Collectors.toList());
        }, MetadataAccess.SERVICE);
        
        assertThat(ids).hasSize(3);
    }
    
    @Test(dependsOnMethods={ "testAddAdminLogs", "testAddUserLogs" })
    public void testFindById() {
        final AuditLogEntry.ID id = this.metadataAccess.read(() -> { 
            return provider.list(1).stream()
                            .findFirst()
                            .map(AuditLogEntry::getId)
                            .orElseThrow(() -> new AssertionError());
        }, MetadataAccess.SERVICE);
        
        AuditLogEntry.ID found = this.metadataAccess.read(() -> { 
            return provider.findById(id)
                            .map(AuditLogEntry::getId)
                            .orElseThrow(() -> new AssertionError("Entry not found with id: " + id));
        }, MetadataAccess.SERVICE);
        
        assertThat(found).isEqualTo(id);
    }
}
