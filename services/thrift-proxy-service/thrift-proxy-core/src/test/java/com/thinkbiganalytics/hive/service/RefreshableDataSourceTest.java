package com.thinkbiganalytics.hive.service;

import com.thinkbiganalytics.kerberos.KerberosTicketConfiguration;
import com.thinkbiganalytics.kerberos.KerberosUtil;
import com.thinkbiganalytics.scheduler.util.CronExpressionUtil;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import javax.sql.DataSource;

@RunWith(PowerMockRunner.class)
@PrepareForTest(RefreshableDataSource.class)
public class RefreshableDataSourceTest {



    @Mock
    Environment env;

    @Mock
    SecurityContextHolder contextHolder;

    @Mock
    SecurityContext securityContext;

    @Mock
    Authentication authentication;

    @Mock
    KerberosUtil kerberosUtil;

    @Mock
    KerberosTicketConfiguration kerberosTicketConfiguration;

    private RefreshableDataSource hiveDs;
    private String principal = "AwesomeUser";


    @Before
    public void init(){
        env = Mockito.mock(Environment.class);
        securityContext = Mockito.mock(SecurityContext.class);
        authentication = Mockito.mock(Authentication.class);
        contextHolder = Mockito.mock(SecurityContextHolder.class);
        kerberosTicketConfiguration = Mockito.mock(KerberosTicketConfiguration.class);
        kerberosUtil = Mockito.mock(KerberosUtil.class);

        Mockito.when(env.getProperty("hive.userImpersonation.enabled"))
            .thenReturn("true");

        Mockito.when(env.getProperty("hive.datasource.driverClassName"))
            .thenReturn("org.apache.hive.jdbc.HiveDriver");

        Mockito.when(env.getProperty("hive.datasource.url"))
            .thenReturn("jdbc:hive2://localhost:10000/default");


        Mockito.when(env.getProperty("hive.datasource.username"))
            .thenReturn(principal);

        Mockito.when(env.getProperty("hive.datasource.password"))
            .thenReturn("password1234");




        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        Mockito.when(authentication.getName()).thenReturn(principal);


        Mockito.when(kerberosTicketConfiguration.isKerberosEnabled()).thenReturn(false);

        hiveDs = new RefreshableDataSource("hive.datasource");
        hiveDs.env = env;

    }

    private void initUserNameMocks(String user, String userNameCase){
        Mockito.when(env.getProperty("hive.datasource.username"))
            .thenReturn(user);

        Mockito.when(env.getProperty("hive.datasource.username.case"))
            .thenReturn(userNameCase);
        Mockito.when(env.getProperty(Mockito.eq("hive.datasource.username.case"),Mockito.anyString()))
            .thenReturn(userNameCase);

        Mockito.when(env.getProperty("hive.server2.proxy.user.case"))
            .thenReturn(userNameCase);
        Mockito.when(env.getProperty(Mockito.eq("hive.server2.proxy.user.case"),Mockito.anyString()))
            .thenReturn(userNameCase);

    }

    @Test
    public void testUpperCase() throws Exception {

        String hiveUser = principal;
        initUserNameMocks(hiveUser, "UPPER_CASE");
        Map<String,String> props = testCreateDataSourceAndGetProperties();
        String url = props.get("url");
        Assert.assertTrue(("jdbc:hive2://localhost:10000/default;hive.server2.proxy.user="+principal.toUpperCase()).equals(url));

    }

    @Test
    public void testLowerCase() throws Exception {

        String hiveUser = principal.toLowerCase();
        initUserNameMocks(hiveUser, "LOWER_CASE");
        Map<String,String> props = testCreateDataSourceAndGetProperties();
        String url = props.get("url");
        Assert.assertTrue(("jdbc:hive2://localhost:10000/default;hive.server2.proxy.user="+principal.toLowerCase()).equals(url));

    }

    @Test
    public void testAsSpecified() throws Exception {

        String hiveUser = principal.toLowerCase();
        initUserNameMocks(hiveUser, "AS_SPECIFIED");
        Map<String,String> props = testCreateDataSourceAndGetProperties();
        String url = props.get("url");
        Assert.assertTrue(("jdbc:hive2://localhost:10000/default;hive.server2.proxy.user="+principal).equals(url));

    }
    private Map<String,String> testCreateDataSourceAndGetProperties() throws Exception{
        DataSource ds = Whitebox.invokeMethod(hiveDs, "create", true, principal);
        Map<String, String> props = Arrays.stream(ds.toString().split("; ")).collect(Collectors.toMap(s -> StringUtils.substringBefore(s, "="), s -> StringUtils.substringAfter(s, "=")));
        return props;
    }



}
