package org.pf4j.spring2;

/**
 * Exception class thrown when a bean isn't found.
 * 
 * @author Tobias Löfstrand
 */
public class BeanNotFoundException extends RuntimeException {

   private final String beanName;

   /**
    * Creates a new BeanNotFoundException.
    */
   public BeanNotFoundException(String beanName, String message) {
      super(message);
      this.beanName = beanName;
   }

   /**
    * Gets the associated bean name.
    */
   public String getBeanName() {
      return beanName;
   }

}
