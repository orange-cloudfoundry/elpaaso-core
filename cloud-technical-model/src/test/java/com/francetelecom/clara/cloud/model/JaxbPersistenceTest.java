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

import com.francetelecom.clara.cloud.commons.MavenReference;
import com.francetelecom.clara.cloud.techmodel.cf.App;
import com.francetelecom.clara.cloud.techmodel.cf.Route;
import com.francetelecom.clara.cloud.techmodel.cf.RouteUri;
import com.francetelecom.clara.cloud.techmodel.cf.Space;
import com.francetelecom.clara.cloud.techmodel.cf.services.managed.ManagedService;
import com.thoughtworks.xstream.XStream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.xstream.XStreamMarshaller;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import static org.junit.Assert.assertEquals;

@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class JaxbPersistenceTest {

	private static Logger logger = LoggerFactory.getLogger(JaxbPersistenceTest.class.getName());

	@Autowired
	XStreamMarshaller marshaller;

	@Test
	public void should_marshall_app_to_xml() throws JAXBException {

		JAXBContext jc = JAXBContext.newInstance(App.class);
		Marshaller m = jc.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

		Space space = new Space();

		MavenReference mavenReference = MavenReference.fromGavString("foo.groupid:foo.artifactid:foo.version");

		App app = new App(space, mavenReference, "appName");

		m.marshal(app, System.out);
	}

	@Test
	public void should_marshall_managed_service_to_xml() throws JAXBException {
		JAXBContext jc = JAXBContext.newInstance(ManagedService.class);
		Marshaller m = jc.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

		Space space = new Space();

		m.marshal(new ManagedService("o-dbaas", "MYSQL_1G", "appName-db", space), System.out);
	}

	@Test
	public void should_marshall_route_to_xml() throws JAXBException {
		JAXBContext jc = JAXBContext.newInstance(Route.class);
		Marshaller m = jc.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

		Space space = new Space();

		m.marshal(new Route(new RouteUri("uri1"), null, space), System.out);
	}

	@Test
	public void testDumpMethod() {
		TechnicalDeploymentTestFactory technicalDeploymentTestFactory = new TechnicalDeploymentTestFactory();
		TechnicalDeployment xtd = technicalDeploymentTestFactory.createWicketJpaTD("springoo", "foo.groupid:foo.artifactid:foo.version");

		String dump = xtd.dumpXml();
		System.out.println(dump);
	}

	@Test
	public void testXstreamXmlPersistence() {
		TechnicalDeploymentTestFactory technicalDeploymentTestFactory = new TechnicalDeploymentTestFactory();

		TechnicalDeployment xtd = technicalDeploymentTestFactory.createWicketJpaTD("springoo", "foo.groupid:foo.artifactid:foo.version");

		// TODO: consider using Spring Marshaller abstraction to hide underling
		// marshalling library ?
		// See
		// http://static.springsource.org/spring/docs/3.0.x/reference/oxm.html

		XStream xstream = this.marshaller.getXStream();

		String xml = xstream.toXML(xtd);
		logger.debug("Xstream xml dump : \n" + xml);

		TechnicalDeployment xtd2 = (TechnicalDeployment) xstream.fromXML(xml);
		assertEquals(xtd.listXaasSubscriptionTemplates().size(), xtd2.listXaasSubscriptionTemplates().size());

		xstream.toXML(xtd2);
		// TODO fix this assertion
		// assertEquals(xml, xml2);
	}
}
