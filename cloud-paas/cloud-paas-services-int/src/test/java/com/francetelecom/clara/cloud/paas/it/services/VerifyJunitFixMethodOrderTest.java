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
package com.francetelecom.clara.cloud.paas.it.services;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * This test verifies that Junit 4.11 @FixMethodOrder is available and works within this module<br>
 * This test has been added as this features can be disabled if a previous junit version is used due to conflicts in maven dependencies
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class VerifyJunitFixMethodOrderTest {

	private static final String INVALID_ORDER_MESSAGE = "Invalid order in test execution: check you are using Junit 4.11 (or above)";
	public static int order = 0;
	
	@BeforeClass
	public static void initOrder() {
		order = 0;
	}
	
	@Before
	public void incrementOrder() {
		order++;
	}
	
	@Test
	public void test4() {
		assertEquals(INVALID_ORDER_MESSAGE, 4, order);
	}

	@Test
	public void test3() {
		assertEquals(INVALID_ORDER_MESSAGE, 3, order);
	}

	@Test
	public void test1() {
		assertEquals(INVALID_ORDER_MESSAGE, 1, order);
	}
	
	@Test
	public void test2() {
		assertEquals(INVALID_ORDER_MESSAGE, 2, order);
	}

}
