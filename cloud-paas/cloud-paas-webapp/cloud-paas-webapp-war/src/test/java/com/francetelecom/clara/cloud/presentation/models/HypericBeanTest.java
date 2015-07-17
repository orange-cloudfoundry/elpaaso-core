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

import org.junit.Assert;
import org.junit.Test;

public class HypericBeanTest {

	@Test
	public void should_get_server_non_secured_URL() {
		HypericBean bean = new HypericBean();
		bean.setHypericIp("124.0.0.1");
		bean.setHypericPort("80");
		bean.setSecured(false);
		Assert.assertEquals("http://124.0.0.1:80", bean.getServerURL());
	}
	
	@Test
	public void should_get_server_secured_URL() {
		HypericBean bean = new HypericBean();
		bean.setHypericIp("124.0.0.1");
		bean.setHypericPort("80");
		bean.setSecured(true);
		Assert.assertEquals("https://124.0.0.1:80", bean.getServerURL());
	}

}
