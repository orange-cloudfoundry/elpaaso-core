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
package com.francetelecom.clara.cloud.presentation;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.francetelecom.clara.cloud.presentation.common.PageTemplate;

public class InvalidReleaseExceptionPage extends ExceptionPage implements ExceptionView {

	private static final transient String INVALID_RELASE_KEY = "portal.error.message.invalidRelease";

	public InvalidReleaseExceptionPage() {
		super(new PageParameters(),INVALID_RELASE_KEY);
	}
		
	@Override
	public PageTemplate asPage() {
		return this;
	}


}
