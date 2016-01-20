/*
 * Copyright (c) 2015. Teradata Inc.
 */
package com.thinkbiganalytics.spark;

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
        dataTypes.put("tinyint", new HCatDataType((long) Byte.MIN_VALUE, (long) Byte.MAX_VALUE, 0));
        dataTypes.put("smallint", new HCatDataType((long) Short.MIN_VALUE, (long) Short.MAX_VALUE, 0));
        dataTypes.put("int", new HCatDataType((long) Integer.MIN_VALUE, (long) Integer.MAX_VALUE, 0));
        dataTypes.put("bigint", new HCatDataType(BigInteger.class));
        dataTypes.put("float", new HCatDataType((double) Float.MIN_VALUE, (double) Float.MAX_VALUE, 0));
        dataTypes.put("double", new HCatDataType(Double.MIN_VALUE, Double.MAX_VALUE, 0));
        dataTypes.put("decimal", new HCatDataType(BigDecimal.class));
        dataTypes.put("string", new HCatDataType(Long.MAX_VALUE));
        dataTypes.put("varchar", new HCatDataType(65355L));
        dataTypes.put("char", new HCatDataType(255L));
    }

    private boolean isbig;

    private boolean istring;

    private boolean binary;

    boolean numeric;

    private boolean isint;

    private Comparable min;

    private Comparable max;

    private long maxlength;

    private int digits;

    private String name;

    private HCatDataType() {
        this.binary = true;
    }

    private HCatDataType(long length) {
        this.maxlength = length;
        this.istring = true;
    }

    private HCatDataType(Long min, Long max, int digits) {
        this.numeric = true;
        this.isint = true;
        this.min = BigInteger.valueOf(min);
        this.max = BigInteger.valueOf(max);
        this.digits = digits;
    }

    private HCatDataType(Double min, Double max, int digits) {
        this.numeric = true;
        this.min = BigDecimal.valueOf(min);
        this.max = BigDecimal.valueOf(max);

        this.digits = digits;
    }

    private HCatDataType(Class clazz) {
        this.numeric = true;
        this.isbig = true;
        if (clazz == BigInteger.class) {
            this.isint = true;
            this.min = new BigDecimal("-9.2E18").toBigInteger();
            this.max = new BigDecimal("9.2E18").toBigInteger();
        }
    }


    public static HCatDataType createFromDataType(String name, String stype) {

        String type = stype;
        Integer decSize = null;
        Integer decDigits = null;
        Long strLen = null;

        // Extract precision portion as in Decimal(8,2) or varchar(255)
        int idx = stype.indexOf("(");
        if (idx > -1) {
            type = stype.substring(0, idx);
            String precisionPart = stype.substring(idx + 1, stype.length() - 1);
            String[] parts = precisionPart.split(",");
            if (parts.length == 2) {
                decSize = Integer.parseInt(parts[0]);
                decDigits = Integer.parseInt(parts[1]);
            } else {
                strLen = Long.parseLong(parts[0]);
            }
        }
        HCatDataType dataType = dataTypes.get(type);
        dataType = (dataType == null ? UNCHECKED_TYPE : dataType);
        try {
            dataType = (HCatDataType) dataType.clone();
            // Determine min max based on column precision
            if (decSize != null) {
                dataType.digits = decDigits;
                dataType.max = BigDecimal.valueOf(Math.abs(Math.pow(decSize, 10) - 1));
                dataType.min =  BigDecimal.valueOf(Math.abs(Math.pow(decSize, 10) - 1) * -1);

            } else if (strLen != null) {
                dataType.maxlength = strLen;
            }
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Unexpected clone exception", e);
        }
        return (dataType == null ? UNCHECKED_TYPE : dataType);
    }

    private int getNumberOfDecimalPlaces(BigDecimal bigDecimal) {
        String string = bigDecimal.stripTrailingZeros().toPlainString();
        int index = string.indexOf(".");
        return index < 0 ? 0 : string.length() - index - 1;
    }

    private int getNumberOfDecimalPlaces(Double dbl) {
        return getNumberOfDecimalPlaces(new BigDecimal(dbl));
    }

    public boolean isConvertible(String val) {
        if (StringUtils.isEmpty(val)) return true;
        if (numeric) {
            try {
                if (isint) {

                    if (isbig) {
                        BigInteger num = new BigInteger(val);

                        if (min.compareTo(num) >= 0) return false;
                        if (max.compareTo(num) <= 0) return false;
                    } else {
                        Long num = new Long(val);
                        if (min.compareTo(num) >= 0) return false;
                        if (max.compareTo(num) <= 0) return false;
                    }
                } else {
                    if (isbig) {
                        BigDecimal num = new BigDecimal(val);
                        if (min.compareTo(num) >= 0) return false;
                        if (max.compareTo(num) <= 0) return false;
                        if (getNumberOfDecimalPlaces(num) > digits) return false;
                    } else {
                        Double num = new Double(val);
                        if (min.compareTo(num) >= 0) return false;
                        if (max.compareTo(num) <= 0) return false;
                        if (getNumberOfDecimalPlaces(num) > digits) return false;
                    }
                }
            } catch (NumberFormatException e) {
                return false;
            }

        } else if (istring) {
            if (val.length() > maxlength) return false;
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
}
