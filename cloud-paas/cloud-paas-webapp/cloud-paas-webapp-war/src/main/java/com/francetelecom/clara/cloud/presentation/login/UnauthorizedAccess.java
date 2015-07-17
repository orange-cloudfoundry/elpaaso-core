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
package com.francetelecom.clara.cloud.presentation.login;

import com.francetelecom.clara.cloud.commons.TechnicalException;

/**
 * Error thrown if login failed because the existing account does not have enough rights.
 */
public class UnauthorizedAccess extends TechnicalException {

	private static final long serialVersionUID = 1072414408850471958L;

	public UnauthorizedAccess(String message) {
		super(message);
	}
}
