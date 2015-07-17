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
package com.francetelecom.clara.cloud.paas.activation.v1.async.exception;

import java.io.Serializable;

/**
 * max retry count exceeded exception
 * 
 * @author Clara
 * 
 */
public class UnexpectedException extends RuntimeException implements
		Serializable {

	private static final long serialVersionUID = 7552671441723224932L;

	protected String localisationClass;
	protected String localisationMethod;

	public UnexpectedException() {
		super();
		localisationClass = "";
		localisationMethod = "";
	}

	public UnexpectedException(String message) {
		super(message);
		localisationClass = "";
		localisationMethod = "";
	}

	public UnexpectedException(String message, Throwable cause) {
		super(message, cause);
		localisationClass = "";
		localisationMethod = "";
	}

	public UnexpectedException(Throwable cause) {
		super(cause);
		localisationClass = "";
		localisationMethod = "";
	}

	public UnexpectedException(String clazz, String method,
			String message) {
		super(message);
		localisationClass = clazz;
		localisationMethod = method;
	}

	public UnexpectedException(String clazz, String method,
			String message, Throwable cause) {
		super(message, cause);
		localisationClass = clazz;
		localisationMethod = method;
	}

	public String getLocalisationClasse() {
		return localisationClass;
	}

	public String getLocalisationMethod() {
		return localisationMethod;
	}

	public String getLocalisation() {
		return localisationClass + "." + localisationMethod;
	}

}
