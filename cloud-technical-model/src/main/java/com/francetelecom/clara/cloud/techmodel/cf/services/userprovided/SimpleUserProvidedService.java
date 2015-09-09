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
package com.francetelecom.clara.cloud.techmodel.cf.services.userprovided;

import com.francetelecom.clara.cloud.techmodel.cf.Space;
import com.francetelecom.clara.cloud.techmodel.cf.services.NoContraintSpecification;
import com.francetelecom.clara.cloud.techmodel.cf.services.ServiceNameSpecification;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.HashMap;
import java.util.Map;

/**
 * A user provided service statically bound (at projection time)
 */
@Entity
@Table(name = "CF_SIMPLE_UPS_V2")
public class SimpleUserProvidedService extends AbstractUserProvidedService {

	private String serviceUrl;

	protected SimpleUserProvidedService() {
	}

	public SimpleUserProvidedService(String serviceName, String serviceUrl, Space space) {
		super(serviceName, space);
		this.serviceUrl = serviceUrl;
	}

	@Override
	public String getServiceUrl() {
		return serviceUrl;
	}

	@Override
	public Map<String, Object> getCredentials() {
		return new HashMap<String, Object>();
	}

	@Override
	public ServiceNameSpecification serviceNameSpecification() {
		return new NoContraintSpecification();
	}

	@Override
	public String getLogUrl() {
		return null;
	}

}
