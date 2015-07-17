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

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This abstract class factors out the generic behaviors common to all of the persistence model classes.
 *
 * TODO Factorisation :
 * - du champ version pour le verouillage optimiste
 * - du champ nom
 * - de la methode toString() pour l'affichage du contenu d'un objet persistant
 * 
 * @author Clara
 */

@XmlAccessorType(XmlAccessType.FIELD)
@MappedSuperclass
public abstract class LogicalEntity implements Serializable {

	@XmlTransient
	@Transient
    @GuiMapping(status = GuiMapping.StatusType.NA)
	private SimpleDateFormat sdf;

	/**
	 * version for optimistic lock Be careful : no setter/getter on version,
	 * only jpa is able to set/get versions
	 */
	@XmlAttribute
	@Version
    @GuiMapping(status = GuiMapping.StatusType.NA)
	protected int version;

	/**
	 * Human readeable name of a logical entity.
     *
     * Should be unique within the logical deployment scope.
     * Note: Watch subclasses for javadoc comments about subclasses that require different {@link GuiMapping} for this field such as {@link LogicalDeployment}
     *
     * This maps to bug #80111, mingle #1344
	 */
	@XmlID
	@XmlAttribute
	@Column(unique = true, length = 150)
	@NotNull
    @GuiMapping(status = GuiMapping.StatusType.SKIPPED)
	protected String name;

	protected static final String[] EXCLUDED_EQUALS_FIELDS = new String[] {"name", "sdf", "version"};
	
	
	@Override
	 public boolean equals(Object obj) {
		//See http://commons.apache.org/lang/api-2.5/org/apache/commons/lang/builder/EqualsBuilder.html
		return EqualsBuilder.reflectionEquals(this, obj, EXCLUDED_EQUALS_FIELDS);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this, EXCLUDED_EQUALS_FIELDS);
	}
	
    public String toString() {
            return (new ReflectionToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE) {
                protected boolean accept(Field f) {
                    return super.accept(f) && ! isFieldExcludedFromToString(f.getName());
                }
                protected Object getValue(Field field) throws IllegalArgumentException, IllegalAccessException {
                    //Try to sort all of our collections so that toString() comparison is easier
                    Class<?> type = field.getType();
                    if (List.class.isAssignableFrom(type)) {
                        List list = (List) field.get(this.getObject());
                        List sortedList = new ArrayList(list);
                        Collections.sort(sortedList);
                        return sortedList;
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
        return isFieldExcludedFromToString(fieldName, LogicalEntity.EXCLUDED_EQUALS_FIELDS);
    }

    /**
     * Utility method to define fields which should be ommited from {@link #toString}. This is called
     * by subclasses.
     */
    protected boolean isFieldExcludedFromToString(String fieldName, String[] excludedEqualsFields) {
        for (String excludedEqualsField : excludedEqualsFields) {
            if (excludedEqualsField.equals(fieldName)) {
                return true;
            }
        }
        return false;
    }

    /**
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * only used by LogicalDeployment
	 * @param name
	 */
//	protected final void setName(String name) {
//        // TODO : once test will be OK uncomment this line and modify method visibility to protected
//        if (this.name != null) throw new IllegalArgumentException("name is supposed to be immutable (current is "+this.name+")");
//		this.name = name;
//	}


}
