package com.thinkbiganalytics.nifi.v2.thrift;

/*-
 * #%L
 * kylo-hadoop-authorization-api
 *
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

import org.apache.commons.pool.ObjectPool;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Created by Binh Van Nguyen (binhnv80@gmail.com)
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(HivePoolableConnectionFactory.class)
public class HivePoolableConnectionFactoryTest {
    @Mock
    ObjectPool pool;

    @Mock
    Connection connection;

    @Mock
    Statement statement;

    private int validationQueryTimeout = 2;
    private String validationQuery = "select 1";
    private HivePoolableConnectionFactory connectionFactory;

    @Before
    public void setUp() {
        initMocks(this);
        connectionFactory = PowerMockito.spy(new HivePoolableConnectionFactory(
                null,
                pool,
                null,
                validationQuery,
                validationQueryTimeout,
                null,
                null,
                false,
                0,
                null,
                null
        ));
        try {
            PowerMockito.doReturn(statement).when(connection).createStatement();
        } catch (SQLException e) {
            Assert.fail("Something is wrong");
        }
    }

    @Test
    public void testValidationQueryWithTimeout() throws Exception {
        PowerMockito.doNothing().when(connectionFactory, "validateConnectionWithTimeout", connection);

        connectionFactory.validateConnection(connection);

        PowerMockito.verifyPrivate(connectionFactory).invoke("validateConnectionWithTimeout", connection);
    }

    @Test
    public void testFastValidationQuery() {
        try {
            PowerMockito.doReturn(true).when(statement).execute(validationQuery);

            connectionFactory.validateConnection(connection);
        } catch (SQLException e) {
            // should not arrive here so just fail it
            Assert.fail("Something is wrong");
        }
    }

    @Test(expected = SQLException.class)
    public void testSlowValidationQuery() throws SQLException {
        // just to be sure we have query timeout set
        Assert.assertThat(validationQueryTimeout, is(greaterThan(0)));

        try {
            Mockito.when(statement.execute(validationQuery)).thenAnswer((InvocationOnMock invocation) -> {
                TimeUnit.SECONDS.sleep(validationQueryTimeout + 1);
                return true;
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
        connectionFactory.validateConnection(connection);
    }
}
