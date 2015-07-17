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
package com.francetelecom.clara.cloud.coremodel;

import com.francetelecom.clara.cloud.commons.DateHelper;
import com.francetelecom.clara.cloud.commons.UUIDUtils;
import com.francetelecom.clara.cloud.model.DeploymentProfileEnum;
import com.francetelecom.clara.cloud.model.TechnicalDeploymentInstance;
import com.francetelecom.clara.cloud.techmodel.cf.Space;
import org.springframework.util.Assert;

import javax.persistence.*;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;
import java.util.Set;

/**
 * Represents an Environment a deployment that is dedicated to a given
 * application release for a given usage (e.g. "Paul's development environment
 * for Springoo 1.0.1 release).
 */
@XmlRootElement
@Entity
@Table(name = "ENVIRONMENT")
@org.hibernate.annotations.Cache(usage = org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE)
public class Environment extends CoreItem {

	private static final int UNIQUE_LABEL_MAX_SIZE = 100;
	private static final String UNIQUE_LABEL_PREFIX = "env ";
	private static final String TRUNCATE_SUFFIX = "...";
	private static final long serialVersionUID = 253819499794917296L;
	public static final String STATUS_MESSAGE_SUFFIX = "[...]";
	/**
	 * Associated application release which defines the logical model.
	 */
	@ManyToOne
	ApplicationRelease applicationRelease;

	/**
	 * Primary owner of the environment. Implies the tenant used for selecting
	 * projection rules and software product catalog.
	 */
	@OneToOne
	private PaasUser paasUser;

	@OneToOne(cascade = CascadeType.ALL)
	private TechnicalDeploymentInstance technicalDeploymentInstance;

	/**
	 * environment creation date
	 */
	public static final String CREATION_DATE = "creationDate";
	protected Date creationDate;

	/**
	 * environment deletion date (should be null if environment is not REMOVED)
	 */
	protected Date deletionDate;

	@Enumerated(EnumType.STRING)
	private DeploymentProfileEnum type;
	@Enumerated(EnumType.STRING)
	private EnvironmentStatus status;

	/**
	 * Status message
	 */
	@Column(length = 5000)
	private String statusMessage;

	/**
	 * Percentage of progress, -1 if not set
	 */
	private int statusPercent = -1;

	/**
	 * environment label. this label is set by user when creating environment.
	 */
	private String label;

	@Size(min = 0, max = 255)
	private String comment;

	/**
	 * 
	 * @param type
	 *            environment type
	 * @param applicationRelease
	 *            application release
	 * @param paasUser
	 *            environment owner
	 * @param tdi
	 *            technical deployment instance
	 */
	public Environment(DeploymentProfileEnum type, String label, ApplicationRelease applicationRelease, PaasUser paasUser, TechnicalDeploymentInstance tdi) {
		super(UUIDUtils.generateUUID("e"));
		Assert.notNull(type, "cannot create environment : environment type should not be null");
		Assert.notNull(applicationRelease, "cannot create environment : environment release should not be null");
		Assert.hasText(label, "cannot create environment : environment label should not be empty");
		Assert.notNull(tdi, "cannot create environment : technical deployment instance should not be null");
		this.type = type;
		this.label = label;
		this.applicationRelease = applicationRelease;
		setOwner(paasUser);
		this.technicalDeploymentInstance = tdi;
		this.status = EnvironmentStatus.CREATING;
		this.creationDate = DateHelper.getNow();
		this.deletionDate = null;
	}

	public Environment() {

	}

	public ApplicationRelease getApplicationRelease() {
		return applicationRelease;
	}

	/**
	 * Accesses the current state stored in DB.
	 * 
	 * Beware that this is currently modified directly by the activation using
	 * directly DAOs, and without going through setter on this class. (see
	 * ActivationTaskHandlerCallback.handleRequest())
	 * 
	 * @return status of the environment
	 */
	public EnvironmentStatus getStatus() {
		return this.status;
	}

	/**
	 * used by Entite::toString() introspection
	 * 
	 * @return
	 */
	public String getStatusString() {
		return getStatus().toString();
	}

	/**
	 * Sets the current status. Set percent to 0 and reset status message.
	 * 
	 * Beware that this is currently modified directly by the activation using
	 * directly DAOs, and without going through setter on this class. (see
	 * ActivationTaskHandlerCallback.handleRequest())
	 * 
	 * @param status
	 *            status to set
	 */
	public void setStatus(EnvironmentStatus status) {
		if (status.equals(this.status)) {
			return;
		}
		this.status = status;
		this.statusMessage = "";
		this.statusPercent = 0;
		this.deletionDate = null;
		if (EnvironmentStatus.REMOVED.equals(status)) {
			deletionDate = DateHelper.getNow();
		}
	}

	public PaasUser getPaasUser() {
		return paasUser;
	}

	private void setOwner(PaasUser paasUser) {
		Assert.notNull(paasUser, "environment owner should not be null");
		this.paasUser = paasUser;
	}

	public DeploymentProfileEnum getType() {
		return type;
	}

	public TechnicalDeploymentInstance getTechnicalDeploymentInstance() {
		return technicalDeploymentInstance;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public Date getDeletionDate() {
		return deletionDate;
	}

	public String getStatusMessage() {
		return statusMessage;
	}

	public void setStatusMessage(String statusMessage) {
		this.statusMessage = statusMessage;
	}

	public int getStatusPercent() {
		return statusPercent;
	}

	public void setStatusPercent(int statusPercent) {
		this.statusPercent = statusPercent;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public void setAndTruncateStatusMessage(String message) {
		// To change body of created methods use File | Settings | File
		// Templates.
		if (message != null && message.length() > 5000) {
			message = message.substring(0, 4994) + STATUS_MESSAGE_SUFFIX;
		}
		statusMessage = message;
	}

	/**
	 * Build and return a unique environment label by prefixing label with uid
	 * in the form <uid>: <label>. The generated string is truncated to 100
	 * characters.
	 * 
	 * @return the generated label.
	 */
	public String getUniqueLabel() {
		String uniqueLabel = UNIQUE_LABEL_PREFIX + this.getUID() + ": " + this.getLabel();
		if (uniqueLabel.length() > UNIQUE_LABEL_MAX_SIZE) {
			uniqueLabel = uniqueLabel.substring(0, UNIQUE_LABEL_MAX_SIZE - TRUNCATE_SUFFIX.length());
			uniqueLabel += TRUNCATE_SUFFIX;
		}
		return uniqueLabel;
	}

	public boolean isOwner(String ssoId) {
		return getPaasUser().getSsoId().getValue().equals(ssoId);
	}

	/**
	 * This generate the log used by splunk (ie. to construct splunk dashboard)
	 * 
	 * @return
	 */
	public String toLogString() {
		StringBuilder sbInfoMsg = new StringBuilder();
		sbInfoMsg.append("createEnvironment query finished: id=").append(getTechnicalDeploymentInstance().getId()).append(", releaseUID=").append(getApplicationRelease().getUID())
				.append(", type=").append(getType()).append(", label=").append(getLabel()).append(", EnvTdiName=").append(getTechnicalDeploymentInstance().getName());
		return sbInfoMsg.toString();
	}

	public boolean isStopped() {
		return getStatus().equals(EnvironmentStatus.STOPPED);
	}

	public boolean isFailed() {
		return getStatus().equals(EnvironmentStatus.FAILED);
	}

	public boolean isRunning() {
		return getStatus().equals(EnvironmentStatus.RUNNING);
	}

	public boolean isStarting() {
		return getStatus().equals(EnvironmentStatus.STARTING);
	}

	public boolean isStopping() {
		return getStatus().equals(EnvironmentStatus.STOPPING);
	}

	public boolean isRemoving() {
		return getStatus().equals(EnvironmentStatus.REMOVING);
	}

	public boolean isRemoved() {
		return getStatus().equals(EnvironmentStatus.REMOVED);
	}

	public String getInternalName() {
		Set<Space> spaces = technicalDeploymentInstance.getTechnicalDeployment().listXaasSubscriptionTemplates(Space.class);
		return ((spaces.size() == 1) ? spaces.iterator().next().getSpaceName().getValue() : "undefined");
	}

	public void updateStatus(EnvironmentStatus newStatus, String message, int percent) {
		if (newStatus != null) {
			setStatus(newStatus);
		}
		setAndTruncateStatusMessage(message);
		if (percent > 100) {
			setStatusPercent(100);
		} else if (percent < 0) {
			setStatusPercent(-1);
		} else {
			setStatusPercent(percent);
		}
	}
}
