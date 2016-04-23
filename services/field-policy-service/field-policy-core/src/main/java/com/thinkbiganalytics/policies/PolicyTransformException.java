package com.thinkbiganalytics.policies;

/**
 * Created by sr186054 on 4/21/16.
 */
public class PolicyTransformException extends Exception {

  public PolicyTransformException() {
    super();
  }

  public PolicyTransformException(String message) {
    super(message);
  }

  public PolicyTransformException(String message, Throwable cause) {
    super(message, cause);
  }

  public PolicyTransformException(Throwable cause) {
    super(cause);
  }

  protected PolicyTransformException(String message, Throwable cause, boolean enableSuppression,
                                     boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
