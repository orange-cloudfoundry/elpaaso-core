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

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Base class for all Cloud management persistant
 * 
 * @author APOG7416
 * 
 */

@XmlRootElement
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "CORE_ITEM")
public abstract class CoreItem extends Entite {

	@XmlAttribute
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	/**
	 * Constructeur vide
	 */
	protected CoreItem() {
	}

	/**
	 * Constructeur
	 * 
	 * @param uid
	 */
	public CoreItem(String uid) {
		super.setUID(uid);
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

}
