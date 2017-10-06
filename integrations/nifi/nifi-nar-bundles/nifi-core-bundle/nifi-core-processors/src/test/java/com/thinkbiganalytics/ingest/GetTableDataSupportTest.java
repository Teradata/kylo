package com.thinkbiganalytics.ingest;

/*-
 * #%L
 * thinkbig-nifi-core-processors
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

import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.TimeZone;

import static com.thinkbiganalytics.nifi.v2.ingest.GetTableData.DATE_TIME_FORMAT;
import static com.thinkbiganalytics.nifi.v2.ingest.GetTableData.toDate;
import static org.junit.Assert.assertEquals;

/**
 */
public class GetTableDataSupportTest {

    private GetTableDataSupport tableDataSupport;
    private Connection conn;
    private Date testDate = new Date(1458872629591L);

    @Before
    public void setUp() throws Exception {
        this.conn = Mockito.mock(Connection.class);
        tableDataSupport = new GetTableDataSupport(conn, 0);
        DateTimeZone.setDefault(DateTimeZone.forTimeZone(TimeZone.getTimeZone("America/Los_Angeles")));
    }

//    @Test //uncomment to run this integration test
    public void testFullLoad() throws ClassNotFoundException, SQLException {

        Class.forName("org.mariadb.jdbc.Driver");
        Connection con = DriverManager.getConnection(
            "jdbc:mysql://localhost:3306/hive","root","hadoop");
        GetTableDataSupport support = new GetTableDataSupport(con, 0);
        support.selectFullLoad("DBS", new String[]{"DESC", "DB_LOCATION_URI"});

    }


    //    @Test //uncomment to run this integration test
    public void testIncrementalHive() throws ClassNotFoundException, SQLException {

        Class.forName("org.mariadb.jdbc.Driver");
        Connection con = DriverManager.getConnection(
            "jdbc:mysql://localhost:3306/hive","root","hadoop");
        GetTableDataSupport support = new GetTableDataSupport(con, 0);
        String dateField = "CREATE_TIME";
        String watermarkValue = "2011-12-03T10:15:30";
        LocalDateTime waterMarkTime = LocalDateTime.parse(watermarkValue, DATE_TIME_FORMAT);
        Date lastLoadDate = toDate(waterMarkTime);
        int overlapTime = 0;
        int backoffTime = 0;
        GetTableDataSupport.UnitSizes unit = GetTableDataSupport.UnitSizes.DAY;
        support.selectIncremental("TBLS", new String[]{"TBL_ID", "DB_ID"}, dateField, overlapTime, lastLoadDate, backoffTime, unit);
    }

    //    @Test //uncomment to run this integration test
    public void testIncrementalKylo() throws ClassNotFoundException, SQLException {

        Class.forName("org.mariadb.jdbc.Driver");
        Connection con = DriverManager.getConnection(
            "jdbc:mysql://localhost:3306/kylo","root","hadoop");
        GetTableDataSupport support = new GetTableDataSupport(con, 0);
        String dateField = "DATEEXECUTED";
        String watermarkValue = "2011-12-03T10:15:30";
        LocalDateTime waterMarkTime = LocalDateTime.parse(watermarkValue, DATE_TIME_FORMAT);
        Date lastLoadDate = toDate(waterMarkTime);
        int overlapTime = 0;
        int backoffTime = 0;
        GetTableDataSupport.UnitSizes unit = GetTableDataSupport.UnitSizes.DAY;
        support.selectIncremental("DATABASECHANGELOG", new String[]{"ID", "FILENAME"}, dateField, overlapTime, lastLoadDate, backoffTime, unit);
    }


    @Test
    public void testMaxAllowableDateFromUnit() throws Exception {
        assertEquals(1458872629591L, GetTableDataSupport.maxAllowableDateFromUnit(testDate, GetTableDataSupport.UnitSizes.NONE).getTime());
        assertEquals(1458871200000L, GetTableDataSupport.maxAllowableDateFromUnit(testDate, GetTableDataSupport.UnitSizes.HOUR).getTime());
        assertEquals(1458802800000L, GetTableDataSupport.maxAllowableDateFromUnit(testDate, GetTableDataSupport.UnitSizes.DAY).getTime());
        assertEquals(1458543600000L, GetTableDataSupport.maxAllowableDateFromUnit(testDate, GetTableDataSupport.UnitSizes.WEEK).getTime());
        assertEquals(1456819200000L, GetTableDataSupport.maxAllowableDateFromUnit(testDate, GetTableDataSupport.UnitSizes.MONTH).getTime());
        assertEquals(1451635200000L, GetTableDataSupport.maxAllowableDateFromUnit(testDate, GetTableDataSupport.UnitSizes.YEAR).getTime());
    }

    @Test
    public void testSelectFullLoad() throws Exception {
        Statement st = Mockito.mock(Statement.class);
        ResultSet rs = Mockito.mock(ResultSet.class);
        DatabaseMetaData databaseMetaData = Mockito.mock(DatabaseMetaData.class);
        Mockito.when(conn.createStatement()).thenReturn(st);
        Mockito.when(conn.getMetaData()).thenReturn(databaseMetaData);
        Mockito.when(databaseMetaData.getIdentifierQuoteString()).thenReturn("");
        Mockito.when(st.executeQuery("select col1, col2 from testTable")).thenReturn(rs);
        tableDataSupport.selectFullLoad("testTable", new String[]{"col1", "col2"});
    }

    @Test
    public void testRangeNoLastLoad() throws Exception {
        GetTableDataSupport.DateRange range = new GetTableDataSupport.DateRange(null, testDate, 0, 0, GetTableDataSupport.UnitSizes.NONE);
        assertEquals(range.getMinDate(), new Date(0L));
        assertEquals(range.getMaxDate(), testDate);
    }

    @Test
    public void testRangeLastLoad() throws Exception {
        Date lastLoad = new Date(1458872000000L);
        GetTableDataSupport.DateRange range = new GetTableDataSupport.DateRange(lastLoad, testDate, 0, 0, GetTableDataSupport.UnitSizes.NONE);
        assertEquals(range.getMinDate(), lastLoad);
        assertEquals(range.getMaxDate(), testDate);
    }

    @Test
    public void testRangeLastLoadWithBackoff() throws Exception {
        Date lastLoad = new Date(1458872000000L);
        int backoffTimeSecs = 10;
        GetTableDataSupport.DateRange range = new GetTableDataSupport.DateRange(lastLoad, testDate, 0, backoffTimeSecs, GetTableDataSupport.UnitSizes.NONE);
        assertEquals(range.getMinDate(), lastLoad);
        assertEquals(range.getMaxDate(), new Date(testDate.getTime() - (backoffTimeSecs * 1000L)));
    }

    @Test
    public void testRangeLastLoadWithOverlap() throws Exception {
        Date lastLoad = new Date(1458872000000L);
        int overlapSecs = 10;
        GetTableDataSupport.DateRange range = new GetTableDataSupport.DateRange(lastLoad, testDate, overlapSecs, 0, GetTableDataSupport.UnitSizes.NONE);
        assertEquals(range.getMinDate(), new Date(lastLoad.getTime() - (overlapSecs * 1000L)));
        assertEquals(range.getMaxDate(), testDate);
    }

    @Test
    public void testRangeUnitSizeHour() throws Exception {
        Date lastLoad = new Date(1458872000000L);
        GetTableDataSupport.DateRange range = new GetTableDataSupport.DateRange(lastLoad, testDate, 0, 0, GetTableDataSupport.UnitSizes.HOUR);
        assertEquals(range.getMinDate(), lastLoad);
        assertEquals(range.getMaxDate(), new Date(1458871200000L));
    }

    @Test
    public void testRangeUnitSizeDay() throws Exception {
        Date lastLoad = new Date(1458872000000L);

        GetTableDataSupport.DateRange range = new GetTableDataSupport.DateRange(lastLoad, testDate, 0, 0, GetTableDataSupport.UnitSizes.DAY);
        assertEquals(range.getMinDate(), lastLoad);
        assertEquals(range.getMaxDate(), new Date(1458802800000L));
    }

    @Test
    public void testSelectIncremental() throws Exception {
        Statement st = Mockito.mock(Statement.class);
        ResultSet rs = Mockito.mock(ResultSet.class);
        DatabaseMetaData databaseMetaData = Mockito.mock(DatabaseMetaData.class);
        Mockito.when(conn.createStatement()).thenReturn(st);
        Mockito.when(st.executeQuery("select col1, col2 from testTable")).thenReturn(rs);
        Mockito.when(conn.prepareStatement(Mockito.anyString())).thenReturn(Mockito.mock(PreparedStatement.class));
        Mockito.when(conn.getMetaData()).thenReturn(databaseMetaData);
        Mockito.when(databaseMetaData.getIdentifierQuoteString()).thenReturn("");
        int overlapTime = 0;
        Date lastLoadDate = new Date(1458872629591L);
        int backoffTime = 0;
        tableDataSupport.selectIncremental("testTable", new String[]{"col1", "col2"}, "col2", overlapTime, lastLoadDate, backoffTime, GetTableDataSupport.UnitSizes.NONE);
    }

}
