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
package com.francetelecom.clara.cloud.logicalmodel;

import com.francetelecom.clara.cloud.commons.GuiClassMapping;
import com.francetelecom.clara.cloud.commons.GuiMapping;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Web GUI Service represents an Web access accessible externally to the
 * application.
 *
 * This typically maps to a JEE module (usually a WAR) with and associated
 * context-root under which HTTP request will be routed to.
 * 
 * @author APOG7416
 * 
 */
@XmlRootElement
@Entity
@Table(name = "HTTP_SERVICE")
@GuiClassMapping(serviceCatalogName = "Logical Web Gui", serviceCatalogNameKey = "webgui", isExternal = true, status = GuiClassMapping.StatusType.SUPPORTED)
public class LogicalWebGUIService extends LogicalService {

	/**
	 * Non-Jee applications may ignore this field and leave the default / value.
	 * 
	 * Java applications using the JeeProcessing can embed multiple web
	 * applications in a single EAR artefact. The context-root field identifies
	 * the Java Web module to which this WebGui service will route traffic to.
	 * (eg http://&lt;host&gt;:&lt;port&gt;/&lt;context-root&gt;/...)
	 * <br>
	 * Context-root field is mandatory. At minimal it should be the root path
	 * i.e. "/". (context-root should always start with a forward slash "/")
	 * <br>
	 * The incoming requests on urls of the form
	 * http://&lt;host&gt;:&lt;port&gt;/&lt;context-root&gt;/...... will be routed to the
	 * processing service that bound to this webgui service.
	 * <br>
	 * The Web module context-root field is declared into the
	 * META-INF/application.xml descriptor. For maven-built apps, this is
	 * typically configured in the maven packaging of the application using the
	 * maven-ear-plugin. See
	 * http://maven.apache.org/plugins/maven-ear-plugin/examples
	 * /customizing-context-root.html
	 */
	@Embedded
	@GuiMapping(status = GuiMapping.StatusType.SUPPORTED, functional = true)
	@Valid
	private ContextRoot contextRoot = new ContextRoot("/");

	/**
	 * Indicates whether stateful sessions are enabled and maintained over HTTP
	 * using mechanism such as cookies
	 */
	@GuiMapping(status = GuiMapping.StatusType.SUPPORTED, functional = true)
	boolean stateful = false;

	/**
	 * Indicates whether the GUI access needs to be secure, such as using HTTPS
	 * as tranport protocol.
	 */
	@GuiMapping(status = GuiMapping.StatusType.READ_ONLY, functional = false)
	boolean secure = false;

	/**
	 * In the case the service is stateful, this indicates the maximum expected
	 * number of concurrent sessions. If the service is stateless, this field is
	 * ignored. This provides hints to the automatic sizing algorithms mainly
	 * RAM.
	 */
	@GuiMapping(status = GuiMapping.StatusType.SUPPORTED, functional = false)
	@Min(value = 1)
	int maxNumberSessions = 10;

	/**
	 * Indicates the max expected typical request per second (workload). The
	 * typical request may be further defined as a mix of small, medium, large
	 * request with associated distribution. This may apply regardless of
	 * whether the service is stateful. This provides hints to the automatic
	 * sizing algorithms, mainly parallelism (amount of CPUs and number of VMs).
	 */
	@GuiMapping(status = GuiMapping.StatusType.READ_ONLY, functional = false)
	int maxReqPerSeconds = 20;

	// TODO: consider adding typical response time, and max response time.

	/**
	 * An optional file path relative to the contextRoot to form an health check
	 * Url (should not start with a leading "/" prefix. When specified,
	 * indicates that the url is expected to be server by the application and
	 * returning a 2xx HTTP status. Failure to return such would raise an
	 * applicative alarm.
	 */
	// @Pattern(regexp = "\w[a-zA-Z]{1}[0-9a-zA-Z.]{2,18}") //TODO: refine
	// regexp to remove leading /
	@GuiMapping(status = GuiMapping.StatusType.SKIPPED)
	String healthCheckFilePath;

	/**
	 * public constructor
	 */
	public LogicalWebGUIService() {

	}

	/**
	 * Constructor
	 * 
	 * @param label
	 *            : label of the web ui service
	 * @param logicalDeployment
	 *            : logical deployment to add web ui service
	 * @deprecated Should not be called anymore, use empty constructor instead
	 *             followed by
	 *             {@link LogicalDeployment#addLogicalService(LogicalService)}
	 */
	public LogicalWebGUIService(String label, LogicalDeployment logicalDeployment) {
		super(label, logicalDeployment);
	}

	/**
	 * 
	 * @return context root of the web ui service
	 */
	public ContextRoot getContextRoot() {
		return contextRoot;
	}

	/**
	 * 
	 * @param contextRoot
	 *            of the web ui service
	 */
	public void setContextRoot(ContextRoot contextRoot) {
		this.contextRoot = contextRoot;
	}

	/**
	 * 
	 * @return if the service is statefull or not
	 */
	public boolean isStateful() {
		return stateful;
	}

	/**
	 * 
	 * @param stateful
	 *            boolean
	 */
	public void setStateful(boolean stateful) {
		this.stateful = stateful;
	}

	/**
	 * 
	 * @return true if the service is secure, false otherwise
	 */
	public boolean isSecure() {
		return secure;
	}

	/**
	 * 
	 * @param secure
	 *            boolean
	 */
	public void setSecure(boolean secure) {
		this.secure = secure;
	}

	public int getMaxNumberSessions() {
		return maxNumberSessions;
	}

	public void setMaxNumberSessions(int maxNumberSessions) {
		this.maxNumberSessions = maxNumberSessions;
	}

	public int getMaxReqPerSeconds() {
		return maxReqPerSeconds;
	}

	public void setMaxReqPerSeconds(int maxReqPerSeconds) {
		this.maxReqPerSeconds = maxReqPerSeconds;
	}

    public String getHealthCheckFilePath() {
        return healthCheckFilePath;
	}

	public void setHealthCheckFilePath(String healthCheckFilePath) {
		this.healthCheckFilePath = healthCheckFilePath;
	}
}
