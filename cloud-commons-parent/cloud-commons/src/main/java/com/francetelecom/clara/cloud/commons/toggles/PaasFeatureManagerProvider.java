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
package com.francetelecom.clara.cloud.commons.toggles;

import org.togglz.core.manager.FeatureManager;
import org.togglz.core.manager.FeatureManagerBuilder;
import org.togglz.core.manager.TogglzConfig;
import org.togglz.core.spi.FeatureManagerProvider;

/**
 * Default FeatureManagerProvider used when no other FeatureManagerProvider can be used<br>
 * In servlet context, the {@link WebAppFeatureManagerProvider} should be available. <br>
 * The {@link PaasFeatureManagerProvider} will be used when code is executed outside a servlet context (e.g. quartz processing)<br>
 */
public class PaasFeatureManagerProvider implements FeatureManagerProvider {

    private static FeatureManager featureManager;
    /**
     * loading priority of this FeatureManagerProvider<br>
     * its default value should be high enough to ensure that this provider is used when no other providers is selected 
     */
    private static int priority = 300;
    
    /**
     * reset static state 
     */
	static void clear() {
		featureManager = null;
		priority = 300;
	}
	
    @Override
    public int priority() {
        return priority;
    }

    @Override
    public synchronized FeatureManager getFeatureManager() {
        return featureManager;
    }
    
    public static void bind(TogglzConfig config) {
          featureManager = new FeatureManagerBuilder()
          			.togglzConfig(config)
                    .build();
        }

    public static void setPriority(int newPriority) {
    	priority = newPriority;
    }


}
