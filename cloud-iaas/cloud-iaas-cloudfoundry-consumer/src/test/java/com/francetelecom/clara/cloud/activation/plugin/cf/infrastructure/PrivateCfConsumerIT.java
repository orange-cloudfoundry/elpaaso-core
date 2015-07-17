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
package com.francetelecom.clara.cloud.activation.plugin.cf.infrastructure;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:com/francetelecom/clara/cloud/cfconsumer/HttpProxyToPivotalTest-context.xml")
public class PrivateCfConsumerIT extends AbstractCfAdapterIT {

	@Value("${cf.jonasBuildPack}")
	String jonasBuildPack;

	@Autowired
	@Qualifier("cf.user")
	public void setCcEmail(String ccEmail) {
		this.ccEmail = ccEmail;
	}
		
	@Autowired
	@Qualifier("cf.domain")
	public void setCfSubdomain(String cfSubdomain) {
		this.cfSubdomain = cfSubdomain;
	}
	
	@Value("${cf.ccng.space}")
	public void setCfDefaultSpace(String space) {
		this.cfDefaultSpace = space;
	}
	
	@Autowired
	@Qualifier("cfAdapter")
	public void setCfAdapter(CfAdapterImpl cfAdapter) {
		this.cfAdapter = cfAdapter;
	}

	@Override
	public String getJonasBuildpackUrl() {
		return jonasBuildPack;
	}

	@Override
	public String getJavaBuildpackUrl() {
		return jonasBuildPack;
	}
	

}
