package org.pf4j.spring2;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.AopUtils;

/**
 * HotBeanProxyFactory is a proxy factory class used for creating proxies for hot beans. HotBeanProxyFactory is also the
 * method interceptor, which intercepts all method calls made through the created proxies.
 *
 * @author Tobias Löfstrand
 */
public class HotBeanProxyFactory extends ProxyFactory implements MethodInterceptor {

   private static final Logger log = LoggerFactory.getLogger(HotBeanProxyFactory.class);
   private final String beanName;
   private Object currentBean;

   /**
    * Creates a new HotBeanProxyFactory.
    */
   public HotBeanProxyFactory (
           String beanName,
           Object initalBean,
           Class<?>[] interfaces
   ) {
      this.beanName = beanName;
      this.currentBean = initalBean;
      super.setInterfaces(interfaces);
      super.addAdvice(this);
      super.setFrozen(true);
   }

   /**
    * To update the current target bean.
    */
   public void updateBean(final Object hotBean) {
      this.currentBean = hotBean;
      log.debug("Updated bean reference in proxy - bean name: '" + this.beanName);
   }

   /**
    * Called to invoke a method on the target bean.
    */
   @Override
   public Object invoke(final MethodInvocation methodInvocation) throws Throwable {
      if (log.isDebugEnabled())
         log.debug("Invoking method " + methodInvocation.getMethod().getName() + " on bean '" + this.beanName
                  + ". Current bean: " + this.currentBean + ".");

      if (this.currentBean != null) {
         return AopUtils.invokeJoinpointUsingReflection(
                    this.currentBean,
                    methodInvocation.getMethod(),
                    methodInvocation.getArguments());
      }
      else {
         throw new BeanNotFoundException(this.beanName, "Unable to find bean '" + this.beanName + "'!");
      }
   }

   /**
    * Gets a string representation of this HotBeanProxyFactory.
    */
   @Override
   public String toString() {
      return "HotBeanProxyFactory@" + this.hashCode();
   }

}
