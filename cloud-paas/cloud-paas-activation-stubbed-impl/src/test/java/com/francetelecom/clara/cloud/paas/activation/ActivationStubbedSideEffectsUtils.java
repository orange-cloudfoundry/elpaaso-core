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
package com.francetelecom.clara.cloud.paas.activation;

import com.francetelecom.clara.cloud.application.ManageTechnicalDeploymentInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Simulates the side effects of the activation on the database content:
 * <ol>
 * <li> IP addresses allocation </li>
 * </ol>
 */
public class ActivationStubbedSideEffectsUtils {

    private static Logger logger = LoggerFactory.getLogger(ActivationStubbedSideEffectsUtils.class.getName());

    @Autowired
    private ManageTechnicalDeploymentInstance manageTechnicalDeploymentInstance;

    public ActivationStubbedSideEffectsUtils() {
    }

}