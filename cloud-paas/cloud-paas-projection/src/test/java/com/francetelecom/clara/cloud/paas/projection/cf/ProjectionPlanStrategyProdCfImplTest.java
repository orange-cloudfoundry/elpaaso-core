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

import com.francetelecom.clara.cloud.paas.constraint.ApplicationCustomizationRule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.fest.assertions.Assertions.assertThat;

@RunWith(org.mockito.runners.MockitoJUnitRunner.class)
public class ProjectionPlanStrategyProdCfImplTest {

    ProjectionPlanStrategyProdCfImpl projectionPlanStrategyProdCf = new ProjectionPlanStrategyProdCfImpl();

    @Before
    public void setUp() {
        projectionPlanStrategyProdCf.setDefaultAppCustomizationRule(new ApplicationCustomizationRule());
    }

    @Test
    public void rounds_amount_of_ram_into_instances() {
        assertThat(projectionPlanStrategyProdCf.computeNbInstances(127)).isEqualTo(2);
        assertThat(projectionPlanStrategyProdCf.computeNbInstances(129)).isEqualTo(2);
        assertThat(projectionPlanStrategyProdCf.computeNbInstances(512)).isEqualTo(2);
        assertThat(projectionPlanStrategyProdCf.computeNbInstances(513)).isEqualTo(2);
        assertThat(projectionPlanStrategyProdCf.computeNbInstances(1023)).isEqualTo(2);
        assertThat(projectionPlanStrategyProdCf.computeNbInstances(1024)).isEqualTo(2);
        assertThat(projectionPlanStrategyProdCf.computeNbInstances(1025)).isEqualTo(3);
        assertThat(projectionPlanStrategyProdCf.computeNbInstances(1024 + 512)).isEqualTo(3);
        assertThat(projectionPlanStrategyProdCf.computeNbInstances(1024 + 512 + 1)).isEqualTo(4);
        assertThat(projectionPlanStrategyProdCf.computeNbInstances(1024 + 512 + 512)).isEqualTo(4);
        assertThat(projectionPlanStrategyProdCf.computeNbInstances(1024 + 512 + 512 + 1)).isEqualTo(5);
    }

}
