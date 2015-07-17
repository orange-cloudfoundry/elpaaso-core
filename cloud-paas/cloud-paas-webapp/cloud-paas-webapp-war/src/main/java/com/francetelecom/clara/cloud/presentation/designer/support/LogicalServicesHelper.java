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
package com.francetelecom.clara.cloud.presentation.designer.support;

import com.francetelecom.clara.cloud.commons.GuiClassMapping;
import com.francetelecom.clara.cloud.commons.GuiMapping;
import com.francetelecom.clara.cloud.logicalmodel.LogicalEntity;
import com.francetelecom.clara.cloud.logicalmodel.LogicalModelItem;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Thomas Escalle - tawe8231
 * Entity : FT/OLNC/RD/MAPS/MEP/MSE
 * Date: 03/01/12
 */
@Service
public class LogicalServicesHelper {

     /**
     * logger
     */
    private static final transient org.slf4j.Logger logger = LoggerFactory.getLogger(LogicalServicesHelper.class);

    @Autowired
    private List<LogicalModelItem> logicalServices;

    public List<LogicalModelItem> getAllLogicalServices() {
        logger.debug("number of registred services : "+logicalServices.size());
        return logicalServices;
    }

    public List<LogicalModelItem> getExternalServices() {

        List<LogicalModelItem> externalServices = new ArrayList<LogicalModelItem>();
        GuiClassMapping annotation;
        for (LogicalModelItem service : logicalServices) {
            annotation = getGuiServiceMappingAnnotation(service.getClass());
            if (annotation.isExternal()) {
                logger.debug("registred external service : "+service.getClass().getSimpleName());
                externalServices.add(service);
            }
        }

        logger.debug("number of registred external services : "+externalServices.size());

        return externalServices;
    }

    public List<LogicalModelItem> getInternalServices() {

        List<LogicalModelItem> internalServices = new ArrayList<LogicalModelItem>();
        GuiClassMapping annotation;
        for (LogicalModelItem service : logicalServices) {
            annotation =getGuiServiceMappingAnnotation(service.getClass());
            if (!annotation.isExternal()) {
                logger.debug("registred external service : "+service.getClass().getSimpleName());
                internalServices.add(service);
            }
        }

        logger.debug("number of registred external services : "+internalServices.size());

        return internalServices;
    }

    public String getLogicalServiceCatalogName(LogicalModelItem service) {
        return getGuiServiceMappingAnnotation(service.getClass()).serviceCatalogNameKey();
    }

    public boolean isLogicalServiceBeta(LogicalModelItem service) {
        boolean beta = true;

        if (GuiClassMapping.StatusType.BETA == getGuiServiceMappingAnnotation(service.getClass()).status()) {
            beta = true;
        } else {
            beta = false;
        }

        return beta;
    }

    public boolean isLogicalServiceEnable(LogicalModelItem service) {
        boolean enable = false;
        if (GuiClassMapping.StatusType.SUPPORTED == getGuiServiceMappingAnnotation(service.getClass()).status() ||
               GuiClassMapping.StatusType.BETA == getGuiServiceMappingAnnotation(service.getClass()).status()) {
            enable = true;
        } else {
            enable = false;
        }

        return enable;
    }

    public boolean isLogicalServiceExternal(LogicalModelItem service) {
        if(getGuiServiceMappingAnnotation(service.getClass()).isExternal()) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isServiceParameterEnable(LogicalModelItem service, String name) {

        boolean enable = true;

        // TODO - find a way to get annotation of superclass of LogicalModelItem for label and name parameters
        // TODO - find a way to get class mapping of complex object in LogicalModelItem (ex : MavenReference.groupId, MavenReference.artifactId....)

        if (getGuiParameterMappingAnnotation(service.getClass(), name) != null) {
            if (GuiMapping.StatusType.SUPPORTED == getGuiParameterMappingAnnotation(service.getClass(), name).status()) {
                enable = true ;
            } else {
                enable = false;
            }
        }

        return enable;
    }

    private GuiClassMapping getGuiServiceMappingAnnotation(Class<? extends LogicalEntity> introspectedClass) {
        return introspectedClass.getAnnotation(GuiClassMapping.class);
    }

    private GuiMapping getGuiParameterMappingAnnotation(Class<? extends LogicalEntity> introspectedClass, String name) {
        try {
            Field field = introspectedClass.getDeclaredField(name);
            GuiMapping clazz = field.getAnnotation(GuiMapping.class);
            logger.debug("Field "+field.getName()+" has status "+clazz.status());

            return clazz;
        } catch (NoSuchFieldException e) {
            logger.debug("Parameter "+name+" is not a "+introspectedClass.getSimpleName()+" field.");
        }
        return null;
    }

}