package com.thinkbiganalytics.spark.validation;

/*-
 * #%L
 * thinkbig-spark-validate-cleanse-api
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

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HCatDataTypeTest {

    @Test
    public void testIsValueConvertibleToIntType() throws Exception {

        HCatDataType intType = HCatDataType.getDataTypes().get("int");

        assertTrue(intType.isValueConvertibleToType("0"));
        assertTrue(intType.isValueConvertibleToType(Integer.MAX_VALUE + ""));
        assertTrue(intType.isValueConvertibleToType(Integer.MIN_VALUE + ""));

        assertTrue(intType.isValueConvertibleToType(null));
        assertTrue(intType.isValueConvertibleToType(""));

        assertFalse(intType.isValueConvertibleToType("21.1"));
        assertFalse(intType.isValueConvertibleToType("-20.001"));
        assertFalse(intType.isValueConvertibleToType(Long.MAX_VALUE + ""));

    }


    @Test
    public void testIsValueConvertibleToSmallIntType() throws Exception {

        HCatDataType smallintType = HCatDataType.getDataTypes().get("smallint");

        assertTrue(smallintType.isValueConvertibleToType("0"));
        assertTrue(smallintType.isValueConvertibleToType(Short.MAX_VALUE + ""));
        assertTrue(smallintType.isValueConvertibleToType(Short.MIN_VALUE + ""));

        assertTrue(smallintType.isValueConvertibleToType(null));
        assertTrue(smallintType.isValueConvertibleToType(""));

        assertFalse(smallintType.isValueConvertibleToType("21.1"));
        assertFalse(smallintType.isValueConvertibleToType("-20.001"));
        assertFalse(smallintType.isValueConvertibleToType(Integer.MAX_VALUE + ""));

    }

    @Test
    public void testIsValueConvertibleToTinyIntType() throws Exception {

        HCatDataType type = HCatDataType.getDataTypes().get("tinyint");

        assertTrue(type.isValueConvertibleToType("0"));
        assertTrue(type.isValueConvertibleToType("127"));
        assertTrue(type.isValueConvertibleToType("-128"));

        assertTrue(type.isValueConvertibleToType(null));
        assertTrue(type.isValueConvertibleToType(""));

        assertFalse(type.isValueConvertibleToType("21.1"));
        assertFalse(type.isValueConvertibleToType("-20.001"));
        assertFalse(type.isValueConvertibleToType(Integer.MAX_VALUE + ""));
    }

    @Test
    public void testIsValueConvertibleToDecimalType() throws Exception {

        HCatDataType type = HCatDataType.getDataTypes().get("decimal");

        assertTrue(type.isValueConvertibleToType("0"));
        assertTrue(type.isValueConvertibleToType("12712"));
        assertTrue(type.isValueConvertibleToType("-12812"));
        assertTrue(type.isValueConvertibleToType("-12812.204154"));
        assertTrue(type.isValueConvertibleToType("-12812.1234"));
        assertTrue(type.isValueConvertibleToType("-128.12E8"));

        assertTrue(type.isValueConvertibleToType(null));
        assertTrue(type.isValueConvertibleToType(""));

        assertFalse(type.isValueConvertibleToType("No number"));
    }

    @Test
    public void testIsValueConvertibleToDecimalPrecisionType() throws Exception {

        HCatDataType type = HCatDataType.createFromDataType("mydecimal", "decimal(3,2)");

        assertTrue(type.isValueConvertibleToType("0"));
        assertTrue(type.isValueConvertibleToType("999"));
        assertTrue(type.isValueConvertibleToType("-999"));
        assertTrue(type.isValueConvertibleToType("999.99"));
        assertTrue(type.isValueConvertibleToType("-128.20"));

        assertTrue(type.isValueConvertibleToType(null));
        assertTrue(type.isValueConvertibleToType(""));

        assertFalse(type.isValueConvertibleToType("-1000"));
        assertFalse(type.isValueConvertibleToType("5.123", true));
        assertTrue(type.isValueConvertibleToType("5.123", false));

        assertFalse(type.isValueConvertibleToType("No number"));
    }

    @Test
    public void testIsValueConvertibleToDecimalPrecisionType2() throws Exception {

        HCatDataType type = HCatDataType.createFromDataType("mydecimal", "decimal(3,0)");

        assertTrue(type.isValueConvertibleToType("0"));
        assertTrue(type.isValueConvertibleToType("999"));
        assertTrue(type.isValueConvertibleToType("-999"));
        assertTrue(type.isValueConvertibleToType("-128.0"));

        assertFalse(type.isValueConvertibleToType("999.9"));
        assertTrue(type.isValueConvertibleToType(null));
        assertTrue(type.isValueConvertibleToType(""));

        assertFalse(type.isValueConvertibleToType("No number"));
    }

    @Test
    public void testIsValueConvertibleToChar() throws Exception {

        HCatDataType type = HCatDataType.getDataTypes().get("char");

        assertTrue(type.isValueConvertibleToType("mystring"));
        assertTrue(type.isValueConvertibleToType("99999"));
        assertTrue(type.isValueConvertibleToType("-999"));
        assertTrue(type.isValueConvertibleToType(null));
        assertTrue(type.isValueConvertibleToType(""));
        assertTrue(type.isValueConvertibleToType(StringUtils.leftPad("X", 255)));
        assertFalse(type.isValueConvertibleToType(StringUtils.leftPad("X", 256)));
    }

    @Test
    public void testIsValueConvertibleToBigDecimal() throws Exception {

        HCatDataType type = HCatDataType.getDataTypes().get("decimal");

        assertTrue(type.isValueConvertibleToType("0"));
        assertTrue(type.isValueConvertibleToType(Double.MAX_VALUE + ""));
        assertTrue(type.isValueConvertibleToType("12712"));
        assertTrue(type.isValueConvertibleToType("-12812"));
        assertTrue(type.isValueConvertibleToType("-12812.204154"));
        assertTrue(type.isValueConvertibleToType("-12812.1234"));
        assertTrue(type.isValueConvertibleToType("-128.12E8"));

        assertTrue(type.isValueConvertibleToType(null));
        assertTrue(type.isValueConvertibleToType(""));
        assertFalse(type.isValueConvertibleToType("No number"));
    }

    @Test
    public void testIsValueConvertibleToBigInteger() throws Exception {

        HCatDataType type = HCatDataType.getDataTypes().get("bigint");

        assertTrue(type.isValueConvertibleToType("0"));
        assertTrue(type.isValueConvertibleToType(Long.MAX_VALUE + ""));
        assertTrue(type.isValueConvertibleToType(Long.MIN_VALUE + ""));

        assertTrue(type.isValueConvertibleToType(null));
        assertTrue(type.isValueConvertibleToType(""));

        assertFalse(type.isValueConvertibleToType("21.1"));
        assertFalse(type.isValueConvertibleToType("-20.001"));

    }

    @Test
    public void testIsValueConvertibletoDate() throws Exception {
        HCatDataType type = HCatDataType.createFromDataType("mydate", "date");
        assertTrue(type.isValueConvertibleToType("2016-05-10"));
        assertFalse(type.isValueConvertibleToType("05-10-2015"));
    }

    @Test
    public void testIsValueConvertibletoTS() throws Exception {
        HCatDataType type = HCatDataType.createFromDataType("myts", "timestamp");
        assertTrue(type.isValueConvertibleToType("2015-01-15 11:10:20.333"));
        assertTrue(type.isValueConvertibleToType("2015-01-15 11:10:20"));
        assertTrue(type.isValueConvertibleToType("2015-01-15 11:10:20.333444555"));

        assertFalse(type.isValueConvertibleToType("2015/01/15 11:10:20"));
        assertFalse(type.isValueConvertibleToType("2016-05-10"));
        assertFalse(type.isValueConvertibleToType("05-10-2015"));
    }

    @Test
    public void testDecimalType() throws Exception {
        HCatDataType decimalType = HCatDataType.createFromDataType("decimal_type", "decimal(10,3)");
        boolean valid = decimalType.isValueConvertibleToType("29.3456");
        assertTrue(valid);
    }

}
