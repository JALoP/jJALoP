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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import mockit.Mock;
import mockit.Mockit;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.apache.log4j.NDC;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.tresys.jalop.producer.JalopAppender;
import com.tresys.jalop.schemas.mil.dod.jalop_1_0.applicationmetadatatypes.LoggerType;

/**
 * Tests for log4j appender.
 */
public class TestJalopAppender {

	private static JalopAppender jalApp;
	private static TestAppender testApp;

	private static Logger log;
	private static String message = "This is the message";
	private static String hostname = "localhost";
	private static String appname = "thisApp";
	private static String mdc = "1234567";
	private static String ndc = "8912345";
	private static String privateKeyPath = "./path/to/private/key";
	private static String publicKeyPath = "./path/to/public/key";
	private static String certPath = "./path/to/cert";

	static class TestAppender extends AppenderSkeleton {
		public LoggingEvent event;

		public TestAppender() {

		}

		public void append(LoggingEvent event) {
			this.event = event;
		}

		@Override
		public void close() {

		}

		@Override
		public boolean requiresLayout() {
			return false;
		}
	}

	public static class MockProducer {
		@Mock
		public void jalpLog(String string) {
			assertTrue(string == null);
		}
	}

	@Before
	public void setup() {
		MDC.put("SessionID", mdc);
		NDC.push(ndc);
		jalApp = new JalopAppender();
		jalApp.setUseLocation(true);
		jalApp.setAppName(appname);
		jalApp.setHostName(hostname);
		log = Logger.getRootLogger();
		try {
			testApp = new TestAppender();
			assertTrue(testApp != null);
			log.addAppender(testApp);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@After
	public void teardown() {
		MDC.clear();
		NDC.clear();
		Mockit.tearDownMocks();
	}

	public LoggingEvent createLoggingEvent() {
		log.info(message);
		LoggingEvent ret = testApp.event;
		return ret;
	}

	@Test
	public void testCreateLoggerMetadata() throws Exception {

		Method method = JalopAppender.class.getDeclaredMethod(
				"createLoggerMetadata", LoggingEvent.class);
		method.setAccessible(true);

		LoggingEvent event = createLoggingEvent();
		ApplicationMetadataXML foo = null;
		try {
			foo = (ApplicationMetadataXML) method.invoke(jalApp, event);
		} catch (InvocationTargetException e) {
			System.out.println("Invocation Error");
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("Something Else");
			e.printStackTrace();
		}

		LoggerType temp = foo.getLogger();
		assertTrue(temp.getApplicationName().toString().equals(appname));
		assertTrue(temp.getHostname().equals(hostname));
		assertTrue(temp.getLocation().getStackFrame().toArray()[0] != null);
		assertTrue(temp.getLoggerName().toString().equals("LOG4J"));
		assertTrue(temp.getMappedDiagnosticContext().equals(mdc));
		assertTrue(temp.getMessage().equals(message));
		assertTrue(temp.getNestedDiagnosticContext().equals(ndc));
		assertTrue(temp.getSeverity().getName().equals("INFO"));
		assertTrue(temp.getSeverity().getValue().toString().equals("20000"));
		assertTrue(temp.getThreadID().equals("main"));
		assertTrue(temp.getTimestamp().toString() != null);
	}

	@Test
	public void testCreateProducer() {
		try {
			LoggingEvent event = createLoggingEvent();
			Method method = JalopAppender.class.getDeclaredMethod(
					"createLoggerMetadata", LoggingEvent.class);
			method.setAccessible(true);

			ApplicationMetadataXML foo = (ApplicationMetadataXML) method
					.invoke(jalApp, event);

			Method method2 = JalopAppender.class.getDeclaredMethod(
					"createProducer", ApplicationMetadataXML.class,
					String.class, String.class, String.class, String.class,
					String.class, String.class);
			method2.setAccessible(true);

			Producer prod = (Producer) method2.invoke(jalApp, foo,
					"path-to-socket", hostname, appname, null, null, null);

			assertTrue(prod.getXml().equals(foo));
			assertTrue(prod.getApplicationName().equals(appname));
			assertTrue(prod.getHostName().equals(hostname));
			assertTrue(prod.getSocketFile().equals("path-to-socket"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testAppend() {
		try {
			LoggingEvent event = createLoggingEvent();

			Mockit.setUpMock(Producer.class, new MockProducer());

			jalApp.append(event);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testSetGetPath() {
		jalApp.setPath("path");
		assertTrue(jalApp.getPath().equals("path"));
	}

	@Test
	public void testSetGetHostName() {
		jalApp.setHostName(hostname);
		assertTrue(jalApp.getHostName().equals(hostname));
	}

	@Test
	public void testSetGetAppName() {
		jalApp.setAppName(appname);
		assertTrue(jalApp.getAppName().equals(appname));
	}

	@Test
	public void testSetGetPrivateKeyPath() {
		jalApp.setPrivateKeyPath(privateKeyPath);
		assertTrue(jalApp.getPrivateKeyPath().equals(privateKeyPath));
	}

	@Test
	public void testSetGetPublicKeyPath() {
		jalApp.setPublicKeyPath(publicKeyPath);
		assertTrue(jalApp.getPublicKeyPath().equals(publicKeyPath));
	}

	@Test
	public void testSetGetCertPath() {
		jalApp.setCertPath(certPath);
		assertTrue(jalApp.getCertPath().equals(certPath));
	}

	@Test
	public void testSetGetUseLocation() {
		jalApp.setUseLocation(false);
		assertTrue(jalApp.getUseLocation() == false);

		jalApp.setUseLocation(true);
		assertTrue(jalApp.getUseLocation() == true);
	}

}
