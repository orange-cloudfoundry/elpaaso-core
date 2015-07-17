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
package com.francetelecom.clara.cloud.presentation.designer.panels;

import com.francetelecom.clara.cloud.logicalmodel.LogicalConfigService;
import com.francetelecom.clara.cloud.logicalmodel.LogicalConfigServiceUtils.ConfigEntry;
import com.francetelecom.clara.cloud.logicalmodel.LogicalDeployment;
import com.francetelecom.clara.cloud.presentation.designer.pages.DesignerHelperPage;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class DesignerArchitectureConfigSetPanelTest {

	
    private WicketTester tester = null;
	DesignerArchitectureConfigSetPanel configPanel = null;
    
	@Before
	public void setup() {
		// we need to set-up a wicket tester and do some minimal configuration in order to create tested panel
		
		// a wicketTester is required to instantiate a wicket panel
        tester = new WicketTester(); 
        
        // a parentPage is required with a logicalDeployment to initialize the config panel
        DesignerHelperPage parentPage = mock(DesignerHelperPage.class);
        LogicalDeployment ld = mock(LogicalDeployment.class);
		when(parentPage.getLogicalDeployment()).thenReturn(ld );
		
		configPanel = new DesignerArchitectureConfigSetPanel("test", parentPage , null);
	}
	
	@Test
    public void entry_key_full_name_is_prefixed_with_key_prefix_when_defined() {
        // Given
		LogicalConfigService service = new LogicalConfigService();
        service.setKeyPrefix("myKeyPrefix");
        ConfigEntry entry = new ConfigEntry("key.a.b", "key value", "comment");

		// When
		String keyFullName = configPanel.getConfigEntryFullKey(service, entry);
		
		// Then
        assertEquals("myKeyPrefixkey.a.b", keyFullName);
    }
	
	@Test
    public void entry_key_full_name_has_no_prefix_when_key_prefix_is_empty() {
        // Given
		LogicalConfigService service = new LogicalConfigService();
        service.setKeyPrefix("");
        ConfigEntry entry = new ConfigEntry("key.a.b", "key value", "comment");

		// When
		String keyFullName = configPanel.getConfigEntryFullKey(service, entry);
		
		// Then
		assertEquals("key.a.b", keyFullName);
	}
	


}
