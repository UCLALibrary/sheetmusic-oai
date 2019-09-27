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

package org.dlese.dpc.ndr.toolkit;

import org.dlese.dpc.schemedit.SchemEditUtils;
import org.dlese.dpc.schemedit.test.TesterUtils;
import org.dlese.dpc.schemedit.ndr.util.ServiceDescription;

import org.dlese.dpc.util.Files;
import org.dlese.dpc.ndr.NdrUtils;
import org.dlese.dpc.ndr.DleseAsUseCaseHelper;
import org.dlese.dpc.ndr.request.*;
import org.dlese.dpc.ndr.reader.*;
import org.dlese.dpc.ndr.apiproxy.InfoXML;
import org.dlese.dpc.ndr.apiproxy.NDRConstants;
import org.dlese.dpc.ndr.apiproxy.NDRConstants.NDRObjectType;
import org.dlese.dpc.xml.Dom4jUtils;
import org.dom4j.*;
import org.apache.axis.encoding.Base64;

import java.util.*;
import java.io.*;

import org.apache.struts.upload.FormFile;

import java.lang.reflect.Method;

public class ContentUtils {

	private static boolean debug = true;
	
	public static String encodeToString (byte[] in) throws Exception {
		return encodeToString (in, -1);
	}
	
	public static String encodeToString (byte[] in, int width) throws Exception {
		// return  Base64.encode(in);
		String raw = Base64.encode(in);
		if (width > 0) {
			StringBuffer buf = new StringBuffer();
			for (int i=0;i<raw.length();i++) {
				buf.append(raw.charAt(i));
				if (i > 0 && (i % 72 == 0))
					buf.append ("=\n");
			}
			return buf.toString();
		}
		else {
			return raw;
		}
	}

	public static byte[] decodeString (String in) {
		return Base64.decode (in);
	}

	public static byte[] readBinaryFile (File file) throws Exception {
		byte [] fileBytes = null;
		try {
			// Wrap the FileInputStream with a DataInputStream
			FileInputStream file_input = new FileInputStream (file);
			DataInputStream data_in    = new DataInputStream (file_input );
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream ();
			
			while (true) {
				try {
					byteStream.write(data_in.readByte());
				} catch (EOFException eof) {
					// prtln ("End of File");
					break;
				}
			}
			data_in.close ();
			fileBytes = byteStream.toByteArray();
		} catch  (IOException e) {
			System.out.println ( "IO Exception =: " + e );
		}		
		return fileBytes;
	}
	
	private static void  pp (Node node) {
		prtln (Dom4jUtils.prettyPrint(node));
	}
	
	
	/**
	 *  Description of the Method
	 *
	 *@param  s  Description of the Parameter
	 */
	private static void prtln(String s) {
		if (debug) {
			SchemEditUtils.prtln(s, "");
		}
	}
}
