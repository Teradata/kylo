package com.thinkbiganalytics.schema;

import com.thinkbiganalytics.kerberos.KerberosTicketConfiguration;
import com.thinkbiganalytics.spring.CommonsSpringConfiguration;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import javax.inject.Inject;
import javax.sql.DataSource;

/**
 * Created by sr186054 on 1/9/17.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource(locations = "classpath:test-application.properties")
@SpringApplicationConfiguration(classes = {CommonsSpringConfiguration.class, TestDbSchemaConfig.class})
@Ignore
public class DBSchemaParserTest {

    @Inject
    private DataSource dataSource;

    @Test
    public void test() {
        final String TABLE_SEARCH_PATTERN = "DBCInfo%";

        DBSchemaParser schemaParser = new DBSchemaParser(dataSource, new KerberosTicketConfiguration());
        List<String> tables = null;
        tables = schemaParser.listTables(null, TABLE_SEARCH_PATTERN);
        if (tables != null) {
            tables.forEach(table -> System.out.println(table));
        }

    }

}
