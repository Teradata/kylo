package com.thinkbiganalytics.kylo.spark.client.jersey;

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


/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// NOTE: take from Apache Nifi project at https://github.com/apache/nifi/blob/a1794b101ea843410606b65bbb75fd8bc87ccd39/nifi-nar-bundles/nifi-extension-utils/nifi-hadoop-utils/src/main/java/org/apache/nifi/hadoop/KerberosKeytabCredentials.java

import org.apache.http.auth.Credentials;

import javax.security.auth.kerberos.KerberosPrincipal;
import java.security.Principal;

/**
 * Crendentials that incorporate a user principal and a keytab file.
 */
public class KerberosKeytabCredentials implements Credentials {

    private final KerberosPrincipal userPrincipal;
    private final String keytab;

    public KerberosKeytabCredentials(String principalName, String keytab) {
        this.userPrincipal = new KerberosPrincipal(principalName);
        this.keytab = keytab;
    }

    @Override
    public Principal getUserPrincipal() {
        return userPrincipal;
    }

    @Override
    public String getPassword() {
        return null;
    }

    public String getKeytab() {
        return keytab;
    }

}
