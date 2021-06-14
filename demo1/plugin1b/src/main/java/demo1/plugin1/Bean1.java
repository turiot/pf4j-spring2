package demo1.plugin1;

import demo1.api.Greeting;
import org.springframework.stereotype.Component;

/**
 * @author Decebal Suiu
 */
@Component("bean1")
public class Bean1 implements Greeting {

    @Override
    public String getGreeting() {
        return "Welcome from plugin Bean1_UPDATED";
    }

}
