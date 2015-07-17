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
package com.francetelecom.clara.cloud.presentation.designer;

import static org.mockito.Mockito.when;

import com.francetelecom.clara.cloud.commons.BusinessException;
import com.francetelecom.clara.cloud.commons.MavenReference;
import com.francetelecom.clara.cloud.mvn.consumer.MavenReferenceResolutionException;
import com.francetelecom.clara.cloud.presentation.utils.CreateObjectsWithGUI;
import com.francetelecom.clara.cloud.presentation.utils.NavigationUtils;

import org.junit.Ignore;
import org.junit.Test;

public class DesignerTest extends AbstractTestApplication {

    @Override
    protected String getSampleAppCatalogName() {
        return "allServicesLogicalModelCatalog";
    }
    
    @Test
    @Override
    @Ignore("generic test inherited from AbstractTestApplication is skipped")
    public void createArchitectureForJeeProcessing() {
    }

    @Test
    @Override
    @Ignore("generic test inherited from AbstractTestApplication is skipped")
    public void createArchitectureForCFJavaProcessing() {
    }

    @Test
    public void validation_fails_when_maven_reference_not_found() throws BusinessException {
    	/**
    	 * Given an invalid maven reference specified on a jee execution node
    	 */
    	createJeeNodeWithInvalidMavenRef();

        /**
         * When user clicks on the next button to reach validation panel (designer step 3)
         */
        NavigationUtils.goOnNextStep(getMyTester());
        
        /**
         * Then a validation error message should be displayed
         */
        getMyTester().assertErrorMessages("Artifact not found for maven reference groupId:artifactId:1.0::ear");
     }

    /**
     * This test has been added due a bug in designer panels (step 1 / 2 / 3) navigation (art #108045)
     * @throws BusinessException
     */
    @Test
    public void validation_fails_when_maven_reference_not_found_even_after_correction() throws BusinessException {

    	/**
    	 * Given an invalid maven reference specified on a jee execution node
    	 */
    	createJeeNodeWithInvalidMavenRef();

    	/**
    	 * That has been detected in the validation panel
    	 */
        NavigationUtils.goOnNextStep(getMyTester());
        getMyTester().assertErrorMessages("Artifact not found for maven reference groupId:artifactId:1.0::ear");

        getMyTester().cleanupFeedbackMessages();
        /**
         * When user goes back and edits jee execution node service
         */
        String path = NavigationUtils.getPathForCell(0, 1);
        getMyTester().executeAjaxEvent(path+":cell-edit", "onclick");

        /**
         * And then clicks again on the next button to access validation panel
         */
        NavigationUtils.goOnNextStep(getMyTester());
        
        /**
         * Then a validation error message should be displayed again
         */
        getMyTester().assertErrorMessages("Artifact not found for maven reference groupId:artifactId:1.0::ear");
     }
    
    /**
     * Edit a logical architecture with a single Jee Exec Node whose maven references are invalid<br>
     * After this method, designer is in step 2
     */
	private void createJeeNodeWithInvalidMavenRef() {
		// Stub maven repo dao
    	MavenReference mr = new MavenReference("groupId", "artifactId", "1.0","ear");
        when(mvnDao.resolveUrl(mr)).thenThrow(new MavenReferenceResolutionException(mr,"artifact not found"));
        // Access designer panel step 1 (external services)
        NavigationUtils.goOnDesignerPage(getMyTester(), getReleaseUid());
        // Access designer panel step 2 (internal services)
        NavigationUtils.goOnNextStep(getMyTester());
        // Create a jee processing node with invalid maven ref
        CreateObjectsWithGUI.createJEEProcessing(getMyTester(), "jee", "groupId", "artifactId", "1.0", "", false, 128);
	}

}
