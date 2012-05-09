/**
 * Holds connection info for sending using sendmsg
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
 */

package com.tresys.jalop.common;

public class ConnectionHeader {

	short protocolVersion;
	short messageType;
	long dataLen;
	long metaLen;

	/**
	 * Constructor
	 *
	 * @param protocolVersion	a short which is the protocol version
	 * @param messageType		the MessageType
	 * @param dataLen			a long which is the length of the data to be sent
	 * @param metaLen			a long which is the length of the application metadata to be sent
	 */
	public ConnectionHeader(short protocolVersion, MessageType messageType,
			long dataLen, long metaLen) {
		this.protocolVersion = protocolVersion;
		this.messageType = messageType.type();
		this.dataLen = dataLen;
		this.metaLen = metaLen;
	}

	/**
	 * Gets the protocolVersion
	 *
	 * @return	the protocolVersion property
	 */
	public short getProtocolVersion() {
		return protocolVersion;
	}

	/**
	 * Sets the protocolVersion
	 *
	 * @param protocolVersion	the short to set
	 */
	public void setProtocolVersion(short protocolVersion) {
		this.protocolVersion = protocolVersion;
	}

	/**
	 * Gets the messageType
	 *
	 * @return	the messageType property
	 */
	public short getMessageType() {
		return messageType;
	}

	/**
	 * Sets the messageType property to the int type of the MessageType
	 *
	 * @param messageType	the MessageType
	 */
	public void setMessageType(MessageType messageType) {
		this.messageType = messageType.type();
	}

	/**
	 * Gets the dataLen
	 *
	 * @return	the dataLen property
	 */
	public long getDataLen() {
		return dataLen;
	}

	/**
	 * Sets the dataLen
	 *
	 * @param dataLen	the long to set
	 */
	public void setDataLen(long dataLen) {
		this.dataLen = dataLen;
	}

	/**
	 * Gets the metaLen
	 *
	 * @return	the metaLen property
	 */
	public long getMetaLen() {
		return metaLen;
	}

	/**
	 * Sets the metaLen
	 *
	 * @param metaLen	the long to set
	 */
	public void setMetaLen(long metaLen) {
		this.metaLen = metaLen;
	}

	/**
	 * An enum for the different message types
	 */
	public enum MessageType {
		JALP_LOG_MSG	((short)1),
		JALP_AUDIT_MSG	((short)2),
		JALP_JOURNAL_MSG	((short)3),
		JALP_JOURNAL_FD_MSG	((short)4);

		private short type;

		MessageType(short type) {
			this.type = type;
		}

		private short type() { return type; }
	}

}