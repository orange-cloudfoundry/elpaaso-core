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
package com.francetelecom.clara.cloud.providersoap.mapping;

import java.lang.reflect.Constructor;
import java.util.List;

import org.dozer.DozerBeanMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SoapMapper extends DozerBeanMapper {

	private static final Logger logger = LoggerFactory.getLogger(SoapMapper.class);

	@Override
	public void setMappingFiles(List<String> mappingFiles) {
		// ensure that files names have no trailing spaces
		for (int i = 0; i < mappingFiles.size(); i++) {
			String item = mappingFiles.get(i);
			mappingFiles.set(i, item.trim());
		}
		super.setMappingFiles(mappingFiles);
	}

	/**
	 * FunctionalException to Fault (here 'Exception') mapping
	 * 
	 * @param source
	 *            the functional exception
	 * @param wrapperExceptionClass
	 *            the exception
	 * @param faultBeanClass
	 *            the fault bean
	 * @return the fault
	 */
	public Exception map(Exception source, Class<?> wrapperExceptionClass, Class<?> faultBeanClass) {
		// 'faultBean' mapping
		Object faultBean = super.map(source, faultBeanClass);

		Constructor<?> c;
		try {
			c = wrapperExceptionClass.getConstructor(new Class[] { String.class, faultBeanClass, Throwable.class });
			return (Exception) c.newInstance(new Object[] { source.getMessage(), faultBean, source });
		} catch (Exception e) {
			logger.error("Exception occured: ", e);
		}
		return null;
	}
}
