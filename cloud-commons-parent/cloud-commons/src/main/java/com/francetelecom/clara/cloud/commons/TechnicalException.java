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
 * Technical RuntimeException (unchecked)
 * for clara-cloud project
 * 
 * 
 * @author APOG7416
 *
 */
public class TechnicalException extends RuntimeException {


	private static final long serialVersionUID = 1L;

	
	public TechnicalException() {
		super();
	}
	
	public TechnicalException(Throwable t){
		super(t);
	}
	
	
	public TechnicalException(String cause){
		super(cause);
	}

	
	public TechnicalException(String cause,Throwable e){
		super(cause,e);
	}
	
}
