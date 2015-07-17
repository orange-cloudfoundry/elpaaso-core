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
package com.francetelecom.clara.cloud.techmodel.cf.services.managed;

import org.junit.Ignore;
import org.junit.Test;

import com.francetelecom.clara.cloud.model.TechnicalDeployment;
import com.francetelecom.clara.cloud.techmodel.cf.Space;


public class ManagedServiceTest {
	
	@Test(expected=IllegalArgumentException.class)
	@Ignore
	public void service_instance_is_mandatory_ie_should_not_be_null() {
		TechnicalDeployment td = new TechnicalDeployment("name");
		Space space = new Space(td);
		new ManagedService("rabbitmq","default",null,space, td);
	}
	
	@Test(expected=IllegalArgumentException.class)
	@Ignore
	public void service_instance_is_mandatory_ie_should_not_be_empty() {
		TechnicalDeployment td = new TechnicalDeployment("name");
		Space space = new Space(td);
		new ManagedService("rabbitmq","default","",space, td);
	}
	
	@Test(expected=IllegalArgumentException.class)
	@Ignore
	public void service_is_mandatory_ie_should_not_be_null() {
		TechnicalDeployment td = new TechnicalDeployment("name");
		Space space = new Space(td);
		new ManagedService(null,"default","myservice",space, td);
	}
	
	@Test(expected=IllegalArgumentException.class)
	@Ignore
	public void service_is_mandatory_ie_should_not_be_empty() {
		TechnicalDeployment td = new TechnicalDeployment("name");
		Space space = new Space(td);
		new ManagedService("","default","myservice",space, td);
	}
	
	@Test(expected=IllegalArgumentException.class)
	@Ignore
	public void service_plan_is_mandatory_ie_should_not_be_null() {
		TechnicalDeployment td = new TechnicalDeployment("name");
		Space space = new Space(td);
		new ManagedService("rabbitmq",null,"myservice",space, td);
	}
	
	@Test(expected=IllegalArgumentException.class)
	@Ignore
	public void service_plan_is_mandatory_ie_should_not_be_empty() {
		TechnicalDeployment td = new TechnicalDeployment("name");
		Space space = new Space(td);
		new ManagedService("rabbitmq","","myservice",space, td);
	}
	
	@Test(expected=IllegalArgumentException.class)
	@Ignore
	public void service_space_is_mandatory() {
		TechnicalDeployment td = new TechnicalDeployment("name");
		new ManagedService("rabbitmq","default","myservice",null, td);
	}
		

}
