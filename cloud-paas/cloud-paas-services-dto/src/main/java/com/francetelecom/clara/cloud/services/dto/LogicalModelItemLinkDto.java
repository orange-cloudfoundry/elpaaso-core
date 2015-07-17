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

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.net.URL;

/**
 * Provides a URL associated to a logical model item. This may be various type
 * of url (such as splunk or webGui access Urls)
 */
@Deprecated
public class LogicalModelItemLinkDto implements Serializable {

	private static final long serialVersionUID = -7588129930887779071L;

	/**
	 * The URL
	 * */
	private URL url;

    /**
	 * The logical model item UID (as returned by LogicalModelItem.getName()) (e.g. "4ca909b0-4bbd-45eb-ae8f-6e754bd483cb")
	 */
	private String logicalModelItemId;

	public LogicalModelItemLinkDto(URL url, String logicalModelItemId) {
		super();
		this.url = url;
		this.logicalModelItemId = logicalModelItemId;
    }

	public URL getUrl() {
		return url;
	}

    public String getLogicalModelItemId() {
		return logicalModelItemId;
	}

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
