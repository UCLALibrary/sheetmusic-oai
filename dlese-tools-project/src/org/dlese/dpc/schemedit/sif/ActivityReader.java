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

package org.dlese.dpc.schemedit.sif;

import org.dlese.dpc.schemedit.SchemEditUtils;
import org.dlese.dpc.standards.asn.NameSpaceXMLDocReader;
import org.dlese.dpc.util.Files;
import org.dom4j.Element;
import org.dom4j.DocumentHelper;

public class ActivityReader extends  SIFDocReader {
	private static boolean debug = true;
	
	public ActivityReader (String xml) throws Exception {
		super (xml);
	}
	
	public String getHeaderText () {
		return getTitle();
	}
	
	public String getDescriptiveText() {
		return getPreamble();
	}
	
	public String getXmlFormat () {
		return "sif_activity";
	}
	
	public String getFormatName () {
		return "Activity";
	}		
	
	public String getTitle () {
		return getNodeText ("/sif:Activity/sif:Title");
	}
	
	public String getPreamble () {
		return getNodeText ("/sif:Activity/sif:Preamble");
	}
	
 	public static void main (String [] args) throws Exception {
		String records = "C:/Documents and Settings/ostwald/devel/dcs-instance-data/ccs/records/";
		String path = records + "sif_activity/1210287646316/ACT-000-000-000-004.xml";
		
		String xml = Files.readFile(path).toString();
		
		ActivityReader reader = new ActivityReader (xml);
		prtln ("xmlFormat: " + reader.getXmlFormat());
		prtln ("formatName: " + reader.getFormatName());
		prtln ("refId: " + reader.getRefId());
		prtln ("title: " + reader.getTitle());
		prtln ("preamble: " + reader.getPreamble());
	}
	
	private static void prtln(String s) {
		if (debug) {
			SchemEditUtils.prtln(s, "ActivityReader: ");
		}
	}
}
