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
package com.francetelecom.clara.cloud.mvn.consumer.maven;

import com.francetelecom.clara.cloud.commons.MavenReference;
import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.francetelecom.clara.cloud.mvn.consumer.MvnConsumerConfigurer;
import com.francetelecom.clara.cloud.mvn.consumer.MvnRepoDaoImpl;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.DefaultModelWriter;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

public class PomGenerator {
	private static Logger logger = LoggerFactory.getLogger(MvnRepoDaoImpl.class);

	
	public PomGenerator(MvnConsumerConfigurer mvnConsumerConfigurer) {
	}

	
	public File generatePom(File projectDirectory, MavenReference mavenReference) throws IOException {
		if (projectDirectory == null) {
			throw new IllegalArgumentException("projectDirectory cannot be null");
		}
		if (mavenReference == null) {
			throw new IllegalArgumentException("mavenReference cannot be null");
		}
		if (!projectDirectory.exists()) {
			FileUtils.forceMkdir(projectDirectory);
		}

		Model project = new Model();

		project.setModelVersion("4.0.0");
		project.setGroupId(mavenReference.getGroupId());
        project.setArtifactId(mavenReference.getArtifactId());
        project.setVersion(mavenReference.getVersion());
        project.setPackaging(mavenReference.getExtension());
        project.setName(mavenReference.getArtifactId() + " (" +  mavenReference.getExtension() + ")" );

		String pom = modelToStringXml(project);
		logger.debug("generated pom : \n" + pom);

		File pomFile = new File(projectDirectory.getAbsolutePath() + "/pom.xml");
		FileUtils.writeStringToFile(pomFile, pom);

		return pomFile;
	}

	/**
	 * Generate a pom.xml file from a MavenProject description
	 * @return
	 */
	private String modelToStringXml(Model model) {

		DefaultModelWriter modelWriter = new DefaultModelWriter();

		StringWriter output = new StringWriter();
		String result = "";
		try {
			modelWriter.write(output, null, model);
			result = output.getBuffer().toString();
		} catch (IOException e) {
			logger.error("Cannot convert model to pom: " + e.getMessage());
			throw new TechnicalException("Cannot convert model to pom", e);
		}
		return result;
	}

}
