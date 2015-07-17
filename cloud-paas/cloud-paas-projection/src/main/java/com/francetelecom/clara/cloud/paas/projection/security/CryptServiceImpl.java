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
package com.francetelecom.clara.cloud.paas.projection.security;


import com.Ostermiller.util.RandPass;
import org.jasypt.util.text.BasicTextEncryptor;
import org.jasypt.util.text.TextEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Internal Crypto Service
 * 
 * 
 * 
 * 
 * @author apog7416
 *
 */
public class CryptServiceImpl implements CryptService {

	private static Logger logger=LoggerFactory.getLogger(CryptServiceImpl.class);
	
	private String basePassword;
	
	private TextEncryptor textEncryptor;
	
	/**
	 * Initial config for the Crypt Service
	 */
	public void init(){
		
		logger.info("initial crypt service configuration");
		
		
		//TODO check JCE extensions for StringTextEncryptor
		BasicTextEncryptor bte=new BasicTextEncryptor(); 
		bte.setPassword(this.basePassword);
		this.textEncryptor=bte;
	}
	
	/* (non-Javadoc)
	 * @see CryptService#encrypt(java.lang.String)
	 */
	public String encrypt(String data){
		logger.debug("encrypt data");
		return this.textEncryptor.encrypt(data);
	}
	
	
	/* (non-Javadoc)
	 * @see CryptService#decrypt(java.lang.String)
	 */
	public String decrypt(String cryptedData){
		logger.debug("decrypt data");
		return this.textEncryptor.decrypt(cryptedData);
	}


	/**
	 * IOC
	 * 
	 * @param basePassword  root password for all PAAS encryption
	 */
	public void setBasePassword(String basePassword) {
		this.basePassword = basePassword;
	}

	/**
	 * Generate a 10 characters length password consisting of upper and lower
	 * case letters A-Z and the digits 0-9 with at least one uppercase, one
	 * lowercase and one digit character
	 */
	@Override
	public String generateRandomPassword() {
		logger.info("generating new random password");
		RandPass rand = new RandPass();
		// Set alphabet consisting of upper and lower case letters A-Z and the digits 0-9
		rand.setAlphabet(RandPass.NUMBERS_AND_LETTERS_ALPHABET);
		// At least one lowercase letter
		rand.addRequirement(RandPass.LOWERCASE_LETTERS_ALPHABET, 1);
		// At least one uppercase letter
		rand.addRequirement(RandPass.UPPERCASE_LETTERS_ALPHABET, 1);
		// At least one digit
		rand.addRequirement(new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' }, 1);
		// Generate a 10 characters length password
		return rand.getPass(10);
	}
	
}
