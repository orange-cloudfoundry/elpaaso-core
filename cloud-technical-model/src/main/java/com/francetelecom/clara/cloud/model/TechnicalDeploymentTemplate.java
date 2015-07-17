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
package com.francetelecom.clara.cloud.model;

import org.springframework.util.Assert;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collections;
import java.util.Set;

@XmlRootElement
@Entity
@Table(name = "TECHNICAL_DEPLOYMENT_TEMPLATE")
/**
 * A TechnicalDeployment Template is a template of TechnicalDeployment.
 * Once instantiated, it feeds a TechnicalDeploymentInstance
 *
 * This entity maps to a vCloud vAppTemplate 
 *
 */
public class TechnicalDeploymentTemplate extends DependantModelItem {

    private static final long serialVersionUID = 7873627229474910137L;

    // TODO: activation should add reference to Vapp template(s) associated to
    // this TDT, i.e. the Vapp template(s)
    // that would be instanciated for all instances of the this same TDT.
    // Note that there might have more than one templates when single VM
    // templates are combined together using
    // VM composition methods to form a multi-VM vApp.

    private String releaseId;

    @Enumerated(EnumType.STRING)
    private DeploymentProfileEnum deploymentProfile;

    @XmlIDREF
    @XmlElement(name = "technicalDeploymentRef")
    @OneToOne(cascade = {CascadeType.ALL})
    @NotNull
    @Valid
    private TechnicalDeployment technicalDeployment;

    @NotNull
    @Size(max = 255)
    private String middlewareProfileVersion;

    /**
     * Required constructor for JPA / Jaxb
     */
    protected TechnicalDeploymentTemplate() {
    }

    /**
     * Construct a TechnicalDeploymentTemplate
     *
     * @param td                TechnicalDeployment describing the instance
     * @param profile           TODO
     * @param releaseId         TODO
     * @param middlewareProfile TODO
     */
    public TechnicalDeploymentTemplate(TechnicalDeployment td, DeploymentProfileEnum profile, String releaseId, String middlewareProfile) {
        super(td.getName() + "-template");
        setReleaseId(releaseId);
        setDeploymentProfile(profile);
        setTechnicalDeployment(td);
        setMiddlewareProfileVersion(middlewareProfile);
    }

    private void setReleaseId(String releaseId) {
        Assert.hasText(releaseId, "Cannot create technical deployment template. No release id has been provided.");
        this.releaseId = releaseId;
    }

    private void setDeploymentProfile(DeploymentProfileEnum profile) {
        Assert.notNull(profile, "Cannot create technical deployment template. No deployment profile has been provided.");
        this.deploymentProfile = profile;
    }

    private void setTechnicalDeployment(TechnicalDeployment td) {
        Assert.notNull(td, "Cannot create technical deployment template. No technical deployment has been provided.");
        this.technicalDeployment = td;
    }

    private void setMiddlewareProfileVersion(String middlewareProfileVersion) {
        Assert.hasText(releaseId, "Cannot create technical deployment template. No middleware profile has been provided.");
        this.middlewareProfileVersion = middlewareProfileVersion;
    }

    public String getMiddlewareProfileVersion() {
        return middlewareProfileVersion;
    }

    /**
     * getter
     *
     * @return
     */
    public TechnicalDeployment getTechnicalDeployment() {
        return technicalDeployment;
    }

    @Override
    /**
     * Describes the overall TDT creation process status:
     * <ol>
     * <li>{@link DeploymentStateEnum#TRANSIENT} when there is a need for creating disk image creation but this has not yet started</li>
     * <li>{@link DeploymentStateEnum#INPROGRESS} when there is pending disk image creation</li>
     * <li>{@link DeploymentStateEnum#CREATED} when the disk image creation process is
     * completed for each PlatformServer (even in case of cache hits)</li>
     * </ol>
     *
     * FIXME: add a new state "INPROGRESS" to
     */
    public DeploymentStateEnum getDeploymentState() {
        return super.getDeploymentState();
    }

    /**
     * @return All DependantModelItem on which this DependantModelItem depends
     * on
     */
    public Set<DependantModelItem> listDepedencies() {
        return Collections.emptySet();
    }

    public String getReleaseId() {
        return releaseId;
    }

    public DeploymentProfileEnum getDeploymentProfile() {
        return deploymentProfile;
    }


}
