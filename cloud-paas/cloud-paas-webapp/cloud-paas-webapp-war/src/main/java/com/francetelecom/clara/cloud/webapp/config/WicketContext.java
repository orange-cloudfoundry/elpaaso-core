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
package com.francetelecom.clara.cloud.webapp.config;

import com.francetelecom.clara.cloud.core.service.ManageApplication;
import com.francetelecom.clara.cloud.core.service.ManageApplicationRelease;
import com.francetelecom.clara.cloud.core.service.ManageEnvironment;
import com.francetelecom.clara.cloud.deployment.logical.service.ManageLogicalDeployment;
import com.francetelecom.clara.cloud.presentation.tools.PopulateDatasService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Created by WOOJ7232 on 22/05/2015.
 */
@Configuration
@Import(WicketContextApplication.class)
@ComponentScan("com.francetelecom.clara.cloud.presentation.designer.support")
public class WicketContext {

	@Autowired
	ManageApplication manageApplication;

	@Autowired
	ManageApplicationRelease manageApplicationRelease;
	@Autowired
	ManageEnvironment manageEnvironment;
	@Autowired
	ManageLogicalDeployment manageLogicalDeployment;


	@Bean(name = "populateService")
	public PopulateDatasService populateDatasService(){
		PopulateDatasService populateDatasService = new PopulateDatasService();
		populateDatasService.setManageApplication(manageApplication);
		populateDatasService.setManageApplicationRelease(manageApplicationRelease);
		populateDatasService.setManageLogicalDeployment(manageLogicalDeployment);
		populateDatasService.setManageEnvironment(manageEnvironment);
		return populateDatasService;
	}
}
