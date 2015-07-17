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
package com.francetelecom.clara.cloud.paas.it.services.helper;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Assert;
import org.junit.internal.AssumptionViolatedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.francetelecom.clara.cloud.logicalmodel.samplecatalog.SampleAppFactory;
import com.francetelecom.clara.cloud.services.dto.LinkDto;

/**
 * PaasServicesEnvApplicationAccessHelper
 * common code of : (PaasServicesEnv)<BaseIT|AdvancedIT>
 * TO FIX BVA : (in progress)
 * Last update  : $LastChangedDate$
 * Last author  : $Author$
 * @version     : $Revision$
 */

public class PaasServicesEnvApplicationAccessHelper {
    // logger
    protected static Logger logger = LoggerFactory.getLogger(PaasServicesEnvApplicationAccessHelper.class.getName());

    private SampleAppFactory logicalModelCatalog;
    // Test url attempt count
    private int webAppTestAttempts = 2;
    // test url wait time in second
    private int webAppTestWaitTime = 5;

    public PaasServicesEnvApplicationAccessHelper(SampleAppFactory logicalModelCatalog, int webAppTestAttempts, int webAppTestWaitTime) {
        this.logicalModelCatalog = logicalModelCatalog;
        this.webAppTestAttempts = webAppTestAttempts;
        this.webAppTestWaitTime = webAppTestWaitTime;
    }

    public void checkWebGuiServicesAccess(List<LinkDto> webGuiAccessLinks, boolean accessibleExpected, PaasServicesEnvITConfiguration itConfiguration) {
        if (itConfiguration.isUseSshTunnel()) {
			throw new TechnicalException("SSH Tunnel aren't supported anymore");
        } else {
            for (LinkDto guiAccessLink : webGuiAccessLinks) {
                String httpProxyHost = null;
                int httpProxyPort = 3128;
                if (itConfiguration.isUseHttpIgeProxy()) {
                    httpProxyHost = itConfiguration.getHttpProxyHost();
                    httpProxyPort = itConfiguration.getHttpProxyPort();
                }
                checkWebGuiAccessLink(guiAccessLink, accessibleExpected, httpProxyHost, httpProxyPort);
            }
        }
    }


    private void checkWebGuiAccessLink(LinkDto link, boolean shouldAccessible, String httpProxyHost, int httpProxyPort) {
        for (Map.Entry<String, String> entry : logicalModelCatalog.getAppUrlsAndKeywords(link.getUrl()).entrySet()) {
            String testedUrl = link.getUrl().toExternalForm() + entry.getKey();
            checkApplicationAccessFromUrlAndKeyword(testedUrl, entry.getValue(), shouldAccessible, httpProxyHost, httpProxyPort);
        }
    }


    private void checkApplicationAccessFromUrlAndKeyword(String accessUrlString, String checkKeyword, boolean shouldAccessible, String httpProxyHost, int httpProxyPort) {
        logger.debug("Testing url {} ", accessUrlString);
        Assert.assertEquals("Access to application is not ok: " + accessUrlString,
                shouldAccessible,
                testUrl(
                        accessUrlString,
                        shouldAccessible ? webAppTestAttempts : 1,
                        webAppTestWaitTime,
                        checkKeyword, httpProxyHost, httpProxyPort));
    }
    //
    //~ Utils static tools
    //

    /**
     *
     * @param httpProxyHost set to null for direct connection or to proxy host to use proxy
     */
    public static boolean testUrl(String url, int maxAttempts, int waitingTimeSeconds, String checkKeyword, String httpProxyHost, int httpProxyPort) {

        boolean urlTestSucceeded = false;

        // Add an arbitrary parameter to disable all possible cache
        final String urlNoCache;
        if (url.contains("?")) {
            urlNoCache = url + "&nocache=" + System.currentTimeMillis();
        } else {
            urlNoCache = url + "?nocache=" + System.currentTimeMillis();
        }
        URL targetApplicationUrl;
        try {
            targetApplicationUrl = new URL(urlNoCache);
        } catch (MalformedURLException e) {
            logger.error("url invalid {}", urlNoCache);
            throw new AssumptionViolatedException(e, new NotValidURLMatcher(urlNoCache));
        }

        for (int tryCount = 0; tryCount < maxAttempts && !urlTestSucceeded; tryCount++) {
            logger.info("Testing " + url + " - Attempt " + (tryCount + 1) + "/" + maxAttempts);
            String cookie = checkStringAndReturnCookie(targetApplicationUrl, checkKeyword, httpProxyHost, httpProxyPort);
            if (cookie != null) {
                urlTestSucceeded = true;
            } else {
                logger.info("Sleeping " + waitingTimeSeconds  + "s before next retry");
                try {
                    Thread.sleep(waitingTimeSeconds * 1000);
                } catch (InterruptedException e) {
                    break;
                }
            }

        }

        return urlTestSucceeded;
    }

    private static class NotValidURLMatcher extends BaseMatcher {
        private String notValidUrl;
        public NotValidURLMatcher(String notValidUrl) {
            this.notValidUrl = notValidUrl;

        }
        @Override
        public boolean matches(Object o) {
            return false;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("unable to convert " + notValidUrl + " in a valid URL");
        }
    }


    /**
     * Check if a string appear in the html page
     *
     * @param url
     *            URL to test
     * @param token
     *            String that must be in html page
     * @return Cookie that identifies the was or null if the test failed. An
     *         empty string means that no cookie was found in the request, but
     *         the check succeeded.
     */
    private static String checkStringAndReturnCookie(URL url, String token, String httpProxyHost, int httpProxyPort) {
        InputStream is = null;
        String cookie = null;
        try {
            HttpURLConnection connection;
            if (httpProxyHost != null) {
                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(httpProxyHost, httpProxyPort));
                connection = (HttpURLConnection) url.openConnection(proxy);
            } else {
                connection = (HttpURLConnection) url.openConnection();
            }
			connection.setRequestMethod("GET");

            is = connection.getInputStream();
            // check http status code
            if (connection.getResponseCode() == 200) {
                DataInputStream dis = new DataInputStream(new BufferedInputStream(is));
                StringWriter writer = new StringWriter();
                IOUtils.copy(dis, writer, "UTF-8");
                if (writer.toString().contains(token)) {
                    cookie = connection.getHeaderField("Set-Cookie");
                    if (cookie == null)
                        cookie = "";
                } else {
                    logger.info("URL " + url.getFile() + " returned code 200 but does not contain keyword '" + token + "'");
                    logger.debug("1000 first chars of response body: " + writer.toString().substring(0, 1000));
                }
            } else {
				logger.error("URL " + url.getFile() + " returned code " + connection.getResponseCode() + " : " + connection.getResponseMessage());
                if (System.getProperty("http.proxyHost") != null) {
                    logger.info("Using proxy=" + System.getProperty("http.proxyHost") + ":" + System.getProperty("http.proxyPort"));
                }
            }
        } catch (IOException e) {
            logger.error("URL test failed: " + url.getFile() + " => " + e.getMessage() + " (" + e.getClass().getName() + ")");
            if (System.getProperty("http.proxyHost") != null) {
                logger.info("Using proxy=" + System.getProperty("http.proxyHost") + ":" + System.getProperty("http.proxyPort"));
            }
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ioe) {
                // just going to ignore this one
            }
        }
        return cookie;

    }


}
