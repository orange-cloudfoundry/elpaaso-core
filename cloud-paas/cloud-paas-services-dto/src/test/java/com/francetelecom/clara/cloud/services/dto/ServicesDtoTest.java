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
package com.francetelecom.clara.cloud.services.dto;

import static org.junit.Assert.assertEquals;

import java.net.MalformedURLException;


import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServicesDtoTest {

	private static Logger logger=LoggerFactory.getLogger(ServicesDtoTest.class.getName());
	
	@Test
	public void test() throws MalformedURLException{

		EnvironmentDeploymentDto env = new EnvironmentDeploymentDto("app", "G1R0", "env");
		DeployableMavenArtifactDto artifact = new DeployableMavenArtifactDto("com.francetelecom.clara.cloud", "demo", "1.0");
		new DeploymentDto("node1", artifact, env);
		new DeploymentDto("node2", artifact, env);
		
		assertEquals(2, env.deployments.size());
		logger.info("test DeploymentDto constructor for application {}, version {}", env.getApplicationName(), env.getReleaseVersion());
		assertEquals(artifact, env.deployments.get(0).getArtifact());
		assertEquals("node2", env.deployments.get(1).getExecutionNode());
		
	}

}
