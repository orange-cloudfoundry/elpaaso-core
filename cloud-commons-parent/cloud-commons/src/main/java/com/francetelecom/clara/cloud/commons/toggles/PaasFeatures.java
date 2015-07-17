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

import org.togglz.core.Feature;
import org.togglz.core.annotation.EnabledByDefault;
import org.togglz.core.annotation.Label;
import org.togglz.core.context.FeatureContext;

/**
 * List of Paas features that can be enable/disable using feature toggles
 * <h2>About feature toggles in PaaS</h2>
 * Feature toogles enable to selectively activate/enable features<br>
 * They are implemented using <a href="http://www.togglz.org/">togglz</a> lib.
 * <h2>Ops user guide</h2>
 * Features can be enabled/disabled using the togglz admin console available under http://&ltelpaaso_webapp_url&gt<b>/togglz</b><br>
 * Only PaaS admin users can access toggles admin console<br><br>
 * Features state can be initialized using PaaS configuration service by defining following properties:<pre>
 *  FEATURE1=true/false
 *  FEATURE1.users=user1, user2, ...</pre>
 *  The user list properties is optional, if not defined or empty the feature is available for all users<br>
 *  If the feature is not defined in config service, it will usually be disabled by default unless it is tagged as @EnabledByDefault in the code.<br>
 *  <br>
 *   
 *  
 * <h2>Developer user guide</h2>
 * <h4>Usage in source code</h4>
 * <pre>
 * if( PaasFeatures.A_FEATURE.isActive() ) {
 *    // do something
 * } else {
 *    // do nothing or something else
 * }
 *</pre>
 *<h4>Impact on tests</h4>
 * Add togglz testing modules (togglz-testing and togglz-junit) dependencies in your pom; this automatically enables all features<br>
 * To selectively turn on/off a feature in test, use <b>TogglzRule</b> junit rule<br>
 * <pre>
 * {@literal @}Rule public TogglzRule togglzRule = TogglzRule.allDisabled(PaasFeatures.class);
 *
 * {@literal @}Test public void test() {
 *    togglzRule.enable(PaasFeatures.IAAS_CAPACITY);
 *    // test code
 *    } </pre>
 * See <a href="http://www.togglz.org/documentation/testing.html">http://www.togglz.org/documentation/testing.html</a> for details
 */
public enum PaasFeatures implements Feature {
	
	@OpsFeature
	@Label("IaaS capacity monitoring")
	IAAS_CAPACITY,
	
	@OpsFeature
	@Label("Ops future feature 2")
	OPS_FEATURE_2,
	
	@LabsFeature
	@Label("Labs feature 1")
	LABS_FEATURE_1,
	
	@LabsFeature
	@Label("Labs feature 2")
	LABS_FEATURE_2;
	
	@Override
	public boolean isActive() {
		return FeatureContext.getFeatureManager().isActive(this);
	}

}
