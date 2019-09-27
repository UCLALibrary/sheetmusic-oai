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

import org.dlese.dpc.schemedit.ndr.util.InfoStream;
import org.dlese.dpc.ndr.request.*;
import org.dlese.dpc.xml.Dom4jUtils;
import org.dom4j.*;

/**
 *  NOT YET DOCUMENTED
 *
 * @author     Jonathan Ostwald
 */
public class TestInfoStream {

	private static boolean debug = true;
	static final  String COLLECTION_MANAGER_METADATA_PROVIDER =
		"2200/NSDL_Collection_Manager_Metadata_Provider";

	/**
	 *  The main program for the TestInfoStream class
	 *
	 * @param  args           The command line arguments
	 * @exception  Exception  NOT YET DOCUMENTED
	 */
	public static void main(String[] args) throws Exception {
		makeInfoStream();
	}


	/**  NOT implemented - test the algorithm to derive an NDR Oai link */
	public static void getNdrInfoLink() {
		String aggHandle = "2200/test.20070615172917219T";
	}


	/**
	 *  tester for creation and insertion of info streams in requests.
	 *
	 * @exception  Exception  NOT YET DOCUMENTED
	 */
	public static void makeInfoStream() throws Exception {
		AddMetadataRequest request = new AddMetadataRequest();

		String id = "FAKO-001";
		request.setUniqueId(id);

		String resourceHandle = "2200/test.20070621145017845T";
		request.setMetadataFor(resourceHandle);

		String aggregatorHandle = "2200/test.20070621192337930T";
		request.setMetadataFor(aggregatorHandle);

		request.setMetadataProvidedBy("2200/NSDL_Collection_Manager_Metadata_Provider");

		Element ncs_collect = DocumentHelper.createElement("record");
		ncs_collect.addElement("recordID").setText(id);
		ncs_collect.addElement("description").setText("this is the metadata for the collection");

		request.addDataStreamCmd("ncs_collect", ncs_collect);

		Element info = InfoStream.getFakeInfoStream().asElement();
		/* 		Element info = DocumentHelper.createElement("info");
		info.addElement("nsdlAboutCategory").setText("item");
		info.addElement("repositoryPrimaryIdentifier").setText("http://my.url.com"); */
		request.setDataInfoStream("ncs_collect", info);
		request.report();
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

