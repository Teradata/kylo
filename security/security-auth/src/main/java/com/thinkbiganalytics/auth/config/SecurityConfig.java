/**
 * 
 */
package com.thinkbiganalytics.auth.config;

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
