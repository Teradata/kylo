/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa.feed;

import static org.assertj.core.api.Assertions.*;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.sql.DataSource;

import org.neo4j.cypher.internal.compiler.v2_2.planner.logical.steps.applyOptional;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.Test;

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
    
    @Inject
    private EntityManager entityMgr;
    
    @Inject 
    private FeedProvider feedProvider;
    
    private JdbcTemplate jdbcTemplate;
    
    @Inject
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    @Transactional
    @Commit
    public void testCreateFeed() {
        Feed feed = this.feedProvider.ensureFeed("test1", "test descr");
        
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
}
