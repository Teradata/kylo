package com.thinkbiganalytics.kylo.catalog.credential.vault;

/*-
 * #%L
 * kylo-catalog-credential-vault
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

import com.thinkbiganalytics.kylo.catalog.credential.spi.AbstractDataSourceCredentialProvider;
import com.thinkbiganalytics.security.GroupPrincipal;
import com.thinkbiganalytics.security.UsernamePrincipal;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.security.Principal;
import java.util.HashSet;
import java.util.Set;


public class Main {

    public static void main(String[] args) {

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(VaultDataSourceCredentialConfig.class);
        context.start();

        SecretStore ss = context.getBean(SecretStore.class);

        System.out.println("ss.contains(\"oracle\")= " + ss.contains("oracle"));

        ss.remove("oracle");
        System.out.println("removed path 'oracle'");

        System.out.println("ss.contains(\"oracle\")= " + ss.contains("oracle"));

        AbstractDataSourceCredentialProvider.Credentials creds = new AbstractDataSourceCredentialProvider.Credentials();
        creds.addUserCredential("dladmin", "user", "user-dladmin", true, true);
        creds.addUserCredential("dladmin", "password", "pass-dladmin", true, true);
        creds.addGroupCredential("admin", "user", "user-admins", true, true);
        creds.addGroupCredential("admin", "password", "pass-admins", true, true);
        creds.addGroupCredential("analysts", "user", "user-analysts", true, true);
        creds.addGroupCredential("analysts", "password", "pass-analysts", true, true);
        creds.addDefaultCredential("user", "root", true, true);
        creds.addDefaultCredential("password", "thinkbig", true, true);

        ss.write("oracle", creds);
        System.out.println("ss.contains(\"oracle\")= " + ss.contains("oracle"));

        Set<Principal> principals = new HashSet<>();
        principals.add(new GroupPrincipal("analysts"));
        principals.add(new GroupPrincipal("admin"));
//        principals.add(new UsernamePrincipal("dladmin"));
        AbstractDataSourceCredentialProvider.Credentials read = ss.read("oracle", principals);
        System.out.println(read);

        context.stop();
    }
}


