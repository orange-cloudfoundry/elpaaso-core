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
package com.francetelecom.clara.cloud.techmodel.cf;

import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.francetelecom.clara.cloud.model.DependantModelItem;
import com.francetelecom.clara.cloud.model.DeploymentStateEnum;
import com.francetelecom.clara.cloud.model.XaasSubscription;
import org.springframework.util.Assert;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a route, and keeps track of the link to the WebGui, and the
 * context root
 */
@XmlRootElement
@Entity
public class Route extends XaasSubscription {

	private static final int HTTP_PORT = 80;
	@Embedded
	@AttributeOverrides({ @AttributeOverride(name = "value", column = @Column(name = "routeUri")) })
	private RouteUri routeUri;

	private String contextRoot;

	@XmlElement(name = "space")
	@OneToOne
	private Space space;
	
	protected Route() {
	}

	public Route(RouteUri initialUri, String contextRoot, Space space) {
		super();
		setSpace(space);
		setRouteUri(initialUri);
		this.contextRoot = contextRoot;
	}


	/**
	 * prefix route uri with given prefix
	 * 
	 * @param prefix
	 */
	public void prefix(String prefix) {
		setRouteUri(routeUri.withHostPrefix(prefix));
	}

	public void activate(RouteUri routeUri) {
		setDeploymentState(DeploymentStateEnum.CREATED);
		setRouteUri(routeUri);
	}

	public void delete() {
		setDeploymentState(DeploymentStateEnum.REMOVED);
	}

	public String getUri() {
		return routeUri.getValue();
	}
	
	public URL getFullHttpAccessUrl() {
		try {
			return new URL("http://" + routeUri.getValue() + ":" + HTTP_PORT + (contextRoot.equals("/") ? "" : contextRoot));
		} catch (MalformedURLException e) {
			throw new TechnicalException("Bad URL: " + e.getMessage(), e);
		}
	}

	public String getContextRoot() {
		return contextRoot;
	}

	public SpaceName getSpace() {
		return space.getSpaceName();
	}

	public String getDomain() {
		return routeUri.getDomain();
	}

	public String getHost() {
		return routeUri.getHost();
	}

	public RouteUri candidateRouteUri() {
		return routeUri.withRandomHostPrefix();
	}

	private void setSpace(Space space) {
		Assert.notNull(space, "unable to scope route <" + routeUri + "> to space <" + space + ">. invalid space.");
		this.space = space;
	}

	private void setRouteUri(RouteUri routeUri) {
		Assert.notNull(routeUri, "unable to set route routeUri <" + routeUri + ">. invalid routeUri.");
		this.routeUri = routeUri;
	}

	@Override
	public Set<DependantModelItem> listDepedencies() {
		Set<DependantModelItem> dependantModelItems = new HashSet<>();
		dependantModelItems.add(space);
		return dependantModelItems;
	}
}
