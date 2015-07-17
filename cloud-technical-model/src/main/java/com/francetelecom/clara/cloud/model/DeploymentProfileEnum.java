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
package com.francetelecom.clara.cloud.model;

/**
 * Deployment profile is the destination of a technical deployment.
 * 
 * A TechnicalDeployment is dedicated to a target activity, corresponding to the following profiles
 *
 * 	DEVELOPMENT,
 *	TEST,
 *	LOAD_TEST,
 *	PRODUCTION
 *
 * @author apog7416
 *
 */
public enum DeploymentProfileEnum {
	DEVELOPMENT,
	TEST,
	LOAD_TEST,
    PRE_PROD, /** works with same data and hardware profile as production but is not visible to customers */
	PRODUCTION
}
