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
package com.francetelecom.clara.cloud.paas.projection.cf;

import com.francetelecom.clara.cloud.commons.MavenReference;
import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.francetelecom.clara.cloud.logicalmodel.LogicalDeployment;
import com.francetelecom.clara.cloud.logicalmodel.LogicalRelationalService;
import com.francetelecom.clara.cloud.logicalmodel.LogicalRelationalServiceSqlDialectEnum;
import com.francetelecom.clara.cloud.model.TechnicalDeployment;
import com.francetelecom.clara.cloud.mvn.consumer.MvnRepoDao;
import com.francetelecom.clara.cloud.paas.projection.security.CryptService;
import com.francetelecom.clara.cloud.techmodel.bindings.DbaasUserProvidedService;
import com.francetelecom.clara.cloud.techmodel.cf.Space;
import com.francetelecom.clara.cloud.techmodel.dbaas.DBaasSubscriptionSqlDialectEnum;
import com.francetelecom.clara.cloud.techmodel.dbaas.DBaasSubscriptionV2;
import com.francetelecom.clara.cloud.techmodel.dbaas.DbOwner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Set;

/**
 * 
 * Generate DBaas subscription according to the DBaasVersion specified.
 * 
 */
public class RelationalServiceProjectionRule implements ServiceProjectionRule {

    private static final Logger logger = LoggerFactory.getLogger(RelationalServiceProjectionRule.class);

	/**
	 * Used to generate random passwords.
	 */
	private CryptService cryptService;

	/**
	 * Maven DAO to check artifact existence
	 */
	private MvnRepoDao mvnDao;

	private String dbaasVersion;

    public RelationalServiceProjectionRule() {
    }

    public RelationalServiceProjectionRule(CryptService cryptService, MvnRepoDao mvnDao, String dbaasVersion) {
        this.cryptService = cryptService;
        this.mvnDao = mvnDao;
		this.dbaasVersion = dbaasVersion;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.francetelecom.clara.cloud.paas.projection.cf.
	 * RelationalServiceProjectionStrategy
	 * #generate(com.francetelecom.clara.cloud
	 * .logicalmodel.LogicalRelationalService,
	 * com.francetelecom.clara.cloud.model.TechnicalDeployment,
	 * com.francetelecom.clara.cloud.model.DeploymentProfileEnum)
	 */
    public DBaasSubscriptionV2 generatedDBaasSubscription(final LogicalRelationalService relationalService, final TechnicalDeployment td) {

		logger.debug("generating DBaaS subscription for " + relationalService);

		// Generate DBaasSubscription
		DBaasSubscriptionV2 sub = new DBaasSubscriptionV2(td);
		String description = "Paas initiated DBaaS subscription for logical service=[" + relationalService.getName() + "-" + relationalService.getLabel() + "].";
		sub.setDescription(description);
		sub.setDbaasVersion(dbaasVersion);
		sub.setSqlDialect(DBaasSubscriptionSqlDialectEnum.valueOf(relationalService.getSqlVersion().name()));

		sub.changeStorageCapacity(relationalService.getCapacityMo());

		DbOwner dbOwner = new DbOwner("dbowner", this.cryptService.generateRandomPassword());
		sub.changeDbOwner(dbOwner);

		sub.setLogicalModelId(relationalService.getName());

		MavenReference initialPopulationScript = relationalService.getInitialPopulationScript();
		if (initialPopulationScript != null) {
			// Resolve URL to be sure that the reference is correct
			MavenReference initialPopulationScriptResolved = mvnDao.resolveUrl(initialPopulationScript);
			URL initialPopulationScriptAccessUrl = initialPopulationScriptResolved.getAccessUrl();
			if (initialPopulationScriptAccessUrl == null) {
				throw new TechnicalException("Unexpected unresolved accessUrl for SQL script MavenRef " + initialPopulationScript + "associated with LRS=" + relationalService);
			}
			sub.setInitialPopulationScript(initialPopulationScript);
		}

		return sub;
	}

    protected DbaasUserProvidedService.UriScheme getUriScheme(LogicalRelationalServiceSqlDialectEnum sqlVersion) {
        switch (sqlVersion) {
            case MYSQL_DEFAULT:
			return DbaasUserProvidedService.UriScheme.mysql;
            case DEFAULT: // fall through
            default: // fall through
            case POSTGRESQL_DEFAULT:
                return DbaasUserProvidedService.UriScheme.postgres;
		}
	}

	public void setCryptService(CryptService cryptService) {
		this.cryptService = cryptService;
	}

	public void setMvnDao(MvnRepoDao mvnDao) {
		this.mvnDao = mvnDao;
	}

	public void setDbaasVersion(String dbaasVersion) {
		this.dbaasVersion = dbaasVersion;
	}

    @Override
    public void apply(LogicalDeployment ld, TechnicalDeployment td, ProjectionContext projectionContext) {
        Set<LogicalRelationalService> logicalRelationalServices = ld.listLogicalServices(LogicalRelationalService.class);
        for (LogicalRelationalService logicalRelationalService : logicalRelationalServices) {
            // /generate dbaas subscription
            generatedDBaasSubscription(logicalRelationalService, td);
            // create service to handle dbaas connection
            for (DBaasSubscriptionV2 subscriptionToBind : td.listXaasSubscriptionTemplates(DBaasSubscriptionV2.class, logicalRelationalService.getName())) {
                toDbaasUserProvidedService(logicalRelationalService, subscriptionToBind, td, projectionContext.getSpace());
            }
        }
    }

    private DbaasUserProvidedService toDbaasUserProvidedService(LogicalRelationalService logicalRelationalService, DBaasSubscriptionV2 subscriptionToBind, TechnicalDeployment td,
                                                                Space space) {
        DbaasUserProvidedService.UriScheme uriScheme = getUriScheme(logicalRelationalService.getSqlVersion());
        DbaasUserProvidedService dbaasUserProvidedService = new DbaasUserProvidedService(td, logicalRelationalService.getServiceName(), uriScheme, subscriptionToBind, space);
        dbaasUserProvidedService.setLogicalModelId(logicalRelationalService.getName());
        return dbaasUserProvidedService;
    }

}
