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
package com.francetelecom.clara.cloud.commons;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
 * Reference to a Maven Artifact
 */
@Embeddable
@XmlRootElement
public class MavenReference implements Serializable {

	/**
	 * Type, packaging and extension are often the same but not always. Type
	 * sources actually means classifier sources and extension jar for example.
	 * Packaging maven-plugin actually means extension jar.
	 * http://maven.40175.n5.nabble.com/standardized-Maven-GAV-URN-tp511480p512233.html
	 */
	private static final long serialVersionUID = 6454307533381753371L;

	private static Logger logger = LoggerFactory.getLogger(MavenReference.class.getName());

	@GuiMapping(status = GuiMapping.StatusType.SUPPORTED)
	private String groupId;

	@GuiMapping(status = GuiMapping.StatusType.SUPPORTED)
	private String artifactId;

	@GuiMapping(status = GuiMapping.StatusType.SUPPORTED)
	@Column(name="MVN_VERSION")
	private String version;

	private String extension = null;

	@GuiMapping(status = GuiMapping.StatusType.SUPPORTED)
	private String classifier = null;

	transient private String accessUrl;

	private static final String[] EXCLUDED_EQUALS_FIELDS = new String[] { "accessUrl" };

	/**
	 * JPA required default constructor
	 */
	public MavenReference() {
	}

	/**
	 * Copy constructor. Create a maven reference from another.
	 * 
	 * @param source Reference to copy
	 */
	public MavenReference(MavenReference source) {
		this.groupId = source.groupId;
		this.artifactId = source.artifactId;
		this.version = source.version;
		this.extension = source.extension;
		this.classifier = source.classifier;
		if (source.getAccessUrl() != null) {
			try {
				this.setAccessUrl(new URL(source.getAccessUrl().toExternalForm()));
			} catch (MalformedURLException e) {
				// Ignores if malformed
				logger.info("Malformed url detected while coping Maven reference. Ignoring accessUrl for " + source.toString());
			}
		}
	}

	public MavenReference(String groupId, String artifactId, String version) {
		this (groupId, artifactId, version, "jar");
	}

	public MavenReference(String groupId, String artifactId, String version, String extension) {
		this (groupId, artifactId, version, extension, null);
	}

	public MavenReference(String groupId, String artifactId, String version, String extension, String classifier) {
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.version = version;
		this.extension = extension;
		this.classifier = classifier;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getArtifactId() {
		return artifactId;
	}

	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * @deprecated invalid getter name, {@link #getExtension()}
	 */
	@Deprecated
	public String getType() {
		return extension;
	}

	/**
	 * @deprecated invalid setter name, {@link #setExtension()}
	 */
	@Deprecated
	public void setType(String extension) {
		this.extension = extension;
	}

	public String getExtension() {
		return extension;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}

	public String getClassifier() {
		return classifier;
	}

	public void setClassifier(String classifier) {
		this.classifier = classifier;
	}

	// TODO add JPA compatible equals and hashcode methods

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj, EXCLUDED_EQUALS_FIELDS);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this, EXCLUDED_EQUALS_FIELDS);
	}

	/**
	 * Display maven reference as a GAV reference
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder(64);
		result.append(toGavString());
		return result.toString();
	}

	/**
	 * Construct a MavenReference from a gav String
	 * @param gav
	 *            format : Group:artifact:version:[classifier:[extension]]
	 * @return Corresponding maven reference
	 */
	public static MavenReference fromGavString(String gav) {
		MavenReference resultMavenRef;

		if (gav == null || gav.length() < 5) {
			throw new IllegalArgumentException("String gav cannot be null nor empty");
		}
		StringTokenizer gavTokenizer = new StringTokenizer(gav, ":", true);
		try {
			String groupId = gavTokenizer.nextToken();
			if (":".equals(groupId)) {
				throw new NoSuchElementException("Missing groupId");
			}
			gavTokenizer.nextToken();
			String artifactId = gavTokenizer.nextToken();
			if (":".equals(artifactId)) {
				throw new NoSuchElementException("Missing artifactId");
			}
			gavTokenizer.nextToken();
			String version = gavTokenizer.nextToken();
			if (":".equals(version)) {
				throw new NoSuchElementException("Missing version");
			}
			if (gavTokenizer.hasMoreTokens()) {
				gavTokenizer.nextToken();
			}

			resultMavenRef = new MavenReference(groupId, artifactId, version);
			if (gavTokenizer.hasMoreTokens()) {
				String classifier = gavTokenizer.nextToken();
				if (!":".equals(classifier)) {
					resultMavenRef.setClassifier(classifier);
					if (gavTokenizer.hasMoreTokens()) {
						gavTokenizer.nextToken();
					}
				}
			}

			if (gavTokenizer.hasMoreTokens()) {
				String extension = gavTokenizer.nextToken();
				if (!":".equals(extension)) {
					resultMavenRef.setExtension(extension);
				}
			}

		} catch (NoSuchElementException nsee) {
			throw new IllegalArgumentException("Wrong format, should be groupId:artifactId:version:[classifier:[extension]]");
		}

		return resultMavenRef;

	}

	/**
	 * Return a generic identifier for the MavenReference
	 * Group:artifact:version:classifier:extension If groupId or version are
	 * null, then inherited is displayed. Other fields are displayed if present.
	 */
	public String toGavString() {
		StringBuilder result = new StringBuilder(64);
		result.append((getGroupId() == null) ? "[inherited]" : getGroupId());
		result.append(":");
		result.append(getArtifactId());
		result.append(":");
		result.append((getVersion() == null) ? "[inherited]" : getVersion());
		result.append(":");
		result.append((getClassifier() == null) ? "" : getClassifier());
		result.append(":");
		result.append((getExtension() == null) ? "" : getExtension());
		return result.toString();
	}

	/**
	 * modified setter (internal storage as String)
	 * 
	 * @return
	 */
	public URL getAccessUrl() {
		if (this.accessUrl == null) {
			return null;
		}
		try {
			return new URL(this.accessUrl);
		} catch (MalformedURLException e) {
			logger.error("internal error : wrong url format??");
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * modified setter Internal Storage as String to permit JPA mapping
	 * 
	 * @param accessUrl
	 */
	public void setAccessUrl(URL accessUrl) {
		if (accessUrl == null) {
			this.accessUrl = null;
		} else {
			this.accessUrl = accessUrl.toString();
		}
	}

	public MavenReference duplicateWithNull() {
		MavenReference result = new MavenReference(this);
		if (result.accessUrl != null && result.accessUrl.equals("")) {
			result.accessUrl = null;
		}
		if (result.artifactId != null && result.artifactId.equals("")) {
			result.artifactId = null;
		}
		if (result.classifier != null && result.classifier.equals("")) {
			result.classifier = null;
		}
		if (result.extension != null && result.extension.equals("")) {
			result.extension = null;
		}
		if (result.groupId != null && result.groupId.equals("")) {
			result.groupId = null;
		}
		if (result.version != null && result.version.equals("")) {
			result.version = null;
		}
		return result;
	}

	public MavenReference duplicateWithEmpty() {
		MavenReference result = new MavenReference(this);
		if (result.accessUrl == null) {
			result.accessUrl = "";
		}
		if (result.artifactId == null) {
			result.artifactId = "";
		}
		if (result.classifier == null) {
			result.classifier = "";
		}
		if (result.extension == null) {
			result.extension = "";
		}
		if (result.groupId == null) {
			result.groupId = "";
		}
		if (result.version == null) {
			result.version = "";
		}
		return result;
	}

	/**
	 * Similar to {link {@link #toGavString()}, except <li>null or "" are not
	 * display <li>separator is - instead of :
	 */
	public String getArtifactName() {
		StringBuilder result = new StringBuilder(64);
		if (getArtifactId() != null && getArtifactId().length() > 0) {
			result.append(getArtifactId());
		}
		if (getVersion() != null && getVersion().length() > 0) {
			result.append("-" + getVersion());
		}
		if (getClassifier() != null && getClassifier().length() > 0) {
			result.append("-" + getClassifier());
		}
		if (getExtension() != null && getExtension().length() > 0 && result.length() > 0) {
			result.append("." + getExtension());
		}
		return result.toString();
	}
}
