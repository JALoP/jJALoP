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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import mockit.Mock;
import mockit.MockUp;

import org.junit.Test;

import com.tresys.jalop.common.ConnectionHeader.MessageType;
import com.tresys.jalop.common.JALException;
import com.tresys.jalop.common.JALUtils;
import com.tresys.jalop.common.JALUtils.DMType;
import com.tresys.jalop.schemas.mil.dod.jalop_1_0.applicationmetadatatypes.LoggerType;

/**
 * Tests for the Producer class.
 */
public class TestProducer {

	@Test
	public void testProducerDefaultConstructorWorks() {
		Producer prod = new Producer();
		assertNotNull(prod);
	}

	@Test
	public void testProducerAppMetadataXMLConstructorWorks() throws Exception {
		LoggerType logger = new LoggerType();
		LoggerXML xml = new LoggerXML(logger);
		Producer prod = new Producer(xml);
		assertNotNull(prod);
		assertSame(xml, prod.getXml());
	}

	@Test
	public void testProducerAppMetaXMLAllParamsConstructorWorks() throws Exception {
		LoggerType logger = new LoggerType();
		LoggerXML xml = new LoggerXML(logger);
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
		KeyPair kp = kpg.generateKeyPair();
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		InputStream in = new FileInputStream("test-input/cert");
		X509Certificate cert = (X509Certificate)cf.generateCertificate(in);
		in.close();
		Producer prod = new Producer(xml, "hostname", "app_name", kp.getPrivate(), kp.getPublic(), cert, DMType.SHA256, "/path/to/socket");
		assertNotNull(prod);
		assertSame(xml, prod.getXml());
		assertEquals("hostname", prod.getHostName());
		assertEquals("app_name", prod.getApplicationName());
		assertSame(kp.getPrivate(), prod.getPrivateKey());
		assertSame(kp.getPublic(), prod.getPublicKey());
		assertSame(cert, prod.getCertificate());
		assertSame(DMType.SHA256, prod.getDigestMethod());
		assertEquals("/path/to/socket", prod.getSocketFile());
	}

	@Test
	public void testSetGetXml() throws Exception {
		LoggerType logger = new LoggerType();
		LoggerXML xml = new LoggerXML(logger);
		Producer prod = new Producer();
		prod.setXml(xml);
		assertSame(xml, prod.getXml());
	}

	@Test
	public void testSetGetHostName() {
		Producer prod = new Producer();
		prod.setHostName("hostname");
		assertEquals("hostname", prod.getHostName());
	}

	@Test
	public void testSetGetApplicationName() {
		Producer prod = new Producer();
		prod.setApplicationName("app_name");
		assertEquals("app_name", prod.getApplicationName());
	}

	@Test
	public void testSetGetPrivateKeyAndPublicKey() throws Exception {
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
		KeyPair kp = kpg.generateKeyPair();
		Producer prod = new Producer();
		prod.setPrivateKey(kp.getPrivate());
		prod.setPublicKey(kp.getPublic());
		assertSame(kp.getPrivate(), prod.getPrivateKey());
		assertSame(kp.getPublic(), prod.getPublicKey());
	}

	@Test
	public void testSetGetCertificate() throws Exception {
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		InputStream in = new FileInputStream("test-input/cert");
		X509Certificate cert = (X509Certificate)cf.generateCertificate(in);
		in.close();
		Producer prod = new Producer();
		prod.setCertificate(cert);
		assertSame(cert, prod.getCertificate());
	}

	@Test
	public void testSetGetDigestMethod() {
		Producer prod = new Producer();
		prod.setDigestMethod(DMType.SHA256);
		assertSame(DMType.SHA256, prod.getDigestMethod());
	}

	@Test
	public void testSetGetSocketFile() {
		Producer prod = new Producer();
		prod.setSocketFile("/path/to/socket");
		assertEquals("/path/to/socket", prod.getSocketFile());
	}

	@Test
	public void testJalpLogWithStringWorks() throws Exception {

		new MockUp<JALUtils>() {
			@Mock
			void processSend(Producer producer, String buffer) throws Exception {}
		};

		Producer prod = new Producer();
		prod.jalpLog("buffer");
		assertEquals(prod.getMessageType(), MessageType.JALP_LOG_MSG);
	}

	@Test
	public void testJalpLogWithFileWorks() throws Exception {

		new MockUp<JALUtils>() {
			@Mock
			void processSend(Producer producer, File file) throws Exception {}
		};

		Producer prod = new Producer();
		prod.jalpLog(new File("test-input/testBuffer"));
		assertEquals(prod.getMessageType(), MessageType.JALP_LOG_MSG);
	}

	@Test
	public void testJalpLogWorksWithBlankBuffer() throws Exception {
		new MockUp<JALUtils>() {
			@Mock
			void processSend(Producer producer, String buffer) throws Exception {
				assertEquals(buffer, "");
			}
		};

		Producer prod = new Producer();
		prod.jalpLog("");
	}

	@Test
	public void testJalpLogWorksWithNullBuffer() throws Exception {
		new MockUp<JALUtils>() {
			@Mock
			void processSend(Producer producer, String buffer) throws Exception {
				assertNull(buffer);
			}
		};

		Producer prod = new Producer();
		prod.jalpLog((String)null);
	}

	@Test
	public void testJalpLogWorksWithNullFile() throws Exception {
		new MockUp<JALUtils>() {
			@Mock
			void processSend(Producer producer, File file) throws Exception {
				assertNull(file);
			}
		};

		Producer prod = new Producer();
		prod.jalpLog((File)null);
	}

	@Test
	public void testJalpAuditWithStringWorks() throws Exception {

		new MockUp<JALUtils>() {
			@Mock
			void processSend(Producer producer, String buffer) throws Exception {}
		};

		Producer prod = new Producer();
		prod.jalpAudit("buffer");
		assertEquals(prod.getMessageType(), MessageType.JALP_AUDIT_MSG);
	}

	@Test
	public void testJalpAuditWithFileWorks() throws Exception {

		new MockUp<JALUtils>() {
			@Mock
			void processSend(Producer producer, File file) throws Exception {}
		};

		Producer prod = new Producer();
		prod.jalpAudit(new File("test-input/testBuffer"));
		assertEquals(prod.getMessageType(), MessageType.JALP_AUDIT_MSG);
	}

	@Test(expected = JALException.class)
	public void testJalpAuditThrowsExceptionWithNullBuffer() throws Exception {
		Producer prod = new Producer();
		prod.jalpAudit((String)null);
	}

	@Test(expected = JALException.class)
	public void testJalpAuditThrowsExceptionWithBlankBuffer() throws Exception {
		Producer prod = new Producer();
		prod.jalpAudit("");
	}

	@Test(expected = JALException.class)
	public void testJalpAuditThrowsExceptionWithNullFile() throws Exception {
		Producer prod = new Producer();
		prod.jalpAudit((File)null);
	}

	@Test
	public void testJalpJournalWithStringWorks() throws Exception {

		new MockUp<JALUtils>() {
			@Mock
			void processSend(Producer producer, String buffer) throws Exception {}
		};

		Producer prod = new Producer();
		prod.jalpJournal("buffer");
		assertEquals(prod.getMessageType(), MessageType.JALP_JOURNAL_MSG);
	}

	@Test
	public void testJalpJournalWithFileWorks() throws Exception {

		new MockUp<JALUtils>() {
			@Mock
			void processSend(Producer producer, File file) throws Exception {}
		};

		Producer prod = new Producer();
		prod.jalpJournal(new File("test-input/testBuffer"));
		assertEquals(prod.getMessageType(), MessageType.JALP_JOURNAL_MSG);
	}

	@Test(expected = JALException.class)
	public void testJalpJournalThrowsExceptionWithNullBuffer() throws Exception {
		Producer prod = new Producer();
		prod.jalpJournal((String)null);
	}

	@Test(expected = JALException.class)
	public void testJalpJournalThrowsExceptionWithBlankBuffer() throws Exception {
		Producer prod = new Producer();
		prod.jalpJournal("");
	}

	@Test(expected = JALException.class)
	public void testJalpJournalThrowsExceptionWithNullFile() throws Exception {
		Producer prod = new Producer();
		prod.jalpJournal((File)null);
	}

}