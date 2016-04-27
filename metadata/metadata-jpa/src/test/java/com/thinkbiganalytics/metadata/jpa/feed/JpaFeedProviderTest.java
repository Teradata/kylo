/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa.feed;

import static org.assertj.core.api.Assertions.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.sql.DataSource;

import org.neo4j.cypher.internal.compiler.v2_2.planner.logical.steps.applyOptional;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Collections2;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;

/**
 *
 * @author Sean Felten
 */
//@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { TestJpaConfiguration.class })
//@Sql("classpath:/create.sql") 
public class JpaFeedProviderTest extends AbstractTransactionalTestNGSpringContextTests {
//public class JpaFeedProviderTest extends AbstractTestNGSpringContextTests {
    
    @Inject
    private EntityManager entityMgr;
    
    @Inject 
    private FeedProvider feedProvider;
    
    @Inject
    private PlatformTransactionManager transactionMgr;

    private JdbcTemplate jdbcTemplate;

    @Inject
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    @Transactional
    @Commit
    public void testCreateFeed() {
        Feed feed = this.feedProvider.ensureFeed("test"+System.currentTimeMillis(), "test descr");
        
        String result = this.jdbcTemplate.query("select id from feed", new ResultSetExtractor<String>() {
            @Override
            public String extractData(ResultSet rs) throws SQLException, DataAccessException {
                StringBuilder result = new StringBuilder();
                while (rs.next()) {
                    result.append(rs.getString(1)).append("\n");
                }
                return result.toString();
            }
        });
        
        System.out.println(result);
        //        assertThat(feed).isNotNull();
        //        assertThat(JdbcTestUtils.countRowsInTable(this.jdbcTemplate, "FEED")).isEqualTo(1);
    }
    
    @Test
    @Transactional
    @DependsOn("testCreateFeed")
    public void testGetFeeds() {
        List<Feed> list = this.feedProvider.getFeeds();
        
        assertThat(list).isNotNull().isNotEmpty();
    }
}
