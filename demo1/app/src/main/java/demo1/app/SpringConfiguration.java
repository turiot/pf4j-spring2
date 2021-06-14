package demo1.app;

import demo1.api.Greeting;
import org.pf4j.spring2.FileWatcher;
import org.pf4j.spring2.SpringPluginManager;
import org.pf4j.spring2.UpdateWatcher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

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

    @Bean
    @DependsOn("pluginManager")
    public Greeting bean3() {
        return new Bean3();
    }

}
