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

import com.francetelecom.clara.cloud.commons.GuiMapping;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.lang.reflect.Field;

/**
 * Logical execution node to service Many to Many Association
 * 
 * @author APOG7416
 * 
 */
@XmlRootElement
@Entity
@Table(name = "EXECUTION_NODE_SERVICE_ASSOCIATION", uniqueConstraints = @UniqueConstraint(columnNames = {
		"processingNode_id", "logicalService_id" }))
public class LogicalNodeServiceAssociation implements Serializable, Comparable<LogicalNodeServiceAssociation> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5468256654529545579L;

	@XmlAttribute
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
    @GuiMapping(status = GuiMapping.StatusType.NA)
	private int id;

	@XmlIDREF
	@XmlElement(name = "processingNodeRef")
	@ManyToOne
	protected ProcessingNode processingNode;

	@XmlIDREF
	@XmlElement(name = "logicalServiceRef")	
	@ManyToOne
	protected LogicalService logicalService;

	// defaut access service acess type
    @GuiMapping(status = GuiMapping.StatusType.SKIPPED)
	private LogicalServiceAccessTypeEnum accessType = LogicalServiceAccessTypeEnum.NOT_APPLICABLE;

	/**
	 * Note: keep only processingNode that are browsed from from logical services.
	 */
	private static final String[] EXCLUDED_EQUALS_FIELDS = new String[] {"name", "sdf", "logicalService", "id"};
    private static final String[] EXCLUDED_EQUALS_DEEP_FIELDS = new String[] {"name", "sdf", "id"};

	/**
	 * Default constructor for mapping tools (Jaxb and Jpa)
	 */
	protected LogicalNodeServiceAssociation() {

	}

	/**
	 * Association constructor
	 */
	public LogicalNodeServiceAssociation(ProcessingNode node,
			LogicalService service) {
		this.processingNode = node;
		this.logicalService = service;
	}

	public LogicalServiceAccessTypeEnum getAccessType() {
		return accessType;
	}

	public void setAccessType(LogicalServiceAccessTypeEnum accessType) {
		this.accessType = accessType;
	}

	public ProcessingNode getProcessingNode() {
		return processingNode;
	}

	public LogicalService getLogicalService() {
		return logicalService;
	}

	public int getId() {
		return id;
	}

    public boolean areAssociated(ProcessingNode processingNode, LogicalService service) {

        boolean associated = false;

        if (processingNode.equals(this.processingNode) && service.equals(logicalService)) {
            associated = true;
        } else {
            associated = false;
        }

        return associated;
    }

	
	
	
	@Override
	 public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj, EXCLUDED_EQUALS_FIELDS);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this, EXCLUDED_EQUALS_FIELDS);
	}

	@Override
	public int compareTo(LogicalNodeServiceAssociation o) {
		return getId() - o.getId();
	}

    public boolean equalsDeep(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj, EXCLUDED_EQUALS_DEEP_FIELDS);
    }
    @Override
    public String toString() {
        return (new ReflectionToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE) {
            protected boolean accept(Field f) {
                return super.accept(f) && ! isFieldExcludedFromToString(f.getName());
            }
            protected Object getValue(Field field) throws IllegalArgumentException, IllegalAccessException {
                //Try to sort all of our collections so that toString() comparison is easier
                Class<?> type = field.getType();
                if (LogicalModelItem.class.isAssignableFrom(type)) {
                    LogicalModelItem item  = (LogicalModelItem) field.get(this.getObject());
                    return item.getLabel();
                }
                return field.get(this.getObject());
            }
        }).toString();
    }

    /**
     * Override this method in subclasses to specify the list of excluded to not display fields
     * (usually the same list as equals)
     */
    protected boolean isFieldExcludedFromToString(String fieldName) {
        for (String excludedEqualsField : LogicalNodeServiceAssociation.EXCLUDED_EQUALS_FIELDS) {
            if (excludedEqualsField.equals(fieldName)) {
                return true;
            }
        }
        return false;
    }

}