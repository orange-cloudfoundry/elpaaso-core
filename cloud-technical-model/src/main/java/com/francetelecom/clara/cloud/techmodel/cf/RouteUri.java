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

import java.util.Random;

import javax.persistence.Embeddable;
import javax.xml.bind.annotation.XmlRootElement;

import org.springframework.util.Assert;

import com.francetelecom.clara.cloud.commons.FqdnHelper;
import com.google.common.net.InternetDomainName;

@Embeddable
@XmlRootElement
public class RouteUri {
	
	private String value;

	protected RouteUri() {
	}
	
	public RouteUri(String value) {
		setValue(value);
	}

	
	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		return value;
	}
	
	public String getDomain() {
		return InternetDomainName.from(value).parent().name();
	}

	public String getHost() {
		return InternetDomainName.from(value).parts().get(0);
	}

	public RouteUri withRandomHostPrefix() {
		Random random = new Random();
		int randomInt = random.nextInt(1000);
		return new RouteUri(new StringBuilder().append("c").append(randomInt).append("-").append(value).toString());
	}
	
	public RouteUri withHostPrefix(String prefix) {
		return new RouteUri(FqdnHelper.sanitizeAndTruncatePart(prefix, 10) + "-" + value);
	}

	private void setValue(String value) {
		Assert.hasText(value,"Fail to create route uri. route uri should not be empty.");
		this.value = value;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		RouteUri other = (RouteUri) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
	

}
