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

import org.apache.wicket.util.tester.WicketTester;

/**
 * Created by IntelliJ IDEA.
 * User: wwnl9733
 * Date: 12/01/12
 * Time: 16:34
 * To change this template use File | Settings | File Templates.
 */
public final class PaasWicketTesterSingleton {
    private static WicketTester tester;

    /**
     * Private constructor. We can't create instances of that object
     */
    private PaasWicketTesterSingleton() {
        super();
    }

    public static final WicketTester getTester() {
        if (tester == null) {
            tester = new PaasWicketTester(new PaasTestApplication(null));
        }
        return tester;
    }
}
