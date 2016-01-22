/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.spark;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

public class HCatDataTypeTests {

    @Test
    public void testStringTypes() {
        HCatDataType type = HCatDataType.createFromDataType("string1", "string");
        type.isValueConvertibleToType("test");
        type.isValueConvertibleToType(null);
        type.isValueConvertibleToType("42.0");
        type.isValueConvertibleToType("");
    }

    @Test
    public void testCharTypes() {
        HCatDataType type = HCatDataType.createFromDataType("char1", "char");
        testString255(type);
    }

    @Test
    public void testVarcharTypes() {
        HCatDataType type = HCatDataType.createFromDataType("varchar", "varchar(255)");
        testString255(type);
    }

    @Test
    public void testDecimal8_2() {
        HCatDataType type = HCatDataType.createFromDataType("double1", "decimal(8,2)");
        type.isValueConvertibleToType("");
        type.isValueConvertibleToType(null);
        type.isValueConvertibleToType("12345678.50");
        type.isValueConvertibleToType("12345678.99");
        type.isValueConvertibleToType("0.52");
        type.isValueConvertibleToType(".5");
        type.isValueConvertibleToType("12345678");
        type.isValueConvertibleToType("1234567.210");
        type.isValueConvertibleToType("-0.52");
        type.isValueConvertibleToType("-.5");
        type.isValueConvertibleToType("-12345678");
        type.isValueConvertibleToType("-1234567.210");
        Assert.assertFalse(type.isValueConvertibleToType("sometext"));
        Assert.assertFalse(type.isValueConvertibleToType("1234567.1234"));
        Assert.assertFalse(type.isValueConvertibleToType("123456789.12"));
        Assert.assertFalse(type.isValueConvertibleToType(StringUtils.repeat("X", 256)));
        Assert.assertFalse(type.isValueConvertibleToType(StringUtils.repeat("X", 256)));
    }

    @Test
    public void testDouble() {
        HCatDataType type = HCatDataType.createFromDataType("double1", "double");
        type.isValueConvertibleToType("");
        type.isValueConvertibleToType(null);
        type.isValueConvertibleToType(Double.MAX_VALUE + "");
        type.isValueConvertibleToType(Double.MIN_VALUE + "");
        type.isValueConvertibleToType("10");
        type.isValueConvertibleToType("-10.005");

        Assert.assertFalse(type.isValueConvertibleToType("sometext"));
        Assert.assertFalse(type.isValueConvertibleToType(BigDecimal.valueOf(Double.MAX_VALUE).add(BigDecimal.ONE).toString()));
    }


    @Test
    public void testInteger() {
        HCatDataType type = HCatDataType.createFromDataType("int1", "int");
        type.isValueConvertibleToType("");
        type.isValueConvertibleToType(null);
        type.isValueConvertibleToType(Integer.MAX_VALUE + "");
        type.isValueConvertibleToType(Integer.MIN_VALUE + "");
        type.isValueConvertibleToType("-10");

        Assert.assertFalse(type.isValueConvertibleToType("sometext"));
        Assert.assertFalse(type.isValueConvertibleToType("-10.05"));
        Assert.assertFalse(type.isValueConvertibleToType("10.0"));
        Assert.assertFalse(type.isValueConvertibleToType((((long) Integer.MAX_VALUE) + 1) + ""));
    }

    @Test
    public void testTinyInt() {
        HCatDataType type = HCatDataType.createFromDataType("tinyint1", "tinyint");
        type.isValueConvertibleToType("");
        type.isValueConvertibleToType(null);
        type.isValueConvertibleToType(Byte.MAX_VALUE + "");
        type.isValueConvertibleToType(Byte.MIN_VALUE + "");
        type.isValueConvertibleToType("-10");

        Assert.assertFalse(type.isValueConvertibleToType("sometext"));
        Assert.assertFalse(type.isValueConvertibleToType("-129"));
        Assert.assertFalse(type.isValueConvertibleToType("128"));
    }

    @Test
    public void testSmallInt() {
        HCatDataType type = HCatDataType.createFromDataType("smallint1", "smallint");
        type.isValueConvertibleToType("");
        type.isValueConvertibleToType(null);
        type.isValueConvertibleToType(Short.MAX_VALUE + "");
        type.isValueConvertibleToType(Short.MIN_VALUE + "");
        type.isValueConvertibleToType("-10");

        Assert.assertFalse(type.isValueConvertibleToType("sometext"));
        Assert.assertFalse(type.isValueConvertibleToType("-32769"));
        Assert.assertFalse(type.isValueConvertibleToType("32768"));
    }

    @Test
    public void testUnchecked() {
        HCatDataType type = HCatDataType.createFromDataType("unchecked1", "random");
        type.isValueConvertibleToType("");
        type.isValueConvertibleToType(null);
        type.isValueConvertibleToType(Short.MAX_VALUE + "");
        type.isValueConvertibleToType(Short.MIN_VALUE + "");
        type.isValueConvertibleToType("-10");
    }


    private void testString255(HCatDataType type) {
        type.isValueConvertibleToType("test");
        type.isValueConvertibleToType(null);
        type.isValueConvertibleToType("42.0");
        type.isValueConvertibleToType("");
        type.isValueConvertibleToType(StringUtils.repeat("X", 255));

        Assert.assertFalse(type.isValueConvertibleToType(StringUtils.repeat("X", 256)));
    }




}
