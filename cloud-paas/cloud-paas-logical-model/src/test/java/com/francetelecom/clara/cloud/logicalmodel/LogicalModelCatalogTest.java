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

import com.francetelecom.clara.cloud.commons.ValidatorUtil;
import com.francetelecom.clara.cloud.logicalmodel.samplecatalog.SampleAppFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.transaction.Transactional;
import java.net.MalformedURLException;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * This test represents the usage made of the {@link SampleAppFactory} interface by the other modules.
 */
@ContextConfiguration(locations = "application-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
public class LogicalModelCatalogTest {

	private static Logger logger = LoggerFactory.getLogger(LogicalModelCatalogTest.class.getName());

	/**
	 * A precise bean is choosen in the spring config using its name
	 */
	@Autowired
	@Qualifier("petclinicLogicalModelCatalog")
	SampleAppFactory petClinicAppFactory;

	/**
	 * Spring automatically injects all known beans of type {@link SampleAppFactory}. See
	 * http://static.springsource.org/spring/docs/current/spring-framework-reference/html/beans.html#beans-annotation-config
	 */
	@Autowired
	Map<String, SampleAppFactory> sampleAppsCatalog;

	@Autowired
	LogicalDeploymentRepository logicalDeploymentRepository;

	/**
	 * Simulates calls made by the projection which uses a named bean defined by annotation {@link #petClinicAppFactory}
	 * 
	 * @throws java.net.MalformedURLException
	 */
	@Test
	@Transactional
	public void testAllSampleAppFactoryInstances() throws MalformedURLException {
		for (Map.Entry<String, SampleAppFactory> entry : sampleAppsCatalog.entrySet()) {
			String beanName = entry.getKey();
			SampleAppFactory appFactory = entry.getValue();
			if (appFactory.isInstantiable()) {

				LogicalDeployment logicalDeployment = appFactory.populateLogicalDeployment(null);

				ValidatorUtil.validate(logicalDeployment);
				logicalDeploymentRepository.save(logicalDeployment);
				LogicalDeployment reloadedLd = logicalDeploymentRepository.findOne(logicalDeployment.getId());
				assertEquals("incorrect ld reloaded from db for sample:" + beanName, logicalDeployment, reloadedLd);
				logger.info("sample catalog [" + beanName + "] properly serialized");
			}
		}
		// Note: the loading from JPA and test equality against the graph in memory is covered paas-services using DAO instead.
	}

	/**
	 * Simulates calls made by the DataScalability using named dependency from the UI (the ScalabilityPage)
	 * 
	 * @throws java.net.MalformedURLException
	 */
	@Test
	public void testFactoryCatalogFromUi() throws MalformedURLException {
		assertTrue(sampleAppsCatalog.containsKey("petclinicLogicalModelCatalog"));

		int appIndex = 0;
		for (Map.Entry<String, SampleAppFactory> entry : sampleAppsCatalog.entrySet()) {
			String beanName = entry.getKey();
			SampleAppFactory appFactory = entry.getValue();
			assertNotNull(beanName);
			assertNotNull(appFactory);

			// create app
			String appName = appFactory.getAppLabel();
			String appCode = appFactory.getAppCode();
			String appDescription = appFactory.getAppDescription();

			appIndex++;
			if (appName == null) {
				appName = "SampleApp" + appIndex;
			}
			if (appCode == null) {
				appCode = appName.substring(0, 2) + appIndex;
			}
			if (appDescription == null) {
				appDescription = "Sample app description for" + appName;
			}
			// create app release, as a side effect, creates the logical deployment

			// Populate logical model.
			LogicalDeployment ld = null; // FIXME: should be loaded from appRelease.
			LogicalDeployment springooLogicalModel = petClinicAppFactory.populateLogicalDeployment(null);

			assertTrue(springooLogicalModel.listLogicalServices().size() > 0);
		}
	}
}
