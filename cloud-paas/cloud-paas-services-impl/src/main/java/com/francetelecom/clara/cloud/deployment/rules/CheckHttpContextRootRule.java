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
package com.francetelecom.clara.cloud.deployment.rules;

import static com.francetelecom.clara.cloud.deployment.result.CriticityEnum.WARNING;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.francetelecom.clara.cloud.deployment.result.RuleValidationMessage;
import com.francetelecom.clara.cloud.logicalmodel.LogicalService;
import com.francetelecom.clara.cloud.logicalmodel.LogicalSoapService;
import com.francetelecom.clara.cloud.logicalmodel.LogicalWebGUIService;
import com.francetelecom.clara.cloud.logicalmodel.ProcessingNode;
import com.francetelecom.clara.cloud.services.dto.DeployableMavenArtifactDto;

@Component
public class CheckHttpContextRootRule implements CheckRule {

	private static Logger logger = LoggerFactory.getLogger(CheckHttpContextRootRule.class);

	private static final String APP_XML = "META-INF/application.xml";

	@Override
	public Set<RuleValidationMessage> validate(DeployableMavenArtifactDto providedArtifact, ProcessingNode node) throws TechnicalException {
		Set<RuleValidationMessage> messages = new HashSet<RuleValidationMessage>();
		if ("ear".equals(providedArtifact.getType())) {

			Set<String> providedContextRoots;
			try {
				providedContextRoots = getWebContextRootsFromEAR(new ByteArrayInputStream(providedArtifact.getBinaryContent()));
			} catch (Exception e) {
				providedContextRoots = new HashSet<String>();
				providedContextRoots.add("<no context root>");
			}
			logger.debug("Context roots from ear : {}", providedContextRoots);
			Set<String> modelContextRoots = getWebContextRootsFromLogicalModel(node);
			logger.debug("Context roots from logical model : {}", modelContextRoots);

			// All defined context root(s) in model should be provided in war(s)
			for (String modelContextRoot : modelContextRoots) {
				if (!providedContextRoots.contains(modelContextRoot)) {
					messages.add(new RuleValidationMessage("Deployment of [" + providedArtifact.getArtifactId() + "] not allowed : model context root [/"
							+ modelContextRoot + "] not found in provided war(s)", WARNING));
					logger.debug("Deployment of {} not allowed : model context root /{} not found in provided war(s)", providedArtifact.getArtifactId(), modelContextRoot);
				}
			}

			// All provided context roots should exist in logical model
			for (String providedContextRoot : providedContextRoots) {
				if (!modelContextRoots.contains(providedContextRoot)) {
					messages.add(new RuleValidationMessage("Deployment of [" + providedArtifact.getArtifactId() + "] not allowed : provided context root [/"
							+ providedContextRoot + "] not found in logical model", WARNING));
					logger.debug("Deployment of {} not allowed : provided context root /{} not found in logical model", providedArtifact.getArtifactId(), providedContextRoot);
				}
			}
		}
		return messages;
	}

	private Set<String> getWebContextRootsFromEAR(InputStream in) throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
		Set<String> contextRoots = new HashSet<String>();
		JarInputStream ear = new JarInputStream(in);
		JarEntry entry;
		while ((entry = ear.getNextJarEntry()) != null) {
			if (APP_XML.equals(entry.getName())) {
				contextRoots = getWebContextRootsFromDeploymentDescriptor(ear);
				break;
			}
		}
		return contextRoots;
	}

	private Set<String> getWebContextRootsFromDeploymentDescriptor(InputStream is) throws ParserConfigurationException, SAXException, IOException,
			XPathExpressionException {
		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = db.parse(is);
		XPath xpath = XPathFactory.newInstance().newXPath();
		NodeList nodes = (NodeList) xpath.evaluate("//context-root", doc, XPathConstants.NODESET);
		Set<String> contextRoots = new HashSet<String>();
		for (int i = 0; i < nodes.getLength(); i++) {
			// Strip leading / if any, because "/toto" and "toto" define same context root
			contextRoots.add(nodes.item(i).getTextContent().replaceFirst("/", ""));
		}
		return contextRoots;
	}

	private Set<String> getWebContextRootsFromLogicalModel(ProcessingNode node) {
		Set<String> modelContextRoots = new HashSet<String>();
		for (LogicalService service : node.listLogicalServices()) {
			// Only WSP and GUI have context root
			if (service instanceof LogicalWebGUIService) {
				// Strip leading / if any, because "/toto" and "toto" define same context root
				modelContextRoots.add(((LogicalWebGUIService) service).getContextRoot().getValue().replaceFirst("/", ""));
			} else if (service instanceof LogicalSoapService) {
				// Strip leading / if any, because "/toto" and "toto" define same context root
				modelContextRoots.add(((LogicalSoapService) service).getContextRoot().getValue().replaceFirst("/", ""));

			}
		}
		return modelContextRoots;
	}
}
