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
package com.francetelecom.clara.cloud.logicalmodel;

import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.francetelecom.clara.cloud.commons.ValidatorUtil;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by IntelliJ IDEA.
 * User: wwnl9733
 * Date: 14/02/12
 * Time: 10:25
 * To change this template use File | Settings | File Templates.
 */
public class LogicalQueueReceiveServiceTest {

    @Test
    public void testServiceNameRegExpWithDot() {
        String serviceName = "ping.in";
        LogicalQueueReceiveService service = new LogicalQueueReceiveService("qrs", new LogicalDeployment(), serviceName, "0.0.1", 1, 1, 1);
        try {
            ValidatorUtil.validate(service);
        } catch (TechnicalException e) {
            Assert.fail("validation failed: " + e.getMessage());
        }
    }

    @Test
    public void testServiceNameRegExpWithUnexpectedCharacter() {
        String serviceName = "ping?in";
        LogicalQueueReceiveService service = new LogicalQueueReceiveService("qrs", new LogicalDeployment(), serviceName, "0.0.1", 1, 1, 1);
        boolean shouldFail = true;
        try {
            ValidatorUtil.validate(service);
        } catch (TechnicalException e) {
            shouldFail = false;
        }
        if (shouldFail) {
            Assert.fail("validation should have failed because of '?' character");
        }
    }
}
