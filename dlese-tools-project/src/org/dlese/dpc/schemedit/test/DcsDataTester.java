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

import java.io.*;
import java.util.*;
import java.text.*;

import org.dlese.dpc.schemedit.*;
import org.dlese.dpc.schemedit.dcs.*;
import org.dlese.dpc.schemedit.config.*;
import org.dlese.dpc.xml.*;
import org.dlese.dpc.xml.schema.*;
import org.dlese.dpc.util.*;
import org.dlese.dpc.repository.*;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Attribute;
import org.dom4j.Node;

/**
 *  Tester for {@link org.dlese.dpc.schemedit.dcs.DcsDataRecord} and related classes.
 *
 *@author    ostwald
 <p>$Id: DcsDataTester.java,v 1.12 2011/07/05 22:05:38 ostwald Exp $
 */
public class DcsDataTester {
	FrameworkConfigReader reader = null;
	MetaDataFramework framework = null;
	FrameworkRegistry registry = null;

	String NewDcsDir = "/dpc/tremor/devel/ostwald/metadata-frameworks/dcs-data";

	/**
	 *  Constructor for the DcsDataTester object
	 */
	public DcsDataTester(String format) {
		String errorMsg;
		String configFileName = format+".xml";

		String configDirPath = NewDcsDir;
		// String configDirPath = "/devel/ostwald/metadata-frameworks/dcs-framework/records";
		// String configDirPath = "/devel/ostwald/tomcat/tomcat/webapps/schemedit/WEB-INF/framework-config";
		File sourceFile = new File(configDirPath, configFileName);
		if (!sourceFile.exists()) {
			prtln("source File does not exist at " + sourceFile.toString());
			return;
		}
		else {
			prtln ("reading frameworkconfig file from: " + sourceFile.toString());
		}
		try {
			reader = new FrameworkConfigReader(sourceFile);
			framework = new MetaDataFramework(reader);

			framework.loadSchemaHelper();
		} catch (Exception e) {
			errorMsg = "Error loading Schema Helper: " + e.getMessage();
			prtln (errorMsg);
		}
	}

	/**
	* create a dcsDataRecord and test accessors
	*/
	void basicsTester () throws Exception {
		File testRecordsDir = new File (NewDcsDir, "v0-0-3-Records");
		String fileName = "Version-3-tester-2.xml";
		File sourceFile = new File (testRecordsDir, fileName);
		DcsDataRecord dcsDataRecord = new DcsDataRecord (sourceFile, framework, null, null);

		// tester.addStatusEntry (dcsDataRecord);

/* 		dcsDataRecord.setStatus ("Holding");
		dcsDataRecord.setLastEditor("olds");
		dcsDataRecord.setStatusNote("i am a status note");
		dcsDataRecord.setChangeDate ("1999-12-30"); */

		dcsDataRecord.updateStatus ("Holding", "i am a status note", "olds");

		dcsDataRecord.setId("ID FOOL!");
		// dcsDataRecord.setIsValid("true");
		dcsDataRecord.setLastTouchDate("2004-01-01");

		/* dcsDataRecord.addStatusEntry(StatusFlags.IMPORTED_STATUS, "Imported via web service", "Unknown editor", changeDate); */
		dcsDataRecord.updateStatus(StatusFlags.IMPORTED_STATUS, "Imported via web service", "Unknown editor");

		Document doc = dcsDataRecord.getDocument();
		prtln (Dom4jUtils.prettyPrint (doc));
		prtln ("current entry");
		dcsDataRecord.getCurrentEntry().printEntry();

		prtln ("getDate test:");
		prtln (SchemEditUtils.fullDateString(dcsDataRecord.getCurrentEntry().getDate()));
		// dcsDataRecord.flushToDisk();
	}

	void convertFileTester () throws Exception {
		File testRecordsDir = new File (NewDcsDir, "v0-0-2-Records");
		String fileName = "SERC-EET-000-000-000-019.xml";
		File sourceFile = new File (testRecordsDir, fileName);
		DcsDataRecord dcsDataRecord = new DcsDataRecord (sourceFile, framework, null, null);

		Document doc = dcsDataRecord.getDocument();
		prtln ("original Document");
		prtln (Dom4jUtils.prettyPrint (doc));

		DcsDataConverter converter = new DcsDataConverter (testRecordsDir.toString(), framework);
		converter.convertFile(sourceFile);
	}

	void convertTester () throws Exception {
		String dcsDataPath = "/devel/ostwald/records/dcs_data";
		DcsDataConverter converter = new DcsDataConverter (dcsDataPath, framework);
		converter.convert();
	}


	/**
	 *  The main program for the DcsDataTester class
	 *
	 *@param  args  The command line arguments
	 */
	public static void main(String[] args) throws Exception {
		prtln("DcsDataTester");
		String format = "dcs_data";
		DcsDataTester tester = new DcsDataTester(format);
		tester.basicsTester();
		// tester.convertFileTester();
		// tester.convertTester();
	}

	/**
	 *  Description of the Method
	 *
	 *@param  s  Description of the Parameter
	 */
	public static void prtln(String s) {
		System.out.println(s);
	}
}
