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

import com.francetelecom.clara.cloud.presentation.WicketApplication;
import com.francetelecom.clara.cloud.presentation.login.SignUpLinks;
import com.francetelecom.clara.cloud.presentation.models.ContactUsBean;
import com.francetelecom.clara.cloud.presentation.models.HypericBean;
import com.francetelecom.clara.cloud.presentation.models.SplunkBean;
import com.francetelecom.clara.cloud.scalability.ManageStatistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * Created by WOOJ7232 on 22/05/2015.
 */
@Configuration
public class WicketContextApplication {
	
	@Autowired
	ManageStatistics manageStatistics;

	

	@ConfigurationProperties("splunk.portal")
	@Bean
	public SplunkBean splunkBean(){
		SplunkBean splunkBean = new SplunkBean();
		return splunkBean;
	}
	
	@Bean
	public ContactUsBean contactUsBean(){
		ContactUsBean contactUsBean = new ContactUsBean();
		contactUsBean.setMailTo(mailTo);
		return contactUsBean;
	}
	
	@Bean
	public HypericBean hypericBean(){
		return new HypericBean();
	}

	@Value("${pwm.signup.url}")
	String pwmSignupUrl;

	@Value("${pwm.recover.url}")
	String pwmRecoverUrl;
	
	@Value("${contactus.mailTo}")
	String mailTo;

	@Bean
	public SignUpLinks signUpLinks(){
		SignUpLinks signUpLinks = new SignUpLinks();
		signUpLinks.setSignupUrl(pwmSignupUrl);
		signUpLinks.setForgottenUrl(pwmRecoverUrl);
		return signUpLinks;
	}

	@Bean
	@Scope("singleton")
	public WicketApplication wicketApplication(){
		WicketApplication wicketApplication = new WicketApplication();
		wicketApplication.setManageStatistics(manageStatistics);
		return wicketApplication;
	}

}
