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

import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Structure holding projection plans informations, issued par
 * CSP solver engine.
 * 
 * Key value in best target TechnicalModel given the LogicalModel and
 * architectural constraints
 * 
 * This assumes a scale-out architecture with the N Jonas VM, and N Jonas instance per VM
 * @author apog7416
 *
 */
public class ProjectionPlan {
	
	/** Number of Jonas PlatformServers */
	public int vmNumber;
	
	/** Amount of RAM in each PlatformServer.
     *
     * Note: This is not used by projection, which instead computes it in
     * BaseProjectionServiceImpl.assignPlatformServerSizing() */
	public int memoryMbPerVm;

	/** Amount of ApplicationServerInstance per PlatformServer */
	public int wasPerVm;

	/** Max Amount of RAM in each ApplicationServerInstance */
	public int memoryMbPerWas;

	/** Number of sessions per WAS.
     *
     * Note: This is mainly used for asserting the projection plan computation, but is not used in the technical model.
     *
	 * http://tomcat.apache.org/tomcat-5.5-doc/cluster-howto.html Only tomcat cluster seem to allow setting limits
	 * Max threads don't control number of sessions http://mail-archives.apache.org/mod_mbox/tomcat-users/200901.mbox/%3C49609808.2040307@apache.org%3E */
	public int sessionPerWas;

	public int getVmNumber() {
		return vmNumber;
	}

	public void setVmNumber(int vmNumber) {
		this.vmNumber = vmNumber;
	}

	public int getMemoryMbPerVm() {
		return memoryMbPerVm;
	}

	public void setMemoryMbPerVm(int memoryMbPerVm) {
		this.memoryMbPerVm = memoryMbPerVm;
	}

	public int getWasPerVm() {
		return wasPerVm;
	}

	public void setWasPerVm(int wasPerVm) {
		this.wasPerVm = wasPerVm;
	}

	public int getMemoryMbPerWas() {
		return memoryMbPerWas;
	}

	public void setMemoryMbPerWas(int memoryMbPerWas) {
		this.memoryMbPerWas = memoryMbPerWas;
	}

	public int getSessionPerWas() {
		return sessionPerWas;
	}

	public void setSessionPerWas(int sessionPerWas) {
		this.sessionPerWas = sessionPerWas;
	}

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

	public int getInstanceCount() {
		return getVmNumber() * getWasPerVm();
	}

}
