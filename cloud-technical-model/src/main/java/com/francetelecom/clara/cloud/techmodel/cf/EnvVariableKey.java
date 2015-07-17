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
public class EnvVariableKey {


	public static final String VALID_VARENV_NAME_REGEXP = "[a-zA-Z_]+[a-zA-Z0-9_.]*";
	private static Logger logger=LoggerFactory.getLogger(EnvVariableKey.class.getName());

	/**
	 * Returning a changed key to be compatible with Linux env variable constraints.
	 * - replace . with a _ (dot / underscore)
	 * - TBC
	 * @return
	 */
	public static String escapeToSystemEnvVariableName(String intialKey){
		String escapedKey=intialKey.replace('.', '_');
		if (!escapedKey.equals(intialKey)){
			logger.debug("replace initial key {} with escaped key {}",intialKey,escapedKey);
		}
		return escapedKey;
	}


    @Size(max = 4000)
	@NotNull
	@Column(name = "`key`", length = 4000)
	private String key;

	/**
	 * protected constructor for jpa
	 */
	@Deprecated
	protected EnvVariableKey() {
	}

	public EnvVariableKey(String key) {
		setKey(escapeToSystemEnvVariableName(key));
	}


    public String getKey() {
		return key;
	}
	
	private void setKey(String key) {
		if (key == null || !isValidEnvName(key)) {
			throw new IllegalArgumentException("Env variable name <" + key + "> is not valid. Should match "+VALID_VARENV_NAME_REGEXP);
		}
		this.key = key;
	}

	private boolean isValidEnvName(String key) {
		return key.matches(VALID_VARENV_NAME_REGEXP);
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
