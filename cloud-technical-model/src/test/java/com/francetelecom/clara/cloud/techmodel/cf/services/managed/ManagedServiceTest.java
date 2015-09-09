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

import com.francetelecom.clara.cloud.techmodel.cf.Space;
import org.junit.Ignore;
import org.junit.Test;


public class ManagedServiceTest {
	
	@Test(expected=IllegalArgumentException.class)
	@Ignore
	public void service_instance_is_mandatory_ie_should_not_be_null() {
		new ManagedService("rabbitmq","default",null, new Space());
	}
	
	@Test(expected=IllegalArgumentException.class)
	@Ignore
	public void service_instance_is_mandatory_ie_should_not_be_empty() {
		new ManagedService("rabbitmq","default","", new Space());
	}
	
	@Test(expected=IllegalArgumentException.class)
	@Ignore
	public void service_is_mandatory_ie_should_not_be_null() {
		new ManagedService(null,"default","myservice", new Space());
	}
	
	@Test(expected=IllegalArgumentException.class)
	@Ignore
	public void service_is_mandatory_ie_should_not_be_empty() {
		new ManagedService("","default","myservice", new Space());
	}
	
	@Test(expected=IllegalArgumentException.class)
	@Ignore
	public void service_plan_is_mandatory_ie_should_not_be_null() {
		new ManagedService("rabbitmq",null,"myservice", new Space());
	}
	
	@Test(expected=IllegalArgumentException.class)
	@Ignore
	public void service_plan_is_mandatory_ie_should_not_be_empty() {
		new ManagedService("rabbitmq","","myservice", new Space());
	}
	
	@Test(expected=IllegalArgumentException.class)
	@Ignore
	public void service_space_is_mandatory() {
		new ManagedService("rabbitmq","default","myservice",null);
	}
		

}
