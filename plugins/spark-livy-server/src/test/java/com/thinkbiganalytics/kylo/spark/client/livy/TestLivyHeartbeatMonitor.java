package com.thinkbiganalytics.kylo.spark.client.livy;

/*-
 * #%L
 * kylo-spark-livy-server
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics, a Teradata Company
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


import com.google.common.collect.Lists;
import com.thinkbiganalytics.kylo.spark.client.DefaultLivyClient;
import com.thinkbiganalytics.kylo.spark.client.LivyClient;
import com.thinkbiganalytics.kylo.spark.client.model.LivyServer;
import com.thinkbiganalytics.kylo.spark.client.model.enums.LivyServerStatus;
import com.thinkbiganalytics.kylo.spark.config.LivyProperties;
import com.thinkbiganalytics.kylo.spark.livy.SparkLivyProcess;
import com.thinkbiganalytics.kylo.spark.model.Session;
import com.thinkbiganalytics.kylo.spark.model.enums.SessionState;
import com.thinkbiganalytics.rest.JerseyRestClient;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.net.SocketTimeoutException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Resource;
import javax.net.ssl.SSLHandshakeException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestLivyHeartbeatMonitor.Config.class},
                      loader = AnnotationConfigContextLoader.class)
@TestPropertySource
@ActiveProfiles("kylo-livy")
public class TestLivyHeartbeatMonitor {

    private static final Logger logger = LoggerFactory.getLogger(TestLivyHeartbeatMonitor.class);

    @Resource
    private LivyServer livyServer;

    @Resource
    private LivyClient livyClient;

    @Resource
    private LivyProperties livyProperties;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    public LivyHeartbeatMonitor livyHeartbeatMonitor() {
        LivyClient mockLivyClient = Mockito.mock(LivyClient.class);

        Session sessionNotStarted = new Session.Builder().id(1).state(SessionState.not_started).build();
        Session sessionStarting = new Session.Builder().id(1).state(SessionState.starting).build();
        Session sessionIdle = new Session.Builder().id(1).state(SessionState.idle).build();
        Session sessioShuttingDown = new Session.Builder().id(1).state(SessionState.shutting_down).build();

        final List<Session> answers = Lists.newArrayList(sessionNotStarted, sessionStarting, sessionIdle, sessioShuttingDown);

        final AtomicInteger numResponse = new AtomicInteger(0);

        JerseyRestClient client = Mockito.mock(JerseyRestClient.class);
        Mockito.when(client.get(Mockito.anyString(), Mockito.eq(Session.class))).thenAnswer(new Answer<Session>() {
            @Override
            public Session answer(InvocationOnMock invocation) throws Throwable {
                try {
                    if (numResponse.get() == 3 || numResponse.get() == 8) {
                        // third response and second to last response is session not_found
                        throw new WebApplicationException("Can't find session", 404);
                    }

                    // Now get from our list of Sessions with certain states
                    Session session = answers.remove(0);
                    logger.debug("Returning mock response for session with id='{}' and state='{}'", session.getId(), session.getState());
                    return session;
                } catch (IndexOutOfBoundsException e) {
                    // used up our list of known responses, pretend the server doesn't know the session anymore
                    throw new WebApplicationException("Can't find session", 404);
                }
            }
        });

        Mockito.when(mockLivyClient.getSession(Mockito.any(), Mockito.any(SparkLivyProcess.class))).thenAnswer(invocation -> {
            int responseNum = numResponse.addAndGet(1);
            logger.debug("Number of responses return from mockLivyClient:: numResponse={}", responseNum);

            if (responseNum == 1 || responseNum >= 9) {
                // first response and last response is server not started
                throw new ProcessingException(new SocketTimeoutException("Server not started"));
            }
            if (responseNum == 2) {
                // second response is server not HTTPS
                throw new ProcessingException(new SSLHandshakeException("Server is not HTTPS"));
            }

            return livyClient.getSession(client, (SparkLivyProcess) invocation.getArguments()[1]);
        });

        return new LivyHeartbeatMonitor(mockLivyClient, client, livyServer, livyProperties);
    }

    /**
     * Tests the heartbeat thread will produce the expected outcomes at the expected intervals
     */
    @Test
    @Ignore("ignored due to a time component of the tests, slower systems may fail")
    public void testHearbeat() throws InterruptedException {
        LivyHeartbeatMonitor livyHeartbeatMonitor = livyHeartbeatMonitor();

        // 1. start a session...   through startLivySession.startLivySession, which will now delegate to LivyClient
        SparkLivyProcess sparkProcess = Mockito.mock(SparkLivyProcess.class);
        assertThat(sparkProcess).isNotNull();

        // DO ONE:
        //sparkLivyProcessManager.start(sparkProcess);
        Integer sessionId = 1;

        livyHeartbeatMonitor.monitorSession(sparkProcess);
        assertThat(livyServer.getLivyServerStatus()).isEqualTo(LivyServerStatus.not_found);

        Thread.sleep(1015);

        logger.debug("Server not found!  SocketTimeout");
        Thread.sleep(115);
        assertThat(livyServer.getLivyServerStatus()).isEqualTo(LivyServerStatus.not_found);

        logger.debug("Server not found!  Not Https");
        Thread.sleep(415);
        assertThat(livyServer.getLivyServerStatus()).isEqualTo(LivyServerStatus.not_found);

        logger.debug("session not found");
        Thread.sleep(1215);
        assertThat(livyServer.getLivyServerStatus()).isEqualTo(LivyServerStatus.alive);
        assertThat(livyServer.getLivySessionState(sessionId)).isNull();

        logger.debug("session found, but not started");
        Thread.sleep(1015);
        assertThat(livyServer.getLivyServerStatus()).isEqualTo(LivyServerStatus.alive);
        assertThat(livyServer.getLivySessionState(sessionId)).isEqualTo(SessionState.not_started);

        logger.debug("session found, and starting");
        Thread.sleep(515);
        assertThat(livyServer.getLivyServerStatus()).isEqualTo(LivyServerStatus.alive);
        assertThat(livyServer.getLivySessionState(sessionId)).isEqualTo(SessionState.starting);

        logger.debug("session found, and idle");
        Thread.sleep(515);
        assertThat(livyServer.getLivyServerStatus()).isEqualTo(LivyServerStatus.alive);
        assertThat(livyServer.getLivySessionState(sessionId)).isEqualTo(SessionState.idle);

        logger.debug("session found, and shutting down");
        Thread.sleep(515);
        assertThat(livyServer.getLivyServerStatus()).isEqualTo(LivyServerStatus.alive);
        assertThat(livyServer.getLivySessionState(sessionId)).isEqualTo(SessionState.shutting_down);

        logger.debug("session NOT found");
        Thread.sleep(515);
        assertThat(livyServer.getLivyServerStatus()).isEqualTo(LivyServerStatus.alive);
        assertThat(livyServer.getLivySessionState(sessionId)).isNull();

        logger.debug("server not alive");
        Thread.sleep(6015);
        assertThat(livyServer.getLivyServerStatus()).isEqualTo(LivyServerStatus.not_found);
        assertThat(livyServer.getLivySessionState(sessionId)).isNull();

        // 2. Loop for max number seconds waiting for session to be ready..
        //     only checking livyServerState
        return;
    }

    @Configuration
    @EnableConfigurationProperties
    @ActiveProfiles("kylo-livy")
    static class Config {

        @Bean
        public static PropertySourcesPlaceholderConfigurer propertyConfigInDev() {
            return new PropertySourcesPlaceholderConfigurer();
        }

        @Bean
        public LivyServer livyServer() {
            return new LivyServer("someHost", 8998);
        }

        @Bean
        public LivyClient livyClient() {
            return new DefaultLivyClient(livyServer(), livyProperties());
        }

        @Bean
        @ConfigurationProperties("spark.livy")
        public LivyProperties livyProperties() {
            return new LivyProperties();
        }
    }
}
