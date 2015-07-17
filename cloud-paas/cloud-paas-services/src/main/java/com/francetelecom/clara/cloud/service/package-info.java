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
 * The “cloud-paas-services” exposes a module includes the service layer which defines the behaviour of the service as seen
 * from the outside.
 *
 * The “cloud-paas-services” is the entry point of the application layer:<ul>
 * <li>assembles business layer services (activation, projection) in order to implement application logic
 * <li>in charge of transaction demarcation
 * <li>exposes facade services that are directly usable by provider layer (the UI and the external web services)
 * </ul>
 *
 * The {@link com.francetelecom.clara.cloud.service} package exposes facade services for performing main application logic:
 * {@link com.francetelecom.clara.cloud.core.service.ManageApplication} {@link com.francetelecom.clara.cloud.application.ManageApplicationRelease]
 * {@link com.francetelecom.clara.cloud.environment.ManageEnvironment}
 *
 * The {@link com.francetelecom.clara.cloud.service} package only include interfaces, whereas the mock and reference
 * implementations are in the {@link com.francetelecom.clara.cloud.service.impl.mock} and {@link com.francetelecom.clara.cloud.service.impl.mock} which manipulates the core and logical
 * models through a set of DAO.
 *
 * Whereas currently, the implementation does not support configureable workflow, in the
 * future the application life cycle management might leverage a BPM engine (see {@link com.francetelecom.clara.cloud.misc.command}) for this.
 * This module interacts with the cloud-paas-projection and cloud-paas-activation modules, and manipulates the model
 * components.
 */
package com.francetelecom.clara.cloud.service;


