/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.com.thinkbiganalytics.validation;


import com.thinkbiganalytics.policies.validation.FieldValidator;
import com.thinkbiganalytics.policies.validation.Validator;

/**
 * Validates credit card number matches pattern
 */
/*
^(?:4[0-9]{12}(?:[0-9]{3})?          # Visa
        |  5[1-5][0-9]{14}                  # MasterCard
        |  3[47][0-9]{13}                   # American Express
        |  3(?:0[0-5]|[68][0-9])[0-9]{11}   # Diners Club
        |  6(?:011|5[0-9]{2})[0-9]{12}      # Discover
        |  (?:2131|1800|35\d{3})\d{11}      # JCB
        )$
http://www.regular-expressions.info/creditcard.html
*/
@FieldValidator(name = "Credit card", description = "Valid credit card")
public class CreditCardValidator extends RegexValidator implements Validator<String> {

  private static final CreditCardValidator instance = new CreditCardValidator();

  private CreditCardValidator() {
    super(
        "^(?:4[0-9]{12}(?:[0-9]{3})?|5[1-5][0-9]{14}|3[47][0-9]{13}|3(?:0[0-5]|[68][0-9])[0-9]{11}||6(?:011|5[0-9]{2})[0-9]{12}|(?:2131|1800|35\\d{3})\\d{11})$");
  }

  public static CreditCardValidator instance() {
    return instance;
  }

}




