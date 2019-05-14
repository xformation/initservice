/**
 * 
 */
package com.synectiks.schemas.config;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.synectiks.commons.utils.IUtils;

/**
 * Class to server application context in whole application
 * @author Rajesh
 */
public class ContextProvider implements ApplicationContextAware {

	private static ApplicationContext context;

	@Override
	public void setApplicationContext(ApplicationContext ctx) throws BeansException {
		context = ctx;
	}

	/**
	 * Returns application context statically
	 * @return
	 */
	public static ApplicationContext getApplicationContext() {
		return context;
	}

	/**
	 * Method returns spring bean from application.
	 * @param clazz
	 * @return
	 */
	public static <T> T getBean(Class<T> clazz) {
		if (!IUtils.isNull(context)) {
			return context.getBean(clazz);
		}
		return null;
	}

}
