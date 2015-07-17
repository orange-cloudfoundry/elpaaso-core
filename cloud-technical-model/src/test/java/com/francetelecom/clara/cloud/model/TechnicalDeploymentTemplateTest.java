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
package com.francetelecom.clara.cloud.model;

import org.junit.Ignore;
import org.junit.Test;

public class TechnicalDeploymentTemplateTest {
	
	@Test(expected=IllegalArgumentException.class)
	@Ignore("need to set a proper name instead of reusin td.name")
	public void fail_to_create_technical_deployment_template_with_no_technical_deployment() {
		new TechnicalDeploymentTemplate(null, DeploymentProfileEnum.DEVELOPMENT, "releaseId", "1.0.0");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void fail_to_create_technical_deployment_template_with_no_profile() {
		new TechnicalDeploymentTemplate(new TechnicalDeployment("name"), null, "releaseId", "1.0.0");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void fail_to_create_technical_deployment_template_with_no_reference_to_a_releaseId() {
		new TechnicalDeploymentTemplate(new TechnicalDeployment("name"), DeploymentProfileEnum.DEVELOPMENT, null, "1.0.0");
	}

}
