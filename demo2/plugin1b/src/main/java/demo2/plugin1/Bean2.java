package demo2.plugin1;

import demo2.api.Greeting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Decebal Suiu
 */
@Service
public class Bean2 implements Greeting {

    @Autowired
    public Greeting bean1;

    @Override
    public String getGreeting() {
        return bean1.getGreeting()+" (called by plugin Bean2_UPDATED)";
    }

}
