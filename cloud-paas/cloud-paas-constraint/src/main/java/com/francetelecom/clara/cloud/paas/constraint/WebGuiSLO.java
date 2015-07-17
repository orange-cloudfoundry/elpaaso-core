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
package com.francetelecom.clara.cloud.paas.constraint;

/**
 * Captures Service Level Objectives for a web GUI service.
 * 
 * Note: this class may eventually be moved to the logical model. Initially locating
 * it into cloud-paas-constraint allow for isolated prototyping of different SLO models
 * without globally impacting the project (i.e. with only local effects).
 */
public class WebGuiSLO {

	/**
	 * Max number of concurrent HTTP sessions. This mainly impacts RAM usage to maintain those
	 * concurrent session.
	 * 
	 * Note: this makes no assumptions about number of concurrent users and session timeouts
	 */
	int numSession = 16000;

	public int getNumSession() {
		return numSession;
	}

	public void setNumSession(int numSession) {
		this.numSession = numSession;
	} 

	//TODO: Add constraint about HA which parametrizes mix number of VMs to 2.
	
}
