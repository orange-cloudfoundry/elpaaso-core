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
package com.francetelecom.clara.cloud.activation.plugin.dbaas;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.francetelecom.clara.cloud.activation.plugin.dbaas.ActivationPluginDBaasUtils;
import com.francetelecom.clara.cloud.activation.plugin.dbaas.DBaasConsumer;
import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.francetelecom.clara.cloud.techmodel.dbaas.DBaasSubscriptionV2;

@RunWith(MockitoJUnitRunner.class)
public class ActivationPluginDBaasUtilsTest {

	private final ActivationPluginDBaasUtils dBaasUtils = new ActivationPluginDBaasUtils();

	@Mock
	private DBaasSubscriptionV2 dbaasSubscription;

	@Mock
	private DBaasConsumer mockDBaasConsumer;
	
	private final String mockVersion = "mockVersion";

	@Before
	public void before() throws Exception {
		when(dbaasSubscription.getDbaasVersion()).thenReturn(mockVersion);
		Map<String, DBaasConsumer> consumers = new HashMap<String, DBaasConsumer>();
		consumers.put(mockVersion, mockDBaasConsumer);
		dBaasUtils.setdBaasConsumers(consumers);
	}
	
	@Test
	public void a_dbaasversion_should_be_linked_to_one_consumer() throws Exception {

		// When
		DBaasConsumer dBaasConsumer = dBaasUtils.getDBaasConsumer(dbaasSubscription);

		// Then
		assertThat(dBaasConsumer).isNotNull().isEqualTo(mockDBaasConsumer);
	}

	@Test(expected = TechnicalException.class)
	public void exception_should_be_raised_for_non_empty_and_unknown_dbaas_version() throws Exception {
		// Given
		String unknownVersion = "unknownVersion";
		when(dbaasSubscription.getDbaasVersion()).thenReturn(unknownVersion);

		// When
		dBaasUtils.getDBaasConsumer(dbaasSubscription);
	}

	@Test(expected = TechnicalException.class)
	public void exception_should_be_raised_for_empty_dbaas_version() throws Exception {
		// Given
		when(dbaasSubscription.getDbaasVersion()).thenReturn("");
		// When
		dBaasUtils.getDBaasConsumer(dbaasSubscription);
	}

	@Test(expected = TechnicalException.class)
	public void exception_should_be_raised_for_null_dbaas_version() throws Exception {
		// Given
		when(dbaasSubscription.getDbaasVersion()).thenReturn(null);
		// When
		dBaasUtils.getDBaasConsumer(dbaasSubscription);
	}

	@Test(expected = TechnicalException.class)
	public void exception_should_be_raised_if_no_consumer_configured() throws Exception {
		// Given
		dBaasUtils.setdBaasConsumers(null);

		// When
		dBaasUtils.getDBaasConsumer(dbaasSubscription);

	}
}
