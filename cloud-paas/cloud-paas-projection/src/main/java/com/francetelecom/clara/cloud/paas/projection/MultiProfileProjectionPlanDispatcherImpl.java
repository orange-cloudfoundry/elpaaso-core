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
package com.francetelecom.clara.cloud.paas.projection;

import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.francetelecom.clara.cloud.logicalmodel.ProcessingNode;
import com.francetelecom.clara.cloud.model.DeploymentProfileEnum;
import com.francetelecom.clara.cloud.paas.constraint.ProjectionPlan;
import com.francetelecom.clara.cloud.paas.projection.cf.ProjectionPlanStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;



/**
 * This new dispatcher enables the paas to produce different architecture, depending on the middlewareprofileversion of the release,
 * and the environment type.
 * 
 * Technically, it makes a lookup in a SpringBeans hashtable of all possible projection, per middlewareProfileVersion and env type.
 * 
 * @author APOG7416
 *
 */
public class MultiProfileProjectionPlanDispatcherImpl implements ProjectionPlanStrategy {

	private static Logger logger=LoggerFactory.getLogger(MultiProfileProjectionPlanDispatcherImpl.class);
	
	
	
	private HashMap<DeploymentProfileEnum,ProjectionPlanStrategy> projectionsPlanPerProfile;

	
	
	@Override
	public ProjectionPlan getApplicationServerProjectionPlan(ProcessingNode node, DeploymentProfileEnum profile) {
		ProjectionPlanStrategy ps=this.projectionsPlanPerProfile.get(profile);
		if (ps==null){
			throw new TechnicalException("No Projection Plan found for profile "+profile);
		}
		
		//found correct strategy, call it
		ProjectionPlan pp=ps.getApplicationServerProjectionPlan(node, profile);
		return pp;
		
	}


	/**
	 * IOC
	 * @param projectionsPlanPerProfile
	 */
	public void setProjectionsPlanPerProfile(HashMap<DeploymentProfileEnum, ProjectionPlanStrategy> projectionsPlanPerProfile) {
		this.projectionsPlanPerProfile = projectionsPlanPerProfile;
	}





}
