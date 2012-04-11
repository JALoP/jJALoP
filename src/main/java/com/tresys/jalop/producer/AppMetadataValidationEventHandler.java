/**
 * This class handles errors found when validating against the schema.
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
package com.tresys.jalop.producer;

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;

public class AppMetadataValidationEventHandler implements ValidationEventHandler {

	/**
	 * Handles validation errors found when validating against the schema. 
	 * This will print the error and the location.
	 * 
	 * @param event 	the ValidationEvent that threw the error
	 * @return			<code>true</code> to continue with the operation;
	 * 					<code>false</code> to stop the operation
	 */
	public boolean handleEvent(ValidationEvent event) {
		System.out.println("\nEVENT");
		System.out.println("SEVERITY:  " + event.getSeverity());
		System.out.println("MESSAGE:  " + event.getMessage());
		System.out.println("LINKED EXCEPTION:  " + event.getLinkedException());
		if(event.getLocator() != null) {
			System.out.println("LOCATOR");
			System.out.println("    LINE NUMBER:  " + event.getLocator().getLineNumber());
			System.out.println("    COLUMN NUMBER:  " + event.getLocator().getColumnNumber());
			System.out.println("    OFFSET:  " + event.getLocator().getOffset());
			System.out.println("    OBJECT:  " + event.getLocator().getObject());
		}
		
		return false;
	}

}