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
package com.francetelecom.clara.cloud.logicalmodel;

import com.francetelecom.clara.cloud.commons.BusinessException;

import java.util.HashSet;
import java.util.Set;

/**
 * Thrown if errors or inconsistence is found in ConfigurationService
 */
public class InvalidConfigServiceException extends BusinessException {

	private static final long serialVersionUID = -3041264286403948893L;

	public enum ErrorType {
		UNKNOWN, TOO_LONG, TOO_MANY_ENTRIES, DUPLICATE_KEYS, SYNTAX_ERROR
	}

	private final Set<String> duplicateKeys = new HashSet<String>();

	private ErrorType type = ErrorType.UNKNOWN;
	
	private int entryCount = -1;

	private int maxEntryCount = -1;

	private long maxLength = -1;

	public InvalidConfigServiceException() {
		super();
	}

	public InvalidConfigServiceException(String message) {
		super(message);
	}

	public InvalidConfigServiceException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidConfigServiceException(Throwable cause) {
		super(cause);
	}
	
	/**
	 * @return Type of error
	 */
	public ErrorType getType() {
		return type;
	}

	/**
	 * Set if error type is TOO_MANY_ENTRIES.
	 * @return Current entry count
	 */
	public int getEntryCount() {
		return this.entryCount;
	}

	/**
	 * Set if error type is TOO_MANY_ENTRIES.
	 * @return Maximum allowed entry count
	 */
	public int getMaxEntryCount() {
		return this.maxEntryCount;
	}

	/**
	 * Set if error type is TOO_LONG.
	 * @return Maximum allowed length
	 */
	public long getMaxLength() {
		return this.maxLength;
	}

	/**
	 * Set if error type is DUPLICATE_KEYS
	 * @return All duplicates keys
	 */
	public Set<String> getDuplicateKeys() {
		return duplicateKeys;
	}

	public void setMaxLength(long length) {
		this.maxLength = length;
	}
	
	public void setEntryCount(int count) {
		this.entryCount = count;
	}
	
	public void setMaxEntryCount(int count) {
		this.maxEntryCount = count;
	}

	public void setType(ErrorType type) {
		this.type = type;
	}
}
