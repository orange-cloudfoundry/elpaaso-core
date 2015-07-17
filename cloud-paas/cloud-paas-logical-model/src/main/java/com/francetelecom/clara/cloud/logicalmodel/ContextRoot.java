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
package com.francetelecom.clara.cloud.logicalmodel;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.Pattern;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.francetelecom.clara.cloud.commons.GuiMapping;
import com.francetelecom.clara.cloud.commons.TechnicalException;

/**
 * A context root identifies a web application in a Java EE server.
 * <P>
 * A context root must start with a forward slash (/) and end with a string.
 * <P>
 * 
 */
@Embeddable
@XmlRootElement
public class ContextRoot implements Serializable {

	@GuiMapping(status = GuiMapping.StatusType.SUPPORTED)
	@Pattern(regexp = "\\/|(\\/[a-zA-Z]+[a-zA-Z_0-9\\-\\.]*)+")
	@Column(name = "contextroot", nullable=false)
	private String value;

	// required by JPA
	public ContextRoot() {
	}

	public ContextRoot(String value) {
		if (value == null || !value.matches("\\/|(\\/[a-zA-Z]+[a-zA-Z_0-9\\-\\.]*)+"))
			throw new TechnicalException("Cannot create context root. A context root must start with a forward slash (/). Value: "+value);
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	/**
	 * @return true if value is a forward slash (/)
	 */
	public boolean isRoot() {
		return this.value.matches("\\/");
	}

	public String toString() {
		return value;
	}

	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

}
