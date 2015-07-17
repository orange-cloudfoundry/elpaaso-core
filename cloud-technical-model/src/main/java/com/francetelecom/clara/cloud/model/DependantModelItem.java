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
package com.francetelecom.clara.cloud.model;

import java.util.Set;

public abstract class DependantModelItem extends ModelItem {
	
	private static final long serialVersionUID = -3819318785679634645L;

	public DependantModelItem() {
		super();
	}

	public DependantModelItem(String name) {
		super(name);
	}

	/**
	 * @return All DependantModelItem on which this DependantModelItem depends on. If not dependent on any element
     * it should return an empty collection
	 */
	abstract public Set<DependantModelItem> listDepedencies();
}
