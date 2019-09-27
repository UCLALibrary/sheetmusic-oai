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

package org.dlese.dpc.schemedit.standards.asn;

import org.dlese.dpc.schemedit.standards.StandardsManager;
import org.dlese.dpc.schemedit.standards.StandardsRegistry;
import org.dlese.dpc.schemedit.standards.adn.util.MappingUtils;

import org.dlese.dpc.standards.asn.AsnDocument;

import org.dlese.dpc.xml.schema.*;
import org.dlese.dpc.schemedit.*;

import java.io.*;
import java.util.*;

import java.net.*;

/**
 *  Extends AsnStandardsManager by exposing a setXmlFormat call, which is necessary to the res_qual scheme.
 *
 * @author    ostwald
 */

public class ResQualStandardsManager extends AsnStandardsManager {

	private static boolean debug = false;
	
	/**
	 *  Constructor for the ResQualStandardsManager object
	 *
	 * @param  schemaHelper   the res_qual schemaHelper
	 * @param  xpath          xpath of instanceDoc to which standards are assigned
	 * @param  source         AsnDocument file
	 * @exception  Exception  if AsnDocument file cannot be processed
	 */
	 
	public ResQualStandardsManager(String xmlFormat, String xpath, File source) throws Exception {
		super (xmlFormat, xpath, source);
		prtln("Instantiated ResQual StandardsManager");
	}
	
	private static void prtln(String s) {
		if (debug) {
			SchemEditUtils.prtln(s, "ResQualHelper");
		}
	}
	
}
