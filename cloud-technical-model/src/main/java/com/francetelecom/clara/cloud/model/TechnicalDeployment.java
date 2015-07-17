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

import com.thoughtworks.xstream.annotations.XStreamImplicit;

import javax.persistence.*;
import javax.validation.Valid;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A Technical deployment is a consistent set of PlatformServers build to
 * represent an ApplicationRelease technical instanciation.
 * <p/>
 * Correspond to the Ovf concept of vAppTemplate.
 *
 * @author APOG7416
 */
@XmlRootElement
@Entity
@Table(name = "TECHNICAL_DEPLOYMENT")
public class TechnicalDeployment extends ModelItem {

    private static final long serialVersionUID = 5150188522548642238L;

    private TechnicalDeploymentStateEnum state = TechnicalDeploymentStateEnum.NEW;

    /**
     * xAAS Subscriptions associated to the TechnicalDeployment
     * <p/>
     * These subscriptions are mutable. They are originally produced by the
     * projection as templates that need to be executed for each environment.
     * Once the TechnicalDeployment instance is attached to a
     * TechnicalDeploymentInstance, then this list represents a list of
     * subscription instances (and the deployment state associated becomes
     * CREATED)
     */
    @XStreamImplicit(itemFieldName = "xaasSubscription")
    @XmlElementWrapper
    @XmlElement(name = "xaasSubscriptions")
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, mappedBy = "technicalDeployment", fetch = FetchType.EAGER)
    @Valid
    protected Set<XaasSubscription> xaasSubscriptions;

    /**
     * Required constructor for JPA / Jaxb
     */
    protected TechnicalDeployment() {
    }

    /**
     * technicalDeployment constructor
     *
     * @param name
     */
    public TechnicalDeployment(String name) {
        super(name);
        this.xaasSubscriptions = new HashSet<XaasSubscription>();
    }

    /**
     * returns an unmodifiable collection of SubscriptionsTemplate
     *
     * @return list of subscriptions template
     */
    public Set<XaasSubscription> listXaasSubscriptionTemplates() {
        return listXaasSubscriptionTemplates(null);
    }

    /**
     * Returns an unmodifiable collection of SubscriptionsTemplate filtered by
     * type.
     *
     * @param filteredType       The class of the XaasSubscription subclass to filter or null
     *                           to return all instances
     * @param logicalModelItemId The logicalModelItemId to filter against (see
     *                           {@link com.francetelecom.clara.cloud.model.ModelItem#getLogicalModelId()}
     *                           or null to not perform such filtering
     * @return list of subscriptions template
     */
    public <E extends XaasSubscription> Set<E> listXaasSubscriptionTemplates(Class<E> filteredType, String logicalModelItemId) {
        if (filteredType == null) {
            return (Set<E>) Collections.unmodifiableSet(this.xaasSubscriptions);
        } else {
            Set<E> filteredSubscriptions = new HashSet<E>();
            for (XaasSubscription subscription : this.xaasSubscriptions) {
                if (filteredType.isInstance(subscription)) {
                    if (logicalModelItemId == null || logicalModelItemId.equals(subscription.getLogicalModelId())) {
                        boolean wasAdded = filteredSubscriptions.add((E) subscription);
                        assert wasAdded : "we don't expect duplicates : " + subscription;
                    }
                }
            }
            return (Set<E>) Collections.unmodifiableSet(filteredSubscriptions);
        }
    }

    /**
     * Returns an unmodifiable collection of SubscriptionsTemplate filtered by
     * type.
     *
     * @param filteredType The class of the XaasSubscription subclass to filter or null
     *                     to return all instances
     * @return list of subscriptions template
     */
    public <E extends XaasSubscription> Set<E> listXaasSubscriptionTemplates(final Class<E> filteredType) {
        return listXaasSubscriptionTemplates(filteredType, null);
    }

    public TechnicalDeploymentStateEnum getState() {
        return state;
    }

    public void setState(TechnicalDeploymentStateEnum state) {
        this.state = state;
    }

}
