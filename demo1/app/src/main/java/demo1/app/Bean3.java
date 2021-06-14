package demo1.app;

import demo1.api.Greeting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Bean3 implements Greeting {

    @Autowired
    public Greeting bean2;

    @Override
    public String getGreeting() {
        return bean2.getGreeting()+" (called by plugin Bean3)";
    }

}
