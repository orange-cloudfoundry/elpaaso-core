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
package com.francetelecom.clara.cloud.webapp.smoketest;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.ProxyConfig;
import com.gargoylesoftware.htmlunit.WebAssert;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebClientOptions;
import com.gargoylesoftware.htmlunit.html.HTMLParserListener;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

/**
 * BaseHmlUnit
 * Class to use to create portal unit test
 */
@ContextConfiguration(classes = BaseHtmlUnitContext.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class BaseHmlUnit {
    protected static Logger log = LoggerFactory.getLogger(BaseHmlUnit.class.getName());
    protected static WebClient webClient;
    protected static HtmlPage home;
    @Autowired
    protected BaseSmokeTestConfig config;

    protected String urlString;

    public BaseHmlUnit() {
        log.info("");
    }

    @BeforeClass
    public static void setUp() throws InterruptedException {
        log.debug("setUp");
        webClient = new WebClient();
		WebClientOptions options = webClient.getOptions();
		options.setPrintContentOnFailingStatusCode(true);
		options.setJavaScriptEnabled(true);
        webClient.setHTMLParserListener(HTMLParserListener.LOG_REPORTER);
        webClient.setAjaxController(new NicelyResynchronizingAjaxController());
        ProxyConfig proxy = new ProxyConfig("proxy.redacted-domain.org", 3128);
        proxy.addHostsToProxyBypass("127.0.0.1");
		options.setProxyConfig(proxy);
    }

    @Before
    public void setup() throws IOException {
        String hostProperty = System.getProperty("jonas.host");
        if (hostProperty != null) {
            this.config.setHost(hostProperty);
        }

        String portProperty = System.getProperty("webcontainer.port");
        if (portProperty != null) {
            this.config.setPort(Long.parseLong(portProperty));
        }

        this.urlString = "http://" + config.getHost() + ":" + config.getPort()+ "/paas-portal/";
        log.debug("setUp urlString=" + urlString);
        final HtmlPage page = webClient.getPage(urlString);
        WebAssert.assertTitleEquals(page, "GASSI - El Paaso login");

        final HtmlForm form = page.getFormByName("loginform");
        final HtmlSubmitInput button = form.getInputByName("submit");
        final HtmlTextInput login = form.getInputByName("login");
        final HtmlPasswordInput password = form.getInputByName("password");
        login.setText("tescalle");
        password.setText("tescalle");
        home = button.click();
        // TODO : uncomment when dbaas deletion will be working on env deletion
//        home = webClient.getPage("http://" + host + ":" + port + "/paas-portal/app/scalability/setup");
    }


    @AfterClass
    public static void cleanUp() throws IOException, InterruptedException {
        // call scalability page to cleanup test datas
        // TODO : uncomment when dbaas deletion will be working on env deletion
//        home = webClient.getPage("http://" + host + ":" + port + "/paas-portal/app/scalability/cleanup");
        webClient.closeAllWindows();
    }

}
