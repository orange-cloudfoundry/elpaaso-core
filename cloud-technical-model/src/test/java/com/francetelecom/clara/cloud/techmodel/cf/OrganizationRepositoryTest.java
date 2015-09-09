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
package com.francetelecom.clara.cloud.techmodel.cf;

import com.francetelecom.clara.cloud.model.TechnicalDeployment;
import com.francetelecom.clara.cloud.model.TechnicalDeploymentRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by sbortolussi on 07/07/2015.
 */
@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class OrganizationRepositoryTest {

    @Autowired
    OrganizationRepository organizationRepository;

    @Autowired
    TechnicalDeploymentRepository technicalDeploymentRepository;

    @Autowired
    ManagedServiceRepository managedServiceRepository;

    @Autowired
    SpaceRepository spaceRepository;

    @Test
    public void should_merge_organization() throws Exception {
        TechnicalDeployment td = new TechnicalDeployment("name");
        final Organization organization = new Organization();
        td.add(organization);
        technicalDeploymentRepository.save(td);
        organization.activate("newName");
        organizationRepository.save(organization);
    }
}