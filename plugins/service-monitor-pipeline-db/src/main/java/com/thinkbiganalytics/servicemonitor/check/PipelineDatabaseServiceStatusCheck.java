package com.thinkbiganalytics.servicemonitor.check;


import com.thinkbiganalytics.servicemonitor.db.dao.DatabaseServiceCheckDao;
import com.thinkbiganalytics.servicemonitor.model.ServiceStatusResponse;

import javax.inject.Inject;

/**
 * Created by sr186054 on 9/30/15.
 */

public class PipelineDatabaseServiceStatusCheck implements ServiceStatusCheck {

  @Inject
  DatabaseServiceCheckDao databaseServiceCheckDao;


  @Override
  public ServiceStatusResponse healthCheck() {

    return databaseServiceCheckDao.healthCheck();


  }

  protected void setDatabaseServiceCheckDao(DatabaseServiceCheckDao databaseServiceCheckDao) {
    this.databaseServiceCheckDao = databaseServiceCheckDao;
  }
}
