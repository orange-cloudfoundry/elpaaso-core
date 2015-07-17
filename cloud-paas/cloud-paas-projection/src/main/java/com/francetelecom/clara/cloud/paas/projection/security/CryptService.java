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

public interface CryptService {

	/**
	 * Encrypts a String
	 * @param data
	 * @return
	 */
	public abstract String encrypt(String data);

	
	/**
	 * 
	 * @param cryptedData
	 * @return
	 */
	public abstract String decrypt(String cryptedData);
	
	/**
	 * Generate a 10 characters length password consisting of upper and lower
	 * case letters A-Z and the digits 0-9 with at least one uppercase, one
	 * lowercase and one digit character
	 */
	public String generateRandomPassword();

}