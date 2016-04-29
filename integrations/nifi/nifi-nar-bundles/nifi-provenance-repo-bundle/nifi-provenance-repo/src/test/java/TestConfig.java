import org.junit.Ignore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;

/**
 * Created by sr186054 on 2/26/16.
 */
// Ignore due to dependency on MySQL
@Ignore
@Configuration
@ComponentScan(basePackages = {"com.thinkbiganalytics"})
@PropertySource("classpath:config.properties")
public class TestConfig {

    @Autowired
    private Environment env;

    @Bean
    public PropertyPlaceholderConfigurer propConfig() {
        PropertyPlaceholderConfigurer placeholderConfigurer = new PropertyPlaceholderConfigurer();
        placeholderConfigurer.setLocation(new ClassPathResource("config.properties"));
        return placeholderConfigurer;
    }

}
