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

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import mockit.Mock;
import mockit.Mocked;
import mockit.Mockit;
import org.junit.Before;
import org.junit.Test;
import com.etsy.net.MessageHeader;
import com.etsy.net.UnixDomainSocket;
import com.etsy.net.UnixDomainSocket.UnixDomainSocketOutputStream;
import com.etsy.net.UnixDomainSocketClient;
import com.tresys.jalop.common.ConnectionHeader.MessageType;
import com.tresys.jalop.common.SendUtils;

public class TestSendUtils {

	private static SendUtils utils;
	
	@Before
	public void setup() {
		utils = new SendUtils();		
	}
	
	public static class MockUnixDomainSocketClient extends UnixDomainSocket {
		@Mock
		public void $init(String socketFile, int type) {
			out = new UnixDomainSocketOutputStream();
		}
	}

	public static class MockUnixDomainSocketOutputStream {
		@Mock
		public void sendmsg(MessageHeader header) {}
	}

	public static class MockUnixDomainSocket {
		@Mocked UnixDomainSocketOutputStream out;
		
		@Mock
		public void $init() {}
		
		@Mock
		public OutputStream getOutputStream() {
			UnixDomainSocketClient udsc;
			try {
				udsc = new UnixDomainSocketClient("socket", 1);
			} catch (Exception e) {
				return null;
			}
			out = udsc.new UnixDomainSocketOutputStream();
			return out;
		}
	}
	
	@Test(expected = Exception.class)
	public void testCreateAndSendHeadersFailNullInput() throws Exception {
		MessageType messageType = null;
		long dataLen = 0;
		long metaLen = 0;
		InputStream is = null;
		byte[] meta = null;
		String socketFile = null;
		
		Mockit.setUpMock(UnixDomainSocketClient.class, new MockUnixDomainSocketClient());
		Mockit.setUpMock(UnixDomainSocketOutputStream.class, new MockUnixDomainSocketOutputStream());
		Mockit.setUpMock(UnixDomainSocket.class, new MockUnixDomainSocket());
		
		try {
			messageType = MessageType.JALP_LOG_MSG;
			SendUtils.createAndSendHeaders(messageType, dataLen, metaLen, is, meta, socketFile);
		} catch (Exception e) {
			throw((Exception)e.getCause());
		}
		
	}
	
	@Test
	public void testCreateAndSendHeadersSuccessEmptyBuffer() throws Exception {
		MessageType messageType = null;
		long dataLen = 0;
		long metaLen = 0;
		InputStream is = new ByteArrayInputStream("".getBytes());
		byte[] meta = null;
		String socketFile = null;
		
		Mockit.setUpMock(UnixDomainSocketClient.class, new MockUnixDomainSocketClient());
		Mockit.setUpMock(UnixDomainSocketOutputStream.class, new MockUnixDomainSocketOutputStream());
		Mockit.setUpMock(UnixDomainSocket.class, new MockUnixDomainSocket());
		
		try {
			messageType = MessageType.JALP_LOG_MSG;
			SendUtils.createAndSendHeaders(messageType, dataLen, metaLen, is, meta, socketFile);
			messageType = MessageType.JALP_AUDIT_MSG;
			SendUtils.createAndSendHeaders(messageType, dataLen, metaLen, is, meta, socketFile);
			messageType = MessageType.JALP_JOURNAL_FD_MSG;
			SendUtils.createAndSendHeaders(messageType, dataLen, metaLen, is, meta, socketFile);
			messageType = MessageType.JALP_JOURNAL_MSG;
			SendUtils.createAndSendHeaders(messageType, dataLen, metaLen, is, meta, socketFile);
		} catch (Exception e) {
			throw((Exception)e.getCause());
		}
		
	}
	
	@Test
	public void testCreateAndSendHeadersSuccess() throws Exception {
		MessageType messageType = null;
		long dataLen = 5;
		long metaLen = 2;
		InputStream is = new ByteArrayInputStream("abcde".getBytes());
		byte[] meta = "ab".getBytes();
		String socketFile = null;
		
		Mockit.setUpMock(UnixDomainSocketClient.class, new MockUnixDomainSocketClient());
		Mockit.setUpMock(UnixDomainSocketOutputStream.class, new MockUnixDomainSocketOutputStream());
		Mockit.setUpMock(UnixDomainSocket.class, new MockUnixDomainSocket());
		
		try {
			messageType = MessageType.JALP_LOG_MSG;
			SendUtils.createAndSendHeaders(messageType, dataLen, metaLen, is, meta, socketFile);
			messageType = MessageType.JALP_AUDIT_MSG;
			SendUtils.createAndSendHeaders(messageType, dataLen, metaLen, is, meta, socketFile);
			messageType = MessageType.JALP_JOURNAL_FD_MSG;
			SendUtils.createAndSendHeaders(messageType, dataLen, metaLen, is, meta, socketFile);
			messageType = MessageType.JALP_JOURNAL_MSG;
			SendUtils.createAndSendHeaders(messageType, dataLen, metaLen, is, meta, socketFile);
		} catch (Exception e) {
			throw((Exception)e.getCause());
		}
	}
	
	@Test
	public void testCreateBreakHeaderSuccess() throws Exception {
		MessageHeader ret = null;
		Method method = SendUtils.class.getDeclaredMethod("createBreakHeader", null);
		method.setAccessible(true);
		ret = (MessageHeader) method.invoke(utils, null);
		assertTrue(ret.getIov()[0].equals("BREAK"));
		
	}
	
	@Test
	public void testCreateMetaHeaderSuccess() throws Exception {
		MessageHeader ret = null;
		Method method = SendUtils.class.getDeclaredMethod("createMetaHeader", byte[].class);
		method.setAccessible(true);
		ret = (MessageHeader) method.invoke(utils, "HEADER".getBytes());
		assertTrue(Arrays.equals((byte[]) ret.getIov()[0], "HEADER".getBytes()));
		assertTrue(ret.getIov()[1].equals("BREAK"));
	}
	
	
	
	@Test
	public void testCreateMetaHeaderEmptyBufferSuccess() throws Exception {
		MessageHeader ret = null;
		Method method = SendUtils.class.getDeclaredMethod("createMetaHeader", byte[].class);
		method.setAccessible(true);
		ret = (MessageHeader) method.invoke(utils, "".getBytes());
		assertTrue(Arrays.equals((byte[]) ret.getIov()[0],  "".getBytes()));
		assertTrue(ret.getIov()[1].equals("BREAK"));
	}
	
	@Test
	public void testCreateDataHeaderSuccess() throws Exception {
		MessageHeader ret = null;
		Method method = SendUtils.class.getDeclaredMethod("createDataHeader", byte[].class, int.class);
		method.setAccessible(true);
		ret = (MessageHeader) method.invoke(utils, "This is data".getBytes(), 12);
		assertTrue(Arrays.equals((byte[]) ret.getIov()[0], "This is data".getBytes()));
	}
	
	@Test
	public void testCreateDataHeaderEmptyBufferSuccess() throws Exception {
		MessageHeader ret = null;
		Method method = SendUtils.class.getDeclaredMethod("createDataHeader", byte[].class, int.class);
		method.setAccessible(true);
		ret = (MessageHeader) method.invoke(utils, "".getBytes(), 0);
		assertTrue(Arrays.equals((byte[]) ret.getIov()[0], "".getBytes()));
	}
	
	@Test
	public void testCreateDataHeaderLargeBufferSuccess() throws Exception {
		MessageHeader ret = null;
		byte[] bufferBytes = new byte[SendUtils.BUFFER_SIZE];
		FileInputStream is = new FileInputStream("test-input/evenBuffer");
		is.read(bufferBytes, 0, bufferBytes.length);
		
		Method method = SendUtils.class.getDeclaredMethod("createDataHeader", byte[].class, int.class);
		method.setAccessible(true);
		ret = (MessageHeader) method.invoke(utils, bufferBytes, bufferBytes.length);
		assertTrue(Arrays.equals((byte[]) ret.getIov()[0], bufferBytes));
	}
	
	@Test(expected = Exception.class)
	public void testCreateDataHeaderIncorrectBufferLengthFailure() throws Exception {
		Method method = SendUtils.class.getDeclaredMethod("createDataHeader", byte[].class, int.class);
		method.setAccessible(true);
		try {
			method.invoke(utils, "foo".getBytes(), 10);
		} catch (IndexOutOfBoundsException e) {
			throw((Exception)e.getCause());
		}
		assertTrue(false);
	}
}

