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
@TypeDefs( {
    @TypeDef(name = "encryptedString",
            typeClass = EncryptedStringType.class,
            parameters = {
                @Parameter(name = "encryptorRegisteredName",
                        value = "strongHibernateStringEncryptor")
            })
})
@javax.xml.bind.annotation.XmlSchema(
	namespace = "http://www.francetelecom.com/cloud", 
        xmlns = { @javax.xml.bind.annotation.XmlNs( prefix = "cloud",
                  namespaceURI = "http://www.francetelecom.com/cloud" ) },
	elementFormDefault = javax.xml.bind.annotation.XmlNsForm.QUALIFIED)

/**
 * The {@link com.francetelecom.clara.cloud.model} module describes physical model: virtual machines (),
 * networks ), operating systems (),
 * software products (), directories ({@link Directory}), subscriptions to external services (XaaS) ({@link XaasSubscription})
 *
 * This is grouped into a template for environments of the same type ({@link TechnicalDeploymentTemplate})
 * and for a specific environment instance {@link TechnicalDeploymentInstance}.
 */
package com.francetelecom.clara.cloud.model;


import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.jasypt.hibernate4.type.EncryptedStringType;


