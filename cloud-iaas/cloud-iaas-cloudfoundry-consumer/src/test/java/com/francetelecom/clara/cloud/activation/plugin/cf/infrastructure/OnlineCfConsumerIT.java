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

import java.io.IOException;

import org.apache.http.HttpHost;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
//@Ignore("Waiting for dependency mgt fix")
@ContextConfiguration(locations = "classpath:com/francetelecom/clara/cloud/cfconsumer/HttpProxyToPivotalTest-context.xml")
public class OnlineCfConsumerIT extends AbstractCfAdapterIT {
	
	@Autowired
	@Qualifier("cf.online.user")
	public void setCcEmail(String ccEmail) {
		this.ccEmail = ccEmail;
	}
			
	@Autowired
	@Qualifier("cf.online.domain")
	public void setCfSubdomain(String cfSubdomain) {
		this.cfSubdomain = cfSubdomain;
	}
	
	@Value("${cf.online.ccng.space}")
	public void setCfDefaultSpace(String space) {
		this.cfDefaultSpace = space;
	}
	
	@Autowired
	@Qualifier("cfOnlineConsumer")
	public void setCfAdapter(CfAdapterImpl cfAdapter) {
		this.cfAdapter = cfAdapter;
	}

	@Override
	public String getJonasBuildpackUrl() {
		return "https://github.com/Orange-OpenSource/java-buildpack.git#stable_v1";
	}

	@Override
	public String getJavaBuildpackUrl() {
		return "https://github.com/cloudfoundry/java-buildpack.git";
	}


    @Test
    public void our_test_can_access_cf_route_uris_to_assert_exposed_webguis() throws IOException {
        testRemoteAppWebGui("api.run.pivotal.io", "info", null);
    }

    //We need to use internet proxy to query routes on redacted-domain.org
    protected HttpClientConfig getHttpProxyConfigToQueryWebGuiRoutes() {
        HttpClientConfig defaultProxyConfig = null;
        if (cfAdapter.isUsingHttpProxy()) {
            final String httpProxyHost = cfAdapter.getHttpProxyHost();
            final int httpProxyPort = cfAdapter.getHttpProxyPort();

            defaultProxyConfig = new HttpClientConfig() {
                @Override
                public void applyConfig(DefaultHttpClient httpclient) {
                    HttpHost proxy = new HttpHost(httpProxyHost, httpProxyPort);
                    httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
                }

                @Override
                public String toString() {
                    return "HttpClientConfig {httpProxyHost=" + httpProxyHost + " httpProxyPort=" + httpProxyPort + " }";
                }
            };

        }
        return defaultProxyConfig;
    }

}
