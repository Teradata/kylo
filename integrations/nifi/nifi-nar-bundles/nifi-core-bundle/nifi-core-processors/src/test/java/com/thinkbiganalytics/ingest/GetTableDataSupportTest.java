/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.ingest;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;

import static org.junit.Assert.assertEquals;

/**
 * Created by matthutton on 3/24/16.
 */
public class GetTableDataSupportTest {

    private GetTableDataSupport tableDataSupport;
    private Connection conn;
    private DateTime testDate = new DateTime(1458872629591L);

    @Before
    public void setUp() throws Exception {
        this.conn = Mockito.mock(Connection.class);
        tableDataSupport = new GetTableDataSupport(conn, 0);
    }

    @Test
    public void testMaxAllowableDateFromUnit() throws Exception {

        assertEquals(1458872629591L, tableDataSupport.maxAllowableDateFromUnit(testDate, GetTableDataSupport.UnitSizes.NONE).getMillis());
        assertEquals(1458871200000L, tableDataSupport.maxAllowableDateFromUnit(testDate, GetTableDataSupport.UnitSizes.HOUR).getMillis());
        assertEquals(1458802800000L, tableDataSupport.maxAllowableDateFromUnit(testDate, GetTableDataSupport.UnitSizes.DAY).getMillis());
        assertEquals(1458543600000L, tableDataSupport.maxAllowableDateFromUnit(testDate, GetTableDataSupport.UnitSizes.WEEK).getMillis());
        assertEquals(1456819200000L, tableDataSupport.maxAllowableDateFromUnit(testDate, GetTableDataSupport.UnitSizes.MONTH).getMillis());
        assertEquals(1451635200000L, tableDataSupport.maxAllowableDateFromUnit(testDate, GetTableDataSupport.UnitSizes.YEAR).getMillis());
    }

    @Test
    public void testSelectFullLoad() throws Exception {
        Statement st = Mockito.mock(Statement.class);
        ResultSet rs = Mockito.mock(ResultSet.class);
        Mockito.when(conn.createStatement()).thenReturn(st);
        Mockito.when(st.executeQuery("select col1, col2 from testTable")).thenReturn(rs);
        tableDataSupport.selectFullLoad("testTable", new String[]{"col1", "col2"});
    }

    @Test
    public void testRangeNoLastLoad() throws Exception {
        GetTableDataSupport.DateRange range = new GetTableDataSupport.DateRange(null, testDate, 0, 0, GetTableDataSupport.UnitSizes.NONE);
        assertEquals(range.getMinDate(), new Date(0L));
        assertEquals(range.getMaxDate(), testDate.toDate());
    }

    @Test
    public void testRangeLastLoad() throws Exception {
        DateTime lastLoad = new DateTime(1458872000000L);
        GetTableDataSupport.DateRange range = new GetTableDataSupport.DateRange(lastLoad, testDate, 0, 0, GetTableDataSupport.UnitSizes.NONE);
        assertEquals(range.getMinDate(), lastLoad.toDate());
        assertEquals(range.getMaxDate(), testDate.toDate());
    }

    @Test
    public void testRangeLastLoadWithBackoff() throws Exception {
        DateTime lastLoad = new DateTime(1458872000000L);
        int backoffTimeSecs = 10;
        GetTableDataSupport.DateRange range = new GetTableDataSupport.DateRange(lastLoad, testDate, 0, backoffTimeSecs, GetTableDataSupport.UnitSizes.NONE);
        assertEquals(range.getMinDate(), lastLoad.toDate());
        assertEquals(range.getMaxDate(), new Date(testDate.toDate().getTime() - (backoffTimeSecs * 1000L)));
    }

    @Test
    public void testRangeLastLoadWithOverlap() throws Exception {
        DateTime lastLoad = new DateTime(1458872000000L);
        int overlapSecs = 10;
        GetTableDataSupport.DateRange range = new GetTableDataSupport.DateRange(lastLoad, testDate, overlapSecs, 0, GetTableDataSupport.UnitSizes.NONE);
        assertEquals(range.getMinDate(), new Date(lastLoad.toDate().getTime() - (overlapSecs * 1000L)));
        assertEquals(range.getMaxDate(), testDate.toDate());
    }

    @Test
    public void testRangeUnitSizeHour() throws Exception {
        DateTime lastLoad = new DateTime(1458872000000L);
        GetTableDataSupport.DateRange range = new GetTableDataSupport.DateRange(lastLoad, testDate, 0, 0, GetTableDataSupport.UnitSizes.HOUR);
        assertEquals(range.getMinDate(), lastLoad.toDate());
        assertEquals(range.getMaxDate(), new Date(1458871200000L));
    }

    @Test
    public void testRangeUnitSizeDay() throws Exception {
        DateTime lastLoad = new DateTime(1458872000000L);

        GetTableDataSupport.DateRange range = new GetTableDataSupport.DateRange(lastLoad, testDate, 0, 0, GetTableDataSupport.UnitSizes.DAY);
        assertEquals(range.getMinDate(), lastLoad.toDate());
        assertEquals(range.getMaxDate(), new DateTime(1458802800000L).toDate());
    }

    @Test
    public void testSelectIncremental() throws Exception {
        Statement st = Mockito.mock(Statement.class);
        ResultSet rs = Mockito.mock(ResultSet.class);
        Mockito.when(conn.createStatement()).thenReturn(st);
        Mockito.when(st.executeQuery("select col1, col2 from testTable")).thenReturn(rs);
        Mockito.when(conn.prepareStatement(Mockito.anyString())).thenReturn(Mockito.mock(PreparedStatement.class));
        int overlapTime = 0;
        DateTime lastLoadDate = new DateTime(1458872629591L);
        int backoffTime = 0;
        tableDataSupport.selectIncremental("testTable", new String[]{"col1", "col2"}, "col2", overlapTime, lastLoadDate, backoffTime, GetTableDataSupport.UnitSizes.NONE);
    }

}