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
package com.francetelecom.clara.cloud.commons;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JasyptPrerequisites check jasypt pre-requisites Last update :
 * $LastChangedDate$ Last author
 * : $Author$
 * 
 * @version : $Revision$
 */
public class JasyptPrerequisites {

	private static final Logger logger = LoggerFactory.getLogger(JasyptPrerequisites.class);

	public JasyptPrerequisites(String passPhraseEnvName, String passPhraseJndiName) {
		logger.debug("Validating jasypt prerequisite for env:'{}' or jndi:'{}'", passPhraseEnvName, passPhraseJndiName);
		assertJasyptPrerequisites(passPhraseEnvName, passPhraseJndiName);
	}

	private void assertJasyptPrerequisites(String passPhraseEnvName, String passPhraseJndiName) {
		Validate.notEmpty(passPhraseEnvName, "passPhraseEnvName cannot be empty, neither null");
		String passPhrase = System.getenv(passPhraseEnvName);

		if(!isPassPhraseCorrect(passPhrase)){
			try {
				InitialContext ctx = new InitialContext();
				String jndiValue = (String)ctx.lookup(passPhraseJndiName);
				passPhrase = jndiValue;
			} catch (NamingException e) {
				logger.info("No Jndi jasypt secret named "+passPhraseJndiName,e);
			}
		}
		
		if (!isPassPhraseCorrect(passPhrase)) {
			throw new TechnicalException("Jasypt require pass phrase into environment or jndi variables : '" + passPhraseEnvName + "'");
		}
	}
	
	private boolean isPassPhraseCorrect(String passPhrase){
		if(passPhrase == null || passPhrase.length() == 0){
			return false;
		}
		return true;
	}
}
