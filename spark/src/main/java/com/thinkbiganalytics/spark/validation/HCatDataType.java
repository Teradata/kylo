/*
 * Copyright (c) 2015. Teradata Inc.
 */
package com.thinkbiganalytics.spark.validation;

import com.thinkbiganalytics.spark.util.InvalidFormatException;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by matthutton on 12/26/15.
 */
public class HCatDataType implements Cloneable, Serializable {

    private static HCatDataType UNCHECKED_TYPE = new HCatDataType();
    private static Map<String, HCatDataType> dataTypes = new HashMap();

    // Build rules around the various column types
    static {
        dataTypes.put("tinyint", new HCatDataType((long) Byte.MIN_VALUE, (long) Byte.MAX_VALUE));
        dataTypes.put("smallint", new HCatDataType((long) Short.MIN_VALUE, (long) Short.MAX_VALUE));
        dataTypes.put("int", new HCatDataType((long) Integer.MIN_VALUE, (long) Integer.MAX_VALUE));
        dataTypes.put("bigint", new HCatDataType(BigInteger.class));
        dataTypes.put("float", new HCatDataType((double) Float.MIN_VALUE, (double) Float.MAX_VALUE));
        dataTypes.put("double", new HCatDataType(Double.MIN_VALUE, Double.MAX_VALUE));
        dataTypes.put("real", new HCatDataType(Double.MIN_VALUE, Double.MAX_VALUE));
        dataTypes.put("decimal", new HCatDataType(BigDecimal.class));
        dataTypes.put("string", new HCatDataType(Long.MAX_VALUE));
        dataTypes.put("varchar", new HCatDataType(65355L));
        dataTypes.put("char", new HCatDataType(255L));
    }

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
     * Whether the value is numeric
     */
    boolean isnumeric;

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
    private int digits;

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

    private HCatDataType(Long min, Long max) {
        this.isnumeric = true;
        this.min = min;
        this.max = max;
        this.convertibleType = Long.class;
    }

    private HCatDataType(Double min, Double max) {
        this.isnumeric = true;
        this.min = min;
        this.max = max;
        this.convertibleType = Double.class;
    }

    private HCatDataType(Class clazz) {
        this.isnumeric = true;
        this.convertibleType = clazz;
        BigDecimal minDecimal = new BigDecimal("-9.2E18");
        BigDecimal maxDecimal = new BigDecimal("9.2E18");
        if (clazz == BigInteger.class) {
            this.min = minDecimal.toBigInteger();
            this.max = maxDecimal.toBigInteger();
        } else if (clazz == BigDecimal.class) {
            // Note: Not sure what to set this to. for now it is not validated anyway
            this.min = minDecimal;
            this.max = maxDecimal;
        } else {
            throw new RuntimeException("Invalid class for constructor " + clazz.getCanonicalName());
        }
    }

    /**
     * Generate a data type validator for the given hive data type
     *
     * @param columnType the defined hive column type
     * @return
     */
    public static HCatDataType createFromDataType(String name, String columnType) {

        Integer decSize = null;
        Integer decDigits = null;
        Long strLen = null;
        String dataType = columnType.toLowerCase();
        // Extract precision and scale portion as in Decimal(8,2) or varchar(255)
        int idx = columnType.indexOf("(");
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
        hcatType = (hcatType == null ? UNCHECKED_TYPE : hcatType);
        try {
            hcatType = (HCatDataType) hcatType.clone();
            hcatType.name = name;
            hcatType.nativeType = dataType;
            // Determine min max based on column precision
            if (decSize != null) {
                hcatType.digits = decDigits;
                hcatType.max = BigDecimal.valueOf(Integer.parseInt(StringUtils.repeat("9",decSize))+1);
                hcatType.min = BigDecimal.valueOf(-1*(Integer.parseInt(StringUtils.repeat("9", decSize))+1));
            } else if (strLen != null) {
                hcatType.maxlength = strLen;
            }
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Unexpected clone exception", e);
        }
        return (hcatType == null ? UNCHECKED_TYPE : hcatType);
    }

    private int getNumberOfDecimalPlaces(BigDecimal bigDecimal) {
        String string = bigDecimal.stripTrailingZeros().toPlainString();
        int index = string.indexOf(".");
        return index < 0 ? 0 : string.length() - index - 1;
    }

    private int getNumberOfDecimalPlaces(Double dbl) {
        return getNumberOfDecimalPlaces(new BigDecimal(dbl));
    }

    public Class getConvertibleType() {
        return this.convertibleType;
    }

    public <T extends Comparable> T toNativeValue(String val) throws InvalidFormatException {
        try {
            if (StringUtils.isEmpty(val) || convertibleType == String.class) {
                return (unchecked || isstring ? (T) val : null);
            } else if (convertibleType == Long.class) {
                return (T) new Long(val);
            } else if (convertibleType == Double.class) {
                return (T) new Double(val);
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
     * @param val the native value
     * @return whether passes validation
     */
    private boolean validatePrecision(Comparable val) {
        if (convertibleType == BigDecimal.class) {
            if (getNumberOfDecimalPlaces((BigDecimal) val) > digits) return false;
        } else if (convertibleType == Double.class) {
            if (getNumberOfDecimalPlaces((Double) val) > digits) return false;
        }
        return true;
    }

    /**
     * Tests whether the string value can be converted to the hive data type defined by this class. If
     * it is not convertible then hive will not be able to show the value
     * @param val the string value
     * @return whether value is valid
     */
    public boolean isValueConvertibleToType(String val) {
        try {
            Comparable nativeValue = toNativeValue(val);
            if (nativeValue != null) {
                if (isnumeric) {
                    if (min.compareTo(nativeValue) >= 0) return false;
                    if (max.compareTo(nativeValue) <= 0) return false;
                    if (!validatePrecision(nativeValue)) {
                        return false;
                    }

                } else if (isstring) {
                    if (val.length() > maxlength) return false;
                }
            }

        } catch (InvalidFormatException | ClassCastException e) {
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

}
