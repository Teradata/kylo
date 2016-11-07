package com.thinkbiganalytics.datalake.authorization.service;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by Jeremy Merrifield on 11/3/16.
 */
public class HadoopAuthorizationServiceTest {

    @Test
    public void testConvertNewlineDelimetedTextToList() throws Exception {
        String stringWithNewLines = "this\nis\na\ntest";
        List<String> result = HadoopAuthorizationService.convertNewlineDelimetedTextToList(stringWithNewLines);
        assertTrue("The string with newline characters were not converted to a comma delimited list", result.get(0).equals("this"));
        assertTrue("The string with newline characters were not converted to a comma delimited list", result.get(1).equals("is"));
        assertTrue("The string with newline characters were not converted to a comma delimited list", result.get(2).equals("a"));
        assertTrue("The string with newline characters were not converted to a comma delimited list", result.get(3).equals("test"));
    }

    @Test
    public void testConvertNewlineDelimetedTextToListForNull() throws Exception {
        String stringWithNewLines = null;
        List<String> result = HadoopAuthorizationService.convertNewlineDelimetedTextToList(stringWithNewLines);
        assertTrue("The list should not be null and should be empty", result != null && result.size() == 0);
    }

    @Test
    public void testConvertNewlineDelimetedTextToListForEmptyString() throws Exception {
        String stringWithNewLines = "";
        List<String> result = HadoopAuthorizationService.convertNewlineDelimetedTextToList(stringWithNewLines);
        assertTrue("The list should should be empty", result != null && result.size() == 0);
    }
}