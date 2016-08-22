/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.security.action;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.AccessControlException;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.JcrTestConfig;
import com.thinkbiganalytics.metadata.modeshape.ModeShapeEngineConfig;
import com.thinkbiganalytics.metadata.modeshape.TestCredentials;
import com.thinkbiganalytics.metadata.modeshape.TestUserPrincipal;
import com.thinkbiganalytics.metadata.modeshape.security.AdminCredentials;
import com.thinkbiganalytics.metadata.modeshape.support.JcrTool;
import com.thinkbiganalytics.security.action.Action;
import com.thinkbiganalytics.security.action.AllowedActions;
import com.thinkbiganalytics.security.action.AllowedModuleActionsProvider;

/**
 *
 * @author Sean Felten
 */
@SpringApplicationConfiguration(classes = { ModeShapeEngineConfig.class, JcrTestConfig.class, TestActionsConfig.class })
public class JcrAllowedActionsTest extends AbstractTestNGSpringContextTests {
    
    public static final Action FEED_SUPPORT = Action.create("accessFeedSupport");
    public static final Action ACCESS_FEEDS = FEED_SUPPORT.subAction("accessFeeds");
    public static final Action CREATE_FEEDS = ACCESS_FEEDS.subAction("createFeeds");
    public static final Action EXPORT_FEEDS = ACCESS_FEEDS.subAction("exportFeeds");

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
            
            actions.checkPermission(EXPORT_FEEDS);
            
            return null;
        });
    }
    
    @Test
    public void testTestGetAvailable() throws Exception {
        this.metadata.read(new TestCredentials(), () -> {
            Optional<AllowedActions> option = this.provider.getAvailavleActions("services");
            
            assertThat(option.isPresent()).isTrue();
            
            option.get().checkPermission(EXPORT_FEEDS); // Throws exception on failure
            
            return null;
        });
    }
    
    @Test(dependsOnMethods="testAdminGetAvailable")
    public void testAdminGetAllowed() throws Exception {
        this.metadata.read(new AdminCredentials(), () -> {
            Optional<AllowedActions> option = this.provider.getAllowedActions("services");
            
            assertThat(option.isPresent()).isTrue();
            
            AllowedActions actions = option.get(); // Throws exception on failure
            
            actions.checkPermission(EXPORT_FEEDS);
            
            return null;
        });
    }
    
    @Test(dependsOnMethods="testTestGetAvailable", expectedExceptions=AccessControlException.class)
    public void testTestGetAllowed() throws Exception {
        this.metadata.read(new TestCredentials(), () -> {
            Optional<AllowedActions> option = this.provider.getAllowedActions("services");
            
            assertThat(option.isPresent()).isTrue();
            
            option.get().checkPermission(EXPORT_FEEDS);
            
            return null;
        });
    }
    
    @Test(dependsOnMethods={ "testAdminGetAllowed", "testTestGetAllowed" })
    public void testEnableExport() {
        boolean changed = this.metadata.commit(new AdminCredentials(), () -> {
            Optional<AllowedActions> option = this.provider.getAllowedActions("services");
            
            assertThat(option.isPresent()).isTrue();
            
            return option.get().enable(EXPORT_FEEDS, new TestUserPrincipal());
        });
        
        assertThat(changed).isTrue();
        
        boolean passed = this.metadata.read(new TestCredentials(), () -> {
            Optional<AllowedActions> option = this.provider.getAllowedActions("services");
            
            assertThat(option.isPresent()).isTrue();
            
            option.get().checkPermission(EXPORT_FEEDS); 
            return true;
        });
        
        assertThat(passed).isTrue();
    }
    
    @Test(dependsOnMethods="testEnableExport", expectedExceptions=AccessControlException.class)
    public void testDisableExport() {
        boolean changed = this.metadata.commit(new AdminCredentials(), () -> {
            Optional<AllowedActions> option = this.provider.getAllowedActions("services");
            
            assertThat(option.isPresent()).isTrue();
            
            return option.get().disable(EXPORT_FEEDS, new TestUserPrincipal());
        });
        
        assertThat(changed).isTrue();
        
        this.metadata.read(new TestCredentials(), () -> {
            Optional<AllowedActions> option = this.provider.getAllowedActions("services");
            
            assertThat(option.isPresent()).isTrue();
            
            option.get().checkPermission(EXPORT_FEEDS); 
            return null;
        });
    }
}
