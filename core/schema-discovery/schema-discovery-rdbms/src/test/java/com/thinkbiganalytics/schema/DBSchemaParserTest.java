package com.thinkbiganalytics.schema;

/*-
 * #%L
 * thinkbig-schema-discovery-rdbms
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
        List<String> tables = schemaParser.listTables(null, TABLE_SEARCH_PATTERN);
        for (final String table : tables) {
            System.out.println(table);
        }
    }

}
