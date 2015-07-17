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

import org.apache.wicket.util.tester.FormTester;
import org.junit.Assert;

/**
 * Created by IntelliJ IDEA.
 * User: wwnl9733
 * Date: 02/02/12
 * Time: 11:24
 * To change this template use File | Settings | File Templates.
 */
public class DeleteEditObjects {

    public static void deleteAssociationAtCell(PaasWicketTester myTester, int row, int col) {
        String path = NavigationUtils.getPathForCell(row, col) + ":form";
        Assert.assertTrue(isCellAssociated(myTester, row, col));
        FormTester cellForm = myTester.newFormTester(path);
        cellForm.setValue("associated", false);
        myTester.executeAjaxEvent(path + ":associated", "onclick");
        Assert.assertFalse(isCellAssociated(myTester, row, col));
    }

    public static void deleteServiceAtRow(PaasWicketTester myTester, int row) {
        String path = NavigationUtils.getPathForCell(row, 0) + ":cell-delete";
        myTester.executeAjaxEvent(path, "onclick");
    }

    public static void deleteNodeAtCol(PaasWicketTester myTester, int col) {
        String path = NavigationUtils.getPathForCell(0, col) + ":cell-delete";
        myTester.executeAjaxEvent(path, "onclick");
    }

    public static void editServiceAtRow(PaasWicketTester myTester, int row) {
        String path = NavigationUtils.getPathForCell(row, 0) + ":cell-edit";
        myTester.executeAjaxEvent(path, "onclick");
    }

    public static void modifyServiceLabelAtRow(PaasWicketTester myTester, int row) throws NoSuchFieldException {
        String servicePath = NavigationUtils.designerParamFormPath;
        FormTester serviceForm = NavigationUtils.getParamsFormTester(myTester);
        String labelValue = serviceForm.getTextComponentValue("label");
        serviceForm.setValue("label", labelValue+row);
        myTester.executeAjaxEvent(servicePath+":addUpdateButton", "onclick");
    }

    public static void editNodeAtCol(PaasWicketTester myTester, int col) {
        String path = NavigationUtils.getPathForCell(0, col) + ":cell-edit";
        myTester.executeAjaxEvent(path, "onclick");
    }

    public static void viewServiceAtRow(PaasWicketTester myTester, int row) {
        String path = NavigationUtils.getPathForCell(row, 0) + ":cell-view";
        myTester.executeAjaxEvent(path, "onclick");
    }

    public static void viewNodeAtCol(PaasWicketTester myTester, int col) {
        String path = NavigationUtils.getPathForCell(0, col) + ":cell-view";
        myTester.executeAjaxEvent(path, "onclick");
    }

    public static boolean isCellAssociated(PaasWicketTester myTester, int row, int col) {
        String path = NavigationUtils.getPathForCell(row, col) + ":form";
        return (Boolean) myTester.getComponentFromLastRenderedPage(path + ":associated").getDefaultModelObject();
    }
}
