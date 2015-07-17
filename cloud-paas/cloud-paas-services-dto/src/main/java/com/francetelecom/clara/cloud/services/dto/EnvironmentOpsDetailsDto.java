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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Provides access to the environment internal, suiteable for paas-ops.
 *
 * This would be typically needed to troubleshoot the environment.
 */
public class EnvironmentOpsDetailsDto extends EnvironmentDetailsDto {

	private static final long serialVersionUID = 162405701711089072L;

    /**
     * Details paas-ops credentials for a single VM
     */
	public static class VMAccessDto implements Serializable {

		private static final long serialVersionUID = 1451274157402405697L;

        /**
         * The IP adress of VM on the external network
         */
		private String ip;

        /**
         * The Guest OS user account to use by paas-ops (e.g. "root"). Has sudo accesses.
         */
		private String user;

        /**
         * The Guest OS user account to use by paas-ops (e.g. "root"). Has sudo accesses.
         */
		private String password;

        /**
         * The Iaas ID of the VM (e.g. ID of the VM in vCloud Director)
         */
		private String iaasId;

        /**
         * The VCD computer name identifying the VM within the Vapp. This is also the hostname in the guestos.
         */
		private String hostname;
		
		public VMAccessDto(String ip, String user, String password, String iaasId, String hostname) {
			super();
			this.ip = ip;
			this.user = user;
			this.password = password;
			this.iaasId = iaasId;
			this.hostname = hostname;
		}

		public String getIp() {
			return ip;
		}

		public String getUser() {
			return user;
		}

		public String getPassword() {
			return password;
		}

		public String getIaasId() {
			return iaasId;
		}

		public String getHostname() {
			return hostname;
		}
	}
	
	private List<VMAccessDto> vmAccesses;

	/**
	 * Creates an EnvironmentDto which carry environment informations for presentation layer.
	 * @param uid Environment uid returned by {@link #createEnvironment(DeploymentProfileEnum, String, String, String)}
	 * @param internalName TODO
	 * @param label A human-readeable name of the environment given as parameter in {@link #createEnvironment(DeploymentProfileEnum, String, String, String)}
	 * @param applicationLabel The application label that is deployed on the environment
	 * @param releaseUID the UID of the application release deployed on the environment
	 * @param releaseVersion The application release version that is deployed on the environment
	 * @param ownerId The primary owner identifier of the environment
	 * @param ownerName The primary owner name of the environment
	 * @param creationDate Creation date of the environment
	 * @param type Environment type
	 * @param status Environment status
	 * @param statusMessage Environment status message (error)
	 * @param statusPercent Environment status progress in percentage, -1 if not set
	 * @param comment Environment comment
	 * @param tdiTdName Technical deployment instance
	 */
	public EnvironmentOpsDetailsDto(String uid, String internalName, String label, String applicationLabel, String releaseUID, String releaseVersion,
			String ownerId, String ownerName, Date creationDate, EnvironmentTypeEnum type, EnvironmentStatusEnum status, String statusMessage, int statusPercent, String comment, String tdiTdName) {
		super(uid, internalName, label, applicationLabel, releaseUID, releaseVersion, ownerId, ownerName, creationDate, type, status, statusMessage, statusPercent, comment, tdiTdName);
		vmAccesses = new ArrayList<VMAccessDto>();
	}
	
	public void addVMAccess(VMAccessDto vmAccess) {
		vmAccesses.add(vmAccess);
	}

    public List<VMAccessDto> listVMAccesses() {
        return Collections.unmodifiableList(vmAccesses);
    }

}
