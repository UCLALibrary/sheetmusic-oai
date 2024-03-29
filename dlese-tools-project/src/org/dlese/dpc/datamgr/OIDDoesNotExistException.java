/*
 *  License and Copyright:
 *
 *  The contents of this file are subject to the Educational Community License v1.0 (the "License"); you may
 *  not use this file except in compliance with the License. You should have received a copy of the License
 *  along with this software; if not, you may obtain a copy of the License at
 *  http://www.opensource.org/licenses/ecl1.php.
 *
 *  Software distributed under the License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF ANY KIND,
 *  either express or implied. See the License for the specific language governing rights and limitations
 *  under the License.
 *
 *  Copyright 2002-2009 by Digital Learning Sciences, University Corporation for Atmospheric Research (UCAR).
 *  All rights reserved.
 */

package org.dlese.dpc.datamgr;

import java.lang.*;

/**
 * Indicates that a DataManager was asked to handle an object of data 
 * associated an OID that does not exist. 
 *
 * @author	Dave Deniman
 * @version	1.0,	9/30/02
 */
public class OIDDoesNotExistException extends Exception {

	/**
	 * Constructs an <code>Exception</code> with no specified detail message. 
	 */
	public OIDDoesNotExistException() {
		super();
	}
	
	/**
	 * Constructs an <code>Exception</code> with the specified detail message. 
	 *
	 * @param message   the detailed message.
	 */
	public OIDDoesNotExistException(String message) {
		super(message);
	}
	
}
