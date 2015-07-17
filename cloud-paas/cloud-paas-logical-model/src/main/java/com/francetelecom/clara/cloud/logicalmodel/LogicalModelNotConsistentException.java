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

import java.util.ArrayList;
import java.util.List;

/**
 * Raise if LogicalModel is not consistent and has some errors
 * Each 'unit' error is represented by a business exception
 *
 */
public class LogicalModelNotConsistentException extends BusinessException {

	private List<BusinessException> errors = new ArrayList<BusinessException>();

	/**
	 * @return the list of errors
	 */
	public List<BusinessException> getErrors() {
		return errors;
	}

	public LogicalModelNotConsistentException() {
		
	}

    public LogicalModelNotConsistentException(List<BusinessException> errors) {
        this.errors = new ArrayList<BusinessException>(errors); //Defensive copy
    }

    /**
	 * Add an error in the error list
	 * @param e
	 */
	public void addError(BusinessException e) {
		errors.add(e);
	}
	
	/**
	 * returns the list of messages corresponding to each encapsulated errors; each message is separated by a newline
	 */
	@Override
	public String getMessage() {
		String message ="";
		
		for(BusinessException e:errors) {
			// add a new line if message is not empty
			if(!message.equals("")) message += "\n";
			message += e.getMessage();
		}
		return message;
	}
	
	
}
