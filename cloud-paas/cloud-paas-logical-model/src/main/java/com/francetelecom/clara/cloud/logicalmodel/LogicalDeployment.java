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

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.Valid;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.francetelecom.clara.cloud.commons.BusinessException;
import com.francetelecom.clara.cloud.commons.GuiMapping;
import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.francetelecom.clara.cloud.commons.UUIDUtils;
import com.francetelecom.clara.cloud.commons.ValidatorUtil;

/**
 * Logical Model for a deployment. 
 * 
 * This is the top level entry for the logical model. 
 * 
 * There is typically a single instance of a LogicalDeployment per
 * application release, which is used to describe parts that common to 
 * all of its environments. 
 *
 * @author APOG7416
 */

@XmlRootElement
@Entity
@Table(name = "LOGICAL_DEPLOYMENT")
public class LogicalDeployment extends LogicalModelItem {

	//See equals() for details
	private static final String[] EXCLUDED_HIBERNATE_BROKEN_EQUALS_COLLECTIONS = new String[] {"logicalServices", "processingNodes"};

    public static final LogicalConfigServiceUtils LOGICAL_CONFIG_SERVICE_UTILS = new LogicalConfigServiceUtils();

	/**
	 * serialversion UID
	 */
	private static final long serialVersionUID = 8754768294184787020L;

	private static Logger logger=LoggerFactory.getLogger(LogicalDeployment.class.getName());
	/**
	 * ????. Mandatory????. 
	 * 
	 * Note: not currently used. This might be useful in the future for references
	 * that the TechnicalModel has on the LogicalDeployment to indicate whether
	 * the current instance is per Technical Model.  
	 */
    @GuiMapping(status = GuiMapping.StatusType.SKIPPED)
	private boolean template = true;

    /**
	 * When set to true, indicates the application described is subject to the Sarbanes–Oxley Act. See
	 * http://en.wikipedia.org/wiki/Sarbanes%E2%80%93Oxley_Act As such specific requirement would apply (such as
	 * collecting audit traces, archiving logs for a long duration, propagating the SOX flag to dependent services such
	 * as DBaaS).
	 */
    @GuiMapping(status = GuiMapping.StatusType.SKIPPED)
    private boolean sox = false;


	/**
	 * All logical services used by all deployment Node Cluster (aggregation).
	 */
	@XmlElementWrapper
	@XmlElement(name = "logicalService")
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "logicalDeployment", orphanRemoval = true)
    @GuiMapping()
    @Valid
	protected List<LogicalService> logicalServices = new ArrayList<LogicalService>();

	/**
	 * All processing nodes in the deployment.
	 */
	@XmlElementWrapper
	@XmlElement(name = "processingNode")
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "logicalDeployment", orphanRemoval = true)
    @GuiMapping()
    @Valid
	protected List<ProcessingNode> processingNodes = new ArrayList<ProcessingNode>();

	/**
	 * Default constructor.
	 */
	public LogicalDeployment() {
        super();
	}

	/**
	 * Public constructor.
	 * 
	 * @param label
	 *            logical deployment label
	 */
    @Deprecated
	public LogicalDeployment(String label) {
        super(UUIDUtils.generateUUID("ld"));
        this.label = label;
	}

    /**
     * List all execution nodes . (Read-only)
     * @return an unmodifiable list of execution nodes sorted by type and label
     */
    //TODO: return a Set instead of a List: no duplicates allowed in ExecNode.
	public List<ProcessingNode> listProcessingNodes() {
		return listProcessingNodes(null);
	}

    /**
     * List all execution nodes . (Read-only)
     * @param filteredType the class to filter execNode or null to return all nodes
     * @return an unmodifiable list of execution nodes sorted by type and label
     */
    //TODO: return a Set instead of a List: no duplicates allowed in ExecNode.
	public <E extends ProcessingNode> List<E> listProcessingNodes(Class<E> filteredType) {
		

        if (filteredType == null) {
        	List<ProcessingNode> nodes = new ArrayList<ProcessingNode>(this.processingNodes);
    		Collections.sort(nodes);
		    return (List<E>) Collections.unmodifiableList(nodes);
        } else {
            List<E> matchingNodes = new ArrayList<E>();
            for (ProcessingNode executionNode : processingNodes) {
                if (filteredType.isInstance(executionNode)) {
                    matchingNodes.add((E) executionNode);
                }
            }
    		Collections.sort(matchingNodes);
            return Collections.unmodifiableList(matchingNodes); //overkill ?
        }

	}

    /**
     * Find a @{link JeeProcessing} by its name
     * @return
     */
    public ProcessingNode findProcessingNodeByName(String name) {
        Validate.notEmpty(name, "unexpected empty or null name");
        for (ProcessingNode node : processingNodes) {
            if ( node.getName().equals( name ) ) return node;
        }
        throw new IllegalArgumentException( "Unknown execution node " + name );
    }

    /**
     * Find a @{link JeeProcessing} by its user-provided label
     * @return
     */
    public ProcessingNode findProcessingNode(String label) {
        Validate.notEmpty(label, "unexpected empty or null label");
        for (ProcessingNode node : processingNodes) {
            if ( node.getLabel().equals( label ) ) return node;
        }
        throw new IllegalArgumentException( "Unknown execution node " + label );
    }

	/**
	 * List all logical services. (Read-only)
	 * FIXME: check if this should not return a Set instead
	 * @return an unmodifiable list of logical services sorted by type and label
	 */
	public List<LogicalService> listLogicalServices() {
		List<LogicalService> services = new ArrayList<LogicalService>(this.logicalServices);
		Collections.sort(services);
		return Collections.unmodifiableList(services);
	}

    /**
     * Logical services filtered by type, and deduplicated. (Read-only), and filtered to match a given name.
     * @param filteredType the class to filter logical services or null to return all services
     * @param name an optional name/label to filter against {@link com.francetelecom.clara.cloud.logicalmodel.LogicalService#getName()} or {@link com.francetelecom.clara.cloud.logicalmodel.LogicalService#getLabel()}
     * or null to not perform any of this filtering
     * @return an unmodifiable list of logical services sorted by type and label
     */
    public <E extends LogicalService> Set<E> listLogicalServices(Class<E> filteredType, String name) {
		Set<E> services = new TreeSet<E>();
        for (LogicalService service : this.logicalServices) {
            boolean includeService = false;

            if (filteredType == null || filteredType.isInstance(service)) {
                if (name == null || name.equals(service.getName()) || name.equals(service.getLabel())) {
                    includeService = true;
                }
            }

            if (includeService) {
                services.add((E) service);
            }

        }

        logger.debug(services.size() + " services of type " + filteredType + "have been found");
        return services;
    }

    /**
     * List all logical services filtered by type, and deduplicated. (Read-only)
     * @param filteredType the class to filter logical services or null to return all services
     * @return an unmodifiable list of logical services sorted by type and label
     */
    public <E extends LogicalService> Set<E> listLogicalServices(Class<E> filteredType) {
        return listLogicalServices(filteredType, null);
    }

    public boolean isSox() {
        return sox;
    }


    public void setSox(boolean sox) {
        this.sox = sox;
    }

    public void addExecutionNode(ProcessingNode node) {
        //Note: Assigning the UUID at the time the service is added in the LD. This should
        //help diagnosing orphan sercvies
//        node.setName(UUID.randomUUID().toString());

        for (ProcessingNode executionNode : this.processingNodes) {
            Validate.isTrue(!executionNode.getName().equals(node.getName()), "ExecutionNode name expected to be unique, found duplicate:" + name);
            Validate.isTrue(!executionNode.getLabel().equals(node.getLabel()), "ExecutionNode label expected to be unique, found duplicate:" + label);
        }

        node.setLogicalDeployment(this);
        this.processingNodes.add(node);

    }

    public void removeProcessingNode(ProcessingNode jeeProcessing) {
		// Before removing the execution node, we have to remove all it's
		// logical service usage (if not, we get a
		// javax.persistence.EntityNotFoundException:
		// deleted entity passed to persist)
		jeeProcessing.removeAllLogicalServiceUsage(jeeProcessing.listLogicalServicesAssociations());
		// then we can remove the node
		this.processingNodes.remove(jeeProcessing);
	}

    public void removeProcessingNodes(
			List<ProcessingNode> processingsList) {
        while (!processingsList.isEmpty()) {
            removeProcessingNode(this.processingNodes.get(0));
        }
	}

    public void removeAllProcessingNodes() {
        List<ProcessingNode> nodes = this.processingNodes;
        removeProcessingNodes(nodes);
    }

    public void addLogicalService(LogicalService service) {
        //Note: Assigning the UUID at the time the service is added in the LD. This should
        //help diagnosing orphan sercvies
//        service.setName(UUID.randomUUID().toString());

        for (LogicalService logicalService : this.logicalServices) {
            Validate.isTrue(!logicalService.getName().equals(service.getName()), "LogicalService name expected to be unique, found duplicate:" + name);
            Validate.isTrue(!logicalService.getLabel().equals(service.getLabel()), "LogicalService label expected to be unique, found duplicate:" + label);
        }

        service.setLogicalDeployment(this);
        this.logicalServices.add(service);

    }

	/**
	 * removes a Logical Service
	 * @param logicalService
	 * @throws BusinessException
	 */
	public void removeLogicalService(LogicalService logicalService)
			throws BusinessException {
		// we can remove a logicalService only if it is not associated to a node

		for (ProcessingNode jeeProcessing : this.listProcessingNodes()) {

            List<LogicalService> services = jeeProcessing.listLogicalServices();

			if (services.contains(
					logicalService)) {
				    // TODO : create business exception subclass ? where ?
                    throw new BusinessException(
                            "Can't delete a service already associated to a node");
            }
        }

        //TODO: Delete this block when Matrix service deletion will be refactored bug #96290
        int serviceIndex = -1;
        for (LogicalService service : this.logicalServices) {
            serviceIndex++;
            if (service.equals(logicalService, true)) {
                break;
            }
        }

        if (serviceIndex != -1) {
            this.logicalServices.remove(serviceIndex);
        }
        //TODO: uncomment when Matrix service deletion will be refactored
//		this.logicalServices.remove(logicalService);
	}

	/**
	 * removes all Logical Services
	 * @param logicalServiceList
	 * @throws BusinessException
	 */
	public void removeAllLogicalService(List<LogicalService> logicalServiceList)
			throws BusinessException {
        while (!logicalServiceList.isEmpty()) {
            removeLogicalService(this.logicalServices.get(0));
        }
	}

    /**
     * Removes all logical services
     * @throws BusinessException exception thrown when a service is still associated to an execution node
     */
    public void removeAllLogicalService() throws BusinessException {
        List<LogicalService> services = this.logicalServices;
        removeAllLogicalService(services);
    }

    /**
     * Utility method to apply consistency checking on the overall model.
     */
    public void checkOverallConsistency() throws LogicalModelNotConsistentException {
        List<BusinessException> errors = new ArrayList<BusinessException>();

        if (processingNodes.isEmpty()) {
            errors.add(new BusinessException("Please configure at least one JEE processing service which is required for your application"));
        }

        for (ProcessingNode executionNode : processingNodes) {
            try {
                //Check config service associated to each execNode given do not overlap
                executionNode.checkConsistency();
            } catch (InvalidConfigServiceException e) {
                errors.add(e);
            }
            //Assertions on LogicalDeployment invariants
            assert executionNode.isAssociationsSymmetryRespected() : "associations between execution nodes and services should be symmetric, check error traces for details";
            assert this == executionNode.getLogicalDeployment() : "relation between logical deployment and execution node should be symmetric";
        }

        for (LogicalService service : logicalServices) {
            assert this == service.getLogicalDeployment() : "relation between logical deployment and service should be symmetrical";
            int nbAssociatedExecNodes = service.listLogicalServicesAssociations().size();
            if (nbAssociatedExecNodes ==0) {
                errors.add(new BusinessException("Please make sure each service is associated to at least one processing service (e.g. JeeProcessing). The following service is dangling: " + service.getLabel()));
            }
        }

        // CHECK CONFIG SETS for duplicate keys within each services
        Set<LogicalConfigService> logicalConfigServices = listLogicalServices(LogicalConfigService.class);
        for (LogicalConfigService logicalConfigService : logicalConfigServices) {
            try {
                LOGICAL_CONFIG_SERVICE_UTILS.parseConfigContent(logicalConfigService.getConfigSetContent());
            } catch(InvalidConfigServiceException e) {
                e.setImpactedElementName(logicalConfigService.getLabel());
                errors.add(e);
            }
        }

		Set<LogicalMomService> logicalMomServices = listLogicalServices(LogicalMomService.class);
		for (LogicalMomService logicalMomService : logicalMomServices) {
			// Check that the dead letter queue is defined when checked.
			if (logicalMomService.hasDeadLetterQueue) {
				if (logicalMomService.getDeadLetterQueueName() == null || logicalMomService.getDeadLetterQueueName().equals("")) {
					BusinessException be = new BusinessException("Dead letter queue has been enabled but no name has been defined: "
							+ logicalMomService.getDestinationName());
					be.setImpactedElementName(logicalMomService.getLabel());
					errors.add(be);
				}
			}
			// Check that we don't have duplicated JNDI names.
			for (LogicalMomService logicalMomService2 : logicalMomServices) {
				if (logicalMomService != logicalMomService2) {
					if (logicalMomService.getDestinationName().equals(logicalMomService2.getDestinationName())) {
						errors.add(new BusinessException("Destination name must be unique. Duplicate destination name: "
								+ logicalMomService.getDestinationName()));
					}
					if (logicalMomService.getDestinationName().equals(logicalMomService2.getDeadLetterQueueName())) {
						errors.add(new BusinessException("Destination name can't be the same as dead letter queue name: "
								+ logicalMomService.getDestinationName()));
					}
				} else {
					if (logicalMomService.getDestinationName().equals(logicalMomService.getDeadLetterQueueName())) {
						errors.add(new BusinessException("Destination name can't be the same as dead letter queue name: "
								+ logicalMomService.getDestinationName()));
					}
				}
			}
		}

        try {
            //Force validation of constraints expressed in annotations, so that we provide feedback to end-user early on.
            ValidatorUtil.validate(this);
        } catch (TechnicalException e) {
            errors.add(new BusinessException("Invalid architecture: fails javax.validation: " + e, e));
        }
        if (!errors.isEmpty()) {
            logger.debug("Detected consistency errors in logical model:" + errors);
            throw new LogicalModelNotConsistentException(errors);
        }
    }

	/**
	 * gets an XML representation of the model
	 * @return
	 */
	public String dumpXml() {

		logger.debug("dumping xml model for "+this + " into a String");
		JAXBContext jc;
		try {
			jc = JAXBContext.newInstance(LogicalDeployment.class);
			Marshaller m = jc.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			ByteArrayOutputStream baos=new ByteArrayOutputStream();

			m.marshal(this, baos);
			return baos.toString();

		} catch (JAXBException e) {
			logger.error("Unable to marshall model");
			throw new TechnicalException(e);
		}

	}

	public void setTemplate(boolean template) {
		this.template = template;
	}

	public boolean isTemplate() {
		return template;
	}

    @Override
    protected boolean isFieldExcludedFromToString(String fieldName) {
        return isFieldExcludedFromToString(fieldName, LogicalDeployment.EXCLUDED_EQUALS_FIELDS);
    }


    @Override
    public boolean equals(Object obj) {
        //Note: Hibernate implementation of List (PersistentBag) breaks  List.equals() contract.
        //http://docs.jboss.org/hibernate/core/3.2/api/org/hibernate/collection/PersistentBag.html#equals%28java.lang.Object%29
        //Therefore, we have to wrap the hibernate collection into a standard one which properly
        //respect the equals() by comparing elements within it.
        //In terms of performance impact, this should be quite limited because PersistentBag is backed
        //by an ArrayList, and therefore this should merely translate into an array copy + one ArrayList
        //wrapper object creation.
        if (obj == null) { return false; }
        if (obj == this) { return true; }
        if (obj.getClass() != getClass()) {
            return false;
        }
        LogicalDeployment rhs = (LogicalDeployment) obj;

        //In addition, we can't simply delegate equals() to super class, otherwise it will pick up the collections
        //from LogicalDeployment "this" instance using reflection. Therefore we explicitly request to exclude it in
        //the first equals() call. This will however, take
        EqualsBuilder equalsBuilder = new EqualsBuilder()
                .appendSuper(super.equals(obj, EXCLUDED_HIBERNATE_BROKEN_EQUALS_COLLECTIONS));
        //Then we manually compare the collections by wrapping them.
        // We need to sort it first (based on labels + class names)
        List<ProcessingNode> thisProcessingNodes = new ArrayList<ProcessingNode>(processingNodes);
        Collections.sort(thisProcessingNodes);
        List<LogicalService> thisLogicalServices = new ArrayList<LogicalService>(logicalServices);
        Collections.sort(thisLogicalServices);
        List<ProcessingNode> rhsProcessingNodes = new ArrayList<ProcessingNode>(rhs.processingNodes);
        Collections.sort(rhsProcessingNodes);
        List<LogicalService> rhsLogicalServices = new ArrayList<LogicalService>(rhs.logicalServices);
        Collections.sort(rhsLogicalServices);
        boolean equals = equalsBuilder
                .append(thisProcessingNodes, rhsProcessingNodes)
                .append(thisLogicalServices, rhsLogicalServices)
                .isEquals();
        return equals;
    }

	@Override
	public int hashCode() {
		//Note: Same workarounds as for equals
		//FIXME: may break in NPE when hibernate uses lazy loading of the processingNodes and logicalServices
		  return new HashCodeBuilder(17, 37).
	       appendSuper(super.hashCode(EXCLUDED_HIBERNATE_BROKEN_EQUALS_COLLECTIONS)).
	       append(new ArrayList<ProcessingNode>(processingNodes)).
	       append(new ArrayList<LogicalService>(logicalServices)).
	       toHashCode();
	}

    public LogicalService findLogicalService(String label) {
        for (LogicalService logicalService : logicalServices) {
            if (logicalService.getLabel().equals(label)) {
                return logicalService;
            }
        }
        throw new IllegalArgumentException( "Unknown execution node " + label );
    }

	public boolean noProcessingNodes() {
		List<JeeProcessing> jeeProcessings = listProcessingNodes(JeeProcessing.class);
		List<CFJavaProcessing> cfJavaProcessings = listProcessingNodes(CFJavaProcessing.class);
		return jeeProcessings.isEmpty() && cfJavaProcessings.isEmpty();
	}
}
