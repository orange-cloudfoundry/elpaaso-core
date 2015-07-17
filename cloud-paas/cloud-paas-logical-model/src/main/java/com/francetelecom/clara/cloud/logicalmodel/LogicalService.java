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

import com.francetelecom.clara.cloud.commons.EqualsUtils;
import com.francetelecom.clara.cloud.commons.GuiClassMapping;
import com.francetelecom.clara.cloud.commons.GuiMapping;
import com.francetelecom.clara.cloud.commons.UUIDUtils;
import com.francetelecom.clara.cloud.commons.jaxb.AnyTypeAdapter;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import java.util.*;

/**
 * Base class for Service in the Logical Model.
 * Factors out common properties to all services that are attached to a {@link ProcessingNode}
 *
 * Note that this does not directly map to each entry in the service catalog PDF document.
 * @author APOG7416
 */
// @XmlRootElement
@Entity
@Table(name = "LOGICAL_SERVICE")
@XmlJavaTypeAdapter(AnyTypeAdapter.class)
public abstract class LogicalService extends LogicalModelItem {

    private static final long serialVersionUID = 1L;

	/**
	 * logical deployment.
	 */
	@XmlIDREF
	@XmlElement(name = "logicalDeploymentRef")	
	@ManyToOne
	@NotNull
    @GuiMapping(status = GuiMapping.StatusType.NA)
	LogicalDeployment logicalDeployment;

	/**
	 * Logical Node using the logical Service. Use an assocation Entity. No xml
	 * element for association (done on execution service side)
	 */
	@XmlTransient
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "logicalService", orphanRemoval = true)
    @GuiMapping()
	protected List<LogicalNodeServiceAssociation> logicalNodeServiceAssociations  = new ArrayList<LogicalNodeServiceAssociation>();

    // @TODO: remove label when transition is ended
	/**
	 * Note: the logicalNodeServiceAssociation is kept, but ignores its link back to us (LogicalService)
	 */
	private static final String[] EXCLUDED_EQUALS_FIELDS = EqualsUtils.mergeExcludedFieldLists(LogicalModelItem.EXCLUDED_EQUALS_FIELDS, 
			new String[] {"logicalDeployment", "targetBasicatCode", "initialPopulationScript"});

	private static final String[] EXCLUDED_HIBERNATE_BROKEN_EQUALS_COLLECTIONS = new String[] { "logicalNodeServiceAssociations" } ;

	/**
	 * Default constructor.
	 */
	public LogicalService() {
        super();
    }

	/**
	 * Logical Service constructor
	 * 
	 * @param label
	 * @param logicalDeployment
     * @deprecated Should not be called anymore, use empty constructor instead
     * followed by {@link LogicalDeployment#addLogicalService(LogicalService)}
	 */
	public LogicalService(String label, LogicalDeployment logicalDeployment) {
		super(UUIDUtils.generateUUID("ls"), label);
		this.logicalDeployment = logicalDeployment;

		// add the LogicalService to the global deployment List
        //FIXME: use logicalDeployment.addLogicalService(this) instead
        logicalDeployment.addLogicalService(this);
	}

	/**
	 * list all ExecutionNode using this Service
	 * 
	 * @return
	 */
	public Set<ProcessingNode> listDependentProcessingNodes() {
		HashSet<ProcessingNode> nodes = new HashSet<ProcessingNode>();
		for (LogicalNodeServiceAssociation association : this.logicalNodeServiceAssociations) {
			nodes.add(association.processingNode);
		}
		return Collections.unmodifiableSet(nodes);
	}

	/**
	 * List all association to related LogicalExecution Node
	 * 
	 * @return
	 */
	public List<LogicalNodeServiceAssociation> listLogicalServicesAssociations() {
		return Collections.unmodifiableList(this.logicalNodeServiceAssociations);
	}

    @Override
    public boolean equals(Object obj) {
        //See rationale for this specific impl in LogicalDeployment.equals()
        return equals(obj, false);
    }

    /**
     * Utility method to factor out logic between {@link #equals(Object)} and {@link #equalsShallow(LogicalService)}
     * @param obj The other object to compare
     * @param excludeAssociations set to true to exclude the {@link #logicalNodeServiceAssociations} in the comparison
     *                            or false to include them.
     * @return
     */
    protected boolean equals(Object obj, boolean excludeAssociations) {
        if (obj == null) { return false; }
        if (obj == this) { return true; }
        if (obj.getClass() != getClass()) {
            return false;
        }
        LogicalService rhs = (LogicalService) obj;

        EqualsBuilder equalsBuilder = new EqualsBuilder()
                .appendSuper(super.equals(obj, EqualsUtils.mergeExcludedFieldLists(EXCLUDED_EQUALS_FIELDS, EXCLUDED_HIBERNATE_BROKEN_EQUALS_COLLECTIONS)));

        if (!excludeAssociations) {
            List<LogicalNodeServiceAssociation> thisLogicalNodeServiceAssociations = new ArrayList<LogicalNodeServiceAssociation>(logicalNodeServiceAssociations);
            Collections.sort(thisLogicalNodeServiceAssociations);
            List<LogicalNodeServiceAssociation> rhsLogicalNodeServiceAssociations = new ArrayList<LogicalNodeServiceAssociation>(rhs.logicalNodeServiceAssociations);
            Collections.sort(rhsLogicalNodeServiceAssociations);
            equalsBuilder.append(thisLogicalNodeServiceAssociations, rhsLogicalNodeServiceAssociations);
        }

        return equalsBuilder.isEquals();
    }

    /**
     * Tests that two LogicalService are semantically equal, ignoring whether the object was connected to other
     * services. This is mainly used in tests (such as UI automated tests) 
     * @param logicalService
     * @return
     */
    public boolean equalsShallow(LogicalService logicalService) {
        return equals(logicalService, true);
    }

    
	@Override
	public int hashCode() {
		//Note: Same workarounds as for equals
		  HashCodeBuilder builder = new HashCodeBuilder(13, 71).
	       appendSuper(super.hashCode(EqualsUtils.mergeExcludedFieldLists(EXCLUDED_EQUALS_FIELDS, EXCLUDED_HIBERNATE_BROKEN_EQUALS_COLLECTIONS)));
		  if (logicalNodeServiceAssociations != null) {
			  //Hack: force eager fetch of associations
			  logicalNodeServiceAssociations.size();
			  builder.append(new ArrayList<LogicalNodeServiceAssociation>(logicalNodeServiceAssociations));
		  }
		return builder.toHashCode();
	}

    /**
     * Utility method to list all supported classes
     * @return
     */
    public static Set<Class> listServicesClass() {
        Set<Class> classes = new HashSet<Class>();
        classes.add(LogicalWebGUIService.class);
        classes.add(LogicalQueueSendService.class);
        classes.add(LogicalQueueReceiveService.class);
        classes.add(LogicalRelationalService.class);
        classes.add(LogicalConfigService.class);
        classes.add(LogicalSoapConsumer.class);
        classes.add(LogicalSoapService.class);
        //TODO: add others

        return classes;
    }

    protected void setLogicalDeployment(LogicalDeployment logicalDeployment) {
        this.logicalDeployment = logicalDeployment;
    }

    public LogicalDeployment getLogicalDeployment() {
        return logicalDeployment;
    }

    @Override
    protected boolean isFieldExcludedFromToString(String fieldName) {
        return isFieldExcludedFromToString(fieldName, LogicalService.EXCLUDED_EQUALS_FIELDS);
    }

    @Override
    public int compareTo(LogicalModelItem o) {

        GuiClassMapping annotationOther = o.getClass().getAnnotation(GuiClassMapping.class);
        GuiClassMapping annotationThis = this.getClass().getAnnotation(GuiClassMapping.class);

        if (annotationThis != null && annotationOther != null) {
            if (annotationThis.isExternal() && annotationOther.isExternal() ||
                    !annotationThis.isExternal() && !annotationOther.isExternal()   ) {
                return super.compareTo(o);
            } else if (annotationThis.isExternal() && !annotationOther.isExternal()) {
                return -1;
            } else {
                return 1;
            }
        } else {
            //If services are in same category, then sort based on class name and labels
            return super.compareTo(o);
        }
    }
}
