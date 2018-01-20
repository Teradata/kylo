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

import com.thinkbiganalytics.policy.validation.DateValidator;
import com.thinkbiganalytics.policy.validation.TimestampValidator;
import com.thinkbiganalytics.spark.util.InvalidFormatException;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

/**
 * Performs validation of a string value to ensure it is convertible to the give Hive column type. The class ensures the precision, range, and data type are compatible.
 */
public class HCatDataType implements Cloneable, Serializable {

    private static final HCatDataType UNCHECKED_TYPE = new HCatDataType();
    private static final Map<String, HCatDataType> dataTypes = new HashMap<>();

    // Build static rules around the various column types
    static {
        dataTypes.put("tinyint", new HCatDataType((int) Byte.MIN_VALUE, (int) Byte.MAX_VALUE));
        dataTypes.put("smallint", new HCatDataType((int) Short.MIN_VALUE, (int) Short.MAX_VALUE));
        dataTypes.put("int", new HCatDataType(Integer.MIN_VALUE, Integer.MAX_VALUE));
        dataTypes.put("bigint", new HCatDataType(BigInteger.class));
        dataTypes.put("decimal", new HCatDataType(BigDecimal.class));
        dataTypes.put("string", new HCatDataType(Long.MAX_VALUE));
        dataTypes.put("varchar", new HCatDataType(65355L));
        dataTypes.put("char", new HCatDataType(255L));
        // We use -MAX_VALUE because MIN_VALUE is actually minimum positive non-zero value
        dataTypes.put("float", new HCatDataType(-Float.MAX_VALUE, Float.MAX_VALUE));
        dataTypes.put("double", new HCatDataType(-Double.MAX_VALUE, Double.MAX_VALUE));
        dataTypes.put("real", new HCatDataType(-Double.MAX_VALUE, Double.MAX_VALUE));
        dataTypes.put("date", new HCatDataType(Date.class));
        dataTypes.put("timestamp", new HCatDataType(Timestamp.class));
        dataTypes.put("binary", new HCatDataType(byte[].class));

    }

    /**
     * Whether the value is numeric
     */
    boolean isnumeric;
    /**
     * Class instance returned from converting string to native of this type
     */
    private Class convertibleType;
    /**
     * Whether this is string type
     */
    private boolean isstring;
    /**
     * whether this is unchecked type (essentially any type not defined)
     */
    private boolean unchecked;
    /**
     * Minimum size of number
     */
    private Comparable min;

    /**
     * Maximum size of number
     */
    private Comparable max;

    /**
     * Max length of string
     */
    private long maxlength;

    /**
     * Number of digits supported (precision)
     */
    private Integer digits;

    /**
     * Name of the field (set after clone)
     */
    private String name;

    /**
     * Hive data type (set after clone)
     */
    private String nativeType;


    private HCatDataType() {
        this.unchecked = true;
        this.convertibleType = String.class;
    }

    private HCatDataType(long length) {
        this.maxlength = length;
        this.isstring = true;
        this.convertibleType = String.class;
    }

    private HCatDataType(Integer min, Integer max) {
        this.isnumeric = true;
        this.min = min;
        this.max = max;
        this.convertibleType = Integer.class;
    }

    private HCatDataType(Double min, Double max) {
        this.isnumeric = true;
        this.min = min;
        this.max = max;
        this.convertibleType = Double.class;
    }

    private HCatDataType(Float min, Float max) {
        this.isnumeric = true;
        this.min = min;
        this.max = max;
        this.convertibleType = Float.class;
    }

    private HCatDataType(Class clazz) {

        this.convertibleType = clazz;
        if (clazz == Date.class || clazz == Timestamp.class || clazz == byte[].class) {
            this.isnumeric = false;
        } else {
            this.isnumeric = true;
            BigDecimal minDecimal = new BigDecimal(Long.MIN_VALUE);
            BigDecimal maxDecimal = new BigDecimal(Long.MAX_VALUE);
            if (clazz == BigInteger.class) {
                this.min = minDecimal.toBigInteger();
                this.max = maxDecimal.toBigInteger();
            } else if (clazz == BigDecimal.class) {
                this.min = null;
                this.max = null;
            } else {
                throw new RuntimeException("Invalid class for constructor " + clazz.getCanonicalName());
            }
        }

    }

    public boolean isDateOrTimestamp() {
        return this.convertibleType == Date.class || this.convertibleType == Timestamp.class;
    }

    /**
     * Generate a data type validator for the given hive data type
     *
     * @param columnType the defined hive column type
     */
    public static HCatDataType createFromDataType(String name, String columnType) {

        Integer decSize = null;
        Integer decDigits = null;
        Long strLen = null;
        String dataType = columnType.toLowerCase();
        // Extract precision and scale portion as in Decimal(8,2) or varchar(255)
        int idx = columnType.indexOf('(');
        if (idx > -1) {
            dataType = columnType.substring(0, idx);
            String precisionPart = columnType.substring(idx + 1, columnType.length() - 1);
            String[] parts = precisionPart.split(",");
            if (parts.length == 2) {
                decSize = Integer.parseInt(parts[0]);
                decDigits = Integer.parseInt(parts[1]);
            } else {
                strLen = Long.parseLong(parts[0]);
            }
        }
        HCatDataType hcatType = dataTypes.get(dataType);
        if (hcatType != null) {
            try {
                hcatType = (HCatDataType) hcatType.clone();
                hcatType.name = name;
                hcatType.nativeType = dataType;
                // Determine min max based on column precision
                if (decSize != null) {
                    hcatType.digits = decDigits;
                    hcatType.max = new BigDecimal(generateRepeatingCharacters(decSize, '9')
                                                  + "."
                                                  + generateRepeatingCharacters(decDigits, '9'));
                    hcatType.min = ((BigDecimal) hcatType.max).negate();

                } else if (strLen != null) {
                    hcatType.maxlength = strLen;
                }
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException("Unexpected clone exception", e);
            }
        }
        return (hcatType == null ? UNCHECKED_TYPE : hcatType);
    }

    private static String generateRepeatingCharacters(int repeatTimes, char repeatChar) {
        if ((repeatTimes <= 0)) {
            return "";
        }

        StringBuilder retVal = new StringBuilder();

        for (int i = 0; i < repeatTimes; i++) {
            retVal.append(repeatChar);
        }
        return retVal.toString();
    }

    public static Map<String, HCatDataType> getDataTypes() {
        return dataTypes;
    }

    private int getNumberOfDecimalPlaces(BigDecimal bigDecimal) {
        String string = bigDecimal.stripTrailingZeros().toPlainString();
        int index = string.indexOf('.');
        return index < 0 ? 0 : string.length() - index - 1;
    }

    public Class getConvertibleType() {
        return this.convertibleType;
    }

    public <T extends Comparable> T toNativeValue(String val) throws InvalidFormatException {
        try {
            if (StringUtils.isEmpty(val) || convertibleType == String.class) {
                return (unchecked || isstring ? (T) val : null);
            } else if (convertibleType == Integer.class) {
                return (T) new Integer(val);
            } else if (convertibleType == Double.class) {
                return (T) new Double(val);
            } else if (convertibleType == Float.class) {
                return (T) new Float(val);
            } else if (convertibleType == BigInteger.class) {
                return (T) new BigInteger(val);
            } else if (convertibleType == BigDecimal.class) {
                return (T) new BigDecimal(val);
            } else {
                throw new RuntimeException("Unexpected conversion [" + convertibleType + "] for value [" + val + "]");
            }
        } catch (NumberFormatException | ClassCastException e) {
            throw new InvalidFormatException(this, val);
        }
    }

    /**
     * Validate scale (digits following the decimal)
     *
     * @param val the native value
     * @return whether passes validation
     */
    private boolean validatePrecision(Comparable val) {
        return convertibleType != BigDecimal.class || getNumberOfDecimalPlaces((BigDecimal) val) <= digits;
    }

    /**
     * Returns true if Hive is able to convert a string value to the correct convertible type.
     *
     * @param strVal the string value
     * @return true if the string value will be ok, false if explicit conversion is needed.
     */
    public boolean isStringValueValidForHiveType(String strVal) {
        if (isstring) {
            return true;
        }
        if (strVal != null && !isnumeric) {
            if (convertibleType == Timestamp.class) {
                return new TimestampValidator(true).validate(strVal);
            } else if (convertibleType == Date.class) {
                return DateValidator.instance().validate(strVal);
            } else if (convertibleType == byte[].class) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tests whether the string value can be converted to the hive data type defined by this class. If it is not convertible then hive will not be able to show the value
     *
     * @param val the value
     * @return whether value is valid
     */
    public boolean isValueConvertibleToType(Object val) {
        return isValueConvertibleToType(val, false);
    }

    public boolean isValueConvertibleToType(Object val, boolean enforcePrecision) {
        try {
            if (val instanceof String) {
                String strVal = (String) val;
                if (strVal != null && !isnumeric) {
                    if (convertibleType == Timestamp.class) {
                        return new TimestampValidator(true).validate(strVal);
                    } else if (convertibleType == Date.class) {
                        return DateValidator.instance().validate(strVal);
                    } else if (convertibleType == byte[].class) {
                        return true;
                    }
                }

                Comparable nativeValue = toNativeValue(strVal);
                if (nativeValue != null) {
                    if (isnumeric) {
                        if (min != null && min.compareTo(nativeValue) > 0) {
                            return false;
                        }
                        if (max != null && max.compareTo(nativeValue) < 0) {
                            return false;
                        }
                        if (digits != null && !(!enforcePrecision || validatePrecision(nativeValue))) {
                            return false;
                        }

                    } else if (isstring && strVal.length() > maxlength) {
                        return false;
                    }
                }
            } else {
                return val == null || val.getClass() == convertibleType || val instanceof Number && Number.class.isAssignableFrom(convertibleType);
            }

        } catch (InvalidFormatException | ClassCastException | IllegalArgumentException e) {
            return false;
        }
        return true;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public String getName() {
        return name;
    }

    public String getNativeType() {
        return nativeType;
    }

    public boolean isUnchecked() {
        return unchecked;
    }

    public boolean isNumeric() {
        return isnumeric;
    }
}
