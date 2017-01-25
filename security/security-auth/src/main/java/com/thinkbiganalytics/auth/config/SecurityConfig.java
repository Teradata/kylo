/**
 * 
 */
package com.thinkbiganalytics.auth.config;

/*-
 * #%L
 * thinkbig-security-auth
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

import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Base security configuration.
 * 
 * @author Sean Felten
 */
@Configuration
public class SecurityConfig {

    @Bean
    @ConfigurationProperties(prefix="security.password.encoder")
    public PasswordEncoderFactory passwordEncoderFactory() {
        return new PasswordEncoderFactory();
    }
    
    
    protected static class PasswordEncoderFactory extends AbstractFactoryBean<PasswordEncoder> {
        
        enum Encoding { PLAIN, BCRYPT  }
        
        private Encoding encoding = Encoding.BCRYPT;
        
        public void setEncoding(String encodingStr) {
            this.encoding = Encoding.valueOf(encodingStr.toUpperCase());
        }

        /* (non-Javadoc)
         * @see org.springframework.beans.factory.config.AbstractFactoryBean#getObjectType()
         */
        @Override
        public Class<?> getObjectType() {
            return PasswordEncoder.class;
        }

        /* (non-Javadoc)
         * @see org.springframework.beans.factory.config.AbstractFactoryBean#createInstance()
         */
        @Override
        protected PasswordEncoder createInstance() throws Exception {
            switch (this.encoding) {
                case BCRYPT:
                    return new BCryptPasswordEncoder(10);
                case PLAIN:
                    return NoOpPasswordEncoder.getInstance();
                default:
                    return new BCryptPasswordEncoder(10);
            }
        }
    }
}
