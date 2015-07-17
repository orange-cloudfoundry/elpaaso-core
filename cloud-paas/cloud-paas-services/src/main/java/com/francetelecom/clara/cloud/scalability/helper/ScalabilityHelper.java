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
package com.francetelecom.clara.cloud.scalability.helper;

import com.francetelecom.clara.cloud.application.ManageLogicalDeployment;
import com.francetelecom.clara.cloud.commons.BusinessException;
import com.francetelecom.clara.cloud.commons.InvalidMavenReferenceException;
import com.francetelecom.clara.cloud.commons.MavenReference;
import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.francetelecom.clara.cloud.core.service.ManageApplication;
import com.francetelecom.clara.cloud.core.service.ManageApplicationRelease;
import com.francetelecom.clara.cloud.core.service.ManagePaasUser;
import com.francetelecom.clara.cloud.coremodel.*;
import com.francetelecom.clara.cloud.environment.ManageEnvironment;
import com.francetelecom.clara.cloud.logicalmodel.*;
import com.francetelecom.clara.cloud.logicalmodel.samplecatalog.SampleAppProperties;
import com.francetelecom.clara.cloud.services.dto.EnvironmentDto;
import com.francetelecom.clara.cloud.services.dto.EnvironmentDto.EnvironmentStatusEnum;
import com.francetelecom.clara.cloud.technicalservice.exception.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.util.*;

/**
 * ScalabilityHelper Class used to create set of user data Sample usage : cf.
 * ManageScalabilityImplTest
 * 
 * @author Clara
 */
public class ScalabilityHelper {

	/**
	 * Logger
	 */
	private static final transient org.slf4j.Logger logger = LoggerFactory.getLogger(ScalabilityHelper.class);
	private static final EnvironmentDto.EnvironmentTypeEnum DEFAULT_ENV_TYPE = EnvironmentDto.EnvironmentTypeEnum.DEVELOPMENT;
	private static final String LETTER_GUI = "G";
	private static final String LETTER_NODE = "N";
	private static final String LETTER_DATABASE = "D";
	private static final String LETTER_STORE = "S";
	/**
	 * paas managers
	 */
	private final ManagePaasUser managePaasUser;
	private final ManageApplication manageApplication;
	private final ManageApplicationRelease manageApplicationRelease;
	private final ManageLogicalDeployment manageLogicalDeployment;
	private final ManageEnvironment manageEnvironment;
	private final SampleAppProperties sampleAppProperties;
	private final boolean isFakeWorld;

	/**
	 * scalability functions manager are provided by impl or mocks bean context
	 * 
	 * @param managePaasUser
	 *            paas user
	 * @param manageApplication
	 *            app manager
	 * @param manageApplicationRelease
	 *            appRelease manager
	 * @param manageLogicalDeployment
	 *            ld manager
	 * @param manageEnvironment
	 *            env manager
	 * @param sampleAppProperties
	 *            app properties
	 * @param isFakeWorld
	 *            must be true on mocked env or when incomplete activation plugin found this arg will determine if we skip environment creation
	 */
	public ScalabilityHelper(ManagePaasUser managePaasUser, ManageApplication manageApplication, ManageApplicationRelease manageApplicationRelease,
			ManageLogicalDeployment manageLogicalDeployment, ManageEnvironment manageEnvironment, SampleAppProperties sampleAppProperties, boolean isFakeWorld) {
		this.managePaasUser = managePaasUser;
		this.manageApplication = manageApplication;
		this.manageApplicationRelease = manageApplicationRelease;
		this.manageLogicalDeployment = manageLogicalDeployment;
		this.manageEnvironment = manageEnvironment;
		this.sampleAppProperties = sampleAppProperties;
		this.isFakeWorld = isFakeWorld;
	}

	public void razData(boolean activationIsAvailable) throws BusinessException {
		Collection<Application> apps = manageApplication.findApplications();
		if (apps != null && apps.size() > 0) {
			logger.info("remove {} apps", apps.size());
			for (Application app : apps) {
				try {
                    List<ApplicationRelease> applicationReleases = manageApplicationRelease.findApplicationReleasesByAppUID(app.getUID());
                    logger.info("remove app {} releases", applicationReleases.size());
                    for (ApplicationRelease ar : applicationReleases) {
                        List<EnvironmentDto> allAREnvironments = manageEnvironment.findEnvironmentsByAppRelease(ar.getUID());
                        logger.info("remove app {} envs", allAREnvironments.size());
                        for (EnvironmentDto env : allAREnvironments) {

                            String envName = env.getUid();
                            logger.info("remove env '{}'", envName);
                            if (activationIsAvailable) {
                                manageEnvironment.deleteEnvironment(envName);
                                logger.info("wait env '{}' to be removed", envName);
                                waitForStatus(envName, EnvironmentStatusEnum.REMOVED);
                            } else {
                                manageEnvironment.forceStatusForAndEnvironment(envName, EnvironmentStatus.REMOVED);
                            }
                            logger.info("purge env '{}'", envName);
                            manageEnvironment.purgeRemovedEnvironment(envName);
						}
                        logger.info("purge release '{}'", ar.getUID());
                        manageApplicationRelease.deleteAndPurgeApplicationRelease(ar.getUID());
					}
					manageApplication.deleteAndPurgeApplication(app.getUID());
				} catch (ObjectNotFoundException onfe) {
					logger.warn(onfe.getMessage());
				}
			}
            logger.info("remove users");
			try {
				List<PaasUser> users = managePaasUser.findAllPaasUsers();
				if (users != null) {
					for (PaasUser usr : users) {
						managePaasUser.deletePaasUser(usr.getId());
					}
				}
			} catch (ObjectNotFoundException onfe) {
				logger.warn(onfe.getMessage());
			}
		}
	}

	private void waitForStatus(String environmentId, EnvironmentStatusEnum expectedStatus) throws ObjectNotFoundException {
        int timeoutInMinutes = 60;
        long timeoutMs = System.currentTimeMillis() + timeoutInMinutes * 60 * 1000;

        EnvironmentDto envDto = manageEnvironment.findEnvironmentByUID(environmentId);
        EnvironmentStatusEnum actualStatus = envDto.getStatus();
        logger.debug("wait for status {}, currently {}", expectedStatus.toString(), actualStatus.toString());
		while (expectedStatus != actualStatus) {
			if (!actualStatus.toString().endsWith("ING")
             && actualStatus != EnvironmentStatusEnum.RUNNING) {
				// In a final step, will not change until an action is requested
				throw new TechnicalException("Activation process failed : " + envDto.getStatusMessage() + " status=" + actualStatus);
			}
            if (System.currentTimeMillis() > timeoutMs) {
                throw new TechnicalException("Activation process timeout: environment not " + expectedStatus + " after " + timeoutInMinutes + " minutes");
            }
			try {
				Thread.sleep(2000);
                logger.debug("wait for status {}, currently {}", expectedStatus.toString(), actualStatus.toString());
			} catch (InterruptedException e) {
				// ignore
			}
			envDto = manageEnvironment.findEnvironmentByUID(environmentId);
            actualStatus = envDto.getStatus();
		}
	}

	public Collection<PaasUser> createPaasUsers(String namePrefix, int nbToCreate) {
		Collection<PaasUser> createdUsers = new ArrayList<PaasUser>();
		for (int nbCreated = 0; nbCreated < nbToCreate; nbCreated++) {
			String nameToUse = namePrefix + nbCreated;
			if (nbCreated > 1) {
				nameToUse += nbCreated;
			}
			PaasUser pUsr = new PaasUser(nameToUse, "aLastName", new SSOId(nameToUse), nameToUse+"@orange.com");
			managePaasUser.checkBeforeCreatePaasUser(pUsr);
			createdUsers.add(pUsr);
		}
		return createdUsers;
	}

	public Collection<PaasUser> createTeam(String namePrefix) {
		Collection<PaasUser> createdUsers = new ArrayList<PaasUser>();
		Collection<String> opsTeam = new ArrayList<String>();
		opsTeam.add("Manager");
		opsTeam.add("Architect");
		opsTeam.add("QA");
		opsTeam.add("Dev1");
		opsTeam.add("Dev2");
		for (String name : opsTeam) {
			String nameToUse = namePrefix + "." + name;

			PaasUser pUsr = new PaasUser(nameToUse,nameToUse,new SSOId("ssoid"),nameToUse + "." + nameToUse + "@orange.com");
			managePaasUser.checkBeforeCreatePaasUser(pUsr);
			createdUsers.add(pUsr);
		}
		return createdUsers;
	}

	private Application appFactory(String appLabel, String appCode, PaasUser author) throws DuplicateApplicationException, ApplicationNotFoundException, PaasUserNotFoundException {
		logger.debug("appFactory({})", appLabel);
		long start = System.currentTimeMillis();
		String appUid = manageApplication.createPublicApplication(appCode, appLabel, "demo application " + appLabel, null, author.getSsoId());
		Application demoApp = manageApplication.findApplicationByUID(appUid);
		logger.debug("STATS createApplication duration: " + (System.currentTimeMillis() - start) + "ms");
		return demoApp;
	}

	private ApplicationRelease releaseFactory(PaasUser author, Application app, String releaseVersion) throws MalformedURLException, ObjectNotFoundException,
			DuplicateApplicationReleaseException {

		// ApplicationRelease demoAppRelease = new ApplicationRelease(app,
		// releaseVersion);
		// demoAppRelease.setReleaseVersion(releaseVersion);

		long start = System.currentTimeMillis();
		String releaseUid = manageApplicationRelease.createApplicationRelease(app.getUID(), author.getSsoId().getValue(), releaseVersion);
		ApplicationRelease demoAppRelease = manageApplicationRelease.findApplicationReleaseByUID(releaseUid);
		demoAppRelease.setDescription("Scalability demo application - initial release");
        demoAppRelease = manageApplicationRelease.updateApplicationRelease(demoAppRelease);

		logger.debug("STATS createApplicationRelease duration: " + (System.currentTimeMillis() - start) + "ms");
		return demoAppRelease;
	}

	private void logicalModelFactory(ApplicationRelease appRelease, String logicalModelPattern) throws ObjectNotFoundException, InvalidMavenReferenceException {
		String releaseName = appRelease.getUID();

		// CREATE SPRINGOO LOGICAL MODEL
		LogicalDeployment ld = manageLogicalDeployment.findLogicalDeployment(appRelease.getLogicalDeployment().getId());

		int nbGui = StringUtils.countMatches(logicalModelPattern, LETTER_GUI);
		int nbDB = StringUtils.countMatches(logicalModelPattern, LETTER_DATABASE);
		int nbStore = StringUtils.countMatches(logicalModelPattern, LETTER_STORE);
		int nbNode = StringUtils.countMatches(logicalModelPattern, LETTER_NODE);
		logger.info("logicalModelFactory {} ", releaseName);
		Collection<LogicalWebGUIService> guis = new ArrayList<LogicalWebGUIService>();
		Collection<LogicalRelationalService> dbs = new ArrayList<LogicalRelationalService>();
		Collection<LogicalOnlineStorageService> stores = new ArrayList<LogicalOnlineStorageService>();
		Collection<ProcessingNode> nodes = new ArrayList<ProcessingNode>();

		for (int g = 0; g < nbGui; g++) {
			LogicalWebGUIService webGuiService = webGuiServiceFactory(releaseName + String.valueOf(g));
			ld.addLogicalService(webGuiService);
			guis.add(webGuiService);
		}
		for (int d = 0; d < nbDB; d++) {
			LogicalRelationalService rdbService = relationalDBServiceFactory(releaseName + String.valueOf(d));
			ld.addLogicalService(rdbService);

			dbs.add(rdbService);
		}
		for (int s = 0; s < nbStore; s++) {
			LogicalOnlineStorageService onlineStorageService = onlineStorageServiceFactory(releaseName + String.valueOf(s));
			ld.addLogicalService(onlineStorageService);
			stores.add(onlineStorageService);
		}
		Iterator<LogicalWebGUIService> itGuis = guis.iterator();
		for (int n = 0; n < nbNode; n++) {
			LogicalWebGUIService gui = null;
			if (itGuis.hasNext()) {
				gui = itGuis.next();
			}
			ProcessingNode lenService = cfJavaProcessingFactory(ld, releaseName + "Node." + n, gui, dbs, stores);
			// ld.addExecutionNode(lenService);
			nodes.add(lenService);
		}

		logger.debug("updateLogicalDeployment {}", ld.getName());
		long start = System.currentTimeMillis();

		try {
			ld = manageLogicalDeployment.checkOverallConsistencyAndUpdateLogicalDeployment(ld);
		} catch (BusinessException e) {
			e.printStackTrace(); // To change body of catch statement use File |
									// Settings | File Templates.
		}

		logger.debug("STATS updateLogicalDeployment duration: " + (System.currentTimeMillis() - start) + "ms");
	}

	/**
	 * Creates the environment if not in real world
	 * 
	 * @param author
	 *            env creation author
	 * @param appReleaseUid
	 *            app release unique id
	 * @param envNumber
	 *            id used to name env
	 * @return envOutputName
	 * @throws com.francetelecom.clara.cloud.technicalservice.exception.ObjectNotFoundException
	 */
	private String environmentFactory(PaasUser author, String appReleaseUid, int envNumber) throws BusinessException {
		String environmentUid = "noenv";
		if (this.isFakeWorld) {
            String releaseName = appReleaseUid;
			String envInputName = releaseName + "Env" + String.valueOf(envNumber);
			long start = System.currentTimeMillis();
			environmentUid = manageEnvironment.createEnvironment(appReleaseUid, DEFAULT_ENV_TYPE, author.getSsoId().getValue(), envInputName);
			logger.debug("STATS createEnvironment duration: " + (System.currentTimeMillis() - start) + "ms");
            logger.info("wait env '{}' to be created", environmentUid);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        } else {
			logger.error("You are running scalability feature in a real world! Ignoring environment creation...");
		}
		return environmentUid;
	}

	/**
	 * WEB_GUI Service production
	 * 
	 * @param releaseName
	 * @return
	 */
	private LogicalWebGUIService webGuiServiceFactory(String releaseName) {
		LogicalWebGUIService lsWebUi = new LogicalWebGUIService();
		lsWebUi.setLabel(releaseName + "WebUi");
		lsWebUi.setContextRoot(new ContextRoot("/"));
		lsWebUi.setSecure(false);
		lsWebUi.setStateful(false);
		return lsWebUi;
	}

	/**
	 * RelationalDB Service production
	 * 
	 * @param releaseName
	 * @return
	 */
	private LogicalRelationalService relationalDBServiceFactory(String releaseName) {
		LogicalRelationalService lsRelational = new LogicalRelationalService();
		lsRelational.setLabel(releaseName + "DB");
		lsRelational.setServiceName("postgres-db");
		lsRelational.setCapacityMo(1000);
		lsRelational.setMaxConnection(50);
		lsRelational.setRelationalReplicaNumber(0);
		lsRelational.setSqlVersion(LogicalRelationalServiceSqlDialectEnum.POSTGRESQL_DEFAULT);

		// manageLogicalDeployment.updateLogicalDeployment(lsRelational);
		return lsRelational;
	}

	/**
	 * Store service production
	 * 
	 * @param releaseName
	 * @return
	 */
	private LogicalOnlineStorageService onlineStorageServiceFactory(String releaseName) {
		LogicalOnlineStorageService lsOnline = new LogicalOnlineStorageService();
		lsOnline.setLabel(releaseName + "eDB");
        lsOnline.setServiceName("demo-edb");
        lsOnline.setStorageCapacityMb(100);
		return lsOnline;
	}

	/**
	 * Execution node production, including service associations.
	 * 
	 * @param name
	 * @param gui
	 * @param dbs
	 * @param stores
	 * @return
	 */
	private ProcessingNode cfJavaProcessingFactory(LogicalDeployment ld, String name, LogicalWebGUIService gui,
												   Collection<LogicalRelationalService> dbs, Collection<LogicalOnlineStorageService> stores) {
		// log only the first logical model
		if (logger.isDebugEnabled()) {
			String svc = (gui != null ? "1" + LETTER_GUI : "") + (dbs != null ? dbs.size() + LETTER_DATABASE : "")
					+ (stores != null ? stores.size() + LETTER_STORE : "");
			logger.debug("Create execution node {} with {}", name, svc);
		}
		ProcessingNode len = new CFJavaProcessing();
		len.setLabel(name);
		String appName = "cf-wicket-jpa";
		String type = "war";
		len.setSoftwareReference(sampleAppProperties.getMavenReference(appName, type));
		MavenReference lenMr = len.getSoftwareReference();
		assert (lenMr != null) : "maven reference should not be null ! for " + appName;
		logger.info("resolve execution node maven reference {}", lenMr.toString());

		ld.addExecutionNode(len);

		if (gui != null) {
			// expecting a single execution node to be producing a webGUI
			len.addLogicalServiceUsage(gui, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
		}

		// db services
		if (dbs != null) {
			for (LogicalRelationalService db : dbs) {
				len.addLogicalServiceUsage(db, LogicalServiceAccessTypeEnum.READ_WRITE);
			}
		}

		// store services
		if (stores != null) {
			for (LogicalOnlineStorageService store : stores) {
				len.addLogicalServiceUsage(store, LogicalServiceAccessTypeEnum.READ_WRITE);
			}
		}
		// deprecated ? // manageLogicalDeployment.resolveMavenURL(len);
		return len;
	}

	/**
	 * @param author
	 * @param appName
	 * @param nbApp
	 * @param nbReleasePerApp
	 * @param logicalModelPattern
	 * @see com.francetelecom.clara.cloud.scalability.ManageScalability
	 * @return
	 * @throws com.francetelecom.clara.cloud.commons.BusinessException
	 */
	public Collection<ApplicationRelease> createApplications(PaasUser author, String appName, int nbApp, int nbReleasePerApp, String logicalModelPattern)
			throws BusinessException {
		Collection<ApplicationRelease> appReleases = new ArrayList<ApplicationRelease>();
		try {
			for (int i = 0; i < nbApp; i++) {
				String appId;
				String appCode;
				Application demoApp;
				appId = "CFWicketJPA" + UUID.randomUUID();
				appCode = "CWJ" + UUID.randomUUID();
				// *** create app
				demoApp = appFactory(appId, appCode, author);
				for (int j = 0; j < nbReleasePerApp; j++) {
					String relMinorId = String.valueOf(j);
					String releaseVersion = "G0R0C" + relMinorId;
					// *** create app-release
					ApplicationRelease demoAppRelease = releaseFactory(author, demoApp, releaseVersion);
					// *** create logical models
					logicalModelFactory(demoAppRelease, logicalModelPattern);
					appReleases.add(demoAppRelease);
				}
			}
		} catch (MalformedURLException mue) {
			throw new BusinessException("incorrect URL : ",mue);
		}
		return appReleases;
	}

	public Application populateSimpleTestPhase(PaasUser author, boolean createEnv) throws BusinessException {
		logger.info("populateSimpleTestPhase()");
        int numberOfEnvironmentToCreate = 0;
        if (createEnv) {
            numberOfEnvironmentToCreate = 1;
        }
        Collection<ApplicationRelease> applicationReleases = populate("GND", "simpleTestPhase", 1, 1, numberOfEnvironmentToCreate);
        ApplicationRelease firstApplicationRelease = applicationReleases.iterator().next();
        return firstApplicationRelease.getApplication();
	}

    /**
     * @param pattern a string that include :
     *         - 'G' to create a gui
     *         - 'N' to create an execution node,
     *         - 'D' to create a relational database
     *         - 'S' to create an online store
     * @param teamName name of the set of sample users
     * @param nbApp number of application to create
     * @param nbReleasePerApp number of release per app to create
     * @param nbEnvPerRelease number of environment per release to create
     * @return
     * @throws BusinessException
     */
	public Collection<ApplicationRelease> populate(String pattern, String teamName, int nbApp, int nbReleasePerApp, int nbEnvPerRelease)
			throws BusinessException {
		// users
		Collection<PaasUser> users = createTeam(teamName);
		PaasUser author = users.iterator().next();

		// app, releases, logicalmodel
		Collection<ApplicationRelease> releases = createApplications(author, "App of " + teamName, nbApp, nbReleasePerApp, pattern);

		// env
		int resultAwaitingEnv = nbApp * nbReleasePerApp * nbEnvPerRelease;
		logger.info("Populating " + resultAwaitingEnv + " environment(s)...");
		for (ApplicationRelease release : releases) {
            final String releaseUid = release.getUID();
            for (int k = 0; k < nbEnvPerRelease; k++) {
				environmentFactory(author, releaseUid, k);
			}
		}
		return releases;
	}
}
