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
/**
 * $LastChangedBy$
 * 
 * $LastChangedRevision$
 * 
 * $HeadURL$
 * 
 * $Id$  
 * 
 * */
package com.francetelecom.clara.cloud.presentation.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.Session;
import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.request.Request;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.francetelecom.clara.cloud.core.service.ManagePaasUser;
import com.francetelecom.clara.cloud.coremodel.PaasRoleEnum;
import com.francetelecom.clara.cloud.coremodel.PaasUser;
import com.francetelecom.clara.cloud.coremodel.SSOId;
import com.francetelecom.clara.cloud.presentation.login.UnauthorizedAccess;
import com.francetelecom.clara.cloud.scalability.helper.PaasStats;
import com.francetelecom.clara.cloud.coremodel.exception.ObjectNotFoundException;
import org.springframework.util.Assert;

/**
 * WicketSession Class that hold application session
 * Sample usage : see ManageStatisticsImplTest
 * 
 */
public class WicketSession extends AuthenticatedWebSession {
	private static Logger LOGGER = LoggerFactory.getLogger(WicketSession.class);

	private static final long serialVersionUID = -7843719714249664102L;
	private static final transient Logger logger = LoggerFactory.getLogger(WicketSession.class);
	private static final transient Logger statsLogger = LoggerFactory.getLogger("com.francetelecom.clara.cloud.StatLogger");
	private static final String VIEW_ALL_KEY = "view_all";

	@SpringBean
	private ManagePaasUser managePaasUser;

	@SpringBean(name = "authenticationManager")
	private AuthenticationManager authenticationManager;

//	private static Map<String, Roles> profilesRoles = new HashMap<String, Roles>();

//	protected static Map<String, Roles> getProfilesRoles() {
//		return profilesRoles;
//	}

/*	static {
		// ADMIN role = ADMIN + USER
		Roles adminRoles = new Roles();
		adminRoles.add("ROLE_USER");
		adminRoles.add("ROLE_ADMIN");
		profilesRoles.put("ROLE_ADMIN", adminRoles);
		// USER role
		profilesRoles.put("ROLE_USER", new Roles("ROLE_USER"));
	}
  */
	private PaasUser paasUser;

	private final int MAX_PAAS_STATS = 10; // paginate not handled now
	List<PaasStats> stats;

	public WicketSession(Request request) {
		super(request);
		injectDependencies();
		ensureDependenciesNotNull();
		razStats();
		updateRoleFromSpringSecurity();
	}

	private void updateRoleFromSpringSecurity() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null) {
			populateSessionPaaSUser(authentication, this);
		} else {
			logger.warn("Authentication from SecurityContextHolder is null. No PaaSUser affected.");
		}
	}

	/**
	 * static method to get the session from everywhere without cast
	 * 
	 * @return WicketSession
	 */
	public static WicketSession get() {
		return (WicketSession) Session.get();
	}

	/**
	 * @return el paaso user connected
	 */
	public PaasUser getPaasUser() {
		return paasUser;
	}
	
	public void setViewAll(Boolean viewAll) {
		setAttribute(VIEW_ALL_KEY, viewAll);
	}

	public Boolean getViewAll() {
		Boolean viewAll = (Boolean) getAttribute(VIEW_ALL_KEY);
		if (viewAll == null) {
			return Boolean.FALSE;
		}
		return viewAll;
	}

	/**
	 * set el paaso user connected
	 * 
	 * @param paasUser
	 *            - el paaso user
	 */
	public void setPaasUser(PaasUser paasUser) {
		this.paasUser = paasUser;
	}

	@Override
	public boolean authenticate(String username, String password) {
		throw new UnsupportedOperationException("Spring-Security should be used instead of wicket authentication mechanism !!!");
	}

	@Override
	public Roles getRoles() {
		Roles roles = new Roles();
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication !=null) {
			for (GrantedAuthority authority : authentication.getAuthorities()) {
				roles.add(authority.getAuthority());
			}
		}  else {
			logger.warn("Authentication from SecurityContextHolder is null. No role affected.");
		}
		return roles;
	}

	/**
   * Set the PaaS User into session (retrieved using gassi user)
   * @param authentication
   * @param session
   * @throws ObjectNotFoundException
   */
	protected void populateSessionPaaSUser(Authentication authentication, WicketSession session) {
		Assert.notNull(authentication);
		String username=authentication.getName();
		logger.debug("find PaaSUser from GASSI ssoid [{}]", username);
		PaasUser registeredPaasUser = null;
		try {
			registeredPaasUser = managePaasUser.findPaasUser(username);
		} catch (ObjectNotFoundException nfe) {
			logger.debug("User not found {}", username);
		}
		PaasRoleEnum role = workoutPaasRole(authentication);
		if (role == null) {return; }
		if (registeredPaasUser == null) {
			authentication.getDetails();
			registeredPaasUser = new PaasUser(username,username,new SSOId(username),username + "." + username + "@orange.com");
			registeredPaasUser.setSubTenant("DDSI");
			registeredPaasUser.setPaasUserRole(role);

			getLoginLogger().debug("First connection for user: {}", username);
			logger.debug("registering user: "+username+" role:" +registeredPaasUser.getPaasUserRole());
			managePaasUser.checkBeforeCreatePaasUser(registeredPaasUser);
		}
		getLoginLogger().info(registeredPaasUser.getSsoId().getValue() + " (" + registeredPaasUser.getFullName() + ") logged IN with wicket session " + session.getId());
		session.setPaasUser(registeredPaasUser);
	}

	/**
	 * work out PaasRole from spring security authorities<br>
	 * when defined in user authorities, ROLE_ADMIN is always selected first
	 * 
	 * @param authentication
	 *            spring security Authentication object
	 * @return PaasRole
	 */
	protected PaasRoleEnum workoutPaasRole(Authentication authentication) {
//		Object[] authorities = authentication.getAuthorities().toArray();
		Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
		if (authorities.size() == 0) {
			throw new UnauthorizedAccess("Account is not enabled for PaaS usage.");
		}
		PaasRoleEnum role = null;
		for (GrantedAuthority authority : authorities) {
			if (authority.getAuthority().equals("ROLE_ANONYMOUS")){
   	    	 // role = PaasRoleEnum.USER;
   	         continue;
			}
			try {
				 role = PaasRoleEnum.valueOf(authority.getAuthority());
			} catch (IllegalArgumentException iae) {
				throw new UnauthorizedAccess("Unexpected role: " + authority.getAuthority() + ". Check your LDAP server configuration. Allowed roles are "
						+ Arrays.toString(PaasRoleEnum.values()));
			}
			if (role == PaasRoleEnum.ROLE_ADMIN) {
				// If user has also ROLE_ADMIN then this role takes precedence
				return role;
			}
		}
		return role;
	}

	public void addStats(PaasStats stat) {
		this.stats.add(stat);
		limitStats();
	}

	/**
	 * keep only MAX_PAAS_STATS recent stats
	 */
	public void limitStats() {
		if (this.stats.size() < MAX_PAAS_STATS) {
			return;
		}
		Collections.sort(this.stats, new Comparator<PaasStats>() {
			public int compare(PaasStats ps1, PaasStats ps2) {
				return Long.valueOf(ps2.getCreationTime()).compareTo(Long.valueOf(ps1.getCreationTime()));
			}
		});
		this.stats = this.stats.subList(0, MAX_PAAS_STATS - 1);
	}

	public void razStats() {
		this.stats = new ArrayList<PaasStats>();
	}

	public PaasStats getStatById(long statId) {
		for (PaasStats ps : this.stats) {
			if (ps.getCreationTime() == statId) {
				return ps;
			}
		}
		return null;
	}

	public List<PaasStats> getStats() {
		return this.stats;
	}

	private void ensureDependenciesNotNull() {
		if (authenticationManager == null) {
			throw new IllegalStateException("AdminSession requires an authenticationManager.");
		}
	}

	protected void injectDependencies() {
		Injector.get().inject(this);
	}
	
	protected WicketSession getSession(){
		return WicketSession.get();
	}
	
	protected Logger getLoginLogger(){
		return statsLogger;
	}

	public void setAuthenticationManager(AuthenticationManager authenticationManager) {
		this.authenticationManager = authenticationManager;
	}

	protected void setManagePassUser(ManagePaasUser managePaasUser) {
		this.managePaasUser = managePaasUser;  
	}

}
