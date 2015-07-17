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
package com.francetelecom.clara.cloud.services.dto;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: shjn2064
 * Date: 23/05/12
 * Time: 16:37
 * To change this template use File | Settings | File Templates.
 */
public class WspInfoDto implements Serializable {


    private String project;
    private String serviceName;
    private String serviceVersion;
    private String accessUrl;
    private String proxifiedAccessUrl;



    /**
     * The human readeable display name of the LogicalModelItem subclass as
     * typed in by the end-user.
     */
    private String logicalModelItemName;

    /**
     * An alternate way to identify the source of the link beside name. May be
     * useful for fetching additional details on the object.
     */
    private String logicalModelItemId;

    public WspInfoDto(String project, String serviceName, String serviceVersion, String accessUrl, String proxifiedAccessUrl, String logicalModelItemName, String logicalModelItemId) {
        this.project = project;
        this.serviceName = serviceName;
        this.serviceVersion = serviceVersion;
        this.accessUrl = accessUrl;
        this.proxifiedAccessUrl = proxifiedAccessUrl;
        this.logicalModelItemName = logicalModelItemName;
        this.logicalModelItemId = logicalModelItemId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getServiceVersion() {
        return serviceVersion;
    }

    public void setServiceVersion(String serviceVersion) {
        this.serviceVersion = serviceVersion;
    }

    public String getAccessUrl() {
        return accessUrl;
    }

    public void setAccessUrl(String accessUrl) {
        this.accessUrl = accessUrl;
    }

    public String getProxifiedAccessUrl() {
        return proxifiedAccessUrl;
    }

    public void setProxifiedAccessUrl(String proxifiedAccessUrl) {
        this.proxifiedAccessUrl = proxifiedAccessUrl;
    }

    public String getLogicalModelItemName() {
        return logicalModelItemName;
    }

    public void setLogicalModelItemName(String logicalModelItemName) {
        this.logicalModelItemName = logicalModelItemName;
    }

    public String getLogicalModelItemId() {
        return logicalModelItemId;
    }

    public void setLogicalModelItemId(String logicalModelItemId) {
        this.logicalModelItemId = logicalModelItemId;
    }

}
