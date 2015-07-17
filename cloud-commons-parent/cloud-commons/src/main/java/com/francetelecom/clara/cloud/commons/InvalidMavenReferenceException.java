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
 * This exception may be thrown when a maven reference is invalid; typically when it access url can not be resolved
 *
 */
public class InvalidMavenReferenceException extends BusinessException {

	public enum ErrorType {
		UNKNOWN, ARTIFACT_NOT_FOUND
	}
	
	MavenReference mavenReference;
	
	ErrorType type = ErrorType.UNKNOWN;
	
	public MavenReference getMavenReference() {
		return mavenReference;
	}

	public ErrorType getType() {
		return type;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -957854386103324821L;

	public InvalidMavenReferenceException(MavenReference mr, ErrorType type) {
		this.mavenReference = mr;
		this.type = type;
	}
	
	@Override
	public String getMessage() {
		return "Invalid Maven Reference: "+mavenReference+":"+type;
	}
	
}
