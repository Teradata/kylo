/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.schema;

import com.thinkbiganalytics.db.model.schema.TableSchema;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by matthutton on 10/26/16.
 */
public class TextFileParserTest {

    TextFileParser parser;

    @Before
    public void setUp() throws Exception {
        this.parser = new TextFileParser();
    }

    @Test
    public void testCSVParser3() {
        // Verify escapes and single quotes
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("csv3.csv")) {
            TableSchema schema = parser.parse(is, null);
            assertTrue(schema.getDelim().equals(","));
            assertTrue(schema.getEscape() != null && schema.getEscape().equals("\\\\"));
            assertTrue(schema.getQuote() != null && schema.getQuote().equals("\\\'"));
            assertEquals(3, schema.getFields().size());
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }


    @Test
    public void testCSVParser2() {
        // Verify escapes and double quotes
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("csv2.csv")) {
            TableSchema schema = parser.parse(is, null);
            assertTrue(schema.getDelim().equals(","));
            assertTrue(schema.getEscape() != null && schema.getEscape().equals("\\\\"));
            assertTrue(schema.getQuote() != null && schema.getQuote().equals("\""));
            assertEquals(3, schema.getFields().size());
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testCSVParser1() {

        try (InputStream is = getClass().getClassLoader().getResourceAsStream("csv1.csv")) {
            TableSchema schema = parser.parse(is, null);
            assertTrue(schema.getDelim().equals(","));
            assertTrue(schema.getEscape() == null);
            assertTrue(schema.getQuote() == null);
            assertEquals(3, schema.getFields().size());
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testTabParser1() {

        try (InputStream is = getClass().getClassLoader().getResourceAsStream("tab1.tab")) {
            TableSchema schema = parser.parse(is, null);
            assertTrue(schema.getDelim() == "\\t");
            assertTrue(schema.getEscape() == null);
            assertTrue(schema.getQuote() == null);
            assertEquals(3, schema.getFields().size());
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testCustomParser1() {

        try (InputStream is = getClass().getClassLoader().getResourceAsStream("plus1.plus")) {
            TableSchema schema = parser.parse(is, '+');
            assertTrue(schema.getDelim().equals("+"));
            assertTrue(schema.getEscape() == null);
            assertTrue(schema.getQuote() == null);
            assertEquals(3, schema.getFields().size());
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

}