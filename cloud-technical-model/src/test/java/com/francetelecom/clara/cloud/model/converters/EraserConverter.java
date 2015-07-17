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
package com.francetelecom.clara.cloud.model.converters;

import org.dozer.DozerConverter;


/**
 * Custom dozer converter which maps an integer value to Zero.
 * Usefull to reset id and version for Persistent entity clone feature
 * @author apog7416
 *
 */
public class EraserConverter extends DozerConverter<Integer,Integer>{

	public EraserConverter() {
		super(Integer.class, Integer.class);
	}

	@Override
	public Integer convertTo(Integer source, Integer destination) {
		return 0;
	}

	@Override
	public Integer convertFrom(Integer source, Integer destination) {
		return 0;
	}

}
