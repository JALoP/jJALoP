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

import static org.junit.Assert.*;

import java.util.GregorianCalendar;

import org.junit.Test;
import org.junit.Before;

import mockit.*;
import mockit.integration.junit4.*;

import com.tresys.jalop.common.JALUtils;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

public class TestJALUtils {
	JALUtils utils;
	
	@Before
	public void setup() {
		utils = new JALUtils();
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
}
