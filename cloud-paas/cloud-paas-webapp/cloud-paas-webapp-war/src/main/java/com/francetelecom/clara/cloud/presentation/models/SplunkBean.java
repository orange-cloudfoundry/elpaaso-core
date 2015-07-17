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
package com.francetelecom.clara.cloud.presentation.models;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: Thomas Escalle - tawe8231
 * Entity : FT/OLNC/RD/MAPS/MEP/MSE
 */
public class SplunkBean implements Serializable {

	/**
	 * splunk server ip
	 */
	private String ip;

	/**
	 * splunk server port
	 */
	private String port;

	/**
	 * Access splunk using https or http
	 */
	private boolean secured;


	/**
	 * @see #getIp()
	 * @deprecated in 2.1.0. Replaced by getIp
	 */
	@Deprecated
	public String getSplunkIp() {
		return ip;
	}

	/**
	 * @see #setIp(String)
	 * @deprecated in 2.1.0. Replaced by setIp
	 */
	@Deprecated
	public void setSplunkIp(String splunkIp) {
		this.ip = splunkIp;
	}

	/**
	 * @see #getPort()
	 * @deprecated in 2.1.0. Replaced by getPort
	 */
	@Deprecated
	public String getSplunkPort() {
		return port;
	}

	/**
	 * @see #setPort(String)
	 * @deprecated in 2.1.0. Replaced by setPort
	 */
	@Deprecated
	public void setSplunkPort(String splunkPort) {
		this.port = splunkPort;
	}

	public boolean isSecured() {
		return secured;
	}

	public void setSecured(boolean secured) {
		this.secured = secured;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getServerURL() {
		if (secured) {
			return "https://" + this.getIp() + ":" + this.getPort();
		} else {
			return "http://" + this.getIp() + ":" + this.getPort();
		}
	}

}
