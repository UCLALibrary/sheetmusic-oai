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

public class ASNResReader extends NameSpaceXMLDocReader {
	
	private static boolean debug = true;
	String id;
	Map nodeMap = null;
	Element target = null;
	
	public ASNResReader (String urlStr) throws Exception {
		super (new URL (urlStr));
		id = urlStr;
		nodeMap = this.getNodeMap();
		this.target = (Element)nodeMap.get (id);
		if (target == null)
			throw new Exception ("Target node not found for " + id);
		prtln ("target found");
	}
	
	private Map getNodeMap () {
		List nodes = this.getNodes ("/rdf:RDF/rdf:Description[@rdf:about]");
		nodeMap = new HashMap();
		prtln (nodes.size() + " nodes found");
		for (Iterator i=nodes.iterator();i.hasNext();) {
			Element el = (Element)i.next();
			String id = el.attributeValue(getQName("rdf:about"));
			nodeMap.put (id, el);
		}
		return nodeMap;
	}
	
	public static void main (String[] args) throws Exception {

		String urlStr = "http://purl.org/ASN/resources/S1024B7C";
		ASNResReader reader = new ASNResReader (urlStr);
		// pp (reader.getDocument());
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
