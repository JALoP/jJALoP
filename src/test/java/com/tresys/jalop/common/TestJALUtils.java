/**
 * Tests for common utility class.
 * <p>
 * Source code in 3rd-party is licensed and owned by their respective
 * copyright holders.
 * <p>
 * All other source code is copyright Tresys Technology and licensed as below.
 * <p>
 * Copyright (c) 2012 Tresys Technology LLC, Columbia, Maryland, USA
 * <p>
 * This software was developed by Tresys Technology LLC
 * with U.S. Government sponsorship.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *    http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.tresys.jalop.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.GregorianCalendar;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import mockit.Capturing;
import mockit.Mock;
import mockit.MockUp;
import mockit.NonStrictExpectations;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.etsy.net.ConnectionHeader.MessageType;
import com.tresys.jalop.common.JALUtils.DMType;
import com.tresys.jalop.schemas.mil.dod.jalop_1_0.applicationmetadatatypes.ApplicationMetadataType;
import com.tresys.jalop.schemas.mil.dod.jalop_1_0.applicationmetadatatypes.LoggerType;
import com.tresys.jalop.schemas.mil.dod.jalop_1_0.applicationmetadatatypes.ObjectFactory;

public class TestJALUtils {

	private static JALUtils utils;
	private static ObjectFactory of;
	private ApplicationMetadataType amt;
	private LoggerType logger;
	private Document doc;
	
	@Before
	public void setup() {
		utils = new JALUtils();
		of = new ObjectFactory();
		amt = new ApplicationMetadataType();
		logger = new LoggerType();		
		
	}

	@Test
	public void testGetCurrentTimeWorks() throws Exception {
		GregorianCalendar gc = new GregorianCalendar();
		XMLGregorianCalendar xmlCal = DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);

		XMLGregorianCalendar date;
		date = utils.getCurrentTime();
		assertTrue(date != null);
		assertTrue(date instanceof XMLGregorianCalendar);
		assertTrue(date.getMonth() == xmlCal.getMonth());
		assertTrue(date.getDay() == xmlCal.getDay());
		assertTrue(date.getYear() == xmlCal.getYear());
		assertTrue(date.getEon() == xmlCal.getEon());
	}
	
	@Test(expected = DatatypeConfigurationException.class)
	public void testGetCurrentTimeThrowsExceptionOnFailure() throws Exception {
		new MockUp<DatatypeFactory>() {
			@Mock
			DatatypeFactory newInstance() throws DatatypeConfigurationException
			{
				throw new DatatypeConfigurationException();
			}
		};
		
		assertTrue(utils.getCurrentTime() == null);
	}
	
	@Test
	public void testMarshalWorks() throws Exception {
		amt.setJID("JID");
		amt.setLogger(logger);
		
		JAXBElement<ApplicationMetadataType> appMeta = of.createApplicationMetadata(amt);
		assertNotNull(appMeta);
		JAXBContext jc = JAXBContext.newInstance(ApplicationMetadataType.class.getPackage().getName());
		assertNotNull(jc);

		doc = utils.marshal(jc, appMeta);
		assertNotNull(doc);
		
		Node appMetaElem = doc.getElementsByTagName("ApplicationMetadata").item(0);
		assertNotNull(appMetaElem);
		assertEquals("ApplicationMetadata", appMetaElem.getNodeName());
		assertEquals("JID", appMetaElem.getAttributes().getNamedItem("JID").getNodeValue());
		Node loggerElem = appMetaElem.getFirstChild();
		assertNotNull(loggerElem);
		assertEquals("Logger", loggerElem.getNodeName());
		assertEquals(null, loggerElem.getNodeValue());
	}
	
	@Test(expected = JAXBException.class)
	public void testMarshalThrowsJAXBExceptionWhenMarshalFails() throws Exception {

		new NonStrictExpectations() {
			@Capturing Marshaller m;
			{
				m.marshal((Object)any, (Document)any); result = new JAXBException("error");
			}
		};
		
		JAXBElement<ApplicationMetadataType> appMeta = of.createApplicationMetadata(amt);
		assertNotNull(appMeta);
		JAXBContext jc = JAXBContext.newInstance(ApplicationMetadataType.class.getPackage().getName());
		assertNotNull(jc);
		
		doc = utils.marshal(jc, appMeta);
	}

	@Test(expected = JAXBException.class)
	public void testMarshalThrowsJAXBExceptionWhenSchemaValidationFails() throws Exception {

		JAXBElement<ApplicationMetadataType> appMeta = of.createApplicationMetadata(amt);
		assertNotNull(appMeta);
		JAXBContext jc = JAXBContext.newInstance(ApplicationMetadataType.class.getPackage().getName());
		assertNotNull(jc);

		doc = utils.marshal(jc, appMeta);
	}

	@Test
	public void testManifestWorks() throws Exception {

		amt.setJID("JID");
		amt.setLogger(logger);

		JAXBElement<ApplicationMetadataType> appMeta = of.createApplicationMetadata(amt);
		assertNotNull(appMeta);
		JAXBContext jc = JAXBContext.newInstance(ApplicationMetadataType.class.getPackage().getName());
		assertNotNull(jc);

		doc = utils.marshal(jc, appMeta);

		Method method = JALUtils.class.getDeclaredMethod("createManifest", Document.class, DMType.class, String.class, MessageType.class);

		method.setAccessible(true);
		method.invoke(utils, doc, DMType.SHA256, "buffer", MessageType.JALP_LOG_MSG);

		Element manifest = (Element)doc.getElementsByTagName("Manifest").item(0);
		assertNotNull(manifest);

		Element reference = (Element)doc.getElementsByTagName("Reference").item(0);
		String uriValue = reference.getAttribute("URI");
		assertEquals(uriValue, "jalop:payload");

		NodeList transforms = manifest.getElementsByTagName("Transform");
		assertTrue(transforms.getLength() == 0);

		Element digestMethod = (Element)manifest.getElementsByTagName("DigestMethod").item(0);
		String digestValue = digestMethod.getAttribute("Algorithm");
		assertEquals(digestValue, DigestMethod.SHA256);
	}

	@Test
	public void testManifestForAuditAddsTransforms() throws Exception {

		amt.setJID("JID");
		amt.setLogger(logger);

		JAXBElement<ApplicationMetadataType> appMeta = of.createApplicationMetadata(amt);
		assertNotNull(appMeta);
		JAXBContext jc = JAXBContext.newInstance(ApplicationMetadataType.class.getPackage().getName());
		assertNotNull(jc);

		doc = utils.marshal(jc, appMeta);

		Method method = JALUtils.class.getDeclaredMethod("createManifest", Document.class, DMType.class, String.class, MessageType.class);

		method.setAccessible(true);
		method.invoke(utils, doc, DMType.SHA256, "buffer", MessageType.JALP_AUDIT_MSG);

		Element manifest = (Element)doc.getElementsByTagName("Manifest").item(0);
		assertNotNull(manifest);

		Element transform = (Element)manifest.getElementsByTagName("Transform").item(0);
		assertNotNull(transform);
		String transformValue = transform.getAttribute("Algorithm");
		assertEquals(transformValue, "http://www.w3.org/TR/2001/REC-xml-c14n-20010315#WithComments");
	}

	@Test(expected = JALException.class)
	public void testManifestWithNullDMTypeThrowsException() throws Exception {

		Method method = JALUtils.class.getDeclaredMethod("createManifest", Document.class, DMType.class, String.class, MessageType.class);
		method.setAccessible(true);

		try{
			method.invoke(utils, doc, null, "buffer", MessageType.JALP_LOG_MSG);
		} catch(InvocationTargetException ite) {
			throw (Exception)ite.getCause();
		}
	}

	@Test(expected = JALException.class)
	public void testManifestWithNullMessageTypeThrowsException() throws Exception {

		Method method = JALUtils.class.getDeclaredMethod("createManifest", Document.class, DMType.class, String.class, MessageType.class);
		method.setAccessible(true);

		try{
			method.invoke(utils, doc, DMType.SHA256, "buffer", null);
		} catch(InvocationTargetException ite) {
			throw (Exception)ite.getCause();
		}
	}
}
