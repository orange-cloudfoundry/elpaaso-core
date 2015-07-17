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
package com.francetelecom.clara.cloud.technicalservice.exception;

import com.francetelecom.clara.cloud.coremodel.SSOId;

public class PaasUserNotFoundException extends ObjectNotFoundException {

	private static final long serialVersionUID = -3041264286403948893L;
    private SSOId missingUserId;

    public PaasUserNotFoundException() {
		super();
	}

	public PaasUserNotFoundException(String message) {
		super(message);
	}

	public PaasUserNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public PaasUserNotFoundException(Throwable cause) {
		super(cause);
	}

	public PaasUserNotFoundException(String message, SSOId missingUserId) {
		super(message);
		this.missingUserId = missingUserId;
    }

    public SSOId getMissingUserId() {
        return missingUserId;
    }
}
