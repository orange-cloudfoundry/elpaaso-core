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
package com.francetelecom.clara.cloud.paas.activation.v1;

import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.francetelecom.clara.cloud.paas.activation.ActivationPlugin;
import com.francetelecom.clara.cloud.paas.activation.ActivationStepEnum;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * ActivationPluginStrategyImplTest
 * <p/>
 * Last update  : $LastChangedDate$
 * Last author  : $Author$
 *
 * @version : $Revision$
 */
@RunWith(MockitoJUnitRunner.class)
public class ActivationPluginStrategyImplTest {

    @Spy
    ActivationPluginStrategyImpl activationPluginStrategy = new ActivationPluginStrategyImpl();

    private void assertNullOnGetPlugin(Class<?> entity, ActivationStepEnum activationStep) {
        ActivationPlugin result = activationPluginStrategy.getPlugin(entity, activationStep);
        assertThat(result).as("awaiting no plugin result").isNull();
    }

    private void assertNoInfoOnGetPlugin(Class<Object> entity, ActivationStepEnum activationStep) {
        reset(activationPluginStrategy);
        ActivationPlugin result = activationPluginStrategy.getPlugin(entity, activationStep);
        verify(activationPluginStrategy).getPlugin(any(Class.class), any(ActivationStepEnum.class));
        verifyNoMoreInteractions(activationPluginStrategy);
        assertThat(result).as("expecting null plugin result").isNull();
    }

    private void assertDebugOnGetPlugin(Class<Object> entity, ActivationStepEnum activationStep) {
        try {
            reset(activationPluginStrategy);
            ActivationPlugin result = activationPluginStrategy.getPlugin(entity, activationStep);
            verify(activationPluginStrategy).getPlugin(any(Class.class), any(ActivationStepEnum.class));
            verify(activationPluginStrategy).logDebugThereIsNoEligiblePluginFor(any(String.class));
            verifyNoMoreInteractions(activationPluginStrategy);
            assertThat(result).as("expecting null plugin result").isNull();
        } catch (TechnicalException te) {
            fail("expecting info only (and no technical exception)  while getPlugin " + entity.getSimpleName() + " for " + activationStep.getName() + " step");
        }
    }

    private void assertTechnicalExceptionOnGetPlugin(Class<Object> entity, ActivationStepEnum activationStep) {
        try {
            activationPluginStrategy.getPlugin(entity, activationStep);
        } catch (TechnicalException te) {
            return;
        }
        fail("expecting technical exception while getPlugin " + entity.getSimpleName() + " for " + activationStep.getName() + " step");
    }


    @Test
    public void should_return_plugin_when_one_eligible_plugin() {
        // GIVEN
        Class<Object> entity = Object.class;
        ActivationStepEnum activationStep = ActivationStepEnum.ACTIVATE;
        ActivationPlugin eligiblePlugin = mock(ActivationPlugin.class);
        when(eligiblePlugin.accept(entity, activationStep)).thenReturn(true);
        activationPluginStrategy.addPlugin(eligiblePlugin);

        assertThat(activationPluginStrategy.getPlugins())
                .as("plugins list should be set")
                .isEqualTo(Collections.singletonList(eligiblePlugin));


        //WHEN
        ActivationPlugin resultPlugin = activationPluginStrategy.getPlugin(entity, activationStep);

        //THEN
        assertThat(resultPlugin)
                .as("awaiting activate plugin")
                .isNotNull();
    }

    @Test
    public void for_an_activate_step_should_throw_an_exception_when_no_eligible_plugin() {
        // GIVEN
        Class<Object> entity = Object.class;
        ActivationStepEnum activationStep = ActivationStepEnum.ACTIVATE;
        ActivationPlugin notEligiblePlugin = mock(ActivationPlugin.class);
        when(notEligiblePlugin.accept(entity, activationStep)).thenReturn(false);
        activationPluginStrategy.setPlugins(Collections.singletonList(notEligiblePlugin));

        //WHEN
        assertTechnicalExceptionOnGetPlugin(entity, activationStep);
    }


    /**
     * This is PlatformServer case
     */
    @Test
    public void for_an_activate_step_should_not_throw_an_exception_when_eligible_plugin() {
        // GIVEN
        ActivationStepEnum activationStep;
        Class<Object> entity = Object.class;
        // PlatformServer case
        ActivationPlugin firstStartOnlyEligiblePlugin = mock(ActivationPlugin.class);
        when(firstStartOnlyEligiblePlugin.accept(entity, ActivationStepEnum.INIT)).thenReturn(false);
        when(firstStartOnlyEligiblePlugin.accept(entity, ActivationStepEnum.ACTIVATE)).thenReturn(false);
        when(firstStartOnlyEligiblePlugin.accept(entity, ActivationStepEnum.START)).thenReturn(false);
        when(firstStartOnlyEligiblePlugin.accept(entity, ActivationStepEnum.STOP)).thenReturn(false);
        when(firstStartOnlyEligiblePlugin.accept(entity, ActivationStepEnum.DELETE)).thenReturn(false);
        when(firstStartOnlyEligiblePlugin.accept(entity, ActivationStepEnum.FIRSTSTART)).thenReturn(true);
        activationPluginStrategy.setPlugins(Collections.singletonList(firstStartOnlyEligiblePlugin));

        //WHEN THEN
        activationStep = ActivationStepEnum.START;
        assertDebugOnGetPlugin(entity, activationStep);
        activationStep = ActivationStepEnum.STOP;
        assertDebugOnGetPlugin(entity, activationStep);
        activationStep = ActivationStepEnum.DELETE;
        assertDebugOnGetPlugin(entity, activationStep);
        activationStep = ActivationStepEnum.ACTIVATE;
        assertNullOnGetPlugin(entity, activationStep);
    }


    @Test
    public void for_other_than_an_init_step_should_info_when_no_eligible_plugin() {
        // GIVEN
        ActivationStepEnum activationStep;
        Class<Object> entity = Object.class;
        ActivationPlugin notEligiblePlugin = mock(ActivationPlugin.class);
        when(notEligiblePlugin.accept(any(Class.class), any(ActivationStepEnum.class))).thenReturn(false);
        activationPluginStrategy.setPlugins(Collections.singletonList(notEligiblePlugin));

        //WHEN THEN
        activationStep = ActivationStepEnum.START;
        assertDebugOnGetPlugin(entity, activationStep);
        activationStep = ActivationStepEnum.STOP;
        assertDebugOnGetPlugin(entity, activationStep);
        activationStep = ActivationStepEnum.DELETE;
        assertDebugOnGetPlugin(entity, activationStep);

        activationStep = ActivationStepEnum.INIT;
        assertNoInfoOnGetPlugin(entity, activationStep);
    }


    @Test
    public void for_any_step_should_throw_an_exception_when_two_activate_eligible_plugins() {
        // GIVEN
        Class<Object> entity = Object.class;
        ActivationPlugin eligiblePluginA = mock(ActivationPlugin.class);
        when(eligiblePluginA.accept(any(Class.class), any(ActivationStepEnum.class))).thenReturn(true);
        ActivationPlugin eligiblePluginB = mock(ActivationPlugin.class);
        when(eligiblePluginB.accept(any(Class.class), any(ActivationStepEnum.class))).thenReturn(true);
        List<ActivationPlugin> plugins = new ArrayList<ActivationPlugin>();
        plugins.add(eligiblePluginA);
        plugins.add(eligiblePluginB);
        activationPluginStrategy.setPlugins(plugins);

        //WHEN THEN
        ActivationStepEnum activationStep = ActivationStepEnum.ACTIVATE;
        assertTechnicalExceptionOnGetPlugin(entity, activationStep);
        activationStep = ActivationStepEnum.INIT;
        assertTechnicalExceptionOnGetPlugin(entity, activationStep);
        activationStep = ActivationStepEnum.START;
        assertTechnicalExceptionOnGetPlugin(entity, activationStep);
        activationStep = ActivationStepEnum.STOP;
        assertTechnicalExceptionOnGetPlugin(entity, activationStep);
        activationStep = ActivationStepEnum.DELETE;
        assertTechnicalExceptionOnGetPlugin(entity, activationStep);
    }
}
