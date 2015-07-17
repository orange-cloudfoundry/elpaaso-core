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

import com.francetelecom.clara.cloud.commons.*;
import com.francetelecom.clara.cloud.commons.jaxb.AnyTypeAdapter;
import com.francetelecom.clara.cloud.logicalmodel.InvalidConfigServiceException.ErrorType;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.*;

@Entity
@Table(name = "PROCESSING_NODE")
//@MappedSuperclass
@XmlJavaTypeAdapter(AnyTypeAdapter.class)
public abstract class ProcessingNode extends LogicalModelItem {

    private static Logger logger = LoggerFactory.getLogger(ProcessingNode.class.getName());
    private static final long serialVersionUID = 1L;
    public static final int MAX_CONFIG_SET_ENTRIES_PER_EXEC_NODE = 300;
    /**
     * logical deployment.
     */
    @XmlIDREF
    @XmlElement(name = "logicalDeploymentRef")
    @ManyToOne
    @NotNull
    @GuiMapping(status = GuiMapping.StatusType.NA)
    protected LogicalDeployment logicalDeployment;
    /**
     * Logical Services used by the Current Node Cluster (relation). Use an
     * association Entity.
     */
    @XmlElementWrapper
    @XmlElement(name = "logicalNodeServiceAssociation")
    @OneToMany(cascade = {CascadeType.ALL}, mappedBy = "processingNode", orphanRemoval = true)
    @GuiMapping
    @Valid
    private List<LogicalNodeServiceAssociation> logicalNodeServiceAssociations = new ArrayList<LogicalNodeServiceAssociation>();
    /**
     * The application artefact that is hosted in this container (packaged as an EAR).
     */
    @Embedded
    @GuiMapping
    @Valid
    MavenReference softwareReference;
    /**
     * Expect the Architect to provided a non pre existing artifact
     * If this is activated, existence control will be skipped, and a default artifact
     * will be provided "on the fly" @ activation time
     */
    @GuiMapping
    boolean optionalSoftwareReference = false;
    private static final String[] EXCLUDED_EQUALS_FIELDS = EqualsUtils.mergeExcludedFieldLists(LogicalModelItem.EXCLUDED_EQUALS_FIELDS,
            new String[]{"logicalDeployment", "logicalNodeServiceAssociations"});

    /**
     * Minimum non persistent disk in Mega bytes (1048576 bytes) that should be allocated to this execution node
     * This should be considered as a hint for disk sizing of production environment
     */
    @GuiMapping(status = GuiMapping.StatusType.SUPPORTED, functional = false)
    @Min(768)
    @Max(100000)
    int minDiskMbHint = 1024;

    /**
     * Minimum memory in Mega bytes (1048576 bytes) that should be allocated to this execution node
     * This should be considered as a hint for memory sizing of production environment
     */
    @GuiMapping(status = GuiMapping.StatusType.SUPPORTED, functional = false)
    @Min(1)
    @Max(10000)
    int minMemoryMbHint = 128;
    /**
     * Memory per session in KB (1024 bytes)
     * This should be considered as a hint for memory sizing of production environment
     */
    @GuiMapping(status = GuiMapping.StatusType.SKIPPED, functional = false)
    @Min(1)
    @Max(10000)
    int memoryKbPerActiveSessionHint = 1024;

    public boolean isOptionalSoftwareReference() {
        return optionalSoftwareReference;
    }

    public void setOptionalSoftwareReference(boolean optionalSoftwareReference) {
        this.optionalSoftwareReference = optionalSoftwareReference;
    }

    /**
     * A custom Icon Url icon which can be null.
     * This is used to display own user icon
     */
    @GuiMapping(status = GuiMapping.StatusType.SUPPORTED, functional = false)
    @Size(min = 0, max = 255)
    String iconUrl;

    protected ProcessingNode() {
        super();
    }

    public ProcessingNode(String label, LogicalDeployment logicalDeployment) {
        super(UUIDUtils.generateUUID("en"), label);
        this.logicalDeployment = logicalDeployment;
        for (ProcessingNode executionNode : logicalDeployment.listProcessingNodes()) {
            Validate.isTrue(!executionNode.getLabel().equals(logicalDeployment), "ExecutionNode label expected to be unique, found duplicate:" + logicalDeployment);
        }
        this.logicalDeployment.processingNodes.add(this);
    }

    /**
     * Logical Node destructor : remove current logical execution node from
     * logical deployment list
     */
    public void discard() {
        this.logicalDeployment.processingNodes.remove(this);
    }

    /**
     * Set the ClusterNode uses given Service Service must be on same deployment
     * <p>
     * An association persistent entity is created
     *
     * @param service
     */
    public void addLogicalServiceUsage(LogicalService service, LogicalServiceAccessTypeEnum accessType) {

        if (service.logicalDeployment != this.logicalDeployment)
            throw new TechnicalException("Node Cluster is not in the same deployment " + logicalDeployment);

        LogicalNodeServiceAssociation association = new LogicalNodeServiceAssociation(this, service);
        association.setAccessType(accessType);
        this.logicalNodeServiceAssociations.add(association);

        // reverse relationship (LogicalService => ExecutionNode)
        service.logicalNodeServiceAssociations.add(association);
    }

    /**
     * Remove an association
     * <p>
     * An association persistent entity is deleted
     *
     * @param logicalNodeServiceAssociation the association to delete
     */
    public void removeLogicalServiceUsage(LogicalNodeServiceAssociation logicalNodeServiceAssociation) {
        // remove reverse relationship (if not, we get a
        // javax.persistence.EntityNotFoundException:
        // deleted entity passed to persist)
        logicalNodeServiceAssociation.getLogicalService().logicalNodeServiceAssociations.remove(logicalNodeServiceAssociation);

        int index = 0;
        boolean found = false;
        for (LogicalNodeServiceAssociation assoc : this.logicalNodeServiceAssociations) {
            if (assoc.equalsDeep(logicalNodeServiceAssociation)) {
                found = true;
                break;
            }
            index++;
        }

        if (found) {
            this.logicalNodeServiceAssociations.remove(index);
        }

    }

    /**
     * Remove a list of associations.
     * <p>
     * one or more association persistent entity are deleted
     *
     * @param logicalNodeServiceAssociationList the list of
     */
    public void removeAllLogicalServiceUsage(List<LogicalNodeServiceAssociation> logicalNodeServiceAssociationList) {
        // NB : we can't call removeLogicalServiceUsage for each list item
        // because it throws a ConcurrentModificationException.

        // remove all reverse relationship
        for (LogicalNodeServiceAssociation assoc : logicalNodeServiceAssociationList) {
            assoc.getLogicalService().logicalNodeServiceAssociations
                    .remove(assoc);
        }
        // remove all assoc
        this.logicalNodeServiceAssociations
                .removeAll(logicalNodeServiceAssociationList);

    }

    /**
     * List the Logical Services used by the ClusterNode
     * <p>
     * Retrieves all LogicalService through the LogicalNodeService association
     * Entity
     *
     * @return
     */
    public List<LogicalService> listLogicalServices() {
        return listLogicalServices(null);
    }

    /**
     * List the Logical Services of a specific class.
     * <p>
     * Retrieves all {@link LogicalService} that is connected to the current ExecNode through the {@link LogicalNodeServiceAssociation}
     *
     * @param filteredClass The class of the logicalService to restrict to (e.g. LogicalWebGUIService.class), or null
     *                      to not perform any filtering. Note this may be a superclass or an implementing interface.
     * @return a non-null, possibly empty list
     */
    public <E extends LogicalService> List<E> listLogicalServices(Class<E> filteredClass) {
        List<E> services = new ArrayList<E>();
        for (LogicalNodeServiceAssociation association : this.logicalNodeServiceAssociations) {
            LogicalService logicalService = association.logicalService;
            boolean includeThisClass = false;
            if (filteredClass == null) {
                includeThisClass = true;
            } else {
                includeThisClass = (filteredClass.isInstance(logicalService));
            }
            if (includeThisClass) {
                services.add((E) logicalService);
            }
        }

        return Collections.unmodifiableList(services);
    }

    /**
     * List all association to used Logical services
     *
     * @return
     */
    public List<LogicalNodeServiceAssociation> listLogicalServicesAssociations() {
        return Collections
                .unmodifiableList(this.logicalNodeServiceAssociations);
    }

    public MavenReference getSoftwareReference() {
        return softwareReference;
    }

    public void setSoftwareReference(MavenReference softwareReference) {
        this.softwareReference = softwareReference;
    }

    public int getMinDiskMbHint() {
        return minDiskMbHint;
    }

    public void setMinDiskMbHint(int minDiskMbHint) {
        this.minDiskMbHint = minDiskMbHint;
    }

    public int getMinMemoryMbHint() {
        return minMemoryMbHint;
    }

    public void setMinMemoryMbHint(int minMemoryMbHint) {
        this.minMemoryMbHint = minMemoryMbHint;
    }

    public int getMemoryKbPerActiveSessionHint() {
        return memoryKbPerActiveSessionHint;
    }

    public void setMemoryKbPerActiveSessionHint(int memoryKbPerActiveSessionHint) {
        this.memoryKbPerActiveSessionHint = memoryKbPerActiveSessionHint;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    @Override
    public boolean equals(Object obj) {
        //See http://commons.apache.org/lang/api-2.5/org/apache/commons/lang/builder/HashCodeBuilder.html
        return EqualsBuilder.reflectionEquals(this, obj, EXCLUDED_EQUALS_FIELDS);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this, EXCLUDED_EQUALS_FIELDS);
    }

    /**
     * Utility method to apply consistency checking on the overall model
     *
     */
    public void checkConsistency() throws InvalidConfigServiceException {
        Properties mergedProperties = getMergedConfigServicesProperties();
        //TODO: add a debug trace on merged properties ?
    }

    /**
     * Utility method to return a merged list of {@link LogicalConfigService} content in a {@link Properties} format.
     *
     * @return a @{link Properties} instance with {@link String} as keys and
     * @throws InvalidConfigServiceException if some of the {@link LogicalConfigService} associated to this object were
     *                                       not valid
     */
    public Properties getMergedConfigServicesProperties() throws InvalidConfigServiceException {
        List<LogicalConfigService> logicalConfigServices = listLogicalServices(LogicalConfigService.class);

        Properties mergedProperties = new Properties();
        Set<String> duplicates = new HashSet<String>();
        StringBuffer collisions = new StringBuffer();
        for (LogicalConfigService logicalConfigService : logicalConfigServices) {
            //Check overlap in key names among the subscribed config services

            logicalConfigService.mergeAndCheckForDuplicateKeys(mergedProperties, duplicates, collisions);
        }
        if (mergedProperties.size() > MAX_CONFIG_SET_ENTRIES_PER_EXEC_NODE) {
            InvalidConfigServiceException invalidConfigServiceException = new InvalidConfigServiceException("Too many Config entries for ExecutionNode=" + this.getLabel());
            invalidConfigServiceException.setType(ErrorType.TOO_MANY_ENTRIES);
            invalidConfigServiceException.setEntryCount(mergedProperties.size());
            invalidConfigServiceException.setMaxEntryCount(MAX_CONFIG_SET_ENTRIES_PER_EXEC_NODE);
            invalidConfigServiceException.setImpactedElementName(getLabel());
            throw invalidConfigServiceException;
        }
        if (collisions.length() > 0) {
            InvalidConfigServiceException invalidConfigServiceException = new InvalidConfigServiceException("Collision for ExecutionNode=" + this.getLabel() + " collision=" + collisions.toString());
            invalidConfigServiceException.setType(ErrorType.DUPLICATE_KEYS);
            invalidConfigServiceException.getDuplicateKeys().addAll(duplicates);
            invalidConfigServiceException.setImpactedElementName(getLabel());
            throw invalidConfigServiceException;
        }
        return mergedProperties;
    }

    protected void setLogicalDeployment(LogicalDeployment logicalDeployment) {
        this.logicalDeployment = logicalDeployment;
    }

    public LogicalDeployment getLogicalDeployment() {
        return logicalDeployment;
    }

    @Override
    protected boolean isFieldExcludedFromToString(String fieldName) {
        return isFieldExcludedFromToString(fieldName, ProcessingNode.EXCLUDED_EQUALS_FIELDS);
    }

    /**
     * Utility method to check that each Service associated to an ExecutionNode is indeed also pointing to this node.
     * This is used with asserts.
     *
     * @return true if symetry is indeed respected, false otherwise.
     */
    public boolean isAssociationsSymmetryRespected() {
        for (LogicalNodeServiceAssociation association : logicalNodeServiceAssociations) {
            //For the current service, check it point back to us
            boolean currentExecNodeFound = false;
            final LogicalService logicalService = association.getLogicalService();
            final List<LogicalNodeServiceAssociation> logicalNodeServiceAssociations1 = logicalService.listLogicalServicesAssociations();
            for (LogicalNodeServiceAssociation logicalNodeServiceAssociation : logicalNodeServiceAssociations1) {
                if (logicalNodeServiceAssociation.getProcessingNode() == this) {
                    currentExecNodeFound = true;
                }
            }
            if (!currentExecNodeFound) {
                logger.error("associations between execution nodes and services should be symmetric, did not find pointer back to:" + this + " from:" + logicalService);
                return false;
            }
        }
        return true;
    }

}