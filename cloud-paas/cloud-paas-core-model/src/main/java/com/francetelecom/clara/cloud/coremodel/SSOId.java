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

import org.apache.commons.lang3.builder.CompareToBuilder;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

/**
 * A {@link SSOId} identifies a PaaS user in ElPaaSo.
 * <P>
 * A SSO Id must start with some letters followed by some optional digits.
 * <P>
 */
@Embeddable
public class SSOId implements Serializable, Comparable {

	@Column(name="ssoid",nullable=false)
	@NotNull
	private String value;

	
	/**
	 * required by jpa
	 */
	protected SSOId() {
	}

	public SSOId(String value) {
		if (value == null || !value.matches("[a-zA-Z]+[0-9]*")) {
			throw new IllegalArgumentException(
					"Invalid SSOId value <" + value + ">. SSOId must match regular expression [a-zA-Z]+[0-9]*.");
		}

		this.value = value.toLowerCase();
	}

	public String getValue() {
		return value;
	}

    @Override
    public int compareTo(Object o) {
        return CompareToBuilder.reflectionCompare(value.toLowerCase(), o);
    }

    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.toLowerCase().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SSOId other = (SSOId) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equalsIgnoreCase(other.value))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return value;
	}

	
}
