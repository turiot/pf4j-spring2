package org.pf4j.spring2;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class HotInvocationHandler implements InvocationHandler {

	public Object currentBean;

	public HotInvocationHandler(Object currentBean) {
		this.currentBean = currentBean;
	}
	
	@Override
	public Object invoke(Object o, Method m, Object[] args) throws Throwable {
        Object result;
        try{
        	result = m.invoke(currentBean, args);
	    } 
		catch (InvocationTargetException e) {
	        throw e;
	    }
		catch (Exception e) {
	        throw e;
	    }
        return result;	}

}
