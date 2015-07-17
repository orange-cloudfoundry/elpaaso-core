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
package com.francetelecom.clara.cloud.webapp.acceptancetest;

import com.francetelecom.clara.cloud.webapp.ElpaasoEndpointContext;
import com.francetelecom.clara.cloud.webapp.cucumber.acceptancetest.gui.stories.GuiStoriesContext;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.test.annotation.DirtiesContext;

import java.net.URL;

/**
 * Created by wooj7232 on 10/03/2015.
 */
@Configuration
@Import({ElpaasoEndpointContext.class,GuiStoriesContext.class})
public class BasePageITContext {

    @Autowired
    @Qualifier("elpaasoBaseEndpoint")
    String elpaasoBaseEndpoint;

    @Autowired
    URL webappLocation;

    @Autowired
    URL populatePageLocation;

    @Bean
    @Scope(value = "prototype")
    public BaseTestConfig baseTestConfig(){
        BaseTestConfig baseTestConfig = new BaseTestConfig();
        baseTestConfig.setPopulatePageLocation(populatePageLocation);
        baseTestConfig.setWebappLocation(webappLocation);
        baseTestConfig.setWebDriver(new HtmlUnitDriver(BrowserVersion.FIREFOX_24));
        return baseTestConfig;

    }

}
