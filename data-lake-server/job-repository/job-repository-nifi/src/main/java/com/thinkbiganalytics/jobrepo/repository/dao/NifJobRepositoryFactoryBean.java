/*
 * Copyright (c) 2016.
 */

package com.thinkbiganalytics.jobrepo.repository.dao;


import com.thinkbiganalytics.jobrepo.service.ExecutionContextValuesService;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.NameMatchMethodPointcut;
import org.springframework.batch.core.repository.ExecutionContextSerializer;
import org.springframework.batch.core.repository.dao.ExecutionContextDao;
import org.springframework.batch.core.repository.dao.JdbcExecutionContextDao;
import org.springframework.batch.core.repository.dao.JdbcJobExecutionDao;
import org.springframework.batch.core.repository.dao.JdbcJobInstanceDao;
import org.springframework.batch.core.repository.dao.JdbcStepExecutionDao;
import org.springframework.batch.core.repository.dao.JobExecutionDao;
import org.springframework.batch.core.repository.dao.JobInstanceDao;
import org.springframework.batch.core.repository.dao.StepExecutionDao;
import org.springframework.batch.core.repository.dao.XStreamExecutionContextStringSerializer;
import org.springframework.batch.item.database.support.DataFieldMaxValueIncrementerFactory;
import org.springframework.batch.item.database.support.DefaultDataFieldMaxValueIncrementerFactory;
import org.springframework.batch.support.DatabaseType;
import org.springframework.batch.support.PropertiesConverter;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.jdbc.support.lob.OracleLobHandler;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.sql.Types;

import javax.sql.DataSource;

/**
 * Created by sr186054 on 3/2/16.
 */
public class NifJobRepositoryFactoryBean implements FactoryBean<NifiJobRepository>, InitializingBean {


  @Autowired
  private ExecutionContextValuesService executionContextValuesService;

  private PlatformTransactionManager transactionManager;
  private ProxyFactory proxyFactory;
  private String isolationLevelForCreate = "ISOLATION_SERIALIZABLE";
  private boolean validateTransactionState = true;
  private static final String DEFAULT_ISOLATION_LEVEL = "ISOLATION_SERIALIZABLE";

  private DataSource dataSource;
  private JdbcOperations jdbcOperations;
  private String databaseType;
  private String tablePrefix = "BATCH_";
  private DataFieldMaxValueIncrementerFactory incrementerFactory;
  private int maxVarCharLength = 2500;
  private LobHandler lobHandler;
  private ExecutionContextSerializer serializer;
  private Integer lobType;

  private DataSource nifiDataSource;

  public void setNifiDataSource(DataSource nifiDataSource) {
    this.nifiDataSource = nifiDataSource;
  }

  public void setDataSource(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public void setTransactionManager(PlatformTransactionManager transactionManager) {
    this.transactionManager = transactionManager;
  }

  public void afterPropertiesSet() throws Exception {
    Assert.notNull(this.dataSource, "DataSource must not be null.");
    if (this.jdbcOperations == null) {
      this.jdbcOperations = new JdbcTemplate(this.dataSource);
    }

    if (this.incrementerFactory == null) {
      this.incrementerFactory = new DefaultDataFieldMaxValueIncrementerFactory(this.dataSource);
    }

    if (this.databaseType == null) {
      this.databaseType = DatabaseType.fromMetaData(this.dataSource).name();
      //logger.info("No database type set, using meta data indicating: " + this.databaseType);
    }

    if (this.lobHandler == null && this.databaseType.equalsIgnoreCase(DatabaseType.ORACLE.toString())) {
      this.lobHandler = new OracleLobHandler();
    }

    if (this.serializer == null) {
      XStreamExecutionContextStringSerializer defaultSerializer = new XStreamExecutionContextStringSerializer();
      defaultSerializer.afterPropertiesSet();
      this.serializer = defaultSerializer;
    }

    Assert.isTrue(this.incrementerFactory.isSupportedIncrementerType(this.databaseType),
                  "\'" + this.databaseType + "\' is an unsupported database type.  The supported database types are "
                  + StringUtils.arrayToCommaDelimitedString(this.incrementerFactory.getSupportedIncrementerTypes()));
    if (this.lobType != null) {
      Assert.isTrue(this.isValidTypes(this.lobType.intValue()), "lobType must be a value from the java.sql.Types class");
    }

    Assert.notNull(this.transactionManager, "TransactionManager must not be null.");
    this.initializeProxy();
  }

  protected JobInstanceDao createJobInstanceDao() throws Exception {
    JdbcJobInstanceDao dao = new JdbcJobInstanceDao();
    dao.setJdbcTemplate(this.jdbcOperations);
    dao.setJobIncrementer(this.incrementerFactory.getIncrementer(this.databaseType, this.tablePrefix + "JOB_SEQ"));
    dao.setTablePrefix(this.tablePrefix);
    dao.afterPropertiesSet();
    return dao;
  }

  protected JobExecutionDao createJobExecutionDao() throws Exception {
    JdbcJobExecutionDao dao = new JdbcJobExecutionDao();
    dao.setJdbcTemplate(this.jdbcOperations);
    dao.setJobExecutionIncrementer(
        this.incrementerFactory.getIncrementer(this.databaseType, this.tablePrefix + "JOB_EXECUTION_SEQ"));
    dao.setTablePrefix(this.tablePrefix);
    dao.setClobTypeToUse(this.determineClobTypeToUse(this.databaseType));
    dao.setExitMessageLength(this.maxVarCharLength);
    dao.afterPropertiesSet();
    return dao;
  }

  protected StepExecutionDao createStepExecutionDao() throws Exception {
    JdbcStepExecutionDao dao = new JdbcStepExecutionDao();
    dao.setJdbcTemplate(this.jdbcOperations);
    dao.setStepExecutionIncrementer(
        this.incrementerFactory.getIncrementer(this.databaseType, this.tablePrefix + "STEP_EXECUTION_SEQ"));
    dao.setTablePrefix(this.tablePrefix);
    dao.setClobTypeToUse(this.determineClobTypeToUse(this.databaseType));
    dao.setExitMessageLength(this.maxVarCharLength);
    dao.afterPropertiesSet();
    return dao;
  }

  protected ExecutionContextDao createExecutionContextDao() throws Exception {
    JdbcExecutionContextDao dao = new JdbcExecutionContextDao();
    dao.setJdbcTemplate(this.jdbcOperations);
    dao.setTablePrefix(this.tablePrefix);
    dao.setClobTypeToUse(this.determineClobTypeToUse(this.databaseType));
    dao.setSerializer(this.serializer);
    if (this.lobHandler != null) {
      dao.setLobHandler(this.lobHandler);
    }

    dao.afterPropertiesSet();
    dao.setShortContextLength(this.maxVarCharLength);
    return dao;
  }

  protected JobParametersDao createJobParametersDao() throws Exception {
    JobParametersDao dao = new JobParametersDao();
    dao.setJdbcTemplate(this.jdbcOperations);
    dao.setTablePrefix(this.tablePrefix);
    dao.setClobTypeToUse(this.determineClobTypeToUse(this.databaseType));
    dao.afterPropertiesSet();
    return dao;
  }

  private int determineClobTypeToUse(String databaseType) throws Exception {
    return this.lobType != null ? this.lobType.intValue()
                                : (DatabaseType.SYBASE == DatabaseType.valueOf(databaseType.toUpperCase()) ? -1 : 2005);
  }

  private boolean isValidTypes(int value) throws Exception {
    boolean result = false;
    Field[] arr$ = Types.class.getFields();
    int len$ = arr$.length;

    for (int i$ = 0; i$ < len$; ++i$) {
      Field field = arr$[i$];
      int curValue = field.getInt((Object) null);
      if (curValue == value) {
        result = true;
        break;
      }
    }

    return result;
  }


  protected NifiPipelineControllerDao createNifiPipelineControllerDao() throws Exception {
    Assert.notNull(this.nifiDataSource, "NifiDataSource must not be null.");
    NifiPipelineControllerDao nifiPipelineControllerDao = new NifiPipelineControllerDao();
    JdbcTemplate jdbcTemplate = new JdbcTemplate(nifiDataSource);
    nifiPipelineControllerDao.setJdbcTemplate(jdbcTemplate);
    return nifiPipelineControllerDao;
  }


  private void initializeProxy() throws Exception {
    if (this.proxyFactory == null) {
      this.proxyFactory = new ProxyFactory();
      TransactionInterceptor
          advice =
          new TransactionInterceptor(this.transactionManager, PropertiesConverter.stringToProperties(
              "create*=PROPAGATION_REQUIRES_NEW," + this.isolationLevelForCreate
              + "\ngetLastJobExecution*=PROPAGATION_REQUIRES_NEW," + this.isolationLevelForCreate + "\n*=PROPAGATION_REQUIRED"));
      if (this.validateTransactionState) {
        DefaultPointcutAdvisor advisor = new DefaultPointcutAdvisor(new MethodInterceptor() {
          public Object invoke(MethodInvocation invocation) throws Throwable {
            if (TransactionSynchronizationManager.isActualTransactionActive()) {
              throw new IllegalStateException(
                  "Existing transaction detected in JobRepository. Please fix this and try again (e.g. remove @Transactional annotations from client).");
            } else {
              return invocation.proceed();
            }
          }
        });
        NameMatchMethodPointcut pointcut = new NameMatchMethodPointcut();
        pointcut.addMethodName("create*");
        pointcut.addMethodName("save*");
        pointcut.addMethodName("getOrCreate*");
        advisor.setPointcut(pointcut);
        this.proxyFactory.addAdvisor(advisor);
      }

      this.proxyFactory.addAdvice(advice);
      this.proxyFactory.setProxyTargetClass(false);
      this.proxyFactory.addInterface(NifiJobRepository.class);
      this.proxyFactory.setTarget(this.getTarget());
    }

  }


  private Object getTarget() throws Exception {
    return new NifiSimpleJobRepository(this.createJobInstanceDao(), this.createJobExecutionDao(), this.createStepExecutionDao(),
                                       this.createExecutionContextDao(), this.createJobParametersDao(),
                                       this.createNifiPipelineControllerDao(), this.executionContextValuesService);
  }

  public NifiJobRepository getObject() throws Exception {
    if (this.proxyFactory == null) {
      this.afterPropertiesSet();
    }

    return (NifiJobRepository) this.proxyFactory.getProxy();
  }

  @Override
  public Class<?> getObjectType() {
    return NifiJobRepository.class;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }
}
