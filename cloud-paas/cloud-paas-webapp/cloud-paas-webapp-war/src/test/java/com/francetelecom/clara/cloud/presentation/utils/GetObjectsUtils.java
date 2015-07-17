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

import java.lang.reflect.Field;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.wicket.markup.html.form.Form;
import org.junit.Assert;

import com.francetelecom.clara.cloud.logicalmodel.LogicalModelItem;
import com.francetelecom.clara.cloud.logicalmodel.LogicalService;
import com.francetelecom.clara.cloud.logicalmodel.ProcessingNode;
import com.francetelecom.clara.cloud.presentation.designer.panels.DesignerArchitectureMatrixPanel;

/**
 * Created by IntelliJ IDEA.
 * User: wwnl9733
 * Date: 02/02/12
 * Time: 10:52
 * To change this template use File | Settings | File Templates.
 */
public class GetObjectsUtils {


    public static DesignerArchitectureMatrixPanel getArchitecturePanel(PaasWicketTester myTester) {
        return (DesignerArchitectureMatrixPanel) myTester.getComponentFromLastRenderedPage(NavigationUtils.matrixPath);
    }

    public static LogicalModelItem getItemAtCell(PaasWicketTester myTester, int row, int col) {
        Assert.assertTrue("row or col should be 0", row == 0 || col == 0);
        String path = NavigationUtils.getPathForCell(row, col);
        return (LogicalModelItem) myTester.getComponentFromLastRenderedPage(path).getDefaultModelObject();
    }

    public static LogicalService getServiceAtRow(PaasWicketTester myTester, int row) {
        return (LogicalService) getItemAtCell(myTester, row, 0);
    }

    public static ProcessingNode getNodeAtCol(PaasWicketTester myTester, int col) {
        return (ProcessingNode) getItemAtCell(myTester, 0, col);
    }

    public static int getPositionForItem(PaasWicketTester myTester, LogicalModelItem item) {
        if (item instanceof ProcessingNode) {
            return getPositionForNode(myTester, (ProcessingNode) item);
        }
        return getPositionForService(myTester, (LogicalService) item);
    }

    private static int getPositionForService(PaasWicketTester myTester, LogicalService service) {
        return getArchitecturePanel(myTester).getIndexOfService(service);
    }

    private static int getPositionForNode(PaasWicketTester myTester, ProcessingNode node) {
        return getArchitecturePanel(myTester).getIndexOfNode(node);
    }

    /**
     * Getter for the create/edit service form
     * @return the create/edit service form
     */
    public static Form<?> getParamsForm(PaasWicketTester myTester) {
        return NavigationUtils.getParamsFormTester(myTester).getForm();
    }

    public static Form<?> getModalParamsForm(PaasWicketTester myTester) {
        return NavigationUtils.getModalParamsFormTester(myTester).getForm();
    }

    /**
     * Util method, which does getDeclaredField() on the class, and on its inheritance hierarchy if needed. Allows to access private fields
     * @param objectClass class of the object which contains the field
     * @param fieldName name of the field to find
     * @return a Field if it exists
     * @throws NoSuchFieldException raised when no field was found
     */
    public static Field getAnyField(Class objectClass, String fieldName) throws NoSuchFieldException {
        try {
            return objectClass.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            Class superClass = objectClass.getSuperclass();
            if (superClass == null) {
                throw e;
            } else {
                return getAnyField(superClass, fieldName);
            }
        }
    }

    public static Field[] getAllFields(Class objectClass) {
        if (objectClass.getSuperclass() == null) {
            return null;
        }
        return (Field[]) ArrayUtils.addAll(objectClass.getDeclaredFields(), getAllFields(objectClass.getSuperclass()));
    }
}
