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
package com.francetelecom.clara.cloud.core.service.exception;

import com.francetelecom.clara.cloud.commons.BusinessException;
import com.francetelecom.clara.cloud.services.dto.ConfigOverrideDTO;

public class InvalidConfigOverrideException extends BusinessException {

    private ConfigOverrideDTO faultyOverride;

	private static final long serialVersionUID = -3041264286403948893L;

	public InvalidConfigOverrideException() {
		super();
	}

	public InvalidConfigOverrideException(String message) {
		super(message);
	}

	public InvalidConfigOverrideException(String message, Throwable cause,  ConfigOverrideDTO faultyOverride) {
		super(message, cause);
        this.faultyOverride = faultyOverride;
    }

	public InvalidConfigOverrideException(Throwable cause) {
		super(cause);
	}

    public ConfigOverrideDTO getFaultyOverride() {
        return faultyOverride;
    }
}
