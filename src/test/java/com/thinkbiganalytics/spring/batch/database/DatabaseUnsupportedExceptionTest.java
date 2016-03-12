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
        assertTrue(ex.getCause() == null);
        assertEquals(ex2.getMessage(), "Your database is not supported by Pipeline Controller");
        assertEquals(ex2.getLocalizedMessage(), "Your database is not supported by Pipeline Controller");
        assertTrue(ex2.getCause() != null);

    }
}