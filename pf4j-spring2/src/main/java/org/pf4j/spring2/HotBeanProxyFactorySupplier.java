package org.pf4j.spring2;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;

/**
 * @author Thierry Uriot
 */
public class HotBeanProxyFactorySupplier<T> implements Supplier<T> {
    
    private static final Logger log = LoggerFactory.getLogger(HotBeanProxyFactorySupplier.class);

    private final String beanName;
    private final Class<?>[] interfaces;
    private final ApplicationContext applicationContext;
    private final Class<T> classBean;
    public HotBeanProxyFactory hotBeanProxyFactory;

    public HotBeanProxyFactorySupplier(String beanName, Class<T> classBean, Class<?>[] interfaces, ApplicationContext applicationContext) {
        this.beanName = beanName;
        this.interfaces = interfaces;
        this.applicationContext = applicationContext;
        this.classBean = classBean;
    }

    @Override
    public T get() {
        var beanFactory = applicationContext.getAutowireCapableBeanFactory();
        log.debug("Instantiate bean class '" + classBean.getName() + "' by using constructor autowiring.");
        var autowiredBean = beanFactory.autowire(classBean, AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR, false);
log.info("wxx--------"+autowiredBean.getClass().getClassLoader().getName()+" "+autowiredBean.getClass().getClassLoader());
        log.debug("Completing autowiring (setters) of bean: " + autowiredBean);
        beanFactory.autowireBean(autowiredBean);
        log.debug("Autowiring has been completed for bean: " + autowiredBean);
		
//        hotBeanProxyFactory = new HotBeanProxyFactory(beanName, autowiredBean, interfaces);
//        @SuppressWarnings("unchecked") var proxy = (T) this.hotBeanProxyFactory.getProxy();
		// wxx
		handler = new HotInvocationHandler(autowiredBean);
		var proxy = (T) Proxy.newProxyInstance(
                            classBean.getClassLoader(),
                            classBean.getInterfaces(),
                            handler);
		
        return proxy;
    }
	public HotInvocationHandler handler;
	
}
