/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.validation;

import com.thinkbiganalytics.com.thinkbiganalytics.validation.CreditCardValidator;
import com.thinkbiganalytics.com.thinkbiganalytics.validation.IPAddressValidator;
import com.thinkbiganalytics.com.thinkbiganalytics.validation.TimestampValidator;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class ValidatorTests {

  @Test
  public void timestampTests() {

    TimestampValidator validator = TimestampValidator.instance();
    Assert.assertTrue(validator.validate("2004-12-01 12:00:00"));
    Assert.assertTrue(validator.validate("2004-12-01T12:00:00"));
    Assert.assertTrue(validator.validate("2004-12-01T12:00:00.124Z"));
    Assert.assertTrue(validator.validate("2009-06-18T18:50:57-06:00"));
    Assert.assertTrue(validator.validate("2009-06-18T18:30:45Z"));
    Assert.assertTrue(validator.validate("2009-06-18T18:39Z"));
    Assert.assertTrue(validator.validate("2001-01-12"));
    Assert.assertTrue(validator.validate("1984-10-16"));

    Assert.assertFalse(validator.validate("2004-1-1 12:00:00"));
    Assert.assertFalse(validator.validate("2004-12-0112:00:00"));
    Assert.assertFalse(validator.validate("20041201120000"));
  }


  @Test
  public void ipAddressTests() {
    IPAddressValidator validator = IPAddressValidator.instance();
    Assert.assertTrue(validator.validate("106.72.28.74"));
    Assert.assertTrue(validator.validate("156.243.130.166"));
    Assert.assertTrue(validator.validate("28.55.168.128"));
    Assert.assertTrue(validator.validate("185.81.160.85"));
    Assert.assertTrue(validator.validate("158.137.238.6"));
    Assert.assertTrue(validator.validate("141.122.136.144"));
    Assert.assertTrue(validator.validate("104.179.97.82"));
    Assert.assertTrue(validator.validate("28.77.158.48"));
    Assert.assertTrue(validator.validate("72.129.239.24"));
    Assert.assertTrue(validator.validate("51.211.70.30"));

    Assert.assertFalse(validator.validate("51.211.70"));
    Assert.assertFalse(validator.validate("51..211.70.30"));
    Assert.assertFalse(validator.validate("a1.211.70.30"));
    Assert.assertFalse(validator.validate("51.211.70.30.0"));
  }

  @Test
  public void creditCardTests() {
    CreditCardValidator validator = CreditCardValidator.instance();

    Assert.assertTrue(validator.validate("4508242795214770"));
    Assert.assertTrue(validator.validate("3534550235909500"));
    Assert.assertTrue(validator.validate("3563436733386890"));

    Assert.assertTrue(validator.validate("30485245023962"));

    Assert.assertTrue(validator.validate("3559979696602300"));
    Assert.assertTrue(validator.validate("3546330084792460"));
    Assert.assertTrue(validator.validate("3571014044514510"));
    Assert.assertTrue(validator.validate("30166467912021"));
    Assert.assertTrue(validator.validate("4074771539744790"));
    Assert.assertTrue(validator.validate("374283138983226"));
    Assert.assertTrue(validator.validate("5100145505218790"));
    Assert.assertTrue(validator.validate("30501574577558"));

    Assert.assertFalse(validator.validate("676306013856639000"));
    Assert.assertFalse(validator.validate("5018278895598920000"));
    Assert.assertFalse(validator.validate("5602249431899030"));
    Assert.assertFalse(validator.validate("5002353015111220"));
    Assert.assertFalse(validator.validate("6771208405057810000"));

  }


}
