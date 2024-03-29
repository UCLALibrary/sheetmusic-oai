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

package org.dlese.dpc.index;

import java.io.*;
import java.util.*;
import java.text.*;
import java.net.*;
import java.lang.reflect.*;
//import org.dlese.dpc.catalog.*;
//import org.dlese.dpc.catalog.DleseBean;
//import org.dlese.dpc.catalog.DleseCatalog;
//import org.dlese.dpc.catalog.DleseCatalogRecord;

//import com.lucene.index.*;
//import com.lucene.document.*;
//import com.lucene.analysis.*;
//import com.lucene.queryParser.*;
//import com.lucene.search.*;

public class Tester {

	public Tester() {
	}

	static String getReader(File reader) {
		String value = null;
		try {
			FileInputStream f = new FileInputStream(reader);
			ObjectInputStream p = new ObjectInputStream(f);
			try {
				value = (String)p.readObject();
				System.out.println("read string = " + value);
			}
			catch (Exception e) {
				value = null;
			}
			finally {
				f.close();
				p.close();
			}
		}
		catch (Exception e) {
			// Will always happen if file doesn't exist.
			// monitor.logError("Exception reading record file: " + file.getName());
		}
		return value;
	}

	static void setReader(File reader, String value) {
	
		try {
			FileOutputStream f = new FileOutputStream(reader);
			ObjectOutputStream p = new ObjectOutputStream(f);

			try {
				p.writeObject(value);
				p.flush();
			}
			catch (Exception e) {
				//
			}
			finally {
				f.close();
				p.close();
			}
		}
		catch (Exception e) {}
	}


	public static void main(String[] args) {
		Tester tst = new Tester();
		try {
			File indexReader = new File("k:/deniman/testfile.txt");
			
			String str = getReader(indexReader);
			str = "string1";
			setReader(indexReader, str);
			str = getReader(indexReader);
			str = "string2";
			setReader(indexReader, str);
			str = getReader(indexReader);
			str = "string3";
			setReader(indexReader, str);
			str = getReader(indexReader);
			
		}
		catch (Exception e) {
			System.err.println("Exception: " + e.getClass() + " with message: " + e.getMessage()); 
		}
	}

}
