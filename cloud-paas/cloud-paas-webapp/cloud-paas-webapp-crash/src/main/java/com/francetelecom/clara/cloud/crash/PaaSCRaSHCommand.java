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
package com.francetelecom.clara.cloud.crash;

import com.francetelecom.clara.cloud.core.service.ManageApplication;
import com.francetelecom.clara.cloud.environment.ManageEnvironment;
import com.francetelecom.clara.cloud.service.OpsService;
import org.crsh.command.CRaSHCommand;
import org.crsh.util.SimpleMap;

public class PaaSCRaSHCommand extends CRaSHCommand {
    protected <E> E getContextBean(Class<E> beanType, String beanName) {
        SimpleMap<String, Object> beans = (SimpleMap<String, Object>) context.getAttributes().get("beans");
        return (E) beans.get(beanName);
    }

    protected ManageEnvironment getManageEnvironment() {
        return getContextBean(ManageEnvironment.class, "manageEnvironment");
    }

    protected ManageApplication getManageApplication() {
        return getContextBean(ManageApplication.class, "manageApplication");
    }

    protected OpsService getOpsService() {
        return getContextBean(OpsService.class, "opsService");
    }

}
