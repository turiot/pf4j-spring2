package org.pf4j.spring2;

import org.pf4j.DefaultPluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.GenericApplicationContext;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Supplier;

/**
 * @author Thierry Uriot
 */
public class SpringPluginManager extends DefaultPluginManager implements ApplicationContextAware {

    private static final Logger log = LoggerFactory.getLogger(SpringPluginManager.class);

    public static final String SPRING_IDX = "META-INF/spring.idx";

    private ApplicationContext applicationContext;
	
    private final UpdateWatcher updateWatcher;

    public SpringPluginManager(UpdateWatcher updateWatcher) {
		this.updateWatcher = updateWatcher;
		this.updateWatcher.setSpringPluginManager(this);
	}

    public SpringPluginManager(UpdateWatcher updateWatcher, Path pluginsRoot) {
        super(pluginsRoot);
		this.updateWatcher = updateWatcher;
		this.updateWatcher.setSpringPluginManager(this);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * This method load plugins and inject components in Spring
     */
    @PostConstruct
    public void init() {
        loadPlugins();
        log.info("plugins loaded");

        plugins.forEach((key, plugin) -> {
            var classLoader = plugin.getPluginClassLoader();
            var pluginId = plugin.getDescriptor().getPluginId();
            log.info("Register spring components from plugin '{}'", pluginId);
            registerPluginBeans(plugin, classLoader);
        });
        log.info("beans registered, init complete");

        new Thread(updateWatcher).start();
    }
    @PreDestroy
    public void stop() {
        log.info("stopping pluginManager");
        updateWatcher.stop();
    }

    public void update(Path path) throws UpdateException {
        var pluginId = loadPlugin(path);

        var plugin = getPlugin(pluginId);
        var classLoader = plugin.getPluginClassLoader();
        log.info("Register spring components from updated plugin '{}'", pluginId);
        try {
            registerPluginBeans(plugin, classLoader);
        }
        catch (UpdateException e) {
            log.error("Update problem, abort");
            throw e;
        }

        var old = getPlugins()
                .stream()
                .filter(p -> p.getPluginId().split("-")[0].equals(pluginId.split("-")[0]))
                .findFirst();
        if (old.isPresent()) {
            var oldPlugin = old.get();
            log.info("Delete plugin : {}", oldPlugin.getPluginId());
            deletePlugin(oldPlugin.getPluginId());
        }
    }

    private void registerPluginBeans(org.pf4j.PluginWrapper plugin, ClassLoader classLoader) {
        var beans = new ArrayList<String>(5);
        try {
            var pluginClassLoader = plugin.getPluginClassLoader();
            try (var resourceStream = pluginClassLoader.getResourceAsStream(SPRING_IDX)) {
                if (resourceStream == null) {
                    log.info("Cannot find '{}'", SPRING_IDX);
                } else {
                    collectBeans(resourceStream, beans);
                }
            }
        }
        catch (IOException e) {
            log.error(e.getMessage(), e);
        }

        beans.forEach(className -> {
            try {
                var classBean = classLoader.loadClass(className);
                GenericBeanDefinition beanDef;
                try {
                    beanDef = (GenericBeanDefinition) ((GenericApplicationContext) applicationContext).getBeanDefinition(classBean.getSimpleName().toLowerCase());
                    log.info("*** Updating bean '{}' from class {}", classBean.getSimpleName().toLowerCase(), classBean.getName());
                    var supplier = (HotBeanProxyFactorySupplier<?>) beanDef.getInstanceSupplier();
                    if (supplier == null)
                        throw new UpdateException(plugin.getPluginId(), "supplier not found");
//                    var hotBeanProxyFactory = supplier.hotBeanProxyFactory;
					// wxx
                    var handler = supplier.handler;
                    if (handler == null)
                        throw new UpdateException(plugin.getPluginId(), "hotBeanProxyFactory not found");
                    var beanFactory = applicationContext.getAutowireCapableBeanFactory();
                    log.debug("Instantiate bean class '" + classBean.getName() + "' by using constructor autowiring.");
                    var autowiredBean = beanFactory.autowire(classBean, AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR, false);
                    log.debug("Completing autowiring (setters) of bean " + autowiredBean);
                    beanFactory.autowireBean(autowiredBean);
//                    hotBeanProxyFactory.updateBean(autowiredBean);
					// wxx
                    handler.currentBean = autowiredBean;
                    log.debug("Bean proxied updated " + autowiredBean);
                }
                catch (NoSuchBeanDefinitionException e) {
                    log.info("*** Registring bean '{}' from class {}", classBean.getSimpleName().toLowerCase(), classBean.getName());
                    beanDef = new GenericBeanDefinition();
                    beanDef.setBeanClass(classBean);
log.info("wx----------"+classBean.getClassLoader().getName()+" "+classBean.getClassLoader());
var cl = (org.pf4j.PluginClassLoader) classBean.getClassLoader();
log.info("wx----------"+classBean.getInterfaces().length);
Arrays.stream(classBean.getInterfaces()).forEach(i -> log.info(i.getCanonicalName()));
                    Supplier<?> supplier = new HotBeanProxyFactorySupplier<>(classBean.getSimpleName(), classBean, classBean.getInterfaces(), applicationContext);
                    beanDef.setLazyInit(true);
                    beanDef.setInstanceSupplier(supplier);
                    ((GenericApplicationContext) applicationContext).registerBeanDefinition(classBean.getSimpleName().toLowerCase(), beanDef);
                }
            }
            catch (ClassNotFoundException e) {
                throw new UpdateException(plugin.getPluginId(), "class not found in plugin " + className);
            }
        });
    }

    private void collectBeans(InputStream inputStream, ArrayList<String> bucket) throws IOException {
        try (Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            try (var bufferedReader = new BufferedReader(reader)) {
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    if (!line.isEmpty()) {
                        bucket.add(line);
                    }
                }
            }
        }
    }

}
