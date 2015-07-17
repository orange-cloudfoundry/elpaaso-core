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

import java.net.URL;

import org.cloudfoundry.client.lib.CloudCredentials;
import org.cloudfoundry.client.lib.CloudFoundryClient;
import org.cloudfoundry.client.lib.CloudFoundryOperations;
import org.cloudfoundry.client.lib.HttpProxyConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CFClientFactory {
	
	public static final Logger logger = LoggerFactory.getLogger(CFClientFactory.class);
		
	public static CloudFoundryOperations login(CloudCredentials credentials, URL apiUrl, String space, String org, String domain, boolean trustSelfSignedCerts, HttpProxyConfiguration httpProxyConfiguration) {

		logger.info("Running on " + apiUrl + " on behalf of " + credentials.getEmail());
		logger.info("Using space " + space + " of organization " + org + " with domain " + domain);
		if (httpProxyConfiguration != null) {
			logger.info("Using proxy to connnect to clound foundry: httpProxyHost=" + httpProxyConfiguration.getProxyHost() + " httpProxyPort=" + httpProxyConfiguration.getProxyPort() );
		}
		else {
			logger.info("Connection to clound foundry will not use proxy. no proxy is set. ");
		}
		
		CloudFoundryClient clientWithinTargetSpace = new CloudFoundryClient(credentials, apiUrl, org, space, httpProxyConfiguration,
				trustSelfSignedCerts);
		clientWithinTargetSpace.login();
		// getOrCreateDomain(domain);
		return clientWithinTargetSpace;
	}
		
}
