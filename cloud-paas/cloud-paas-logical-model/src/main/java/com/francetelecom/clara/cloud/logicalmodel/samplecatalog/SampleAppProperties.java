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
package com.francetelecom.clara.cloud.logicalmodel.samplecatalog;

import com.francetelecom.clara.cloud.commons.MavenReference;
import com.francetelecom.clara.cloud.commons.TechnicalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Utility class enabling to get references of applications used in examples of logical models
 * Properties are elements such as artifact maven references (ear,sql) or url context root
 * Properties shall be defined in paas-samples.properties file   
 */
public class SampleAppProperties {

	ResourceBundle mavenRefProps;

	private static Logger logger = LoggerFactory.getLogger(SampleAppProperties.class.getName());

	public SampleAppProperties() {
		String propsFile = "paas-samples";
		mavenRefProps = ResourceBundle.getBundle(propsFile);
		if(mavenRefProps == null) throw new TechnicalException("unable to load "+propsFile+".properties");
	}
	
	/**
	 * Get Maven reference of an application artifact
	 * If Maven reference can not be found a TechnicalException is thrown
	 * @param app application code
	 * @param type artifact type (ear, sql)
	 * @return MavenReference
	 */
	public MavenReference getMavenReference(String app, String type) {
		logger.debug("getting maven reference for "+app+ " "+type);
		
		String groupId = getProperty(app, type, "group");
		String artifactId = getProperty(app, type, "artifact");
		String version = getProperty(app, type, "version");
		String classifier = getProperty(app, type, "classifier");

		// this method throws a TechnicalException if one mandatory properties is not defined
		checkMavenProperties(app, type, groupId, artifactId, version, classifier);
		
		MavenReference mr = new MavenReference();

        mr.setGroupId(groupId);
        mr.setArtifactId(artifactId);
        mr.setVersion(version);
        mr.setType(type);
        mr.setClassifier(classifier);

		logger.debug("maven reference for "+app+ " "+type+": "+mr);

		return mr;
	}

	/**
	 * Get a property of an application
	 * First look for the property &lt;app&gt;.&lt;type&gt;.&lt;key&gt;
	 * If not found look for the default value defined by  default.&lt;type&gt;.&lt;key&gt;
	 * @param app application code
	 * @param type artifact type (ear, sql)
	 * @param key property type (e.g. version, context-root) 
	 * @return the property value or null if not found
	 */
	public String getProperty(String app, String type, String key) {
		try {
			return mavenRefProps.getString(app+"."+type+"."+key);
		} catch(MissingResourceException e) {}
		// try default
		try {
			return mavenRefProps.getString("default."+type+"."+key);
		} catch(MissingResourceException e) {
			return null;
		}
	}
	
	/**
	 * Check whether all maven properties could be read
	 * Throw a technicalException is one mandatory value is missing
	 * @param app
	 * @param type
	 * @param groupId
	 * @param artifactId
	 * @param version
	 * @param classifier
	 */
	private void checkMavenProperties(String app, String type, String groupId, String artifactId, String version, String classifier) {

		String missingProps = "";
		if(groupId == null) missingProps += ", group";
		if(artifactId == null) missingProps += ", artifact";
		if(version == null) missingProps += ", version";
		if( !missingProps.equals("")) {
			missingProps = missingProps.substring(2);
			String errorMessage = "some maven properties are not found for "+app+" "+type+": "+missingProps;
			logger.error(errorMessage);
			throw new TechnicalException(errorMessage);
		}
	}

}
