package com.tresys.jalop.producer;

import com.tresys.jalop.common.JALUtils;
import com.tresys.jalop.schemas.mil.dod.jalop_1_0.applicationmetadatatypes.SyslogType;

/**
 * Class to be instantiated for syslog data.
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
public class SyslogXML extends ApplicationMetadataXML {

	/**
	 * Constructor that takes a SyslogType and calls ApplicationMetadataXML's constructor
	 * 
	 * @param syslog	the SyslogType to set
	 * @throws Exception
	 */
	public SyslogXML(SyslogType syslog) throws Exception {
		super(syslog);
	}

	/**
	 * Overridden from super to be public so SyslogType can be accessed outside of the package
	 * 
	 * @return	the SyslogType
	 */
	public SyslogType getSyslog() {
		return super.getSyslog();
	}

	/**
	 * Sets fields in the syslog to prepare for sending the xml
	 * 
	 * @param hostName			the name of the host
	 * @param applicationName	the name of the application
	 * @throws Exception
	 */
	public void prepareSend(String hostName, String applicationName) throws Exception {
		SyslogType syslog = getSyslog();
		super.prepareSend(hostName, applicationName);
		if(syslog.getTimestamp() == null) {
			syslog.setTimestamp(JALUtils.getCurrentTime());
		}
		if(syslog.getHostname() == null) {
			syslog.setHostname(hostName);
		}
		if(syslog.getApplicationName() == null) {	
			syslog.setApplicationName(applicationName);
		}
	}

}