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
package com.francetelecom.clara.cloud.services.dto;

import java.net.URL;

import javax.validation.constraints.NotNull;

import com.francetelecom.clara.cloud.commons.GuiMapping;

public class ApplicationDTO {

	private static final long serialVersionUID = 3258703452143929264L;

	/**
	 * application uid.
	 */
	private String uid;

	/**
	 * application code
	 */
	@NotNull
	private String code;

	/**
	 * application label
	 */
	@NotNull
	private String label;

	/**
	 * application description.
	 */
	private String description;

	/**
	 * FT CARTO url link.
	 */
	@GuiMapping(status = GuiMapping.StatusType.SKIPPED)
	private URL applicationRegistryUrl;

	/**
	 * public constructor
	 */
	public ApplicationDTO() {
	}

	public ApplicationDTO(String uid, String code, String label, String description, URL applicationRegistryUrl) {
		super();
		this.uid = uid;
		this.code = code;
		this.label = label;
		this.description = description;
		this.applicationRegistryUrl = applicationRegistryUrl;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getUid() {
		return uid;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public URL getApplicationRegistryUrl() {
		return applicationRegistryUrl;
	}

	public void setApplicationRegistryUrl(URL applicationRegistryUrl) {
		this.applicationRegistryUrl = applicationRegistryUrl;
	}

	public String getLabel() {
		return label;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public void setLabel(String label) {
		this.label = label;
	}

}
