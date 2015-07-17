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

import java.io.Serializable;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlTransient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cette classe abstraite factorise des comportements g�n�riques � l'ensemble
 * des classes du mod�le de persistance. Factorisation :
 * - du champ version pour le v�rouillage optimiste
 * - du champ nom
 * - de la m�thode toString() pour l'affichage du contenu d'un objet persistant
 * 
 * @author Clara
 */

@XmlAccessorType(XmlAccessType.FIELD)
@MappedSuperclass
public abstract class Entite implements Serializable {

    private static Logger logger=LoggerFactory.getLogger(Entite.class.getName());

	/**
	 * 
	 */
	private static final long serialVersionUID = -4215134861806088973L;

	@XmlTransient
	@Transient
	private SimpleDateFormat sdf;

	/**
	 * numero de version pour gestion du lock optimist
	 */
	@XmlAttribute	
	@Version
	protected int version;

	/**
	 * Nom
	 */
	@XmlAttribute	
	@XmlID
	@Column(unique = true, length = 250)
	@NotNull
	protected String name;

	public int getVersion() {
		return this.version;
	}

	/**
	 * @param version
	 *            The version to set.
	 */
	public void setVersion(int version) {
		this.version = version;
	}



	/*
	 * Le nom de l'objet est discriminant. Il ne doit pas y avoir deux
	 * enregistrement en base de donn�es de m�me nom.
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if ((obj == null) || (obj.getClass() != this.getClass()))
			return false;
		// object must be Test at this point
		Entite test = (Entite) obj;
        //  BVA : FIX NPE when this.name is null (ie hibernate rollback case)
        String testName = test.getName();
        if ((testName == null) && getName() == null)
            return true;
        return testName.equals(getName());
	}

	/*
	 * Le nom de l'objet est discriminant. Il ne doit pas y avoir deux
	 * enregistrement en base de donn�es de m�me nom.
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return name.hashCode();
	}

	/**
	 * affiche le r�sultat de tous les getters de type String, Date, int,
	 * boolean (par introspection)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer(500);
		String className = getClass().getName();
		sb.append(className.substring(className.lastIndexOf('.') + 1));
		sb.append(" - ");
		boolean first = true;
		Method[] m = getClass().getMethods();
		for (int i = 0; i < m.length; i++) {
			String name = m[i].getName();
			if (name.startsWith("get") 
					&& !name.toLowerCase().contains("password")
					&& (String.class.equals(m[i].getReturnType())
							|| Date.class.equals(m[i].getReturnType())
							|| float.class.equals(m[i].getReturnType())
							|| boolean.class.equals(m[i].getReturnType())
							|| long.class.equals(m[i].getReturnType())							
							|| int.class.equals(m[i].getReturnType()))) {
				if (!first) {
					sb.append(", ");
				}
				first = false;
				sb.append(name.substring(3));
				sb.append(": ");
				try {
					Object result = m[i].invoke(this, new Object[0]);
					if (result instanceof Date) {
						if(sdf == null){
							sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
						}
						result = sdf.format(result);
					}
					sb.append(result);
				} catch (IllegalAccessException e) {
					sb.append("(IllegalAccessException)");
				} catch (InvocationTargetException e) {
					// �a se produirait si on n'a pas
					// m[i].getParameterTypes().length==0
					// (getter avec argument)
					sb.append("(InvocationTargetException)");
				} catch (IllegalArgumentException e ) {
                    sb.append("(IllegalArgumentException)");
                }
			}
		}
		return sb.toString();
	}

	/**
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * 
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

    /**
     * gets an XML representation of this entity
     * @return
     */
    public String dumpXml() {
        try {
            return dumpXmlWithExceptions();
        } catch (JAXBException e) {
            logger.error("Unable to marshall model.", e);
            //throw new TechnicalException(e);
            return "ERROR MARSHALLING TD";
        }
    }


    /**
     * gets an XML representation of this entity
     * @return
     */
    public String dumpXmlWithExceptions() throws JAXBException {

        logger.debug("dumping xml model for " + this);
        JAXBContext jc;
            jc = JAXBContext.newInstance(this.getClass());
            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            StringWriter stringWriter = new StringWriter();

            m.marshal(this, stringWriter);
            return stringWriter.toString();
    }
}
