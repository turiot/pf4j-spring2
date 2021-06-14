package demo2.app;

import demo2.api.Greeting;
import demo2.api.Interface;
import demo2.api.Model;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.pf4j.PluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * A boot class that start the demo.
 *
 * @author Decebal Suiu
 */
@SpringBootApplication
public class Boot {
    private static final Logger log = LoggerFactory.getLogger(Boot.class);

    public static void main(String[] args) {
        try (var ctx = SpringApplication.run(Boot.class, args)) {
			/*
			log.info(">>> app start, context initialized");
			var bean1 = (Greeting) ctx.getBean("bean1");
			log.info(">>> appel bean1 : " + bean1.getGreeting());
			var bean2 = (Greeting) ctx.getBean("bean2");
			log.info(">>> appel bean2 : " + bean2.getGreeting());
			var bean3 = (Greeting) ctx.getBean("bean3");
			log.info(">>> appel bean3 : " + bean3.getGreeting());
			// update a plugin by moving a new version from staging directory
			var pluginManager = ctx.getBean(PluginManager.class);
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
			*/
			
			// TODO Interface dao = ctx.getBean(Interface.class);
			var dao = (Interface) ctx.getBean("dao");
		
			var model = new Model();
			model.key = "clef1";
			model.value = "value1";
			dao.save(model);
			model = dao.get("clef1");
			log.info("relu {}", model.value);
			
			log.info(">>> app end, closing context");
			ctx.stop();
        }
    }

}
