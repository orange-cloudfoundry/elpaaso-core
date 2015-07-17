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
package com.francetelecom.clara.cloud.coremodel;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

import org.springframework.util.Assert;

@XmlRootElement
@Embeddable
public class ConfigValue {

    @NotNull
	private String configSet;
    public static final int MAX_CONFIG_KEY_LENGTH= 255;
	@NotNull
    @Size(min = 0, max = MAX_CONFIG_KEY_LENGTH)
    @Column(length = MAX_CONFIG_KEY_LENGTH)
    private String key;

    public static final int MAX_CONFIG_VALUE_LENGTH= 10000;

    @Size(min = 0, max = MAX_CONFIG_VALUE_LENGTH)
    @Column(length = MAX_CONFIG_VALUE_LENGTH)
	private String value;

    public static final int MAX_CONFIG_COMMENT_LENGTH= 10000;

    @Size(min = 0, max = MAX_CONFIG_COMMENT_LENGTH)
    @Column(length = MAX_CONFIG_COMMENT_LENGTH)
	private String comment;

	protected ConfigValue() {
	}

	public ConfigValue(String configSet, String key, String value, String comment) {
		super();
		setConfigSet(configSet);
		setKey(key);
		setValue(value);
		setComment(comment);
	}

	/**
	 * @return the configSet
	 */
	public String getConfigSet() {
		return configSet;
	}

	/**
	 * @param configSet
	 *            the configSet to set
	 */
	private void setConfigSet(String configSet) {
		Assert.hasText(configSet, "Cannot create ConfigValue: config set should not be empty");
		this.configSet = configSet;
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
	private void setKey(String key) {
		Assert.hasText(key, "Cannot create ConfigValue: key should not be empty");
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
	private void setValue(String value) {
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
	private void setComment(String comment) {
		this.comment = comment;
	}
}
