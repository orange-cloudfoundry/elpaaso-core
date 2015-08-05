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
package com.francetelecom.clara.cloud.mvn.consumer.aether;

import org.eclipse.aether.repository.Proxy;
import org.eclipse.aether.repository.ProxySelector;
import org.eclipse.aether.util.repository.DefaultProxySelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyManager {
    private static Logger logger = LoggerFactory.getLogger(ProxyManager.class);

    public ProxyManager() {
    }

    public ProxySelector selectProxies() {
        DefaultProxySelector proxySelector = new DefaultProxySelector();
        String httpProxyHost = System.getProperty("http.proxyHost");
        if (httpProxyHost != null) {
            logger.debug("Setting same proxy for http and https");
            int proxyPort = -1;
            try {
                proxyPort = Integer.parseInt(System.getProperty("http.proxyPort"));
            } catch (NumberFormatException nfe) {
                throw new RuntimeException("Invalid (or null) http.proxyPort specified into system.properties");
            }
            String nonProxyHosts = System.getProperty("http.nonProxyHosts");
            nonProxyHosts = (nonProxyHosts != null ? nonProxyHosts : "");
            Proxy httpProxy = new Proxy("http", httpProxyHost, proxyPort, null);
            Proxy httpsProxy = new Proxy("https", httpProxyHost, proxyPort, null);

            proxySelector.add(httpProxy, nonProxyHosts);
            proxySelector.add(httpsProxy, nonProxyHosts);
            logger.debug("MvnRepo proxy set to {}:{}" + (nonProxyHosts != null ? " (nonProxyHosts:" + nonProxyHosts + ")" : ""), httpProxyHost, proxyPort);
        } else {
            logger.debug("No proxy set (http.proxyHost not defined).");

        }
        return proxySelector;
    }
}
