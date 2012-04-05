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

import java.io.File;
import static org.junit.Assert.*;

import java.util.GregorianCalendar;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import mockit.*;
import mockit.integration.junit4.*;

import org.junit.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.tresys.jalop.common.JALUtils;
import com.tresys.jalop.schemas.mil.dod.jalop_1_0.applicationmetadatatypes.ApplicationMetadataType;
import com.tresys.jalop.schemas.mil.dod.jalop_1_0.applicationmetadatatypes.LoggerType;
import com.tresys.jalop.schemas.mil.dod.jalop_1_0.applicationmetadatatypes.ObjectFactory;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

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
}
