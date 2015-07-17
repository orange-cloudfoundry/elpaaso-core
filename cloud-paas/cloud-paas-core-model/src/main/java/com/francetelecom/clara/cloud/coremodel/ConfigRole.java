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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.springframework.util.Assert;

import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.francetelecom.clara.cloud.commons.UUIDUtils;

import org.apache.commons.lang3.StringUtils;

/**
 * A {@link ConfigRole} holds a set of {@link ConfigValue}. It is associated to an environment at creation time
 * to specify the configuration properties to override.
 */
@XmlRootElement
@Entity
@Table(name = "CONFIG_ROLE")
public class ConfigRole extends CoreItem {

	private static final long serialVersionUID = 2354695605078990373L;
    public static final int MAX_COMMENT_SIZE = 255;

    @NotNull
	private Date lastModificationDate;

	@Size(max = MAX_COMMENT_SIZE)
	private String lastModificationComment;
	
	@NotNull
	private String applicationUID;

	@XmlElementWrapper
	@XmlElement(name = "configValues")
	@ElementCollection(fetch=FetchType.EAGER)
	@CollectionTable(name = "CONFIG_ROLE_VALUES")
	private Set<ConfigValue> values = new HashSet<>();

	protected ConfigRole() {
	}

	public ConfigRole(String applicationUID) {
		super(UUIDUtils.generateUUID("cfg"));

		Assert.hasText(applicationUID, "Cannot create ConfigRole: applicationUID should not be empty");
		this.applicationUID = applicationUID;

		lastModificationDate = new Date();
	}

	/**
	 * @return the lastModificationDate
	 */
	public Date getLastModificationDate() {
		return lastModificationDate;
	}

	/**
	 * @param lastModificationDate
	 *            the lastModificationDate to set
	 */
	public void setLastModificationDate(Date lastModificationDate) {
		this.lastModificationDate = lastModificationDate;
	}

	/**
	 * @return the lastModificationComment
	 */
	public String getLastModificationComment() {
		return lastModificationComment;
	}

	/**
	 * @return the applicationUID
	 */
	public String getApplicationUID() {
		return applicationUID;
	}

	/**
	 * @param lastModificationComment
	 *            the lastModificationComment to set
	 */
	public void setLastModificationComment(String lastModificationComment) {
		this.lastModificationComment = StringUtils.left(lastModificationComment, MAX_COMMENT_SIZE);
	}

	public List<ConfigValue> listValues() {
		return Collections.unmodifiableList(new ArrayList<ConfigValue>(values));
	}

	public void setValues(List<ConfigValue> configValues) {
		if (configValues == null || configValues.isEmpty() || configValues.contains(null) ) {
			throw new TechnicalException("Incorrect configuration values: can't be empty or contain null elements");
		}
		this.values.clear();
		this.values.addAll(configValues);
	}

}
