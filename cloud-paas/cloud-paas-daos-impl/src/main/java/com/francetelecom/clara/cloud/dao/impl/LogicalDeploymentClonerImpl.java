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
package com.francetelecom.clara.cloud.dao.impl;

import com.francetelecom.clara.cloud.dao.LogicalDeploymentCloner;
import com.francetelecom.clara.cloud.logicalmodel.LogicalDeployment;

/**
 * Clones a logical deployment using stream XML serialization and deserialization,
 * and inserting clearing of id and adding a name prefix.
 */
public class LogicalDeploymentClonerImpl extends ModelItemClonerXstreamImpl<LogicalDeployment> implements LogicalDeploymentCloner {
}
