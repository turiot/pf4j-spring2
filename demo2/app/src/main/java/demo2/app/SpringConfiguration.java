package demo2.app;

import org.pf4j.spring2.FileWatcher;
import org.pf4j.spring2.SpringPluginManager;
import org.pf4j.spring2.UpdateWatcher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Decebal Suiu
 */
@Configuration
public class SpringConfiguration {

    @Bean
    public UpdateWatcher updateWatcher() {
        return new FileWatcher();
    }

    @Bean
    public SpringPluginManager pluginManager() {
        return new SpringPluginManager(updateWatcher());
    }

}
