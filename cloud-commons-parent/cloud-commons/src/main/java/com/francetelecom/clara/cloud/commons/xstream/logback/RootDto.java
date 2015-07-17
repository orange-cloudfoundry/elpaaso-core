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
package com.francetelecom.clara.cloud.commons.xstream.logback;

import java.util.ArrayList;
import java.util.List;

public class RootDto {

	ConfigurationDto configuration;
	List<AppenderRefDto> appenderRefs;
	String level;

	public RootDto(ConfigurationDto configuration, String level) {
		this.configuration= configuration;
		configuration.root = this;
		this.level = level;
		appenderRefs = new ArrayList<AppenderRefDto>();
	}

}
