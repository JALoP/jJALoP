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

import mockit.*;

import org.junit.Before;
import org.junit.Test;

import com.tresys.jalop.schemas.mil.dod.jalop_1_0.applicationmetadatatypes.SyslogType;

/**
 * Tests for the SyslogXML class.
 */
public class TestSyslogXML {

	private SyslogType syslog;
	private SyslogXML syslogXML;

	@Before
	public void setup() throws Exception {
		syslog = new SyslogType();
		syslogXML = new SyslogXML(syslog);
	}

	@Test
	public void testGetSyslog() throws Exception {
		assertTrue(syslogXML.getSyslog().equals(syslog));
	}

	@Test
	public void testPrepareSend() throws Exception {
		String hostName = "Test Host Name";
		String appName = "Test Application Name";
		syslogXML.prepareSend(hostName, appName);
		assertTrue(hostName.equals(syslogXML.getSyslog().getHostname()));
		assertTrue(appName.equals(syslogXML.getSyslog().getApplicationName()));
		assertTrue(syslogXML.getSyslog().getTimestamp() != null);
		assertTrue(syslogXML.getJID() != null && !"".equals(syslogXML.getJID()));
	}

	@Test
	public void testPrepareSendSetSyslogFirst() throws Exception {
		SyslogType syslogType = new SyslogType();
		String hostName = "Test Host Name";
		String appName = "Test Application Name";
		GregorianCalendar gc = new GregorianCalendar();
		XMLGregorianCalendar xmlCal = DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);
		syslogType.setApplicationName(appName);
		syslogType.setHostname(hostName);
		syslogType.setTimestamp(xmlCal);
		SyslogXML xml = new SyslogXML(syslogType);
		xml.prepareSend("Another host name", "Another app name");
		assertTrue(hostName.equals(xml.getSyslog().getHostname()));
		assertTrue(appName.equals(xml.getSyslog().getApplicationName()));
		assertTrue(xmlCal.equals(xml.getSyslog().getTimestamp()));
	}

	@Test(expected = DatatypeConfigurationException.class)
	public void testPrepareSendThrowsExceptionOnFailure() throws Exception {
		new MockUp<DatatypeFactory>() {
			@Mock
			DatatypeFactory newInstance() throws DatatypeConfigurationException {
				throw new DatatypeConfigurationException();
			}
		};
		syslogXML.prepareSend("host name", "application name");
	}

}
