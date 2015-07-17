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
 * The logical model package focuses on logical representation of the application.
 * This is a programmatic representation of the <a href="http://private-url.elpaaso.orgShared%20Documents/Architecture/El%20PaaSo%20Service%20Catalog.doc">service catalog</a>
 *
 * This is a high level representation that does not make hypothesis how the technical implementation
 * will be done.
 *
 * The a logical model is represented by a {@link com.francetelecom.clara.cloud.logicalmodel.LogicalDeployment} which holds a set of external services:
 * <ul>
 *     <li>{@link com.francetelecom.clara.cloud.logicalmodel.LogicalWebGUIService} for HTTP-based UIs</li>
 *     <li>{@link com.francetelecom.clara.cloud.logicalmodel.LogicalSoapConsumer} for consumming SOAP web services exposed by other applications</li>
 *     <li>{@link com.francetelecom.clara.cloud.logicalmodel.LogicalSoapService} for exposing SOAP web services to other applications</li>
 *     <li>{@link com.francetelecom.clara.cloud.logicalmodel.LogicalQueueSendService} for sending asynchrnous recexposing SOAP web services to other applications</li>
 * </ul>
 */
package com.francetelecom.clara.cloud.logicalmodel;


