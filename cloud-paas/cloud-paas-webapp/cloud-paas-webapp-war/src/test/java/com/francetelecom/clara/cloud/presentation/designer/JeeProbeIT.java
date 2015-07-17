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

import com.francetelecom.clara.cloud.logicalmodel.JeeProcessing;
import com.francetelecom.clara.cloud.presentation.utils.CreateObjectsWithGUI;
import com.francetelecom.clara.cloud.presentation.utils.NavigationUtils;

import org.apache.wicket.util.tester.FormTester;
import org.junit.Ignore;
import org.junit.Test;

/**
 * User: shjn2064
 */
public class JeeProbeIT extends AbstractTestApplication {

    @Override
    protected String getSampleAppCatalogName() {
        return "allServicesLogicalModelCatalog";
    }

    @Test
    @Override
    @Ignore("generic test inherited from AbstractTestApplication is skipped")
    public void createArchitectureForCFJavaProcessing() {
    }

    @Test
    public void testPreviewWrongUrl() {

        goOnGoodPage();
        //Create FormTester
        FormTester formTester = getMyTester().newFormTester(NavigationUtils.designerParamFormPath);
        //Set wrong url
        formTester.setValue("iconUrl","hicons.iconarchive.com/icons/ahdesign91/media-player/32/WinAmp-icon");
        //Update modelObject
        getMyTester().executeAjaxEvent(NavigationUtils.designerParamFormPath + ":iconUrl", "onchange");
        //Click on preview button
        getMyTester().executeAjaxEvent(NavigationUtils.designerParamFormPath+":imageContainer:preview", "onclick");
        //Check error
        getMyTester().assertContains("Invalid icon Url format");
        getMyTester().assertContains("no protocol: hicons.iconarchive.com/icons/ahdesign91/media-player/32/WinAmp-icon");

    }

    @Test
    public void testPreviewGoodUrl() {
        goOnGoodPage();
        //Create FormTester
        FormTester formTester = getMyTester().newFormTester(NavigationUtils.designerParamFormPath);
        //Set wrong url
        formTester.setValue("iconUrl","http://icons.iconarchive.com/icons/ahdesign91/media-player/32/WinAmp-icon.png");
        //Update modelObject
        getMyTester().executeAjaxEvent(NavigationUtils.designerParamFormPath+":iconUrl", "onchange");
        //Click on preview button
        getMyTester().executeAjaxEvent(NavigationUtils.designerParamFormPath+":imageContainer:preview", "onclick");
        //Check no error
        getMyTester().assertNoErrorMessage();

    }

    private void goOnGoodPage() {
        // go on designer step two page to access internal services
        NavigationUtils.goOnDesignerPage(getMyTester(), getReleaseUid());
        NavigationUtils.goOnNextStep(getMyTester());

        //Select internal mom service
        CreateObjectsWithGUI.selectService(getMyTester(), JeeProcessing.class);
    }

}
