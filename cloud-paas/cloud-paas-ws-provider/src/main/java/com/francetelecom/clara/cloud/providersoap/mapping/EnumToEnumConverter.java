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
package com.francetelecom.clara.cloud.providersoap.mapping;

import org.dozer.CustomConverter;

public class EnumToEnumConverter implements CustomConverter {

	@Override
	public Object convert(Object existingDestinationFieldValue, Object sourceFieldValue, Class<?> destinationClass, Class<?> sourceClass) {
		if (null == sourceFieldValue)
			return null;
		if (Enum.class.isAssignableFrom(sourceClass) && Enum.class.isAssignableFrom(destinationClass)) {
			return Enum.valueOf((Class<Enum>) destinationClass, sourceFieldValue.toString());
		}
		return null;
	}

}
