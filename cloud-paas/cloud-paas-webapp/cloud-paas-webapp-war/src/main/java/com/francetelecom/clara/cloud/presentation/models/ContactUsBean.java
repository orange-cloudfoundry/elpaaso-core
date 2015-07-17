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
package com.francetelecom.clara.cloud.presentation.models;

import java.io.Serializable;

import org.springframework.util.Assert;

public class ContactUsBean implements Serializable {


	/**
	 * 
	 */
	private static final long serialVersionUID = 2210375382793101749L;
	private static final String MAILTO = "mailto:";
	private String mailTo;
	private static final String EMAIL_OK_PATTERN = 
			"^[_a-z0-9-]+(\\.[_a-z0-9-]+)*@[a-z0-9-]+(\\.[a-z0-9-]+)+$";
	
	
			

	public String getMailTo() {
		return this.mailTo;
	}

	public void setMailTo(String mailTo) {
		Assert.hasText(mailTo);		
		Assert.isTrue(mailTo.matches(EMAIL_OK_PATTERN));
		this.mailTo = mailTo;
	}

}
