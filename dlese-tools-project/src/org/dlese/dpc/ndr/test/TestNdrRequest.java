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

package org.dlese.dpc.ndr.test;

import org.dlese.dpc.ndr.apiproxy.NDRConstants.NDRObjectType;
import org.dlese.dpc.ndr.request.*;
import org.dlese.dpc.xml.Dom4jUtils;
import org.dom4j.*;

/**
 *  NOT YET DOCUMENTED
 *
 * @author     Jonathan Ostwald
 * @version    $Id: TestNdrRequest.java,v 1.4 2009/03/20 23:33:53 jweather Exp $
 */
public class TestNdrRequest {

	private static boolean debug = true;


	/**
	 *  The main program for the TestNdrRequest class
	 *
	 * @param  args           The command line arguments
	 * @exception  Exception  NOT YET DOCUMENTED
	 */
	public static void main(String[] args) throws Exception {
		NdrRequest request = new NdrRequest();
		request.setHandle ("2200/test.20070713185856398T");
		request.setObjectType(NDRObjectType.METADATAPROVIDER);
		request.setVerb("modifyMetadataProvider");
		request.addNcsPropertyCmd("collectionId", "mynasastds");
		request.submit();
	}


	/**
	 *  Prints a dom4j.Node as formatted string.
	 *
	 * @param  node  NOT YET DOCUMENTED
	 */
	protected static void pp(Node node) {
		prtln(Dom4jUtils.prettyPrint(node));
	}


	/**
	 *  Description of the Method
	 *
	 * @param  s  Description of the Parameter
	 */
	private static void prtln(String s) {
		if (debug) {
			System.out.println(s);
		}
	}
}

