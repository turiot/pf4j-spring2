package demo2.plugin1;

import demo2.api.Greeting;
import org.springframework.stereotype.Component;

/**
 * @author Decebal Suiu
 */
@Component("bean1")
public class Bean1 implements Greeting {

    @Override
    public String getGreeting() {
        return "Welcome from plugin Bean1";
    }

}
