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

import static org.fest.assertions.Assertions.assertThat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:com/francetelecom/clara/cloud/cfconsumer/HttpProxyToPivotalTest-context.xml")
public class HttpProxyToPivotalIT {

	private static final Logger logger = LoggerFactory.getLogger(HttpProxyToPivotalIT.class);

	@Autowired
	@Qualifier("cfOnlineConsumer")
	CfAdapter cfAdapter;

	@Test
	public void it_gets_injected_the_external_proxy_host_and_port() throws IOException {
		final String httpProxyHost = cfAdapter.getHttpProxyHost();
		final int httpProxyPort = cfAdapter.getHttpProxyPort();

		logger.info("using proxy host={} port={}", httpProxyHost, httpProxyPort);

		Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(httpProxyHost, httpProxyPort));

		URL url = new URL("https://api.run.pivotal.io/info");
		URLConnection urlConnection = url.openConnection(proxy);

		Object content = urlConnection.getContent();
		assertThat(content).isNotNull();
	}

}
