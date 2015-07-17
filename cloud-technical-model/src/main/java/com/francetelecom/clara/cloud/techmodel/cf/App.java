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
package com.francetelecom.clara.cloud.techmodel.cf;

import com.francetelecom.clara.cloud.commons.MavenReference;
import com.francetelecom.clara.cloud.model.DependantModelItem;
import com.francetelecom.clara.cloud.model.DeploymentStateEnum;
import com.francetelecom.clara.cloud.model.TechnicalDeployment;
import com.francetelecom.clara.cloud.model.XaasSubscription;
import com.francetelecom.clara.cloud.techmodel.cf.services.managed.ManagedService;
import com.francetelecom.clara.cloud.techmodel.cf.services.userprovided.AbstractUserProvidedService;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.util.Assert;

import javax.persistence.*;
import javax.validation.Valid;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * an application
 */
@XmlRootElement
@Entity
public class App extends XaasSubscription {

    private String appName;

    @Embedded
    private MavenReference appBinaries;

    private String buildPackUrl;

    private String stack;

    @XmlElementWrapper
    @XmlElement(name = "route")
    @OneToMany(fetch = FetchType.EAGER)
    @Valid
    Set<Route> routes;

    /**
     * the space, the application is scoped to
     */
    @XmlElement(name = "space")
    @OneToOne
    private Space space;

    /**
     * user provided services used by this app
     */
    @ManyToMany(fetch = FetchType.EAGER)
    private Set<AbstractUserProvidedService> userProvidedServices;

    /**
     * managed services used by this app
     */
    @ManyToMany(fetch = FetchType.EAGER)
    private Set<ManagedService> managedServices;

    private int ramMb = 256;
    private int instanceCount = 1;

    @ColumnDefault(value = "false")
    private boolean optionalApplicationBinaries = false;

    /**
     * The set of Env variables that would be made available in the cf app as
     * environement variables
     */
    @XStreamImplicit(itemFieldName = "entry")
    @XmlElementWrapper
    @XmlElement(name = "entry")
    @ElementCollection(fetch = FetchType.EAGER)
    private Map<EnvVariableKey, EnvVariableValue> envVariables;

    private String externalReference;
    private int diskSizeMb = 1024;

    /**
     * constuctor for JPA Jaxb
     */
    protected App() {
    }

    /**
     * Constructor with only mandatory arguments. Mainly used in tests
     *
     * @param td          the technical deployment
     * @param space       the space, the application is scoped to
     * @param appBinaries maven app binaries reference
     * @param appName     the app name
     */
    public App(TechnicalDeployment td, Space space, MavenReference appBinaries, String appName) {
        super(td);
        setAppName(appName);
        setAppBinaries(appBinaries);
        setSpace(space);
        this.routes = new HashSet<>();
        this.userProvidedServices = new HashSet<>();
        this.managedServices = new HashSet<>();
        this.envVariables = new HashMap<>();

    }

    /**
     * Constructs an app spec
     *
     * @param td            the technical deployment
     * @param space         the space, the application is scoped to
     * @param appName       mandatory appName. Should be unique within the CF
     *                      account/space.
     * @param appBinaries   mandatory pointer to app artefacts
     * @param buildPackUrl  optional custom buildpack url, or null to use default
     *                      buildpack
     * @param ramMb         optional amount of ram
     * @param instanceCount instance count
     */
    public App(TechnicalDeployment td, Space space, String appName, MavenReference appBinaries, String buildPackUrl, int ramMb, int instanceCount) {
        this(td, space, appBinaries, appName);
        setBuildPackUrl(buildPackUrl);
        setRamMb(ramMb);
        setInstanceCount(instanceCount);
    }

    public void activate(UUID externalId) {
        setDeploymentState(DeploymentStateEnum.CREATED);
        setExternalReference(externalId);
    }

    public void stop() {
        setDeploymentState(DeploymentStateEnum.STOPPED);
    }

    public void delete() {
        setDeploymentState(DeploymentStateEnum.REMOVED);
    }

    private void setBuildPackUrl(String buildPackUrl) {
        Assert.hasText(buildPackUrl, "Fail to set build pack url. build pack url value <" + buildPackUrl + "> is not valid.");
        this.buildPackUrl = buildPackUrl;
    }

    public String getBuildPackUrl() {
        return buildPackUrl;
    }

    public MavenReference getAppBinaries() {
        return appBinaries;
    }

    public int getRamMb() {
        return ramMb;
    }

    public String getAppName() {
        return appName;
    }

    private void setAppName(String appName) {
        Assert.hasText(appName, "Fail to set app name. app name <" + appName + ">is not valid.");
        this.appName = appName;
    }

    public int getInstanceCount() {
        return instanceCount;
    }

    public void updateAppBinaries(MavenReference resolvedAppBinaries) {
        appBinaries = resolvedAppBinaries;
    }

    public Set<Route> getRoutes() {
        return routes;
    }

    public List<String> getRouteURIs() {
        return routes.stream().map(Route::getUri).collect(Collectors.toList());
    }

    /**
     * @param route CF uri to bind, or null to have no uri bound (e.g.
     *              "webGui1-env1-paasinstance1.cfapps.io" or
     *              "webGui1-env1-paasinstance1.cfapps.redacted-domain.org") Those are the
     *              FQDN of the app (which should be relative to the configured
     *              subdomain or CF default subdomain)
     */
    public void mapRoute(Route route) {
        Assert.notNull(route, "Fail to map route<" + route + "> to app<" + appName + ">. route is not valid.");
        this.routes.add(route);
    }

    public void bindService(AbstractUserProvidedService service) {
        Assert.notNull(service, "Fail to bind service<" + service + "> to app<" + appName + ">. service is not valid.");
        this.userProvidedServices.add(service);
    }

    public void bindService(ManagedService service) {
        Assert.notNull(service, "Fail to bind service<" + service + "> to app<" + appName + ">. service is not valid.");
        this.managedServices.add(service);
    }

    public List<String> getServiceNames() {
        return Stream.concat(userProvidedServices.stream().map(AbstractUserProvidedService::getServiceName), managedServices.stream().map(ManagedService::getServiceInstance)).collect(Collectors.toList());
    }

    public String getExternalReference() {
        return externalReference;
    }

    private void setRamMb(int ramMb) {
        Assert.isTrue(ramMb > 0, "Fail to set app ram. ram value <" + ramMb + "> is not valid");
        this.ramMb = ramMb;
    }

    private void setInstanceCount(int instanceCount) {
        Assert.isTrue(instanceCount >= 1, "Fail to set app instance count <" + instanceCount + ">. At least one instance is required.");
        this.instanceCount = instanceCount;
    }

    private void setExternalReference(UUID externalReference) {
        Assert.notNull(externalReference, "Fail to reference cloud foundry app uid. app uid <" + externalReference + ">is not valid.");
        this.externalReference = externalReference.toString();
    }

    private void setAppBinaries(MavenReference appBinaries) {
        Assert.notNull(appBinaries, "Fail to set app binaries. app binaries value <" + appBinaries + "> is not valid.");
        this.appBinaries = appBinaries;
    }

    public SpaceName getSpace() {
        return space.getSpaceName();
    }

    public void setSpace(Space space) {
        Assert.notNull(space, "unable to scope service <" + appName + "> to space <" + space + ">. invalid space.");
        this.space = space;
    }


    /**
     * List currently env variables
     *
     * @return a non modifiable list
     */
    public Map<EnvVariableKey, EnvVariableValue> listEnvVariables() {
        return Collections.unmodifiableMap(this.envVariables);
    }

    public String getEnvVarValue(String key) {
        EnvVariableValue envVariableValue = envVariables.get(new EnvVariableKey(key));
        if (envVariableValue == null) {
            return null;
        }
        return envVariableValue.getValue();
    }

    public void setEnvVariable(String key, String value) {
        envVariables.put(new EnvVariableKey(key), new EnvVariableValue(value));
    }

    @Override
    public Set<DependantModelItem> listDepedencies() {
        Set<DependantModelItem> dependantModelItems = new HashSet<>();
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (XaasSubscription.class.isAssignableFrom(field.getType())) {
                try {
                    XaasSubscription xaasSubscription = XaasSubscription.class.cast(field.get(this));
                    dependantModelItems.add(xaasSubscription);
                    dependantModelItems.addAll(xaasSubscription.listDepedencies());
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            } else {
                if (Collection.class.isAssignableFrom(field.getType())) {
                    Collection collections;
                    try {
                        collections = Collection.class.cast(field.get(this));
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                    for (Object object : collections) {
                        if (XaasSubscription.class.isAssignableFrom(object.getClass())) {
                            try {
                                XaasSubscription xaasSubscription = XaasSubscription.class.cast(object);
                                dependantModelItems.add(xaasSubscription);
                                dependantModelItems.addAll(xaasSubscription.listDepedencies());
                            } catch (IllegalArgumentException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
            }
        }
        return dependantModelItems;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public boolean isOptionalApplicationBinaries() {
        return optionalApplicationBinaries;
    }


    public void setOptionalApplicationBinaries(boolean optionalApplicationBinaries) {
        this.optionalApplicationBinaries = optionalApplicationBinaries;
    }

    public boolean isStopped() {
        return DeploymentStateEnum.STOPPED.equals(getDeploymentState());
    }

    public String getStack() {
        return stack;
    }

    public void setStack(String stack) {
        this.stack = stack;
    }

    public void setDiskSizeMb(int diskSize) {
        Assert.isTrue(diskSize > 0, "Disk size should be greater than 0. <" + diskSize + ">is not valid.");

        this.diskSizeMb = diskSize;
    }

    public int getDiskSizeMb() {
        return diskSizeMb;
    }
}
