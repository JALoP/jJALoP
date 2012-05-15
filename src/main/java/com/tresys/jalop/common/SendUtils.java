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

package com.tresys.jalop.common;

import java.io.InputStream;

import com.etsy.net.JUDS;
import com.etsy.net.MessageHeader;
import com.etsy.net.UnixDomainSocket.UnixDomainSocketOutputStream;
import com.etsy.net.UnixDomainSocketClient;
import com.tresys.jalop.common.ConnectionHeader.MessageType;

/**
 * Class containing send utility functions.
 */
public class SendUtils {

	public static final String JALP_BREAK_STR = "BREAK";
	public static final int BUFFER_SIZE = 102400;

	/**
	 * Connects to the socket, creates the MessageHeaders and sends them with sendmsg.
	 *
	 * @param messageType	the MessageType
	 * @param dataLen		a long which is the length of the data
	 * @param metaLen		a long which is the length of the metadata
	 * @param is			the data as an InputStream
	 * @param meta			a byte array which is the metadata
	 * @param socketFile	a String which contains the path to the socket file
	 * @throws Exception
	 */
	public static void createAndSendHeaders(MessageType messageType,
											long dataLen,
											long metaLen,
											InputStream is,
											byte[] meta,
											String socketFile) throws Exception {

		UnixDomainSocketClient socket = new UnixDomainSocketClient(socketFile,
				JUDS.SOCK_STREAM);

		UnixDomainSocketOutputStream out = (UnixDomainSocketOutputStream)socket.getOutputStream();

		ConnectionHeader connectionHeader = new ConnectionHeader((short)1, messageType, dataLen, metaLen);

		// Create MessageHeader with ConnectionHeader info - send
		out.sendmsg(createHeader(connectionHeader));

		// If messageType != 4 (fd) create MessageHeader with data info - send
		if(MessageType.JALP_JOURNAL_FD_MSG != messageType) {

			if(is != null) {
				byte[] bufferBytes = new byte[BUFFER_SIZE];
				int read;

				while((read = is.read(bufferBytes, 0, bufferBytes.length)) > 0) {
					MessageHeader dataHeader = createDataHeader(bufferBytes, read);
					out.sendmsg(dataHeader);
				}
			}
			out.sendmsg(createBreakHeader());
		}

		// Create MessageHeader with meta info - send
		out.sendmsg(createMetaHeader(meta));

		socket.close();
	}

	/**
	 * Creates a MessageHeader object with the info from connectionHeader in iov.
	 *
	 * @param connectionHeader	A ConnectionHeader object filled in with the correct data
	 * @return the MessageHeader object
	 */
	private static MessageHeader createHeader(ConnectionHeader connectionHeader) {

		Object[] iov = new Object[4];
		iov[0] = connectionHeader.getProtocolVersion();
		iov[1]= connectionHeader.getMessageType();
		iov[2] = connectionHeader.getDataLen();
		iov[3] = connectionHeader.getMetaLen();

		MessageHeader mh = new MessageHeader();
		mh.setIov(iov);

		return mh;
	}

	/**
	 * Creates a MessageHeader object with a byte[] buffer and corrects the byte[] size if necessary.
	 *
	 * @param bufferBytes	A byte array which is put into iov
	 * @param length		An int which is the length of bytes read into the buffer.
	 * @return the MessageHeader object
	 */
	private static MessageHeader createDataHeader(byte[] bufferBytes, int length) {

		Object[] iov = new Object[1];

		if (length != BUFFER_SIZE) {
			byte[] lastBuffer = new byte[length];
			System.arraycopy(bufferBytes, 0, lastBuffer, 0, length);
			bufferBytes = lastBuffer;
		}
		iov[0] = bufferBytes;

		MessageHeader mh = new MessageHeader();
		mh.setIov(iov);

		return mh;
	}

	/**
	 * Creates a MessageHeader object and with the meta byte[] and JALP_BREAK_STR in iov.
	 *
	 * @param meta	A byte array with the metadata
	 * @return the MessageHeader object
	 */
	private static MessageHeader createMetaHeader(byte[] meta) {
		Object[] iov;
		if(meta != null) {
			iov = new Object[2];
			iov[0] = meta;
			iov[1] = JALP_BREAK_STR;
		} else {
			iov = new Object[1];
			iov[0] = JALP_BREAK_STR;
		}

		MessageHeader mh = new MessageHeader();
		mh.setIov(iov);

		return mh;
	}

	/**
	 * Create a MessageHeader with the JALP_BREAK_STR in iov.
	 *
	 * @return the MessageHeader object
	 */
	private static MessageHeader createBreakHeader() {

		Object[] iov = new Object[1];
		iov[0] = JALP_BREAK_STR;

		MessageHeader mh = new MessageHeader();
		mh.setIov(iov);

		return mh;
	}

}