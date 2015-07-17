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

import org.junit.Test;

import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.francetelecom.clara.cloud.commons.ValidatorUtil;

public class LogicalSoapServiceTest {

	@Test(expected = TechnicalException.class)
	public void context_root_is_mandatory() {
		String contextRoot = null;
		LogicalSoapService service = new LogicalSoapService("label", new LogicalDeployment(), "servicename", 1, 0, contextRoot, new Path("/service"),
				null, "description");

		ValidatorUtil.validate(service);
	}

	@Test(expected = TechnicalException.class)
	public void fail_to_add_context_root_with_no_value() {
		String contextRoot = "";
		LogicalSoapService service = new LogicalSoapService("label", new LogicalDeployment(), "servicename", 1, 0, contextRoot, new Path("/service"),
				null, "description");

		ValidatorUtil.validate(service);
	}

	@Test(expected = TechnicalException.class)
	public void service_path_is_mandatory() {
		new LogicalSoapService("label", new LogicalDeployment(), "servicename", 1, 0, "/api", null, null, "description");
	}

}
