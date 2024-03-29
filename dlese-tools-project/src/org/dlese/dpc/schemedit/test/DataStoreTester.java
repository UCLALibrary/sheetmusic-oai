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
import org.dlese.dpc.datamgr.*;
import org.dlese.dpc.schemedit.*;

/**
 *  Utilities for manipulating XPaths, represented as String
 *
 *@author     ostwald
 */
public class DataStoreTester {

	private static boolean debug = true;
	private SimpleDataStore dataStore = null;
	private int NUM_EXPORTING_MESSAGES = 10;
	
	DataStoreTester (String dataDir) {
		try {
			File f = new File(dataDir);
			f.mkdir();
			dataStore = new SimpleDataStore(f.getAbsolutePath(), true);
		} catch (Exception e) {
			prtln ("Error initializing SimpleDataStore: " + e);
		}
	}
	
	private void addExportingMessage(String msg) {
		ArrayList exportingMessages =
			(ArrayList) dataStore.get("EXPORTING_STATUS_MESSAGES");
		if (exportingMessages == null) {
			exportingMessages = new ArrayList(NUM_EXPORTING_MESSAGES);
		}
		exportingMessages.add(getSimpleDateStamp() + " " + msg);
		if (exportingMessages.size() > NUM_EXPORTING_MESSAGES) {
			exportingMessages.remove(0);
		}
		dataStore.put("EXPORTING_STATUS_MESSAGES", exportingMessages);
	}
	
	public ArrayList getExportingMessages() {
		ArrayList exportingMessages =
			(ArrayList) dataStore.get("EXPORTING_STATUS_MESSAGES");
		if (exportingMessages == null) {
			exportingMessages = new ArrayList();
			exportingMessages.add("No exporting messages logged yet...");
		}
		return exportingMessages;
	}
	
	private String getSimpleDateStamp () {
		return SchemEditUtils.fullDateString(new Date());
	}
	
	public static void main (String [] args) {
		// String dataDir = "/dpc/tremor/devel/ostwald/SchemEdit/testers/dataStore";
		String dataDir = "/dpc/tremor/devel/ostwald/tomcat/tomcat/dcs_conf/exporting_service_data";
		DataStoreTester dst = new DataStoreTester (dataDir);
		/* 
		dst.addExportingMessage ("Hello world. I am an indexing message");
		prtln ("DataStoreTester created. Number of files: " + dst.dataStore.getNumRecords()); 
		*/
		List messages = dst.getExportingMessages();
		prtln ("there are " + messages.size() + " messages");
		for (Iterator i=messages.iterator();i.hasNext();) {
			prtln ((String)i.next());
		}
	}

	/**
	 *  Description of the Method
	 *
	 *@param  s  Description of the Parameter
	 */
	private static void prtln(String s) {
		if (debug) {
			System.out.println("DataStoreTester: " + s);
		}
	}

}

