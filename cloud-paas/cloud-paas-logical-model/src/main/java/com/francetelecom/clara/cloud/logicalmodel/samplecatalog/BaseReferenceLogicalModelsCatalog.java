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
package com.francetelecom.clara.cloud.logicalmodel.samplecatalog;

import com.francetelecom.clara.cloud.commons.MavenReference;
import com.francetelecom.clara.cloud.logicalmodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Base class for sharing a catalog of test logical models samples.
 */
public abstract class BaseReferenceLogicalModelsCatalog implements SampleAppFactory {

	public enum ArtefactType {

		ear, war, jar
	}

	/**
	 * logger
	 */
	private static Logger logger = LoggerFactory.getLogger(BaseReferenceLogicalModelsCatalog.class.getName());

	/**
	 * See {@link #setSimulateMavenReferenceResolution(boolean)}
	 */
	protected boolean simulateMavenReferenceResolution = false;

	// Null for default stable profile
	private String appReleaseMiddlewareProfile = null;

	// @Autowired(required=true)
	protected static SampleAppProperties sampleAppProperties;

	@Autowired
	public void setSampleAppProperties(SampleAppProperties sampleAppProperties) {
		this.sampleAppProperties = sampleAppProperties;
	}

	//
	// LogicalModelFactory API impl
	//

	@Override
	public String getAppCode() {
		return null;
	}

	@Override
	public String getAppLabel() {
		return null;
	}

	@Override
	public String getAppDescription() {
		return null;
	}

	@Override
	public String getApplicationVersionControl() {
		return null;
	}

	@Override
	public String getApplicationReleaseVersionControl() {
		return null;
	}

	@Override
	public String getAppReleaseDescription() {
		return null;
	}

	@Override
	public String getAppReleaseVersion() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * The sample catalog is supported by default, specifically specify the ones
	 * that are not supported.
	 */
	@Override
	public boolean isSupported() {
		return true;
	}

	@Override
	public LogicalDeployment populateLogicalDeployment(LogicalDeployment logicalDeployment) {
		String logicalModelLabel;
		if (logicalDeployment != null) {
			logicalModelLabel = logicalDeployment.getLabel();
		} else {
			logicalModelLabel = "ld-sampleCatalog";
		}
		return createLogicalModel(logicalModelLabel, logicalDeployment);
	}

	/**
	 * create webUi service
	 *
	 * @param label
	 * @param appCode
	 * @param statefull
	 * @param secure
	 * @param maxNumberSessions
	 * @param maxReqPerSeconds
	 *
	 * @return created web ui service
	 */
	protected static LogicalWebGUIService createLogicalWebGuiService(String label, String appCode, boolean statefull, boolean secure, int maxNumberSessions, int maxReqPerSeconds,
			ArtefactType type) {

		LogicalWebGUIService webGUIService = new LogicalWebGUIService();

		webGUIService.setLabel(label);
		webGUIService.setContextRoot(new ContextRoot(sampleAppProperties.getProperty(appCode, type.toString(), "context-root")));
		webGUIService.setStateful(statefull);
		webGUIService.setSecure(secure);
		webGUIService.setMaxNumberSessions(maxNumberSessions);
		webGUIService.setMaxReqPerSeconds(maxReqPerSeconds);

		return webGUIService;
	}

	protected static LogicalWebGUIService createLogicalWebGuiService(String label, String appCode, boolean statefull, boolean secure, int maxNumberSessions, int maxReqPerSeconds) {

		return createLogicalWebGuiService(label, appCode, statefull, secure, maxNumberSessions, maxReqPerSeconds, ArtefactType.ear);
	}

	/**
	 * create relational db service
	 *
	 *
	 * @param baseReferenceLogicalModelsCatalog
	 * @param label
	 * @param sqlVersion
	 * @param capacity
	 * @param applicationName
	 *            (eg springoo)
	 * @return created relational db service
	 */
	protected static LogicalRelationalService createLogicalRelationalService(BaseReferenceLogicalModelsCatalog baseReferenceLogicalModelsCatalog, String label, String jndiName,
			LogicalRelationalServiceSqlDialectEnum sqlVersion, int capacity, String applicationName) {

		LogicalRelationalService relationalService = new LogicalRelationalService();
		relationalService.setLabel(label);
		relationalService.setServiceName(jndiName);
		relationalService.setSqlVersion(sqlVersion);

		relationalService.setCapacityMo(capacity);

		return relationalService;

	}

	/**
	 * create mysql db service
	 *
	 *
	 * @param label
	 * @param serviceName
	 *
	 * @return mysql db service
	 */
	protected static LogicalMysqlService createLogicalMysqlService(String label, String serviceName) {

		LogicalMysqlService logicalMysqlService = new LogicalMysqlService();
		logicalMysqlService.setLabel(label);
		logicalMysqlService.setServiceName(serviceName);

		return logicalMysqlService;

	}

	/**
	 * create configuration service
	 *
	 * @param label
     * @param keyPrefix
     * @param configSetContent
	 *
	 * @return created configuration service
	 */
    protected static LogicalConfigService createLogicalConfigService(String label, String keyPrefix, String configSetContent) {

		LogicalConfigService service = new LogicalConfigService();
		service.setLabel(label);
        service.setKeyPrefix(keyPrefix);
        try {
			service.setConfigSetContent(configSetContent);
		} catch (InvalidConfigServiceException e) {
			logger.error("InvalidConfigServiceException : " + e.getMessage());
		}

		return service;
	}

	/**
	 * create soap consumer service
	 *
	 * @param label
	 * @param serviceProviderName
	 * @param serviceName
	 * @param serviceMajorVersion
	 * @param serviceMinorVersion
	 * @param jndiPrefix
	 *
	 * @return created soap consumer service
	 */
	protected static LogicalSoapConsumer createLogicalSoapConsumer(String label, String serviceProviderName, String serviceName, int serviceMajorVersion, int serviceMinorVersion,
			String jndiPrefix) {

		LogicalSoapConsumer service = new LogicalSoapConsumer();
		service.setLabel(label);
		service.setServiceProviderName(serviceProviderName);
		service.setServiceName(serviceName);
		service.setServiceMajorVersion(serviceMajorVersion);
		service.setServiceMinorVersion(serviceMinorVersion);
		service.setJndiPrefix(jndiPrefix);

		return service;
	}

	/**
	 * create soap service service
	 * 
	 * @param baseReferenceLogicalModelsCatalog
	 * @param label
	 * @param serviceName
	 * @param serviceMajorVersion
	 * @param serviceMinorVersion
	 * @param jndiPrefix
	 * @param rootFilename
	 * @param description
	 * @param appCode
	 * @return created soap service
	 */
	protected static LogicalSoapService createLogicalSoapService(BaseReferenceLogicalModelsCatalog baseReferenceLogicalModelsCatalog, String label, String serviceName,
			int serviceMajorVersion, int serviceMinorVersion, String jndiPrefix, String rootFilename, String description, String appCode, ArtefactType type) {

		LogicalSoapService service = new LogicalSoapService(label, new LogicalDeployment(), serviceName, serviceMajorVersion, serviceMinorVersion, new ContextRoot(
				sampleAppProperties.getProperty(appCode, type.toString(), "service-context-root")).getValue(), new Path(sampleAppProperties.getProperty(appCode, type.toString(), "service-path")),
				null, description);
		service.setJndiPrefix(jndiPrefix);
		service.setRootFileName(rootFilename);
		if (appCode != null) {
			service.setServiceAttachments(baseReferenceLogicalModelsCatalog.createMavenReference(appCode, "jar"));
		}

		return service;
	}

	protected static LogicalSoapService createLogicalSoapService(BaseReferenceLogicalModelsCatalog baseReferenceLogicalModelsCatalog, String label, String serviceName,
			int serviceMajorVersion, int serviceMinorVersion, String jndiPrefix, String rootFilename, String description, String appCode) {
		return createLogicalSoapService(baseReferenceLogicalModelsCatalog, label, serviceName, serviceMajorVersion, serviceMinorVersion, jndiPrefix, rootFilename, description, appCode, ArtefactType.ear);
	}

	/**
	 * create queue receive service
	 *
	 * @param label
	 * @param serviceName
	 * @param serviceVersion
	 * @param jndiQueueName
	 * @param msgMaxSizeKB
	 * @param maxNbMsgPerDay
	 * @param nbRetentionDay
	 *
	 * @return created queue receive service
	 */
	protected static LogicalQueueReceiveService createLogicalQueueReceive(String label, String serviceName, String serviceVersion, String jndiQueueName, long msgMaxSizeKB,
			long maxNbMsgPerDay, long nbRetentionDay) {

		LogicalQueueReceiveService service = new LogicalQueueReceiveService();
		service.setLabel(label);
		service.setServiceName(serviceName);
		service.setServiceVersion(serviceVersion);
		service.setJndiQueueName(jndiQueueName);
		service.setMsgMaxSizeKB(msgMaxSizeKB);
		service.setMaxNbMsgPerDay(maxNbMsgPerDay);
		service.setNbRetentionDay(nbRetentionDay);

		return service;
	}

	/**
	 * create queue send service
	 *
	 * @param label
	 * @param targetServiceName
	 * @param targetServiceVersion
	 * @param targetBasicatCode
	 * @param targetApplicationVersion
	 * @param targetApplicationVersion
	 * @param jndiQueueName
	 * @param msgMaxSizeKB
	 * @param maxNbMsgPerDay
	 * @param nbRetentionDay
	 *
	 * @return created queue send service
	 */
	protected static LogicalQueueSendService createLogicalQueueSend(String label, String targetServiceName, String targetServiceVersion, String targetBasicatCode,
			String targetApplicationName, String targetApplicationVersion, String jndiQueueName, long msgMaxSizeKB, long maxNbMsgPerDay, long nbRetentionDay) {

		LogicalQueueSendService service = new LogicalQueueSendService();
		service.setLabel(label);
		service.setTargetServiceName(targetServiceName);
		service.setTargetServiceVersion(targetServiceVersion);
		service.setTargetBasicatCode(targetBasicatCode);
		service.setTargetApplicationName(targetApplicationName);
		service.setTargetApplicationVersion(targetApplicationVersion);
		service.setJndiQueueName(jndiQueueName);
		service.setMsgMaxSizeKB(msgMaxSizeKB);
		service.setMaxNbMsgPerDay(maxNbMsgPerDay);
		service.setNbRetentionDay(nbRetentionDay);

		return service;
	}

	/**
	 * create jee processing service
	 *
	 *
	 * @param baseReferenceLogicalModelsCatalog
	 * @param label
	 * @param applicationName
	 * @return created jee processing
	 */
	protected static ProcessingNode createJeeProcessing(BaseReferenceLogicalModelsCatalog baseReferenceLogicalModelsCatalog, String label, String applicationName,
			ArtefactType type) {

		ProcessingNode node = new JeeProcessing();
		node.setLabel(label);
		node.setSoftwareReference(baseReferenceLogicalModelsCatalog.createMavenReference(applicationName, type.toString()));

		return node;

	}
	
	protected static ProcessingNode createJeeProcessing(BaseReferenceLogicalModelsCatalog baseReferenceLogicalModelsCatalog, String label, String applicationName) {

		return createJeeProcessing(baseReferenceLogicalModelsCatalog, label, applicationName, ArtefactType.ear);

	}

	/**
	 * create cf java processing service
	 *
	 *
	 * @param baseReferenceLogicalModelsCatalog
	 * @param label
	 * @param applicationName
	 * @return created cf java processing
	 */
	protected static CFJavaProcessing createCFJavaProcessing(BaseReferenceLogicalModelsCatalog baseReferenceLogicalModelsCatalog, String label, String applicationName,
			ArtefactType type) {

		CFJavaProcessing node = new CFJavaProcessing();
		node.setLabel(label);
		node.setSoftwareReference(baseReferenceLogicalModelsCatalog.createMavenReference(applicationName, type.toString()));

		return node;

	}

	/**
	 * create online storage service
	 *
	 * @param label
     * @param serviceName
     * @param storageCapacity
	 *
	 * @return online storage service
	 */
    protected static LogicalOnlineStorageService createLogicalOnlineStorage(String label, String serviceName, int storageCapacity) {

		LogicalOnlineStorageService service = new LogicalOnlineStorageService();
		service.setLabel(label);
        service.setServiceName(serviceName);
        service.setStorageCapacityMb(storageCapacity);

		return service;
	}

	/**
	 * Creates an internal MOM service.
	 * 
	 * @param label
	 * @param destinationName
	 * @param destinationCapacity
	 * @return
	 */
	protected static LogicalMomService createInternalMomService(String label, String destinationName, int retryCount, int destinationCapacity, String deadLetterQueueName) {
		LogicalMomService service = new LogicalMomService();
		service.setDestinationName(destinationName);
		service.setDestinationCapacity(destinationCapacity);
		service.setRetriesBeforeMovingToDeadLetterQueue(retryCount);
		service.setLabel(label);

		if (deadLetterQueueName != null && !deadLetterQueueName.equals("")) {
			service.setHasDeadLetterQueue(true);
			service.setDeadLetterQueueName(deadLetterQueueName);
		} else {
			service.setHasDeadLetterQueue(false);
			// FIXME dead letter queue name must support null value without
			// wicket testing fail.
			service.setDeadLetterQueueName("");
		}
		return service;
	}
	
	/**
	 * create an internal rabbit service
	 * @param label
	 * @param serviceName
	 * @return
	 */
	protected static LogicalRabbitService createInternalRabbitService(String label, String serviceName) {
		LogicalRabbitService service=new LogicalRabbitService();
		service.setLabel(label);
		service.setServiceName(serviceName);
		return service;
	}
	
	
	

	/**
	 * Create maven reference using paas-sample.properties file
	 *
	 * Get Maven reference of an application artifact If Maven reference can not
	 * be found a TechnicalException is thrown
	 * 
	 * @param application
	 *            application code
	 * @param refType
	 *            artifact type (ear, sql)
	 * @return MavenReference
	 */
	private MavenReference createMavenReference(String application, String refType) {
		return sampleAppProperties.getMavenReference(application, refType);
	}

	//
	// API for other tests not yet leveraging the SampleAppFactory API
	//
	public LogicalDeployment createLogicalModel(String logicalModelName) {
		return createLogicalModel(logicalModelName, null);
	}

	/**
	 * Used by the projection unit tests
	 * 
	 * @param logicalModelName
	 *            a constant used (e.g. "ProjectionServiceTest")
	 * @param existingLDToUpdate
	 *            the logical deployment to update or null to create a new one.
	 * @return
	 */
	public abstract LogicalDeployment createLogicalModel(String logicalModelName, LogicalDeployment existingLDToUpdate);

	/**
	 * Used by the projection to request that the MavenReferences in the
	 * generated logical model be properly resolved. This is the expected
	 * behavior handled by the ManageEnvironmentImpl prior to calling the
	 * ProjectionService.
	 *
	 * Subclasses when this field is set should assign values to the
	 * MavenReferences, even if those are invalid and do not point anywhere.
	 * 
	 * @param simulateMavenReferenceResolution
	 */
	public void setSimulateMavenReferenceResolution(boolean simulateMavenReferenceResolution) {
		this.simulateMavenReferenceResolution = simulateMavenReferenceResolution;
	}

	@Override
	public String getAppReleaseMiddlewareProfile() {
		return appReleaseMiddlewareProfile;
	}

	public void setAppReleaseMiddlewareProfile(String appReleaseMiddlewareProfile) {
		this.appReleaseMiddlewareProfile = appReleaseMiddlewareProfile;
	}

}
