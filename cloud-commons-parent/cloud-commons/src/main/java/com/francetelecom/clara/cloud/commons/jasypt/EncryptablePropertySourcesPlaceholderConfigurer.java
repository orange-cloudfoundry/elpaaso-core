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
package com.francetelecom.clara.cloud.commons.jasypt;

import java.io.IOException;
import java.util.Properties;

import org.jasypt.commons.CommonUtils;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.properties.PropertyValueEncryptionUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.ConfigurablePropertyResolver;
import org.springframework.util.StringValueResolver;

/**
 * This calss was ispired by antoher {@link EncryptablePropertySourcesPlaceholderConfigurer}
 * but allow spring to decode JNDI/System/Env properties also
 * 
 * When this class was created {@link EncryptablePropertySourcesPlaceholderConfigurer} was final (and so not directly extensible)
 *  
 * @author Ludovic Meurillon
 */
public class EncryptablePropertySourcesPlaceholderConfigurer extends PropertySourcesPlaceholderConfigurer{

	private final StringEncryptor stringEncryptor;

	
	/*
	 * This flag will keep track of whether the "convertProperties()" method
	 * (which decrypts encrypted property entries) has already been called
	 * or not. 
	 * 
	 * This is needed because of a bug in Spring 3.1.0.RELEASE:
	 * https://jira.springsource.org/browse/SPR-8928
	 * 
	 * This flag will avoid calling "convertProperties()" twice once this
	 * bug has been solved.
	 */
	private boolean alreadyConverted = false;
	

	/**
	 * <p>
	 * Creates an <tt>EncryptablePropertyPlaceholderConfigurer</tt> instance
	 * which will use the passed {@link StringEncryptor} object to decrypt
	 * encrypted values.
	 * </p>
	 * 
	 * @param stringEncryptor
	 *            the {@link StringEncryptor} to be used do decrypt values. It
	 *            can not be null.
	 */
	public EncryptablePropertySourcesPlaceholderConfigurer(
	        final StringEncryptor stringEncryptor) {
		super();
		CommonUtils.validateNotNull(stringEncryptor, "Encryptor cannot be null");
		this.stringEncryptor = stringEncryptor;
	}

	/*
	 * This is needed because of https://jira.springsource.org/browse/SPR-8928
	 */
	@Override
    protected Properties mergeProperties() throws IOException {
        final Properties mergedProperties = super.mergeProperties();
        convertProperties(mergedProperties);
        return mergedProperties;
    }

    /*
     * This is needed because of https://jira.springsource.org/browse/SPR-8928
     */
    @Override
    protected void convertProperties(final Properties props) {
        if (!this.alreadyConverted) {
            super.convertProperties(props);
            this.alreadyConverted = true;
        }
    }

    /**
	 * Visit each bean definition in the given bean factory and attempt to replace ${...} property
	 * placeholders with values from the given properties.
	 */
    @Override
	protected void processProperties(ConfigurableListableBeanFactory beanFactoryToProcess,
			final ConfigurablePropertyResolver propertyResolver) throws BeansException {
		propertyResolver.setPlaceholderPrefix(this.placeholderPrefix);
		propertyResolver.setPlaceholderSuffix(this.placeholderSuffix);
		propertyResolver.setValueSeparator(this.valueSeparator);

		StringValueResolver valueResolver = new StringValueResolver() {
			public String resolveStringValue(String strVal) {
				String resolved = ignoreUnresolvablePlaceholders ?
						propertyResolver.resolvePlaceholders(strVal) :
						propertyResolver.resolveRequiredPlaceholders(strVal);
				return (resolved.equals(nullValue) ? null : convertPropertyValue(resolved));
			}
		};

		doProcessProperties(beanFactoryToProcess, valueResolver);
	}


    /*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.beans.factory.config.PropertyResourceConfigurer#convertPropertyValue(java.lang.String)
	 */
    @Override
	protected String convertPropertyValue(final String originalValue) {
		if (!PropertyValueEncryptionUtils.isEncryptedValue(originalValue)) {
			return originalValue;
		}
		return PropertyValueEncryptionUtils.decrypt(originalValue, this.stringEncryptor);
	}
}
