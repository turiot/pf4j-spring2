package demo1.app;

import demo1.api.Greeting;
import org.pf4j.PluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * A boot class that start the demo.
 *
 * @author Decebal Suiu
 */
public class Boot {
    private static final Logger log = LoggerFactory.getLogger(Boot.class);

    public static void main(String[] args) {
        try (GenericApplicationContext applicationContext = new AnnotationConfigApplicationContext(SpringConfiguration.class)) {
            log.info(">>> app start, context initialized");

            var bean1 = (Greeting) applicationContext.getBean("bean1");
            log.info(">>> appel bean1 : " + bean1.getGreeting());

            var bean2 = (Greeting) applicationContext.getBean("bean2");
            log.info(">>> appel bean2 : " + bean2.getGreeting());

            var bean3 = (Greeting) applicationContext.getBean("bean3");
            log.info(">>> appel bean3 : " + bean3.getGreeting());

            // update a plugin by moving a new version from staging directory
            var pluginManager = applicationContext.getBean(PluginManager.class);
            try (var stream = Files.list(Path.of("target/staging"))) {
                stream.forEach(from -> {
                    var to = Path.of(pluginManager.getPluginsRoots().get(0).toString(), from.getFileName().toFile().getName());
                    log.info("move {} to {}", from, to);
                    try {
                        Files.move(from, to, StandardCopyOption.ATOMIC_MOVE);
                    }
                    catch (IOException e) { log.error("ignored" ,e); }
                });
            }
            catch (IOException e) { log.error("ignored" ,e); }
            log.info("waiting for deploy");
            try { Thread.sleep(2000L); } catch (InterruptedException e) { log.debug("ignored"); }

            log.info(">>> appel bean1 : " + bean1.getGreeting());
            log.info(">>> appel bean3 : " + bean3.getGreeting());

            log.info(">>> app end");
        }
    }

}
