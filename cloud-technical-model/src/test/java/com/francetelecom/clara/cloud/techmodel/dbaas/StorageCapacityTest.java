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
package com.francetelecom.clara.cloud.techmodel.dbaas;

import org.junit.Assert;
import org.junit.Test;

import com.francetelecom.clara.cloud.techmodel.dbaas.StorageCapacity.SizeUnit;

public class StorageCapacityTest {

	@Test
	public void _1000_MB_should_give_1000_MB() {
		StorageCapacity storageCapacity = new StorageCapacity(1000.0F, SizeUnit.MB);
		Assert.assertTrue(1000.0F == storageCapacity.getValue(SizeUnit.MB));
	}

	@Test
	public void _1000_MB_should_give_1_GB() {
		StorageCapacity storageCapacity = new StorageCapacity(1024.0F, SizeUnit.MB);
		Assert.assertTrue(1.0F == storageCapacity.getValue(SizeUnit.GB));
	}

	@Test
	public void _1200_MB_should_give_2_GB() {
		StorageCapacity storageCapacity = new StorageCapacity(1200.0F, SizeUnit.MB);
		int ceil = storageCapacity.ceil(SizeUnit.GB);
		Assert.assertTrue(2.0F == ceil);
	}

	@Test
	public void _1_GB_should_give_1000_MB() {
		StorageCapacity storageCapacity = new StorageCapacity(1.0F, SizeUnit.GB);
		Assert.assertTrue(1024.0F == storageCapacity.getValue(SizeUnit.MB));
	}

	@Test
	public void _1_GB_should_give_1_GB() {
		StorageCapacity storageCapacity = new StorageCapacity(1.0F, SizeUnit.GB);
		Assert.assertTrue(1.0F == storageCapacity.getValue(SizeUnit.GB));
	}

}
