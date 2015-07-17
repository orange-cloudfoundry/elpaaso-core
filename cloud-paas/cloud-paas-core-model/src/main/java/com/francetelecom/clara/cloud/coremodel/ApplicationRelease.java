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

import java.net.URL;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.springframework.util.Assert;

import com.francetelecom.clara.cloud.commons.GuiMapping;
import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.francetelecom.clara.cloud.commons.UUIDUtils;
import com.francetelecom.clara.cloud.logicalmodel.LogicalDeployment;

@XmlRootElement
@Entity
@Table(name = "CORE_APPLICATION_RELEASE")
/**
 * An Application Release is a version of an application
 */
public class ApplicationRelease extends CoreItem {

	private static final long serialVersionUID = -2984627032687890422L;
	/**
	 * /** application release version. Mandatory.
	 */
	@NotNull
	@Size(max = 255)
	private String releaseVersion;

	/**
	 * level of middleware products for this Release. used at projection time to
	 * select correct set of Server products (Apache / Jonas / ...)
	 */
	@NotNull
	@Size(max = 255)
	private String middlewareProfileVersion = MiddlewareProfile.getDefault().getVersion();

	/**
	 * application release description. Optional.
	 */
	@GuiMapping()
	@Size(max = 255)
	private String description = "";

	/**
	 * Application release version control repository url. Optional. This is
	 * useful for sharing apps source code of apps, in particular Paas samples.
	 */
	@GuiMapping(status = GuiMapping.StatusType.SUPPORTED)
	private URL versionControlUrl;

	/**
	 * application the release is linked to.
	 */
	@XmlIDREF
	@XmlElement(name = "applicationRef")
	@ManyToOne
	// @NotNull workaround hibernate schema bug
	private Application application;

	@NotNull
	@XmlElement(name = "logicalDeployment")
	@OneToOne(cascade = { CascadeType.PERSIST, CascadeType.REMOVE, CascadeType.REFRESH })
	private LogicalDeployment logicalDeployment;

	/**
	 * Describes state changes as the release gets deployed on environments and
	 * possibly discarded. FIXME This should be mapped as
	 * 
	 * @Enumerated(EnumType.STRING) to be more maintenable. Currently, if you
	 *                              change the enum values order this will be
	 *                              inconsistent with your database
	 */
	@NotNull
	@GuiMapping(status = GuiMapping.StatusType.SKIPPED)
	private ApplicationReleaseStateEnum state = ApplicationReleaseStateEnum.EDITING;

	/**
	 * public constructor.
	 */
	public ApplicationRelease() {
		super(UUIDUtils.generateUUID("r"));
		this.logicalDeployment = new LogicalDeployment();
	}

	/**
	 * public constructor.
	 * 
	 * @param application
	 *            application the release belongs to (mandatory)
	 * @param version
	 *            application release version (mandatory)
	 */
	public ApplicationRelease(Application application, String version) {
		super(UUIDUtils.generateUUID("r"));
		// an application release must belong to an application.
		Assert.notNull(application, "Cannot create application release. No application has been supplied.");
		// application release version must not be empty
		Assert.hasText(version, "Cannot create application release. No version has been supplied.");
		// cannot add an application release to a REMOVED application
		if (application.isRemoved()) {
			throw new IllegalArgumentException("Cannot create application release. You cannot add an application release to a REMOVED application.");
		}
		this.releaseVersion = version;
		this.application = application;
		// TODO create empty logical depl ?
		this.logicalDeployment = new LogicalDeployment();
	}

	public void validate() {
		setState(ApplicationReleaseStateEnum.VALIDATED);
	}

	public void lock() {
		setState(ApplicationReleaseStateEnum.LOCKED);
	}

	private void setState(ApplicationReleaseStateEnum toState) throws TechnicalException {
		ApplicationReleaseStateEnum fromState = this.state;

		// TODO: consider moving that into the enum and define a global FSM
		// pattern consistent in the model.
		boolean isValidTransition = true;
		switch (fromState) {
		case EDITING:
			isValidTransition = toState == ApplicationReleaseStateEnum.REMOVED
					|| (toState == ApplicationReleaseStateEnum.VALIDATED || toState == ApplicationReleaseStateEnum.DISCARDED);
			break;

		case VALIDATED:
			isValidTransition = toState == ApplicationReleaseStateEnum.REMOVED
					|| (toState == ApplicationReleaseStateEnum.EDITING || toState == ApplicationReleaseStateEnum.LOCKED || toState == ApplicationReleaseStateEnum.DISCARDED);
			break;

		case DISCARDED: // final state
			isValidTransition = toState == ApplicationReleaseStateEnum.REMOVED;
			break;

		case LOCKED:
			isValidTransition = toState == ApplicationReleaseStateEnum.REMOVED || (toState == ApplicationReleaseStateEnum.DISCARDED);
			break;

		default:
			assert false : "unsupported state:" + fromState;
		}

		if (!isValidTransition) {
			throw new TechnicalException("Illegal state transition from " + fromState + " to " + toState);
		}
		this.state = toState;
	}

	/**
	 * Utility method to replace the built-in logical deployment for the case of
	 * the cloning of a LD.
	 * 
	 * Should not be used by client code.
	 * 
	 * @param logicalDeployment
	 *            a non null LogicalDeployment
	 */
	public void replaceLd(LogicalDeployment logicalDeployment) {
		// TODO: find a way to prevent misuses of this method as a way to break
		// encapsulation
		if (logicalDeployment == null) {
			throw new NullPointerException("unexpected null logicalDeployment param");
		}

		this.logicalDeployment = logicalDeployment;
	}

	public void markAsRemoved() {
		this.setState(ApplicationReleaseStateEnum.REMOVED);
		this.setReleaseVersion("R" + System.currentTimeMillis() + "-" + this.releaseVersion);
	}

	public String getMiddlewareProfileVersion() {
		return middlewareProfileVersion;
	}

	public void setMiddlewareProfileVersion(String middlewareProfileVersion) {
		this.middlewareProfileVersion = middlewareProfileVersion;
	}

	// Getters and setters
	public Application getApplication() {
		return application;
	}

	public void setApplication(Application application) {
		Assert.notNull(application, "Cannot update application release. No application has been supplied.");
		this.application = application;
	}

	public LogicalDeployment getLogicalDeployment() {
		return logicalDeployment;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public URL getVersionControlUrl() {
		return versionControlUrl;
	}

	public void setVersionControlUrl(URL versionControlUrl) {
		this.versionControlUrl = versionControlUrl;
	}

	public String getReleaseVersion() {
		return releaseVersion;
	}

	public void setReleaseVersion(String releaseVersion) {
		Assert.hasText(releaseVersion, "Cannot update application release. No version has been supplied.");
		this.releaseVersion = releaseVersion;
	}

	public ApplicationReleaseStateEnum getState() {
		return state;
	}

	public boolean isValidated() {
		return ApplicationReleaseStateEnum.VALIDATED.equals(this.state);
	}

	public boolean isLocked() {
		return ApplicationReleaseStateEnum.LOCKED.equals(this.state);
	}

	public boolean isEditing() {
		return ApplicationReleaseStateEnum.EDITING.equals(this.state);
	}

	public boolean isRemoved() {
		return ApplicationReleaseStateEnum.REMOVED.equals(this.state);
	}

	public boolean isDiscarded() {
		return ApplicationReleaseStateEnum.DISCARDED.equals(this.state);
	}
}