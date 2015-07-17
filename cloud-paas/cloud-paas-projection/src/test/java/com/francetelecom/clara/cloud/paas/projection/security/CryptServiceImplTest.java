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

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class CryptServiceImplTest {

	private static Logger logger=LoggerFactory.getLogger(CryptServiceImplTest.class.getName());
	
	private CryptServiceImpl crypt;
	
	
	@Before
	public void setUp(){
		this.crypt=new CryptServiceImpl();
		this.crypt.setBasePassword("paas is a better way");
		this.crypt.init();
	}
	
	
	@Test
	public void testCryptDecrypt(){
		String password="myPassword";
		String cryptedPassword=this.crypt.encrypt(password);
		logger.info("crypt "+password+ " => "+cryptedPassword);
		
		String deCryptedPassword=this.crypt.decrypt(cryptedPassword);
		logger.info("decrypt "+cryptedPassword+ " => "+deCryptedPassword);
		assertEquals(password, deCryptedPassword);
		
	}
	
	
	@Test 
	public void testRandomPasswordGeneration(){
		for (int i=0;i<50;i++){
			String pass=this.crypt.generateRandomPassword();
			logger.info("generated pass  "+i+ "=> "+pass);
			Assert.assertTrue("pass too short", pass.length() >= 8);
			Assert.assertTrue("pass too long", pass.length() <= 12);
			boolean hasDigit = false;
			boolean hasLower = false;
			boolean hasUpper = false;
			for (char c : pass.toCharArray()) {
				hasDigit = hasDigit || "0123456789".indexOf(c) >= 0;
				hasLower = hasLower || "abcdefghijklmnopqrstuvwxyz".indexOf(c) >= 0;
				hasUpper = hasUpper || "ABCDEFGHIJKLMNOPQRSTUVWXYZ".indexOf(c) >= 0;
			}
			Assert.assertTrue("no digit: "+pass, hasDigit);
			Assert.assertTrue("no lower: "+pass, hasLower);
			Assert.assertTrue("no upper: "+pass, hasUpper);
		}
	}
	
}
