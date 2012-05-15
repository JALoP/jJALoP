/*
 * Source code in 3rd-party is licensed and owned by their respective
 * copyright holders.
 *
 * All other source code is copyright Tresys Technology and licensed as below.
 *
 * Copyright (c) 2012 Tresys Technology LLC, Columbia, Maryland, USA
 *
 * This software was developed by Tresys Technology LLC
 * with U.S. Government sponsorship.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tresys.jalop.producer;

import static org.junit.Assert.assertTrue;

import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import mockit.Mock;
import mockit.MockUp;

import org.junit.Before;
import org.junit.Test;

import com.tresys.jalop.schemas.mil.dod.jalop_1_0.applicationmetadatatypes.LoggerType;

/**
 * Tests for the LoggerXML class.
 */
public class TestLoggerXML {

	private LoggerType logger;
	private LoggerXML loggerXML;

	@Before
	public void setup() throws Exception {
		logger = new LoggerType();
		loggerXML = new LoggerXML(logger);
	}

	@Test
	public void testGetLogger() throws Exception {
		assertTrue(loggerXML.getLogger().equals(logger));
	}

	@Test
	public void testPrepareSend() throws Exception {
		String hostName = "Test Host Name";
		String appName = "Test Application Name";
		loggerXML.prepareSend(hostName, appName);
		assertTrue(hostName.equals(loggerXML.getLogger().getHostname()));
		assertTrue(appName.equals(loggerXML.getLogger().getApplicationName()));
		assertTrue(loggerXML.getLogger().getTimestamp() != null);
		assertTrue(loggerXML.getJID() != null && !"".equals(loggerXML.getJID()));
	}

	@Test
	public void testPrepareSendSetLoggerFirst() throws Exception {
		LoggerType loggerType = new LoggerType();
		String hostName = "Test Host Name";
		String appName = "Test Application Name";
		GregorianCalendar gc = new GregorianCalendar();
		XMLGregorianCalendar xmlCal = DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);
		loggerType.setApplicationName(appName);
		loggerType.setHostname(hostName);
		loggerType.setTimestamp(xmlCal);
		LoggerXML xml = new LoggerXML(loggerType);
		xml.prepareSend("Another host name", "Another app name");
		assertTrue(hostName.equals(xml.getLogger().getHostname()));
		assertTrue(appName.equals(xml.getLogger().getApplicationName()));
		assertTrue(xmlCal.equals(xml.getLogger().getTimestamp()));
	}

	@Test(expected = DatatypeConfigurationException.class)
	public void testPrepareSendThrowsExceptionOnFailure() throws Exception {
		new MockUp<DatatypeFactory>() {
			@Mock
			DatatypeFactory newInstance() throws DatatypeConfigurationException {
				throw new DatatypeConfigurationException();
			}
		};
		loggerXML.prepareSend("host name", "application name");
	}

}
