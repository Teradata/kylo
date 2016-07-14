/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa.feed;

import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;

import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Sean Felten
 */
@SpringApplicationConfiguration(classes = { TestJpaConfiguration.class })
//@Sql("classpath:/create.sql") 
public class JpaFeedProviderTest extends AbstractTransactionalTestNGSpringContextTests {
    
    @Inject
    private EntityManager entityMgr;
    
    @Inject 
    private FeedProvider feedProvider;
    
    private String nameTag = "" + System.currentTimeMillis();

//    @Test
    @Transactional
    @Commit
    public void testEnsureFeed() {
        Feed feed = this.feedProvider.ensureFeed("category","test"+this.nameTag, "test descr");
        
        assertThat(this.entityMgr.createQuery("select count(f) from JpaFeed f").getSingleResult()).isEqualTo(1L);
        assertThat(feed).isNotNull();
        assertThat(feed.getId()).isNotNull();
        
        Feed same = this.feedProvider.ensureFeed("category","test"+this.nameTag, "test descr");
        
        assertThat(feed).isNotNull();
        assertThat(same.getId()).isNotNull().isEqualTo(feed.getId());
    }
    
//    @Test(dependsOnMethods="testEnsureFeed")
    @Transactional
    public void testGetFeeds() {
        List<? extends Feed> list = this.feedProvider.getFeeds();
        
        assertThat(list).isNotNull().isNotEmpty();
    }
}
