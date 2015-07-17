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
package com.francetelecom.clara.cloud.techmodel.cf;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Represents the key of a Cf environment variable set on a Cf App
 */
@XmlRootElement
@Embeddable
public class EnvVariableValue {


	private static Logger logger=LoggerFactory.getLogger(EnvVariableValue.class.getName());


    @Size(max = 4000)
	@Column(length = 4000)
	@NotNull
	private String value;

	/**
	 * protected constructor for jpa
	 */
	@Deprecated
	protected EnvVariableValue() {
	}

	public EnvVariableValue(String value) {
		setValue(value);
	}


	public String getValue() {
		return value;
	}

	private void setValue(String value) {
		if (value == null) {
			throw new IllegalArgumentException("Value: <" + value + "> is not valid.");
		}
		this.value = value;
	}

	public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

}
