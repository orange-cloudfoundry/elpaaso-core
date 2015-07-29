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

import com.francetelecom.clara.cloud.commons.GuiMapping;
import com.francetelecom.clara.cloud.commons.MissingDefaultUserException;
import com.francetelecom.clara.cloud.commons.UUIDUtils;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.util.Assert;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URL;
import java.util.*;

/**
 * An application is a IT component
 * 
 * @author apog7416
 * 
 */
@XmlRootElement
@Entity
@Table(name = "CORE_APPLICATION")
public class Application extends CoreItem {

	private static final long serialVersionUID = 3258703452143929264L;

	/**
	 * application code. Mandatory.
	 */
	@NotNull
	@Size(max = 255)
	private String code;

	/**
	 * application label. Mandatory.
	 */
	@NotNull
	@Size(max = 255)
	private String label;

	/**
	 * application description. Optional.
	 */
	@Size(max = 255)
	private String description;

	/**
	 * Describes state of Application. Mandatory.
	 */
	@NotNull
	@Enumerated(EnumType.STRING)
	private ApplicationStateEnum state;

	/**
	 * FT CARTO url link.
	 */
	@GuiMapping(status = GuiMapping.StatusType.SKIPPED)
	private URL applicationRegistryUrl;

	/**
	 * Application visibility: public or private
	 */
	@NotNull
    @ColumnDefault("true")
	private boolean isPublic = true;

	/**
	 * Application members
	 */
	@XmlElementWrapper
	@XmlElement(name = "members")
	@ElementCollection(fetch=FetchType.EAGER)
    @CollectionTable(name = "CORE_APPLICATIONS_MEMBERS")
	private final List<SSOId> members = new ArrayList<>();

	/**
	 * Application config roles, used to override environment properties
	 */
	@XmlElementWrapper
	@XmlElement(name = "configRoles")
	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.REMOVE },fetch = FetchType.EAGER)
	private final Set<ConfigRole> configRoles = new HashSet<>();
	
	@GuiMapping(status = GuiMapping.StatusType.SKIPPED)
	@Transient
	//FIXME this a temp fix. that property should be moved to ApplicationDTO
	private boolean editable = true;

	/**
	 * required by jpa
	 */
	protected Application() {
	}

	/**
	 * Public constructor.
	 * 
	 * @param label
	 *            application label (must have text)
	 * @param code
	 *            application code (must have text)
	 */
	public Application(String label, String code) {
		super(UUIDUtils.generateUUID("a"));
		setLabel(label);
		setCode(code);
		this.state = ApplicationStateEnum.CREATED;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public URL getApplicationRegistryUrl() {
		return applicationRegistryUrl;
	}

	public void setApplicationRegistryUrl(URL url) {
		Assert.notNull(url, "Invalid application registry url. No url has been supplied.");
		Assert.isTrue(!isRemoved(), "Cannot set registry url. Application is REMOVED.");
		this.applicationRegistryUrl = url;
	}

	public String getLabel() {
		return label;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		Assert.hasText(code, "Invalid value for application code. No code has been supplied.");
		Assert.isTrue(!isRemoved(), "Cannot set application code. Application is REMOVED.");
		this.code = code;
	}

	public void setLabel(String label) {
		Assert.hasText(label, "Invalid value for application label. No label has been supplied.");
		Assert.isTrue(!isRemoved(), "Cannot set application label. Application is REMOVED.");
		this.label = label;
	}
	
	/**
	 * @return true if {@link Application} is marked as removed
	 */
	public boolean isRemoved() {
		return ApplicationStateEnum.REMOVED.equals(this.state);
	}

	/**
	 * @return mark {@link Application} as removed (soft delete)
	 */
	public void markAsRemoved() {
		this.setLabel("R" + System.currentTimeMillis() + "-" + this.label);
		this.state = ApplicationStateEnum.REMOVED;
	}

	/**
	 * @return true if {@link Application} is public; false if {@link Application} is private
	 */
	public boolean isPublic() {
		return isPublic;
	}

	public void setAsPublic() {
		this.isPublic = true;
	}
	
	/**
	 * Set {@link Application} as private.
	 */
	public void setAsPrivate() {
		this.isPublic = false;
		// reset application members
	}
	
	/**
	 * Set {@link Application} members.
	 * <P>
	 * <B>Caution :</B> Clear and replace existing application members application with provided application members.
	 * @param members
	 */
	public void setMembers(Set<SSOId> members) {
		if (members == null || members.isEmpty() || members.contains(null) ) {
			throw new MissingDefaultUserException("Cannot set application members. No paas user member is provided (at least one is required).");
		}
		Assert.isTrue(!isRemoved(), "Cannot set application members. Application is REMOVED.");
		// reset application members
		this.members.clear();
		this.members.addAll(members);
	}

	public List<SSOId> listMembers() {
		return Collections.unmodifiableList(new ArrayList<SSOId>(members)); //workaround hibernate persistent bags whose equals fails.
	}

	public boolean hasForMember(SSOId member) {
		return members.contains(member);
	}

	public boolean isEditable() {
		return editable;
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
	}
	
	public List<ConfigRole> listConfigRoles() {
		return Collections.unmodifiableList(new ArrayList<ConfigRole>(configRoles)); //workaround hibernate persistent bags whose equals fails.
	}

	public void addConfigRole(ConfigRole configRole) {
		Assert.notNull(configRole, "Cannot add application config role <"+configRole+">. Invalid value");
		Assert.isTrue(!isRemoved(), "Cannot add application config role. Application is REMOVED.");
		configRoles.add(configRole);
	}
}
