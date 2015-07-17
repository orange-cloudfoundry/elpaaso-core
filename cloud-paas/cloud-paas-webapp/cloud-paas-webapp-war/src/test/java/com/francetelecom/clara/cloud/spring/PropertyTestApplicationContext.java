/**
 * Copyright (C) 2015 Orange
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.francetelecom.clara.cloud.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.web.context.support.StandardServletEnvironment;

/**
 * This spring context is not a real context beacause it doesn't do all lifecycle of a real Spring context initialization when doing refresh().
 * 
 * It basically used to start context instanciation, check definition problems, resolve placeholders and then allow users to do assertions on context
 * 
 * 
 * @author Ludovic Meurillon
 */
abstract class PropertyTestApplicationContext extends ClassPathXmlApplicationContext{
	
	public PropertyTestApplicationContext(String...contextFileLocations){
		super(contextFileLocations, false, null);
	}
	
	/**
	 * Environment need to be overriden to map with normal webapp spring 
	 * context behavior (jndi resolution of properties)
	 */
	@Override
	protected ConfigurableEnvironment createEnvironment() {
		return new StandardServletEnvironment();
	}
	
	@Override
	public void refresh() throws BeansException, IllegalStateException {
		prepareRefresh();

		// Tell the subclass to refresh the internal bean factory.
		ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();

		// Prepare the bean factory for use in this context.
		prepareBeanFactory(beanFactory);

		// Allows post-processing of the bean factory in context subclasses.
		postProcessBeanFactory(beanFactory);
		
		// Allows 
		afterPostProcessBeanFactory(beanFactory);
		
		// Invoke factory processors registered as beans in the context.
		invokeBeanFactoryPostProcessors(beanFactory);
	}

	/**
	 * Override this method to be able to scan context state before properties resolution (properties are not yet resolved)
	 * @param beanFactory
	 */
	public abstract void afterPostProcessBeanFactory(ConfigurableListableBeanFactory beanFactory);
}