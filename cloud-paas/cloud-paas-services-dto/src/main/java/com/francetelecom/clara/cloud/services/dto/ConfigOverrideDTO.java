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

import com.francetelecom.clara.cloud.commons.ValidatorUtil;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class ConfigOverrideDTO {

    @NotNull
    @Size(min=1)
	private String configSet;

    public static final int MAX_CONFIG_KEY_LENGTH= 255;
    @NotNull
    @Size(min = 0, max = MAX_CONFIG_KEY_LENGTH)
    private String key;

    public static final int MAX_CONFIG_VALUE_LENGTH= 10000;

    @Size(min = 0, max = MAX_CONFIG_VALUE_LENGTH)
    private String value;

    public static final int MAX_CONFIG_COMMENT_LENGTH= 10000;

    @Size(min = 0, max = MAX_CONFIG_COMMENT_LENGTH)
    private String comment;
	
	public ConfigOverrideDTO() {
	}

	public ConfigOverrideDTO(String configSetLabel, String key, String value, String comment) {
		super();
		this.configSet = configSetLabel;
		this.key = key;
		this.value = value;
		this.comment = comment;
	}

	/**
	 * @return the configSetLabel
	 */
	public String getConfigSet() {
		return configSet;
	}

	/**
	 * @param configSetLabel
	 *            the configSetLabel to set
	 */
	public void setConfigSet(String configSetLabel) {
		this.configSet = configSetLabel;
	}

	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @param key
	 *            the key to set
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * @return the comment
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * @param comment
	 *            the comment to set
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	@Override
	public String toString() {
		return "ConfigOverrideDTO [configSet=" + configSet + ", key=" + key + ", value=" + value + ", comment=" + comment + "]";
	}

}
