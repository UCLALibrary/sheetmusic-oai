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
 *  Replaces all the occurances of an editor with another .
 *
 *@author    ostwald
 <p>$Id: DcsRecordEditorFix.java,v 1.4 2011/07/05 22:05:38 ostwald Exp $
 */
public class DcsRecordEditorFix {
	FrameworkConfigReader reader = null;
	MetaDataFramework framework = null;

	String NewDcsDir = "/dpc/tremor/devel/ostwald/metadata-frameworks/dcs-data";

	/**
	 *  Constructor for the DcsRecordEditorFix object
	 */
	public DcsRecordEditorFix(String format) {
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

	void fixFile (File sourceFile) throws Exception {
		prtln ("fixing " + sourceFile.getName() + " ...");
		DcsDataRecord dcsDataRecord = new DcsDataRecord (sourceFile, framework, null, null);
		List statusEntries = dcsDataRecord.getEntryList();
		for (Iterator i=statusEntries.iterator();i.hasNext();) {
			// Element entryElement = (Element) i.next();
			StatusEntry statusEntry = (StatusEntry) i.next();
			// StatusEntry statusEntry = new StatusEntry (entryElement);
			if (statusEntry.getEditor().equals("ostwald")) {
				statusEntry.setEditor("Dawn");
				prtln ("... converted editor to Dawn");
			}
			// statusEntry.printEntry();
			// prtln (" ");
		}
		// prtln ("fixed File: " + Dom4jUtils.prettyPrint (dcsDataRecord.getDocument()));
		dcsDataRecord.flushToDisk();
	}

	/**
	 *  The main program for the DcsRecordEditorFix class
	 *
	 *@param  args  The command line arguments
	 */
	public static void main(String[] args) throws Exception {
		prtln("DcsRecordEditorFix");
		String format = "dcs_data";
		DcsRecordEditorFix fixer = new DcsRecordEditorFix(format);
		String fixDir = "/devel/ostwald/projects/tmp/tapestry/1112905194690";

		File [] files = new File (fixDir).listFiles();
		for (int i = 0; i < files.length;i++) {
			fixer.fixFile (files[i]);
		}
		// String fileName = "DLISR-000-000-000-065.xml";
		// fixer.fixFile (new File (fixDir, fileName));
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
