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

public class HypericBean implements Serializable {

	private String hypericIp = "disabled-feature.redacted-domain.org";
	private String hypericPort = "80";

    /**
     * Access hyperic using https or http
     */
	private boolean secured = true;

    public String getHypericIp() {
        return hypericIp;
    }

    public String getHypericPort() {
        return hypericPort;
    }

    public void setHypericIp(String hypericIp) {
        this.hypericIp = hypericIp;
    }

    public void setHypericPort(String hypericPort) {
        this.hypericPort = hypericPort;
    }

	public boolean isSecured() {
		return secured;
	}

	public void setSecured(boolean secured) {
		this.secured = secured;
	}

	public String getServerURL() {
		if (secured) {
			return "https://" + this.getHypericIp() + ":" + this.getHypericPort();
		} else {
			return "http://" + this.getHypericIp() + ":" + this.getHypericPort();
		}
	}
}
