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



import org.dlese.dpc.xml.*;

import org.dlese.dpc.util.*;



import java.io.*;

import java.lang.*;



public class ValidationTester {



	public ValidationTester () {}

	

	public static String doValidateFile (String path) {

		File file = new File (path);

		if (!file.exists()) {

			return "file does not exist at " + path;

		}

		return XMLValidator.validateFile (file);

		}



	public static StringBuffer doValidateDir (String dirPath) {

		StringBuffer buf = new StringBuffer();

		try {

			buf =  new XMLValidator().validate(dirPath);

		} catch (Exception e) {

			prtln (e.getMessage());

		}

		return buf;

	}

	/**

	* get the xml as string from file at path and validate it as string

	*/

	public static String doValidateString (String path) {

		String xmlRecord = "";

		try {

			StringBuffer buff = Files.readFile(path);

			xmlRecord = buff.toString();

		} catch (IOException e) {

			return "ERROR: " + e.getMessage();

		}

		String s = XMLValidator.validateString(xmlRecord);

		if (s == null || s.trim().length() == 0)

			return "the xml file is VALID";

		else

			return "the xml file is NOT valid: " + s;

	}

			

	

	public static void main (String[] args) {

		String filePath = "/dpc/tremor/devel/ostwald/projects/PutRecordTest2.xml";

		String dirPath = "/dpc/tremor/devel/ostwald/records/adn/dwel";

/* 		StringBuffer buf = doValidateDir (dirPath);

		prtln (buf.toString()); */

		prtln (doValidateString (filePath));

	}

		

	private static void prtln (String s) {

		System.out.println (s);

	}

}

