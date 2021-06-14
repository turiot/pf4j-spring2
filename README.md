PF4J - Spring Framework integration (POC 2)
=====================

This project is a proof of concept showing how you can integrate [PF4J](https://github.com/pf4j/pf4j) with Spring Framework.

The difference with pf4j-spring are :
- no need for plugin class nor extension (__transparent__ modularization)
- __update__ of plugged beans without stop-starting the running application

In fact PF4J brings a modular classloader, with facultative extensions and lifecycle.

Lightweight (around 12KB) extension for PF4J, with minimal dependencies (only pf4j).

Thanks to HotBeans (https://github.com/tolo/HotBeans) for the proxy code.

Components
----------
- **AnnotationProcessor** a compile time scanning of Spring beans
- **SpringPluginManager** a Spring aware PluginManager registering beans wrapped in a reloadable proxy
- **FileWatcher** pluggable monitor to update plugins

How to use
----------
Create a PF4J plugin as usual, containing Spring beans, but use an annotation processor in maven.
```xml
        <annotationProcessors>
            <annotationProcessor>org.pf4j.spring2.apt.AnnotationProcessor</annotationProcessor>
        </annotationProcessors>
```

Create the Spring configuration (declare some beans) using annotations with:
```java
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
```
Bean3 depends on Bean2 present in plugin1, wich depends on Bean1, also in plugin1.\
Non plugged beans must either be declared __@Lazy__ or @DependOn("pluginManager").

Start your application (plain java code):
```java
    try (GenericApplicationContext applicationContext = new AnnotationConfigApplicationContext(SpringConfiguration.class)) {

```

Consume the beans injected from plugins :
```java
        var bean3 = (Greeting) applicationContext.getBean("bean3");
        log.info(">>> appel bean3 : " + bean3.getGreeting());
```

The output is :
```
[INFO]      [java] 2021-04-18 19:08:44,423 DEBUG org.pf4j.AbstractPluginManager - Loading plugin '/media/titi/Data/data/projects/pf4j-spring2/demo1/app/target/plugins/pf4j-spring2-demo1-plugin1-0.8.0-SNAPSHOT'
[INFO]      [java] 2021-04-18 19:08:44,428 INFO org.pf4j.spring2.SpringPluginManager - Register spring components from plugin 'plugin1-1.0.0'
[INFO]      [java] 2021-04-18 19:08:44,436 INFO org.pf4j.spring2.SpringPluginManager - *** Registring bean 'bean1' from class demo1.plugin1.Bean1
[INFO]      [java] 2021-04-18 19:08:44,437 INFO org.pf4j.spring2.SpringPluginManager - *** Registring bean 'bean2' from class demo1.plugin1.Bean2
[INFO]      [java] 2021-04-18 19:08:44,437 INFO org.pf4j.spring2.SpringPluginManager - beans registered, init complete
[INFO]      [java] 2021-04-18 19:08:44,460 DEBUG org.pf4j.spring2.HotBeanProxyFactorySupplier - Instantiate bean class 'demo1.plugin1.Bean2' by using constructor autowiring.
[INFO]      [java] 2021-04-18 19:08:44,463 DEBUG org.pf4j.spring2.HotBeanProxyFactorySupplier - Completing autowiring (setters) of bean: demo1.plugin1.Bean2@5fa07e12
[INFO]      [java] 2021-04-18 19:08:44,464 DEBUG org.pf4j.spring2.HotBeanProxyFactorySupplier - Instantiate bean class 'demo1.plugin1.Bean1' by using constructor autowiring.
[INFO]      [java] 2021-04-18 19:08:44,464 DEBUG org.pf4j.spring2.HotBeanProxyFactorySupplier - Completing autowiring (setters) of bean: demo1.plugin1.Bean1@482bce4f
[INFO]      [java] 2021-04-18 19:08:44,465 DEBUG org.pf4j.spring2.HotBeanProxyFactorySupplier - Autowiring has been completed for bean: demo1.plugin1.Bean1@482bce4f
[INFO]      [java] 2021-04-18 19:08:44,495 DEBUG org.pf4j.spring2.HotBeanProxyFactorySupplier - Autowiring has been completed for bean: demo1.plugin1.Bean2@5fa07e12
[INFO]      [java] 2021-04-18 19:08:44,541 INFO demo1.app.Boot - >>> appel bean3 : Welcome from plugin Bean1 (called by plugin Bean2) (called by plugin Bean3)
```

In order to update plugins, the pluginId __must__ be structured like this : __pluginName-version__.
So pluginX-2.0.0 will override pluginX-1.0.0.\
In fact beans are be updated if they exist in the registry, else they are added; the old plugin is unloaded. 

copy the new plugin in the plugin directory

The output is now :
```
[INFO]      [java] 2021-04-18 19:08:44,547 INFO org.pf4j.spring2.FileWatcher - New plugin detected : pf4j-spring2-demo1-plugin1b-0.8.0-SNAPSHOT.zip
[INFO]      [java] 2021-04-18 19:08:44,551 INFO org.pf4j.spring2.SpringPluginManager - Register spring components from updated plugin 'plugin1-2.0.0'
[INFO]      [java] 2021-04-18 19:08:44,552 INFO org.pf4j.spring2.SpringPluginManager - *** Updating bean 'bean2' from class demo1.plugin1.Bean2
[INFO]      [java] 2021-04-18 19:08:44,552 DEBUG org.pf4j.spring2.SpringPluginManager - Instantiate bean class 'demo1.plugin1.Bean2' by using constructor autowiring.
[INFO]      [java] 2021-04-18 19:08:44,553 DEBUG org.pf4j.spring2.SpringPluginManager - Completing autowiring (setters) of bean demo1.plugin1.Bean2@2da4da9e
[INFO]      [java] 2021-04-18 19:08:44,571 DEBUG org.pf4j.spring2.HotBeanProxyFactory - Updated bean reference in proxy - bean name: 'Bean2
[INFO]      [java] 2021-04-18 19:08:44,571 DEBUG org.pf4j.spring2.SpringPluginManager - Bean proxied updated demo1.plugin1.Bean2@2da4da9e
[INFO]      [java] 2021-04-18 19:08:44,572 INFO org.pf4j.spring2.SpringPluginManager - *** Updating bean 'bean1' from class demo1.plugin1.Bean1
[INFO]      [java] 2021-04-18 19:08:44,572 DEBUG org.pf4j.spring2.SpringPluginManager - Instantiate bean class 'demo1.plugin1.Bean1' by using constructor autowiring.
[INFO]      [java] 2021-04-18 19:08:44,572 DEBUG org.pf4j.spring2.SpringPluginManager - Completing autowiring (setters) of bean demo1.plugin1.Bean1@7fb9f6c4
[INFO]      [java] 2021-04-18 19:08:44,572 DEBUG org.pf4j.spring2.HotBeanProxyFactory - Updated bean reference in proxy - bean name: 'Bean1
[INFO]      [java] 2021-04-18 19:08:44,572 DEBUG org.pf4j.spring2.SpringPluginManager - Bean proxied updated demo1.plugin1.Bean1@7fb9f6c4
[INFO]      [java] 2021-04-18 19:08:44,573 INFO org.pf4j.spring2.SpringPluginManager - Delete plugin : plugin1-1.0.0
[INFO]      [java] 2021-04-18 19:14:06,491 INFO demo1.app.Boot - >>> appel bean3 : Welcome from plugin Bean1_UPDATED (called by plugin Bean2_UPDATED) (called by plugin Bean3)
```

Caveat
------
During update, the beans are not updated in the order of their dependency.\
A simple solution will consist on sorting the index via 'DependsOn'.\
A more complex would be to inspect spring running configuration for the beans to be updated.

Plugged beans are lazy.\
DependsOn, prototype scope not (yet) managed.

Plugins start stop are not mandatory but can be used to setup/release resources.

Extensions are not yet used, beans scanned via interface can be listed.  

Implementation details
----------------------
__AnnotationProcessor__ collects classes annotated with @Component and other stereotypes to generate a META-INF/spring.idx listing the plugin's beans to register; 
so there is no need to extend classes nor classpath scanning.

__SpringPluginManager__ register beans found at compile-time and update them when loading a new plugin

__HotBeanProxyFactorySupplier__ lazy factory giving a proxy to the real bean

__HotBeanProxyFactory__ the proxy

__FileWatcher__ watches plugin directory and call the pluginmanager's update method 

Demo
----
A tiny demo application lives in demo1 modules.\
app brings api, loads bean1 and bean2 from plugin1, bean1 is injected in bean3.\
Under pf4j-spring2-demo1-app, run the maven goal 'test'.

Todo
----
a second demo with 3 typical layers : persistence (pluggable), service (transactions), web (BOOT)
