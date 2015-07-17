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
import java.util.Date;

import javax.validation.constraints.Size;

/**
 * Smallest representation of an Environment. Subclasses DTO add further details.
 *
 * An environment is a deployment dedicated to a given application release for a given
 * usage (e.g. "Paul's development environment for Springoo 1.0.1 release).
 */
public class EnvironmentDto implements Serializable {

    private static final long serialVersionUID = -1365579789015743608L;

    public enum EnvironmentTypeEnum {
		DEVELOPMENT, TEST, LOAD_TEST, PRE_PROD, PRODUCTION
	}

    /**
     * The visible state for an environment.
     *
     * Maps to EnvironmentStatus. TODO: refactor once EnvironmentStatus is cleaned up.
     */
	public enum EnvironmentStatusEnum {
		/**
		 * Default state. The environment creation request has not been sent to
		 * activation module.
		 **/
		TRANSIENT,

		/** The environment creation is in progress **/
		CREATING,

		/**
		 * Default state. The environment creation request was received and is
		 * pending.
		 **/
		CREATED,

		/**
		 * The environment is starting / initializing OS, networks,
		 * communication to services
		 **/
		STARTING,

		/**
		 * The environment is started and ready to be used. This may be the case
		 * either following its initial creation, or following a stop/start
		 * cycle.
		 * 
		 * TODO: consider if refinements with a new substate for the case of an
		 * environment being partially started, such as DBaaS powered down while
		 * the other XaaS (e.g. IaaS, store) are properly up.
		 */
		STARTED,

		/** The environment is fully working **/
		RUNNING,

		/** The environment is stopping **/
		STOPPING,

		/**
		 * The environment was properly created and is current stopped and can
		 * not be currently used without being started. This is typically the
		 * case when trying to save power and stopping environment during
		 * inactivity periods (especially non-production environments).
		 */
		STOPPED,

		/**
		 * The environment is removing. All vm's, network connections and
		 * services will be removed
		 */
		REMOVING,

		/**
		 * The environment and all vm's, network connections and services have
		 * been removed
		 */
		REMOVED,

		/**
		 * The creation of the environment failed hard. The environment may be
		 * kept in this state to perform manual diagnosis by the PaaS team.
		 * Usually, this state is final and the environment would not be
		 * repaired. This is not a transient error (such as DBaaS powered down
		 * and the environment is in a corrupted state)
		 */
		FAILED,

		/** the environment is in unknown state */
		UNKNOWN,

		/** The environment creation is discarding */
		DISCARDING,

		/** the environment creation has been discarded */
		DISCARDED,

	}

	/** Environment identifier returned by ManageEnvironment#createEnvironment(String, DeploymentProfileEnum, String, String)} */
	private String uid;
	
	/** A human-readeable uid of the environment given as parameter in ManageEnvironment#createEnvironment(String, DeploymentProfileEnum, String, String)} */
	private String label;

	/** The environment id*/
	private String internalName;

	/** The application label that is deployed on the environment */
	private String applicationLabel;

    /** The application release version that is deployed on the environment */
    private String releaseVersion;

    /** The UID of the application release that is deployed on the environment */
	private String releaseUID;
     
	/** The primary owner identifier of the environment */
	private String ownerId;
	
	/** The primary owner uid of the environment */
	private String ownerName;
	
	
	/** The creation date of the environment */
	private Date creationDate;

	/** Environment type */
	private EnvironmentTypeEnum type;
	
	/** Environment status */
	private EnvironmentStatusEnum status;

	/** Environment status message (mostly used for failed status) */
	private String statusMessage;
	
	/** Environment status progress in percentage, -1 if not set */
	private int statusPercent;

    /** Env comment **/
	@Size(min = 0, max = 255)
    private String comment;

    /** Technical deployment instance TD uid **/
    private String tdiTdName;

	private boolean editable = true;

    /**
     * create ean empty environmentDto which will carry environment information for presentation layer
     */
    public EnvironmentDto() {
        super();
    }

    /**
	 * Creates an EnvironmentDto which carry environment informations for presentation layer.
	 * @param uid Environment internal uid
     * @param internalName environment internal name. set with cloudfoundry space name used to scope the environment
     * @param label A human-readeable uid of the environment
     * @param applicationLabel The application label
     * @param releaseUID the release UID
     * @param releaseVersion The application release version that is deployed on the environment
     * @param ownerId The primary owner identifier of the environment
     * @param ownerName The primary owner uid of the environment
     * @param creationDate Creation date of the environment
     * @param type Environment type
     * @param status Environment status
     * @param statusMessage Environment status message (error)
     * @param statusPercent Environment status progress in percentage, -1 if not set
     * @param comment Environment comment
     * @param tdiTdName Technical deployment instance Td uid
	 */
	public EnvironmentDto(String uid, String internalName, String label, String applicationLabel, String releaseUID, String releaseVersion, String ownerId, String ownerName, Date creationDate, EnvironmentTypeEnum type, EnvironmentStatusEnum status, String statusMessage, int statusPercent, String comment, String tdiTdName) {
		super();
		this.uid = uid;
		this.label = label;
		this.applicationLabel = applicationLabel;
        this.releaseUID = releaseUID;
        this.releaseVersion = releaseVersion;
		this.ownerId = ownerId;
		this.ownerName = ownerName;
		this.creationDate = creationDate;
		this.type = type;
		this.status = status;
		this.statusMessage = statusMessage;
		this.statusPercent = statusPercent;
        this.comment = comment;
        this.tdiTdName = tdiTdName;
        this.internalName = internalName;
	}

	public String getUid() {
		return uid;
	}

	public String getInternalName() {
		return internalName;
	}

	public String getLabel() {
		return label;
	}

	public String getOwnerId() {
		return ownerId;
	}

	public EnvironmentTypeEnum getType() {
		return type;
	}

	public EnvironmentStatusEnum getStatus() {
		return status;
	}

    public String getApplicationLabel() {
        return applicationLabel;
    }

    public String getReleaseVersion() {
        return releaseVersion;
    }

    public String getReleaseUID() {
        return releaseUID;
    }    

	public String getOwnerName() {
		return ownerName;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public String getStatusMessage() {
		return statusMessage;
	}

	public int getStatusPercent() {
		return statusPercent;
	}

    public String getComment() {
        return comment;
    }

    public String getTdiTdName() {
        return tdiTdName;
    }

    public boolean isEditable() {
    	return this.editable;
    }

    public void setEditable(boolean editable) {
		this.editable = editable;
	}
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((applicationLabel == null) ? 0 : applicationLabel.hashCode());
		result = prime * result + ((comment == null) ? 0 : comment.hashCode());
		result = prime * result + ((creationDate == null) ? 0 : creationDate.hashCode());
		result = prime * result + (editable ? 1231 : 1237);
		result = prime * result + ((internalName == null) ? 0 : internalName.hashCode());
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		result = prime * result + ((ownerId == null) ? 0 : ownerId.hashCode());
		result = prime * result + ((ownerName == null) ? 0 : ownerName.hashCode());
		result = prime * result + ((releaseUID == null) ? 0 : releaseUID.hashCode());
		result = prime * result + ((releaseVersion == null) ? 0 : releaseVersion.hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		result = prime * result + ((statusMessage == null) ? 0 : statusMessage.hashCode());
		result = prime * result + statusPercent;
		result = prime * result + ((tdiTdName == null) ? 0 : tdiTdName.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((uid == null) ? 0 : uid.hashCode());
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
		EnvironmentDto other = (EnvironmentDto) obj;
		if (applicationLabel == null) {
			if (other.applicationLabel != null)
				return false;
		} else if (!applicationLabel.equals(other.applicationLabel))
			return false;
		if (comment == null) {
			if (other.comment != null)
				return false;
		} else if (!comment.equals(other.comment))
			return false;
		if (creationDate == null) {
			if (other.creationDate != null)
				return false;
		} else if (!creationDate.equals(other.creationDate))
			return false;
		if (editable != other.editable)
			return false;
		if (internalName == null) {
			if (other.internalName != null)
				return false;
		} else if (!internalName.equals(other.internalName))
			return false;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		if (ownerId == null) {
			if (other.ownerId != null)
				return false;
		} else if (!ownerId.equals(other.ownerId))
			return false;
		if (ownerName == null) {
			if (other.ownerName != null)
				return false;
		} else if (!ownerName.equals(other.ownerName))
			return false;
		if (releaseUID == null) {
			if (other.releaseUID != null)
				return false;
		} else if (!releaseUID.equals(other.releaseUID))
			return false;
		if (releaseVersion == null) {
			if (other.releaseVersion != null)
				return false;
		} else if (!releaseVersion.equals(other.releaseVersion))
			return false;
		if (status != other.status)
			return false;
		if (statusMessage == null) {
			if (other.statusMessage != null)
				return false;
		} else if (!statusMessage.equals(other.statusMessage))
			return false;
		if (statusPercent != other.statusPercent)
			return false;
		if (tdiTdName == null) {
			if (other.tdiTdName != null)
				return false;
		} else if (!tdiTdName.equals(other.tdiTdName))
			return false;
		if (type != other.type)
			return false;
		if (uid == null) {
			if (other.uid != null)
				return false;
		} else if (!uid.equals(other.uid))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "EnvironmentDto [uid=" + uid + ", label=" + label + ", internalName=" + internalName + ", applicationLabel=" + applicationLabel + ", releaseVersion="
				+ releaseVersion + ", releaseUID=" + releaseUID + ", ownerId=" + ownerId + ", ownerName=" + ownerName + ", creationDate=" + creationDate + ", type=" + type
				+ ", status=" + status + ", statusMessage=" + statusMessage + ", statusPercent=" + statusPercent + ", comment=" + comment + ", tdiTdName=" + tdiTdName
				+ ", editable=" + editable + "]";
	}
	
}
