package com.thinkbiganalytics.test;

/*-
 * #%L
 * kylo-commons-test
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.internal.mapping.Jackson2Mapper;
import com.jayway.restassured.mapper.factory.Jackson2ObjectMapperFactory;
import com.jayway.restassured.specification.RequestSpecification;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.optional.ssh.SSHExec;
import org.apache.tools.ant.taskdefs.optional.ssh.Scp;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Superclass for all functional tests.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {FunctionalTestConfig.class})
public class FunctionalTest {

    private static final Logger LOG = LoggerFactory.getLogger(FunctionalTest.class);

    @Autowired
    private KyloConfig kyloConfig;

    @Autowired
    private SshConfig sshConfig;

    @Before
    public void setupRestAssured() {
        RestAssured.baseURI = kyloConfig.getHost();
        RestAssured.port = kyloConfig.getPort();
        RestAssured.basePath = kyloConfig.getBasePath();

        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        Jackson2ObjectMapperFactory factory = (aClass, s) -> {
            ObjectMapper om = new ObjectMapper();
            om.registerModule(new JodaModule());
            om.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
            om.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

            configureObjectMapper(om);

            return om;
        };
        com.jayway.restassured.mapper.ObjectMapper objectMapper = new Jackson2Mapper(factory);
        RestAssured.objectMapper(objectMapper);
    }

    /**
     * Do nothing implementation, but subclasses may override to add extra
     * json serialisation/de-serialisation modules
     */
    protected void configureObjectMapper(ObjectMapper om) {

    }

    protected RequestSpecification given() {
        return given(null);
    }

    protected RequestSpecification given(String base) {
        if (base != null) {
            RestAssured.basePath = kyloConfig.getBasePath() + base;
        }

        return RestAssured.given()
            .log().method().log().path()
            .auth().preemptive().basic(kyloConfig.getUsername(), kyloConfig.getPassword())
            .contentType("application/json");
    }

    protected final void scp(final String localFile, final String remoteDir) {
        Scp scp = new Scp() {
            @Override
            public String toString() {
                return String.format("scp -P%s %s %s@%s:%s", sshConfig.getPort(), localFile, sshConfig.getUsername(), sshConfig.getHost(), remoteDir);
            }
        };
        scp.setTrust(true);
        scp.setProject(new Project());
        scp.setKnownhosts(sshConfig.getKnownHosts());
        scp.setVerbose(true);
        scp.setHost(sshConfig.getHost());
        scp.setPort(sshConfig.getPort());
        scp.setUsername(sshConfig.getUsername());
        scp.setPassword(sshConfig.getPassword());
        scp.setLocalFile(localFile);
        scp.setTodir(String.format("%s@%s:%s", sshConfig.getUsername(), sshConfig.getHost(), remoteDir));
        LOG.info(scp.toString());
        scp.execute();
    }

    protected final String ssh(final String command) {
        SSHExec ssh = new SSHExec() {
            @Override
            public String toString() {
                return String.format("ssh -p %s %s@%s %s", sshConfig.getPort(), sshConfig.getUsername(), sshConfig.getHost(), command);
            }
        };
        ssh.setTrust(true);
        Project project = new Project();
        ssh.setProject(project);
        ssh.setOutputproperty("output");
        ssh.setKnownhosts(sshConfig.getKnownHosts());
        ssh.setHost(sshConfig.getHost());
        ssh.setPort(sshConfig.getPort());
        ssh.setUsername(sshConfig.getUsername());
        ssh.setPassword(sshConfig.getPassword());
        ssh.setCommand(command);
        LOG.info(ssh.toString());
        ssh.execute();
        return project.getProperty("output");
    }


}
