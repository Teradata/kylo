package com.thinkbiganalytics.spring.batch.database;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by matthutton on 3/12/16.
 */
public class DatabaseUnsupportedExceptionTest {

    private DatabaseUnsupportedException ex;
    private DatabaseUnsupportedException ex2;

    @Before
    public void setUp() throws Exception {
        ex = new DatabaseUnsupportedException();
        ex2 = new DatabaseUnsupportedException(new RuntimeException("test"));
    }

    @Test
    public void testDB() throws Exception {
        assertEquals(ex2.getMessage(), "test");
        assertEquals(ex2.getLocalizedMessage(), "test");
    }
}