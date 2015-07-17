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
package com.francetelecom.clara.cloud.deployment.result;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Store a deployment rule validation message (information, warning or error)
 */
public class RuleValidationMessage {

    private String message;

    private CriticityEnum criticity;

	private static Logger logger = LoggerFactory.getLogger(RuleValidationMessage.class);

    public RuleValidationMessage(String message, CriticityEnum criticity) {
        this.setMessage(message);
        this.setCriticity(criticity);
        logger.debug("{} ({})", message, criticity);
    }

	/**
	 * @param message the message to set
	 */
	private void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param criticity the criticity to set
	 */
	private void setCriticity(CriticityEnum criticity) {
		this.criticity = criticity;
	}

	/**
	 * @return the criticity
	 */
	public CriticityEnum getCriticity() {
		return criticity;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((criticity == null) ? 0 : criticity.hashCode());
		result = prime * result + ((message == null) ? 0 : message.hashCode());
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
		RuleValidationMessage other = (RuleValidationMessage) obj;
		if (criticity != other.criticity)
			return false;
		if (message == null) {
			if (other.message != null)
				return false;
		} else if (!message.equals(other.message))
			return false;
		return true;
	}
}
