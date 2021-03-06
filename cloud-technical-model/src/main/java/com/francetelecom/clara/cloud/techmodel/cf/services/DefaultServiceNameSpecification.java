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
package com.francetelecom.clara.cloud.techmodel.cf.services;

import org.springframework.util.Assert;

public class DefaultServiceNameSpecification implements ServiceNameSpecification {

	@Override
	public void assertIsSatisfiedBy(String serviceName) {
		Assert.hasText(serviceName,"service name <"+serviceName+"> is not valid.");
		if (serviceName.length() >=50)
			throw new IllegalArgumentException("service name <"+serviceName+"> is not valid. service name should not exceed 50 characters");
	}

}
