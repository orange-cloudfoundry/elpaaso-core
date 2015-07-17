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
package com.francetelecom.clara.cloud;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.core.context.SecurityContextHolder;

import com.francetelecom.clara.cloud.application.impl.AliceAuthentication;
import com.francetelecom.clara.cloud.application.impl.BobAuthentication;
import com.francetelecom.clara.cloud.coremodel.SSOId;

import static org.fest.assertions.Assertions.assertThat;

/**
 * TestHelper
 * <p>
 * Last update  : $LastChangedDate$
 * Last author  : $Author$
 *
 * @version : $Revision$
 */
public class TestHelper {
	
	public static final SSOId USER_WITH_ADMIN_ROLE_SSOID = new SSOId("bob123");
	public static final SSOId USER_WITH_USER_ROLE_SSOID = new SSOId("alice123");

    /**
     * generate a string out of length when
     *   a String attribute is stored and there is no size annotation and DBMS default varchar size is 255
     * @return
     */
    public static String generateOutOfLengthForDefaultString() {
        int limit = 255;
        String myString = RandomStringUtils.randomAlphanumeric(limit + 1);
        assertThat(myString).hasSize(256);
        return myString;
    }
    
    public static void loginAsAdmin() {
		SecurityContextHolder.getContext().setAuthentication(new BobAuthentication());
	}
    
    public static void loginAsUser() {
		SecurityContextHolder.getContext().setAuthentication(new AliceAuthentication());
	}
    
    public static void logout() {
		SecurityContextHolder.getContext().setAuthentication(null);
	}
}
