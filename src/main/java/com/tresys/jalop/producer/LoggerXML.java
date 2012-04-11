/**
 * Class to be instantiated for logger data.
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

package com.tresys.jalop.producer;

import com.tresys.jalop.common.JALUtils;
import com.tresys.jalop.schemas.mil.dod.jalop_1_0.applicationmetadatatypes.LoggerType;

public class LoggerXML extends ApplicationMetadataXML {

	/**
	 * Constructor that takes a LoggerType and calls ApplicationMetadataXML's constructor
	 * 
	 * @param logger	the LoggerType to set
	 * @throws Exception
	 */
	public LoggerXML(LoggerType logger) throws Exception {
		super(logger);
	}

	/**
	 * Overridden from super to be public so LoggerType can be accessed outside of the package
	 * 
	 * @return	the LoggerType
	 */
	public LoggerType getLogger() {
		return super.getLogger();
	}

	/**
	 * Sets fields in the logger to prepare for sending the xml
	 * 
	 * @param hostName			the name of the host
	 * @param applicationName	the name of the application
	 * @throws Exception
	 */
	public void prepareSend(String hostName, String applicationName) throws Exception {
		LoggerType logger = getLogger();
		super.prepareSend(hostName, applicationName);
		if(logger.getTimestamp() == null) {
			logger.setTimestamp(JALUtils.getCurrentTime());
		}
		if(logger.getHostname() == null) {
			logger.setHostname(hostName);
		}
		if(logger.getApplicationName() == null) {	
			logger.setApplicationName(applicationName);
		}
	}

}