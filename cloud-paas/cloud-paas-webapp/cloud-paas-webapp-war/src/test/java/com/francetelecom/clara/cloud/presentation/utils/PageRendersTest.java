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


import com.francetelecom.clara.cloud.presentation.common.PageTemplate;
import org.apache.wicket.Page;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * Created by IntelliJ IDEA.
 * User: wwnl9733
 * Date: 13/01/12
 * Time: 10:23
 * To change this template use File | Settings | File Templates.
 */
public class PageRendersTest {

    public static void testPageRenders(PaasWicketTester myTester, PageTemplate page) {
        myTester.startPage(page);
        myTester.assertRenderedPage(page.getClass());
    }

    public static void testPageRenders(PaasWicketTester myTester, Class<? extends Page> pageClass) {
        myTester.startPage(pageClass);
        myTester.assertRenderedPage(pageClass);
    }


    public static void testPageRenders(PaasWicketTester myTester, Class<? extends Page> pageClass, PageParameters parameters) {
        myTester.startPage(pageClass, parameters);
        myTester.assertRenderedPage(pageClass);
    }
}
