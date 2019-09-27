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

package org.dlese.dpc.serviceclients.asn;

import org.dlese.dpc.standards.asn.AsnDocument;

import org.dlese.dpc.standards.asn.util.AsnIdMapper;
import org.dlese.dpc.schemedit.standards.asn.AsnDocKey;
import org.dlese.dpc.serviceclients.asn.acsr.ACSRToolkit;
import org.dlese.dpc.util.strings.FindAndReplace;
import org.dlese.dpc.schemedit.SchemEditUtils;
import org.dom4j.*;
import org.dlese.dpc.xml.Dom4jUtils;
import java.util.*;
import java.net.*;

/**
 *  Provides static methods for working with the ASN standards via webservices.
 *
 * @author    Jonathan Ostwald
 */
public class AsnClientToolkit {

	private static boolean debug = true;
	/**  NOT YET DOCUMENTED */
	public static String asnIdMapperData = null;
	private static AsnIdMapper asnIdMapperInstance = null;

	/**
	* AsnIdMapper as a singleton (requires that "asnIdMapperData" is set
	*/
	private static AsnIdMapper getAsnIdMapper() throws Exception {
		if (asnIdMapperInstance == null) {
			if (asnIdMapperData == null)
				throw new Exception("Error: getAsnIdMapper requires that \"asnIdMapperData\" is set");
			asnIdMapperInstance = new AsnIdMapper(asnIdMapperData);
		}
		return asnIdMapperInstance;
	}


	/**
	 *  Sets the asnIdMapperData attribute of the AsnClientToolkit class
	 *
	 * @param  dataDirPath  The new asnIdMapperData value
	 */
	public static void setAsnIdMapperData(String dataDirPath) {
		asnIdMapperData = dataDirPath;
		prtln ("setAsnIdMapperData -> " + asnIdMapperData);
	}


	/**
	 *  Retrieve an ASN document for provided docID using web service.
	 *
	 * @param  docId          docId (purl)
	 * @return                a corresponding Asn Document instance
	 * @exception  Exception  if the asn document cannot be obtained
	 */
	public static AsnDocument fetchAsnDocumentAcsr (String docId) throws Exception {
		AsnIdMapper asnIdMaper = null;
		try {
			asnIdMaper = getAsnIdMapper();
		} catch (Exception e) {
			throw new Exception("could not instantiate asnIdMapper: " + e.getMessage());
		}
		String acsrId = asnIdMaper.getAcsrId(docId);
		if (acsrId == null)
			throw new Exception("could not find acsrId");
		return ACSRToolkit.getAsnDocument(acsrId);
	}

	public static Document fetchAsnSourceForKey (String docKey) throws Exception {
		prtln ("\nfetchAsnSourceForKey (" + docKey + ")");
		AsnDocKey asnDocKey = AsnDocKey.makeAsnDocKey(docKey);
		String asnDocId = asnDocKey.getAsnId();
		// prtln ("  asnDocId: " + asnDocId);
		String urlStr = asnDocId+"_full.xml";
		Document doc = null;
		try {
			URL url = new URL (urlStr);
			doc = Dom4jUtils.getXmlDocument(url);
		} catch (Exception e) {
			prtln ("ERROR: " + e.getMessage());
		}
		return doc;

	}
	
	public static AsnDocument fetchAsnDocument(String asnDocId) throws Exception {
		prtln ("\nfetchAsnDocument (" + asnDocId + ")");
		String urlStr = asnDocId+"_full.xml";
		Document doc = null;
		try {
			URL url = new URL (urlStr);
			doc = Dom4jUtils.getXmlDocument(url);
			return new AsnDocument(doc, "fooberry");
		} catch (Exception e) {
			prtln ("ERROR: " + e.getMessage());
		}
		return null;
	}	

	public static void main (String[] args) throws Exception {
		//  http://purl.org/ASN/resources/D100025D
		
/* 		// TEST FETCH_ASN_DOCUMENT
		String asnId = "http://purl.org/ASN/resources/D100025D_full.xml";
		// String asnId = "http://asn.jesandco.org/resources/D100025D_full.xml"
		AsnDocument asnDoc = fetchAsnDocument(asnId);
		if (asnDoc == null)
			prtln ("could not obtain asnDoc for " + asnId);
		else
			prtln (asnDoc.toString()); 
*/
		
		// TEST GetDocForStdId
		String asnId = "http://purl.org/ASN/resources/S1021C5B";
		String docId = getDocIdForStdId (asnId);
		prtln ("docId: " + docId);
		
	}
	
	/**
	 *  Gets the docId for the provided stdId using the ASN Resolver Service.
	 *
	 * @param  asnStdId       asn standard id
	 * @return                doc id for the document containing the std
	 * @exception  Exception  NOT YET DOCUMENTED
	 */
	public static String getDocIdForStdId(String asnStdId) throws Exception {
		// prtln ("getDocIdForStdId: " + asnStdId);
		URL purl = new URL(asnStdId+".xml");
		AsnResolutionResponse arr = new AsnResolutionResponse(purl);
		return arr.getDocId();
	}


	/**
	 *  NOT YET DOCUMENTED
	 *
	 * @param  s  NOT YET DOCUMENTED
	 */
	private static void prtln(String s) {
		if (debug) {
			// System.out.println("AsnDocument: " + s);
			System.out.println(s);
		}
	}
}

