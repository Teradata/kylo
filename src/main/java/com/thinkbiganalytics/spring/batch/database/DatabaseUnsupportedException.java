package com.thinkbiganalytics.spring.batch.database;

/**
 * Created by sr186054 on 9/16/15.
 */
public class DatabaseUnsupportedException extends RuntimeException {


    private String message;

    public DatabaseUnsupportedException() {
        initMessage();
    }

    public DatabaseUnsupportedException(Throwable cause) {
        super(cause);
      initMessage();
    }
    private void initMessage(){
        message = "Pipeline Controller only supports mysql or Postgres Db.";
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getLocalizedMessage() {
        return message;
    }
}
