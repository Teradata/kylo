/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.security.action;

import static org.assertj.core.api.Assertions.assertThat;

import java.security.AccessControlException;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.JcrTestConfig;
import com.thinkbiganalytics.metadata.modeshape.ModeShapeEngineConfig;
import com.thinkbiganalytics.metadata.modeshape.TestCredentials;
import com.thinkbiganalytics.metadata.modeshape.TestUserPrincipal;
import com.thinkbiganalytics.metadata.modeshape.security.AdminCredentials;
import com.thinkbiganalytics.security.action.AllowedActions;
import com.thinkbiganalytics.security.action.AllowedModuleActionsProvider;

/**
 *
 * @author Sean Felten
 */
@SpringApplicationConfiguration(classes = { ModeShapeEngineConfig.class, JcrTestConfig.class, TestSecurityConfig.class })
public class JcrAllowedActionsTest extends AbstractTestNGSpringContextTests {
    
    @Inject
    private JcrMetadataAccess metadata;
    
    @Inject
    private AllowedModuleActionsProvider provider;

//    @BeforeClass
//    public void print() {
//        this.metadata.read(new AdminCredentials(), () -> {
//            StringWriter sw = new StringWriter();
//            PrintWriter pw = new PrintWriter(sw);
//    
//            JcrTool tool = new JcrTool(true, pw);
//            tool.printSubgraph(JcrMetadataAccess.getActiveSession(), "/metadata/security/prototypes");
//            pw.flush();
//            String result = sw.toString();
//            System.out.println(result);
//    
//            return null;
//        });
//    }
    
    @Test
    public void testAdminGetAvailable() throws Exception {
        this.metadata.read(new AdminCredentials(), () -> {
            Optional<AllowedActions> option = this.provider.getAvailavleActions("services");
            
            assertThat(option.isPresent()).isTrue();
            
            AllowedActions actions = option.get(); // Throws exception on failure
            
            actions.checkPermission(TestSecurityConfig.EXPORT_FEEDS);
            
            return null;
        });
    }
    
    @Test
    public void testTestGetAvailable() throws Exception {
        this.metadata.read(new TestCredentials(), () -> {
            Optional<AllowedActions> option = this.provider.getAvailavleActions("services");
            
            assertThat(option.isPresent()).isTrue();
            
            option.get().checkPermission(TestSecurityConfig.EXPORT_FEEDS); // Throws exception on failure
            
            return null;
        });
    }
    
    @Test(dependsOnMethods="testAdminGetAvailable")
    public void testAdminGetAllowed() throws Exception {
        this.metadata.read(new AdminCredentials(), () -> {
            Optional<AllowedActions> option = this.provider.getAllowedActions("services");
            
            assertThat(option.isPresent()).isTrue();
            
            AllowedActions actions = option.get(); // Throws exception on failure
            
            actions.checkPermission(TestSecurityConfig.EXPORT_FEEDS);
            
            return null;
        });
    }
    
    @Test(dependsOnMethods="testTestGetAvailable", expectedExceptions=AccessControlException.class)
    public void testTestGetAllowed() throws Exception {
        this.metadata.read(new TestCredentials(), () -> {
            Optional<AllowedActions> option = this.provider.getAllowedActions("services");
            
            assertThat(option.isPresent()).isTrue();
            
            option.get().checkPermission(TestSecurityConfig.EXPORT_FEEDS);
            
            return null;
        });
    }
    
    @Test(dependsOnMethods={ "testAdminGetAllowed", "testTestGetAllowed" })
    public void testEnableExport() {
        boolean changed = this.metadata.commit(new AdminCredentials(), () -> {
            Optional<AllowedActions> option = this.provider.getAllowedActions("services");
            
            assertThat(option.isPresent()).isTrue();
            
            return option.get().enable(new TestUserPrincipal(), TestSecurityConfig.EXPORT_FEEDS);
        });
        
        assertThat(changed).isTrue();
        
        boolean passed = this.metadata.read(new TestCredentials(), () -> {
            Optional<AllowedActions> option = this.provider.getAllowedActions("services");
            
            assertThat(option.isPresent()).isTrue();
            
            option.get().checkPermission(TestSecurityConfig.EXPORT_FEEDS); 
            return true;
        });
        
        assertThat(passed).isTrue();
    }
    
    @Test(dependsOnMethods="testEnableExport", expectedExceptions=AccessControlException.class)
    public void testDisableExport() {
        boolean changed = this.metadata.commit(new AdminCredentials(), () -> {
            Optional<AllowedActions> option = this.provider.getAllowedActions("services");
            
            assertThat(option.isPresent()).isTrue();
            
            return option.get().disable(new TestUserPrincipal(), TestSecurityConfig.EXPORT_FEEDS);
        });
        
        assertThat(changed).isTrue();
        
        this.metadata.read(new TestCredentials(), () -> {
            Optional<AllowedActions> option = this.provider.getAllowedActions("services");
            
            assertThat(option.isPresent()).isTrue();
            
            option.get().checkPermission(TestSecurityConfig.EXPORT_FEEDS); 
            return null;
        });
    }
    
    @Test(dependsOnMethods="testDisableExport", expectedExceptions=AccessControlException.class)
    public void testEnableOnlyCreate() {
        boolean changed = this.metadata.commit(new AdminCredentials(), () -> {
            Optional<AllowedActions> option = this.provider.getAllowedActions("services");
            
            assertThat(option.isPresent()).isTrue();
            
            option.get().enable(new TestUserPrincipal(), TestSecurityConfig.EXPORT_FEEDS);
            return option.get().enableOnly(new TestUserPrincipal(), TestSecurityConfig.CREATE_FEEDS);
        });
        
        assertThat(changed).isTrue();
        
        this.metadata.read(new TestCredentials(), () -> {
            Optional<AllowedActions> option = this.provider.getAllowedActions("services");
            
            assertThat(option.isPresent()).isTrue();
            
            try {
                option.get().checkPermission(TestSecurityConfig.CREATE_FEEDS);
            } catch (Exception e) {
                Assert.fail("Permission check should pass", e);
            } 
            
            option.get().checkPermission(TestSecurityConfig.EXPORT_FEEDS); 
            return null;
        });
    }
}
