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

package org.dlese.dpc.schemedit.test;

import org.dlese.dpc.standards.asn.*;
import org.dlese.dpc.schemedit.standards.asn.*;

import org.dlese.dpc.schemedit.SchemEditUtils;
import org.dlese.dpc.schemedit.standards.*;
import org.dom4j.*;
import org.dlese.dpc.xml.Dom4jUtils;
import org.dlese.dpc.xml.schema.SchemaHelper;
import java.util.*;
import java.io.File;
import java.net.*;

public class ASNTester {
	
	private static boolean debug = true;
	
	public static void main (String[] args) throws Exception {

		String urlStr = "http://purl.org/ASN/resources/S101996E";
		URL url = new URL (urlStr);
		NameSpaceXMLDocReader reader = new NameSpaceXMLDocReader (url);
		// pp (reader.getDocument());
		List nodes = reader.getNodes ("/rdf:RDF/rdf:Description[@rdf:about]");
		prtln (nodes.size() + " nodes found");
		for (Iterator i=nodes.iterator();i.hasNext();) {
			Node node = (Node)i.next();
			prtln (((Element)node).attributeValue(reader.getQName("rdf:about")));
		}
	}
	
	private static void pp (Node node) {
		prtln (Dom4jUtils.prettyPrint(node));
	}
	
	private static void prtln(String s) {
		if (debug) {
			// System.out.println("AsnDocument: " + s);
			System.out.println(s);
		}
	}
}
