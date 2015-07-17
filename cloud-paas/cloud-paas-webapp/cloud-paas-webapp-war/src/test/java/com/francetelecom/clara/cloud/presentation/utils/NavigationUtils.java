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
package com.francetelecom.clara.cloud.presentation.utils;

import com.francetelecom.clara.cloud.presentation.designer.pages.DesignerPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.tester.FormTester;

/**
 * Created by IntelliJ IDEA.
 * User: wwnl9733
 * Date: 17/01/12
 * Time: 15:24
 * To change this template use File | Settings | File Templates.
 */
public class NavigationUtils {

    public final static String selectServicePath = "logicalServicesListSelect";
    public final static String selectFormPath = "firstPartContainer:serviceDefinitionPanel:selectForm";
    public final static String designerParamFormPath = "firstPartContainer:serviceDefinitionPanel:container:logicalServicePanel:serviceForm";
    public final static String matrixPath = "matrix";
    public final static String modalPath = "modalServiceView";

    /**
     * Goes on next step in architecture creation stepped process
     */
    public static void goOnNextStep(PaasWicketTester myTester) {
        myTester.executeAjaxEvent("stepButtonsPanel:nextButton", "onclick");
    }

    /**
     * Goes on designer step one page
     */
    public static void goOnDesignerPage(PaasWicketTester myTester, String releaseUid) {
        PageParameters params = new PageParameters();
        params.set("releaseUid", releaseUid);
        myTester.startPage(DesignerPage.class, params);
        myTester.assertRenderedPage(DesignerPage.class);
    }

    /**
     * Creates the path to reach a cell
     * @param row row of the cell
     * @param col column index of the cell
     * @return path to the cell
     */
    public static String getPathForCell(int row, int col) {
        return matrixPath + ":matrixContainer:listRows:" + row + ":listCols:" + col + ":content";
    }

    /**
     * Creates the form tester for the parameters form
     * @return the form tester for the parameters form
     */
    public static FormTester getParamsFormTester(PaasWicketTester myTester) {
        return myTester.newFormTester(designerParamFormPath);
    }

    public static FormTester getModalParamsFormTester(PaasWicketTester myTester) {
        myTester.assertVisible(modalPath + ":content:serviceForm");
        return myTester.newFormTester(modalPath + ":content:serviceForm");
    }

    /**
     * Submits the parameters form, through the add button
     */
    public static void submitParamsForm(PaasWicketTester myTester) {
        myTester.executeAjaxEvent(designerParamFormPath + ":addUpdateButton", "onclick");
    }

    public static void closeModalWindow(PaasWicketTester myTester) {
        myTester.executeAjaxEvent(modalPath + ":content:serviceForm:cancelCloseButton", "onclick");
    }
}
