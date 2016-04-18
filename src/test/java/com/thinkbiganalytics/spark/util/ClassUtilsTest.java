package com.thinkbiganalytics.spark.util;

import org.junit.Assert;
import org.junit.Test;

public class ClassUtilsTest {

    /** Verify invoking a method on an object. */
    @Test
    public void invoke() {
        String obj = "Hello, world!";

        // Test with no arguments
        String result1 = ClassUtils.invoke(obj, "toLowerCase");
        Assert.assertEquals("hello, world!", result1);

        // Test with one argument
        Character result2 = ClassUtils.invoke(obj, "charAt", 5);
        Assert.assertNotNull(result2);
        Assert.assertEquals(',', result2.charValue());
    }

    /** Verify exception for invoking a method that throws an exception. */
    @Test(expected = IllegalArgumentException.class)
    public void invokeException() {
        ClassUtils.invoke("Object", "charAt", -1);
    }

    /** Verify exception for a method that does not exist. */
    @Test(expected = UnsupportedOperationException.class)
    public void invokeInvalid() {
        ClassUtils.invoke("Object", "invalid");
    }
}
