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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Basic ActivationPluginStrategy implementation.
 */
public class ActivationPluginStrategyImpl implements ActivationPluginStrategy {
    private static final Logger logger = LoggerFactory.getLogger(ActivationPluginStrategyImpl.class);

    /**
     * All available plugins
     */
    private List<ActivationPlugin> plugins = new ArrayList<ActivationPlugin>();

    /**
     * Add a plugin
     *
     * @param plugin Plugin to add
     */
    public void addPlugin(ActivationPlugin plugin) {
        plugins.add(plugin);
    }

    public List<ActivationPlugin> getPlugins() {
        return plugins;
    }

    public void setPlugins(List<ActivationPlugin> plugins) {
        this.plugins = plugins;
    }

    /**
     * {@inheritDoc}. Go thru all available plugins and return the first that
     * accept the entity at this step.
     */
    @Override
    public ActivationPlugin getPlugin(Class<?> entityClass, ActivationStepEnum step) {
        // collect eligible plugins
        List<ActivationPlugin> eligiblePlugins = new ArrayList<ActivationPlugin>();
        for (ActivationPlugin plugin : plugins) {
            if (plugin.accept(entityClass, step)) {
                eligiblePlugins.add(plugin);
            }
        }
        int eligiblePluginsSize = eligiblePlugins.size();
        String criteriaStr = entityClass.getSimpleName() + " for " + step.getName() + " step";

        //~ >1 multiple eligible plugins : exception
        if (eligiblePluginsSize > 1) {
            throw new TechnicalException(
                    "There is more than one (" + eligiblePluginsSize + ") eligible plugin that accept " + criteriaStr);
        }

        //~ 0 eligible plugin

        // on ACTIVATE step only : check that there is a loaded plugin that handle the entity
        if (eligiblePluginsSize == 0
                && ActivationStepEnum.ACTIVATE.equals(step)
                && !isEligiblePluginForThisEntity(entityClass)) {
            throw new TechnicalException("There is no eligible plugin that accept " + criteriaStr);
        }
        // on other than INIT and ACTIVATE step : log info
        if (eligiblePluginsSize == 0
                && !ActivationStepEnum.ACTIVATE.equals(step)
                && !ActivationStepEnum.INIT.equals(step)) {
            // there is no eligible plugin for this step
            logDebugThereIsNoEligiblePluginFor(criteriaStr);
        }
        // no plugin eligible early return
        if (eligiblePluginsSize == 0) {
            return null;
        }
        //~ 1 and only 1 eligible plugin so return it // nominal case //
        return eligiblePlugins.iterator().next();
    }

    /**
     * check if an entity could be eligible by one plugin for one step
     *
     * @param entityClass
     * @return
     */
    private boolean isEligiblePluginForThisEntity(Class<?> entityClass) {
        boolean eligibleForAStep = false;
        for (ActivationStepEnum curStep : ActivationStepEnum.values()) {
            for (ActivationPlugin plugin : plugins) {
                if (plugin.accept(entityClass, curStep)) {
                    eligibleForAStep = true;
                    break;
                }
            }
        }
        return eligibleForAStep;
    }

    /**
     * debug that there is no plugin for this entity for the current step
     *
     * @param criteriaStr
     */
    void logDebugThereIsNoEligiblePluginFor(String criteriaStr) {
        logger.debug("there is no plugin that accepts " + criteriaStr);
    }
}
