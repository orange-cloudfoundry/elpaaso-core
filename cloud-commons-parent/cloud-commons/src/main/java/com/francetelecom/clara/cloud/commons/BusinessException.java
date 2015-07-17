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



/**
 * Business Exception (checked)
 * for clara-cloud project
 * 
 * 
 * @author APOG7416
 *
 */
public class BusinessException extends Exception {


	private static final long serialVersionUID = 1L;
	/**
	 * This property may be used to identify element related to this exception
	 */
	private String impactedElementName = "";


	public BusinessException() {
		super();
	}
	
	public BusinessException(Throwable t){
		super(t);
	}

	public BusinessException(String message) {
		super(message);
	}
	
	public BusinessException(String message, Throwable cause) {
		super(message, cause);
	}

	public String getImpactedElementName() {
		return impactedElementName;
	}

	public void setImpactedElementName(String impactedElementName) {
		this.impactedElementName = impactedElementName;
	}
	
	
}
