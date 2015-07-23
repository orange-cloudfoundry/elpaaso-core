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
package com.francetelecom.clara.cloud.application.impl;

import com.francetelecom.clara.cloud.application.ManageLogicalDeployment;
import com.francetelecom.clara.cloud.commons.BusinessException;
import com.francetelecom.clara.cloud.commons.InvalidMavenReferenceException;
import com.francetelecom.clara.cloud.commons.MavenReference;
import com.francetelecom.clara.cloud.commons.ValidatorUtil;
import com.francetelecom.clara.cloud.coremodel.Application;
import com.francetelecom.clara.cloud.coremodel.ApplicationRelease;
import com.francetelecom.clara.cloud.coremodel.ApplicationReleaseRepository;
import com.francetelecom.clara.cloud.coremodel.exception.InvalidReleaseException;
import com.francetelecom.clara.cloud.coremodel.exception.ObjectNotFoundException;
import com.francetelecom.clara.cloud.dao.LogicalDeploymentCloner;
import com.francetelecom.clara.cloud.logicalmodel.*;
import com.francetelecom.clara.cloud.model.TechnicalDeployment;
import com.francetelecom.clara.cloud.mvn.consumer.MavenReferenceResolutionException;
import com.francetelecom.clara.cloud.mvn.consumer.MvnRepoDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Business implementation for LogicalDeployment component
 * 
 * All methods are defined as transactional. If no transaction in progress
 * during method call, then it will start a new transaction.
 * 
 * Last updated : $LastChangedDate: 2012-06-11 17:23:44 +0200 (lun., 11 juin
 * 2012) $ Last author : $Author$
 * 
 * @author Clara
 * @version : $Revision$
 */
public class ManageLogicalDeploymentImpl implements ManageLogicalDeployment {

	private static final Logger log = LoggerFactory.getLogger(ManageLogicalDeploymentImpl.class);

    @Autowired
	private LogicalDeploymentRepository logicalDeploymentRepository;

	@Autowired
	private MvnRepoDao mvnRepoDao;

	@Autowired
	private ApplicationReleaseRepository applicationReleaseRepository;

	@Autowired
	private LogicalDeploymentCloner cloner;

	@Override
	public void checkLogicalSoapServiceConsistency(LogicalSoapService logSoapService, boolean fullValidation) throws BusinessException {
		TechnicalDeployment td = new TechnicalDeployment("TestLogicalSoapServiceOnly");
		// check soap attachments maven reference
		MavenReference serviceAttachmentsMavenRef = logSoapService.getServiceAttachments();
		try {
			URL accessUrl = checkMavenReference(serviceAttachmentsMavenRef);
			serviceAttachmentsMavenRef.setAccessUrl(accessUrl);
		} catch (InvalidMavenReferenceException e) {
			log.warn("Invalid logical soap service attachments maven reference");
			throw e;
		}

		// assert logical soap service is valid
		//FIXME is commented because cannot map to wso2 strategy 
		//projectionTmaasStrategy.assertIsValid(td,logSoapService,fullValidation);

	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, rollbackForClassName = { "BusinessException" })
	public LogicalDeployment checkOverallConsistencyAndUpdateLogicalDeployment(LogicalDeployment logicalDeployment) throws BusinessException {
		// First check logical deployment has been persisted
		LogicalDeployment persisted = logicalDeploymentRepository.findOne(logicalDeployment.getId());
		if (persisted == null) {
			String message = "LogicalDeployment[" + logicalDeployment.getName() + "] does not exist";
			log.error(message);
			throw new ObjectNotFoundException(message);
		}

		// check consistency and update any model element which might be
		// generated/modified during check
		// typically resolved maven references are updated on execution nodes
		// and logical services
		checkOverallConsistency(logicalDeployment, true);

		// if no error, then persist our updates
		return logicalDeploymentRepository.save(logicalDeployment);
	}

	@Override
	public void checkOverallConsistency(LogicalDeployment logicalDeployment) throws BusinessException {
		// check consistency but do not update model elements which might be
		// generated/modified during check
		// typically resolved maven references are NOT updated on execution
		// nodes and logical services
		checkOverallConsistency(logicalDeployment, false);
	}

	/**
	 * Check that logical model is consistent. Check config sets and maven
	 * references. Maven references are checked by resolving their access urls.
	 * 
	 * @param logicalDeployment
	 *            ld
	 * @param updateModelFlag
	 *            true if model elements such as resolved maven references must
	 *            be updated
	 * @throws BusinessException
	 *             , the exact exception is LogicalModelNotConsistentException
	 *             which wraps all inconsistency error exceptions raised during
	 *             check
	 */
	protected void checkOverallConsistency(LogicalDeployment logicalDeployment, boolean updateModelFlag) throws BusinessException {
		// Exception is created in case we need to fill it
		// We will run a set of checks
		// For each check we catch business exception and add it as a unit
		// errors to be added to a main LogicalModelNotConsistentException
		List<BusinessException> errors = new ArrayList<BusinessException>();

		// CHECK MAVEN REFERENCES (check if they can be resolved)

		// build the list of all maven references
		List<MavenReference> mavenReferences = new ArrayList<MavenReference>();
		for (ProcessingNode jeeProcessing : logicalDeployment.listProcessingNodes()) {
			if (jeeProcessing.getSoftwareReference() != null){
				
				if (jeeProcessing.isOptionalSoftwareReference()){
					log.info("Ignoring optional artifact reference verification for "+ jeeProcessing.toString());
				} else
				{ mavenReferences.add(jeeProcessing.getSoftwareReference());
				}
			}
		}
		// check soap service files (wsdl & xsd) package
		for (LogicalSoapService soapService : logicalDeployment.listLogicalServices(LogicalSoapService.class)) {
			if (soapService.getServiceAttachments() != null)
				mavenReferences.add(soapService.getServiceAttachments());
		}
		// Check each maven references and update access URL if updateModelFlag
		// is true
		for (MavenReference mavenRef : mavenReferences) {
			URL accessUrl = null;
			try {
				accessUrl = checkMavenReference(mavenRef);
			} catch (InvalidMavenReferenceException e) {
				errors.add(e);
			}
			if (updateModelFlag)
				mavenRef.setAccessUrl(accessUrl);
		}

		try {
			logicalDeployment.checkOverallConsistency();
		} catch (LogicalModelNotConsistentException e) {
			errors.addAll(e.getErrors());
		}
		// is our list of errors empty ? if not throw exception
		if (!errors.isEmpty()) {
			for (BusinessException businessException : errors) {
				log.warn("Error when validating logical model. Invalid user input ? ", businessException);
			}
			throw new LogicalModelNotConsistentException(errors);
		}
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, rollbackForClassName = { "BusinessException" })
	public LogicalDeployment updateLogicalDeployment(LogicalDeployment logicalDeployment) throws ObjectNotFoundException, InvalidMavenReferenceException {
		LogicalDeployment persisted = logicalDeploymentRepository.findOne(logicalDeployment.getId());
		if (persisted == null) {
			String message = "LogicalDeployment[" + logicalDeployment.getName() + "] does not exist";
			log.error(message);
			throw new ObjectNotFoundException(message);
		}

		// Validate model
		ValidatorUtil.validate(logicalDeployment);

		return logicalDeploymentRepository.save(logicalDeployment);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, rollbackForClassName = { "BusinessException" })
	public void cloneLogicalDeployment(String applicationReleaseSourceUID, String applicationReleaseTargetUID) throws InvalidReleaseException {
		ApplicationRelease sourceAr = applicationReleaseRepository.findByUID(applicationReleaseSourceUID);
		if (sourceAr == null) {
			throw new InvalidReleaseException("Invalid applicationReleaseSourceInternalName" + applicationReleaseSourceUID);
		}
		ApplicationRelease targetAr = applicationReleaseRepository.findByUID(applicationReleaseTargetUID);
		if (targetAr == null) {
			throw new InvalidReleaseException("Invalid applicationReleaseTargetInternalName" + applicationReleaseTargetUID);
		}
		Application sourceApp = sourceAr.getApplication();
		Application targetApp = targetAr.getApplication();
		if ((applicationReleaseRepository.findApplicationReleasesByAppUID(targetAr.getUID()).size() > 1) && !targetApp.getUID().equals(sourceApp.getUID())) {
			throw new InvalidReleaseException("Invalid applicationReleaseTargetInternalName" + applicationReleaseTargetUID);
		}

		if (targetAr.isDiscarded()) {
			throw new InvalidReleaseException("cannot clone logical deployment to a DISCARDED application release");
		}

		LogicalDeployment sourceLd = sourceAr.getLogicalDeployment();
		LogicalDeployment targetLd = targetAr.getLogicalDeployment();

		LogicalDeployment sourceClone = cloner.deepCopy(sourceLd);
		targetAr.replaceLd(sourceClone);

		// then delete the target Ld to avoid dangling objects in DB
		logicalDeploymentRepository.delete(targetLd);

		logicalDeploymentRepository.save(sourceClone);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
	public LogicalDeployment findLogicalDeployment(int logicalDeploymentId) throws ObjectNotFoundException {
		LogicalDeployment logicalDeployment = logicalDeploymentRepository.findOne(logicalDeploymentId);
		if (logicalDeployment == null) {
			String message = "LogicalDeployment[" + logicalDeploymentId + "] does not exist";
			log.error(message);
			throw new ObjectNotFoundException(message);
		}
		// TODO verifier strategie de loading des logicalServices et
		// processingNodes pour forcer le chargement des LogicalServices
		logicalDeployment.listProcessingNodes().size();
		logicalDeployment.listLogicalServices().size();
		for (LogicalService logicalService : logicalDeployment.listLogicalServices()) {
			logicalService.listLogicalServicesAssociations().size();
		}
		// pour forcer le chargement des NodeClusters
		// logicalDeployment.listNodeClusters().size();
		for (ProcessingNode jeeProcessing : logicalDeployment.listProcessingNodes()) {
			jeeProcessing.listLogicalServicesAssociations().size();
		}
		// retourne un logical deployment contenant des logicalServices et
		// processingNodes charges
		return logicalDeployment;
	}

	@Override
	public void resolveMavenURL(ProcessingNode node) {
		MavenReference reference = mvnRepoDao.resolveUrl(node.getSoftwareReference());
		node.setSoftwareReference(reference);
	}

	@Override
	public URL checkMavenReference(MavenReference mavenReference) throws InvalidMavenReferenceException {
		MavenReference updatedMavenReference = null;
		try {
			updatedMavenReference = mvnRepoDao.resolveUrl(mavenReference);
		} catch (MavenReferenceResolutionException e) {
			throw new InvalidMavenReferenceException(mavenReference, InvalidMavenReferenceException.ErrorType.ARTIFACT_NOT_FOUND);
		}
		assert (updatedMavenReference != null) : "updatedMavenReference provided by mvnRepoDao.resolveUrl(...) is null (mvnRepoDao mocked ? cf. wiki)";
		return updatedMavenReference.getAccessUrl();
	}

	public void setLogicalDeploymentRepository(LogicalDeploymentRepository logicalDeploymentRepository) {
		this.logicalDeploymentRepository = logicalDeploymentRepository;
	}

	public void setMvnRepoDao(MvnRepoDao mvnRepoDao) {
		this.mvnRepoDao = mvnRepoDao;
	}

	@Override
	public List<String> getQrsApplicationVersions(String domain, String appName) throws ObjectNotFoundException {
        List<String> lstApplicationVersions = new ArrayList<String>();

        if ("ADV".equalsIgnoreCase(appName)){
            lstApplicationVersions.add("G1R0C0");
            lstApplicationVersions.add("G2R0C0");
            lstApplicationVersions.add("G3R0C0");
        } else if ("IODA".equalsIgnoreCase(appName) || "SOFT".equalsIgnoreCase(appName)) {
            lstApplicationVersions.add("G1R0C0");
            lstApplicationVersions.add("G2R0C0");
        } else if ("PtpmFrontOffice".equalsIgnoreCase(appName)) {
            lstApplicationVersions.add("G1R1C0");
        } else if ("PtpmBackOffice".equalsIgnoreCase(appName)) {
            lstApplicationVersions.add("G1R2C0");
        } else if ("SEBA IN PAAS".equalsIgnoreCase(appName) || ("SEBA IN CLOUD".equalsIgnoreCase(appName))
                || "SEBA OUT CLOUD".equalsIgnoreCase(appName)){
            lstApplicationVersions.add("G6R0C0");
        } else if ("Momoo Client".equalsIgnoreCase(appName) || "Momoo Server".equalsIgnoreCase(appName)){
            lstApplicationVersions.add("G6R0C0");
        }
        return lstApplicationVersions;
	}

	@Override
	public List<String> getQrsApplications(String domain) throws ObjectNotFoundException {
        List<String> lstApplicationName = new ArrayList<String>();
        lstApplicationName.add("ADV");
        lstApplicationName.add("IODA");
        lstApplicationName.add("DLIS");
        lstApplicationName.add("SOFT");
        lstApplicationName.add("PtpmFrontOffice");
        lstApplicationName.add("PtpmBackOffice");
        lstApplicationName.add("SEBA IN PAAS");
        lstApplicationName.add("SEBA IN CLOUD");
        lstApplicationName.add("SEBA OUT CLOUD");
        lstApplicationName.add("Momoo Client");
        lstApplicationName.add("Momoo Server");
        return lstApplicationName;
    }

	@Override
	public List<String> getQrsServices(String domain, String appName, String appVersion) throws ObjectNotFoundException {
        List<String> lstServices = new ArrayList<String>();
        if ("ADV".equalsIgnoreCase(appName)) {
            lstServices.add("getClient");
            lstServices.add("getMISDN");
            lstServices.add("getIMSI");
        } else if ("IODA".equalsIgnoreCase(appName)) {
            lstServices.add("setIMSI");
            lstServices.add("setMISDN");
        } else if ("DLIS".equalsIgnoreCase(appName)) {
            lstServices.add("getDelayLiv");
        } else if ("SOFT".equalsIgnoreCase(appName)) {
            lstServices.add("setDelayLiv");
        } else if ("PtpmFrontOffice".equalsIgnoreCase(appName)) {
            lstServices.add("pong");
        } else if ("PtpmBackOffice".equalsIgnoreCase(appName)) {
            lstServices.add("ping");
        } else if ("SEBA IN PAAS".equalsIgnoreCase(appName) || "SEBA IN CLOUD".equalsIgnoreCase(appName)
                || "SEBA OUT CLOUD".equalsIgnoreCase(appName)){
            lstServices.add("subscribeLine");
        } else if ("Momoo Server".equalsIgnoreCase(appName)){
            lstServices.add("createMarket");
            lstServices.add("updateMarket");
            lstServices.add("deleteMarket");
            lstServices.add("createCatalog");
            lstServices.add("updateCatalog");
            lstServices.add("deleteCatalog");
            lstServices.add("createArticle");
            lstServices.add("updateArticle");
            lstServices.add("deleteArticle");
        } else if ("Momoo Client".equalsIgnoreCase(appName)){
            lstServices.add("returnMarket");
            lstServices.add("returnCatalog");
            lstServices.add("returnArticle");
        }
        return lstServices;
	}

	@Override
	public List<String> getQrsServicesVersions(String domain, String appName, String appVersion, String serviceName) throws ObjectNotFoundException {
        List<String> lstServiceVersions = new ArrayList<String>();
        if ("getClient".equalsIgnoreCase(serviceName)){
            lstServiceVersions.add("G1R0C0");
            lstServiceVersions.add("G2R0C0");
        } else if ("getMISDN".equalsIgnoreCase(serviceName)) {
            lstServiceVersions.add("G1R0C0");
            lstServiceVersions.add("G2R0C0");
            lstServiceVersions.add("G2R1C0");
        } else if ("getIMSI".equalsIgnoreCase(serviceName)) {
            lstServiceVersions.add("G1R0C0");
            lstServiceVersions.add("G2R0C0");
        } else if ("setIMSI".equalsIgnoreCase(serviceName)) {
            lstServiceVersions.add("G1R0C0");
            lstServiceVersions.add("G2R0C0");
        } else if ("setMISDN".equalsIgnoreCase(serviceName)) {
            lstServiceVersions.add("G1R0C0");
            lstServiceVersions.add("G2R0C0");
        } else if ("getDelayLiv".equalsIgnoreCase(serviceName)) {
            lstServiceVersions.add("G1R0C0");
        } else if ("setDelayLiv".equalsIgnoreCase(serviceName)) {
            lstServiceVersions.add("G1R0C0");
        } else if ("ping".equalsIgnoreCase(serviceName)) {
            lstServiceVersions.add("G1R1C0");
        } else if ("pong".equalsIgnoreCase(serviceName)) {
            lstServiceVersions.add("G1R1C0");
        } else if ("subscribeLine".equalsIgnoreCase(serviceName)) {
            lstServiceVersions.add("G1R0C0");
        } else {
            lstServiceVersions.add("G1R0C0");
        }
        return lstServiceVersions;
	}


}
