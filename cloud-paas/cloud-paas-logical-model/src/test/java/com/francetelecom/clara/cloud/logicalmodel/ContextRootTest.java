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
package com.francetelecom.clara.cloud.logicalmodel;

import org.junit.Assert;
import org.junit.Test;

import com.francetelecom.clara.cloud.commons.TechnicalException;

public class ContextRootTest {

	@Test(expected=TechnicalException.class)
	public void fail_to_create_context_root_with_null_value() {
		new ContextRoot(null);
	}
	
	@Test(expected=TechnicalException.class)
	public void fail_to_create_context_root_with_no_value() {
		new ContextRoot("");
	}
	
	@Test(expected=TechnicalException.class)
	public void fail_to_create_context_root_if_does_not_start_with_forward_slash() {
		new ContextRoot("dbaas");
	}
	
	@Test(expected=TechnicalException.class)
	public void fail_to_create_context_root_if_forward_slash_digit() {
		new ContextRoot("/1");
	}
	
	@Test
	public void forward_slash_should_be_a_valid_context_root() {
		ContextRoot contextRoot = new ContextRoot("/");
		Assert.assertEquals("/", contextRoot.getValue());
	}

	@Test
	public void forward_slash_dbaas_should_be_a_valid_context_root() {
		ContextRoot contextRoot = new ContextRoot("/dbaas");
		Assert.assertEquals("/dbaas", contextRoot.getValue());
	}
	
	@Test
	public void forward_slash_dbaas1_should_be_a_valid_context_root() {
		ContextRoot contextRoot = new ContextRoot("/dbaas1");
		Assert.assertEquals("/dbaas1", contextRoot.getValue());
	}
	
	@Test(expected=TechnicalException.class)
	public void forward_slash_dbaas1_forward_slash_should_not_be_a_valid_context_root() {
		new ContextRoot("/dbaas/");
	}
	
	@Test
	public void forward_slash_dbaas1_forward_slash_api_should_be_a_valid_context_root() {
		ContextRoot contextRoot = new ContextRoot("/dbaas1/api");
		Assert.assertEquals("/dbaas1/api", contextRoot.getValue());
	}
	
	@Test
	public void forward_slash_dbaas1_forward_slash_api_forward_slash_client_dash_1_dot_0_should_be_a_valid_context_root() {
		ContextRoot contextRoot = new ContextRoot("/dbaas1/api/client-1.0");
		Assert.assertEquals("/dbaas1/api/client-1.0", contextRoot.getValue());
	}
	
	@Test
	public void forward_slash_is_root() {
		ContextRoot contextRoot = new ContextRoot("/");
		Assert.assertTrue(contextRoot.isRoot());
	}
	
	@Test
	public void forward_slash_dbaas_is_not_root() {
		ContextRoot contextRoot = new ContextRoot("/dbaas");
		Assert.assertFalse(contextRoot.isRoot());
	}

}
