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
package com.francetelecom.clara.cloud.coremodel;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

/**
 * Cette classe abstraite factorise des comportements generiques a l'ensemble
 * des classes du modele de persistance. Factorisation : - du champ version pour
 * le verouillage optimiste - du champ nom - de la methode toString() pour
 * l'affichage du contenu d'un objet persistant
 * 
 * @author Clara
 * 
 *         TODO: translate javadoc in french and rename into Entity
 */

@XmlType(name = "coreEntite")
@XmlAccessorType(XmlAccessType.FIELD)
@MappedSuperclass
public abstract class Entite implements Serializable {

	@XmlTransient
	@Transient
	private SimpleDateFormat sdf;

	/**
	 * numero de version pour gestion du lock optimist
	 */
	@XmlAttribute
	@Version
	private int version;

	/**
	 * Nom
	 */
	@XmlAttribute
	@XmlID
	@Column(unique = true, length = 150, name = "name")
	@NotNull
	private String uid;

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
	 * enregistrement en base de donnees de meme nom.
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if ((obj == null) || (obj.getClass() != this.getClass()))
			return false;
		// object must be Test at this point
		Entite test = (Entite) obj;
		return uid.equals(test.uid);
	}

	/*
	 * Le nom de l'objet est discriminant. Il ne doit pas y avoir deux
	 * enregistrement en base de donnees de meme nom.
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return uid.hashCode();
	}

	/**
	 * affiche le resultat de tous les getters de type String, Date, int,
	 * boolean (par introspection)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(500);
		String className = getClass().getName();
		sb.append(className.substring(className.lastIndexOf('.') + 1));
		sb.append(" - ");
		boolean first = true;
		Method[] m = getClass().getMethods();
		for (int i = 0; i < m.length; i++) {
			String name = m[i].getName();
			if (name.startsWith("get")
					&& (String.class.equals(m[i].getReturnType()) || Date.class.equals(m[i].getReturnType()) || float.class.equals(m[i].getReturnType())
							|| boolean.class.equals(m[i].getReturnType()) || long.class.equals(m[i].getReturnType()) || int.class.equals(m[i].getReturnType()))) {
				if (!first) {
					sb.append(", ");
				}
				first = false;
				sb.append(name.substring(3));
				sb.append(": ");
				try {
					Object result = m[i].invoke(this, new Object[0]);
					if (result instanceof Date) {
						if (sdf == null) {
							sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
						}
						result = sdf.format(result);
					}
					sb.append(result);
				} catch (IllegalAccessException e) {
					sb.append("(IllegalAccessException)");
				} catch (InvocationTargetException e) {
					// ca se produirait si on n'a pas
					// m[i].getParameterTypes().length==0
					// (getter avec argument)
					sb.append("(InvocationTargetException)");
				}
			}
		}
		return sb.toString();
	}

	public void setUID(String uid) {
		this.uid = uid;
	}

	public String getUID() {
		return uid;
	}

}
