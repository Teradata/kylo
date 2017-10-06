package com.thinkbiganalytics.jdbc.util;

/*-
 * #%L
 * thinkbig-commons-util
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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Database Utils tests
 */

@RunWith(MockitoJUnitRunner.class)
public class DatabaseUtilsTest {

    @Mock
    private Connection connection;

    @Mock
    private Connection connection2;

    @Mock
    private DatabaseMetaData databaseMetaData;

    @Mock
    private DatabaseMetaData databaseMetaData2;


    @Before
    public void setUp() throws SQLException {
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(databaseMetaData.getIdentifierQuoteString()).thenReturn("|");

        when(connection2.getMetaData()).thenReturn(databaseMetaData2);
        when(databaseMetaData2.getIdentifierQuoteString()).thenReturn(null);
    }

    @Test
    public void getDatabaseIdentifierQuoteStringTest_ReturnsActualIdentifier() throws SQLException {
        //An actual identifier returned by the database
        String quoteIdentifier = DatabaseUtils.getDatabaseIdentifierQuoteString(connection);
        assertEquals("Quote identifier (actual) not retrieved correctly", "|", quoteIdentifier);
    }

    @Test
    public void getDatabaseIdentifierQuoteStringTest_ReturnsNullIdentifier() throws SQLException {
        //A null identifier returned by the database
        String quoteIdentifier = DatabaseUtils.getDatabaseIdentifierQuoteString(connection2);
        assertEquals("Quote identifier (null) not retrieved correctly", "", quoteIdentifier);
    }

    @Test
    public void getDatabaseIdentifierQuoteStringTest_NullConnection() throws SQLException {
        //A null connection is provided to retrieve identifier
        String quoteIdentifier = DatabaseUtils.getDatabaseIdentifierQuoteString(null);
        assertEquals("Quote identifier for null connection not retrieved correctly", "", quoteIdentifier);
    }

}
