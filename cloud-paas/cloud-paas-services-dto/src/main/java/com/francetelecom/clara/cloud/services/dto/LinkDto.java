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
import java.net.URL;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Thomas Escalle - tawe8231
 * Entity : FT/OLNC/RD/MAPS/MEP/MSE
 * Date: 21/06/12
 */
public class LinkDto implements Serializable {

    /**
     * The URL
     * */
    private URL url;

    /**
     *  enum of different user role
     */
    public enum TargetUserEnum {
        PAAS_OPS, PAAS_USER, PAAS_ALL
    };

    /**
     * enum of different types of link
     */
    public enum LinkTypeEnum {
        ACCESS_LINK, LOGS_LINK, METRICS_LINK
    }

    /*
     * group of users which will see the link
     */
    private TargetUserEnum targetUser;

    /**
     * type of link
     */
    private LinkTypeEnum linkType;

    /**
     * maps of different bindings that can be used.
     * this map can be null or empty.
     * key is a jndi name
     * value is a jndi value
     */
    Map<String, String> serviceBindings;


    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public Map<String, String> getServiceBindings() {
        return serviceBindings;
    }

    public void setServiceBindings(Map<String, String> serviceBindings) {
        this.serviceBindings = serviceBindings;
    }

    public TargetUserEnum getTargetUser() {
        return targetUser;
    }

    public void setTargetUser(TargetUserEnum targetUser) {
        this.targetUser = targetUser;
    }

    public LinkTypeEnum getLinkType() {
        return linkType;
    }

    public void setLinkType(LinkTypeEnum linkType) {
        this.linkType = linkType;
    }
    public String toString() {
        return (getLinkType() != null ? getLinkType().toString() + "=>" : "") + getUrl();
    }
}
