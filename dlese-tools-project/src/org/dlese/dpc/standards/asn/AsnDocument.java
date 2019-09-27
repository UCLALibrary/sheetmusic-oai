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
package org.dlese.dpc.standards.asn;

import org.dlese.dpc.schemedit.SchemEditUtils;
import org.dlese.dpc.schemedit.standards.*;
import org.dom4j.*;
import org.dlese.dpc.xml.Dom4jUtils;
import org.dlese.dpc.xml.schema.SchemaHelper;
import org.dlese.dpc.util.Files;
import java.util.*;
import java.io.File;

/**
 *  Encapsulates an ASN Standards Document by reading the source XML file and
 *  creating a hierarchy of AsnStandard instances. Also provides lookup for
 *  AsnStandards by their id.
 *
 * @author    ostwald
 */
public class AsnDocument {
	private static boolean debug = true;

	private AsnHelper asnHelper = null;
	private Document rawDoc = null;
	private Map asnIdMap = null;
	private Map statementIdMap = null;
	private String path;
	private String identifier = null;
	private String uid = null;
	private String title = null;
	private String fileCreated = null;
	private String created = null;
	private String author = null;
	private String topic = null;
	private String description = null;
	private String version = null;
	private String authorPurl = null;
	private String topicPurl = null;


	/**
	 *  Constructor for the AsnDocument object given the path to an ASN XML file.
	 *
	 * @param  file           NOT YET DOCUMENTED
	 * @exception  Exception  Description of the Exception
	 */
	public AsnDocument(File file) throws Exception {

		this.asnHelper = AsnHelper.getInstance();
		this.path = file.getCanonicalPath();
		asnIdMap = new HashMap();
		statementIdMap = new HashMap();

		try {
			StringBuffer xml = Files.readFileToEncoding(new File(path), "UTF-8");
			rawDoc = Dom4jUtils.getXmlDocument(xml.toString());
			init();
		} catch (Throwable e) {
			// e.printStackTrace();
			throw new Exception("init error: " + e.getMessage());
		}
		// prtln ("AsnDocument instantiated");
	}


	/**
	 *  Constructor taking an xml docuement and "Path" (a unique id that can be
	 *  used to identify the document)
	 *
	 * @param  xmlDoc         NOT YET DOCUMENTED
	 * @param  path           NOT YET DOCUMENTED
	 * @exception  Exception  NOT YET DOCUMENTED
	 */
	public AsnDocument(Document xmlDoc, String path) throws Exception {

		this.asnHelper = AsnHelper.getInstance();
		this.path = path;
		this.rawDoc = xmlDoc;
		asnIdMap = new HashMap();
		statementIdMap = new HashMap();
		try {
			init();
		} catch (Throwable e) {
			// e.printStackTrace();
			throw new Exception("init error: " + e.getMessage());
		}
		// prtln ("AsnDocument instantiated");
	}

	public Document getDocument() {
		return this.rawDoc;
	}

	/**
	 *  Gets the path attribute of the AsnDocument object
	 *
	 * @return    The path value
	 */
	public String getPath() {
		return this.path;
	}


	/**
	 *  Gets the fileCreated attribute of the AsnDocument object
	 *
	 * @return    The fileCreated value
	 */
	public String getFileCreated() {
		return this.fileCreated;
	}


	/**
	 *  Gets the created attribute of the AsnDocument object
	 *
	 * @return    The created value
	 */
	public String getCreated() {
		return this.created;
	}


	/**
	 *  Gets the identifier attribute of the AsnDocument object (the full ASN Purl
	 *  id)
	 *
	 * @return    The identifier value (e.g., "http://purl.org/ASN/resources/S1015D9B")
	 */
	public String getIdentifier() {
		return this.identifier;
	}


	/**
	 *  Gets the unique part of the ASN purl id
	 *
	 * @return    The uid value (e.g., "S1015D9B")
	 */
	public String getUid() {
		if (this.uid == null) {
			try {
				this.uid = new File(this.identifier).getName();
			} catch (Throwable t) {
				prtln("get uid error (" + t.getMessage() + ") - will use full purl id for uid");
				this.uid = identifier;
			}
		}
		return this.uid;
	}


	/**
	 *  Gets the title attribute of the AsnDocument object
	 *
	 * @return    The title value
	 */
	public String getTitle() {
		return this.title;
	}


	/**
	 *  Gets the description attribute of the AsnDocument object
	 *
	 * @return    The description value
	 */
	public String getDescription() {
		return this.description;
	}


	/**
	 *  Gets the version attribute of the AsnDocument object
	 *
	 * @return    The version value
	 */
	public String getVersion() {
		return this.version;
	}


	/**
	 *  Gets the authorPurl attribute of the AsnDocument object
	 *
	 * @return    The authorPurl value
	 */
	public String getAuthorPurl() {
		return this.authorPurl;
	}


	/**
	 *  Gets the author attribute of the AsnDocument object
	 *
	 * @return    The author value
	 */
	public String getAuthor() {
		return this.author;
	}


	/**
	 *  Gets the author attribute of the AsnDocument object
	 *
	 * @param  authors  Description of the Parameter
	 * @return          The author value
	 */
	public String getAuthor(AsnAuthors authors) {
		if (this.author == null && authors != null) {
			this.author = authors.getAuthor(this.authorPurl);
		}
		return this.author;
	}


	/**
	 *  Gets the topicPurl attribute of the AsnDocument object
	 *
	 * @return    The topicPurl value
	 */
	public String getTopicPurl() {
		return this.topicPurl;
	}


	/**
	 *  Gets the topic attribute of the AsnDocument object
	 *
	 * @return    The topic value
	 */
	public String getTopic() {
		return this.topic;
	}


	/**
	 *  Gets the topic attribute of the AsnDocument object
	 *
	 * @param  topics  Description of the Parameter
	 * @return         The topic value
	 */
	public String getTopic(AsnTopics topics) {
		if (this.topic == null && topics != null) {
			this.topic = topics.getTopic(this.topicPurl);
		}
		return this.topic;
	}


	/**
	 *  Gets the rootStandard attribute of the AsnDocument object
	 *
	 * @return    The rootStandard value
	 */
	public AsnStandard getRootStandard() {
		// why aren't we returning a RootStdNode?
		return (AsnStandard) asnIdMap.get(this.getIdentifier());
	}


	/**
	 *  Gets the AsnStandard having provicded id
	 *
	 * @param  id  Description of the Parameter
	 * @return     The standard value
	 */
	public AsnStandard getStandard(String id) {
		return (AsnStandard) asnIdMap.get(id);
	}
	
	public AsnStandard getStandardForStatmentId (String statementId) {
		return (AsnStandard) statementIdMap.get(statementId);
	}


	/**
	 *  Gets all standards contained in this AsnDocument
	 *
	 * @return    The standards value
	 */
	public Collection getStandards() {
		return asnIdMap.values();
	}


	/**
	 *  Gets the standards at the specified level of the standards hierarchy of the
	 *  AsnDocument object
	 *
	 * @param  level  Description of the Parameter
	 * @return        The standardsAtLevel value
	 */
	public List getStandardsAtLevel(int level) {
		List ret = new ArrayList();
		if (getStandards() != null) {
			for (Iterator i = getStandards().iterator(); i.hasNext(); ) {
				AsnStandard std = (AsnStandard) i.next();
				if (std.getLevel() == level) {
					ret.add(std);
				}
			}
		}
		return ret;
	}


	/* purls for identifying standards by specific "authors" */
	static String COLORADO_PURL_AUTHOR = "http://purl.org/ASN/scheme/ASNJurisdiction/CO";
	static String AAAS_PURL_AUTHOR = "http://purl.org/ASN/scheme/ASNJurisdiction/AAAS";


	/**
	 *  Returns true if the author of this AsnDocument is Colorado
	 *
	 * @return    The coloradoBenchmark value
	 */
	public boolean authorIsColorado() {
		return (COLORADO_PURL_AUTHOR.equals(this.getAuthorPurl()));
	}


	/**
	 *  Returns true if
	 *
	 * @return    The aAASBenchmark value
	 */
	public boolean authorIsAAAS() {
		return (AAAS_PURL_AUTHOR.equals(this.getAuthorPurl()));
	}


	/**
	 *  Factory to creates an AsnStandard instance based on the AsnDocuments
	 *  authorPurl attribute
	 *
	 * @param  e  statement element from the XML document
	 * @return    AsnStandard instance created from statement element
	 */
	private AsnStandard makeAsnStandard(Element e) {
		AsnStatement stmnt = new AsnStatement(e);
		AsnStandard std = null;
		if (authorIsColorado()) {
			std = new ColoradoBenchmark(stmnt, this);
		}
		else if (authorIsAAAS()) {
			std = new AAASBenchmark(stmnt, this);
		}
		else {
			std = new AsnStandard(stmnt, this);
		}
		return std;
	}


	/**
	 *  Gets the list of asn IDs defined by the AsnDocument object
	 *
	 * @return    The identifiers value
	 */
	public Set getIdentifiers() {
		return asnIdMap.keySet();
	}


	/**
	 *  Read XML document and initialize values for the AsnDocumennt object
	 *
	 * @param  rawDoc         NOT YET DOCUMENTED
	 * @exception  Exception  Description of the Exception
	 */
	private void init() throws Exception {
		NameSpaceXMLDocReader doc = null;
		try {
			doc = new NameSpaceXMLDocReader(this.rawDoc);
		} catch (Exception e) {
			throw new Exception("Couldn't read standards doc: " + e.getMessage());
		}

		Element stdDocElement = (Element) doc.getNode("/rdf:RDF/asn:StandardDocument");
		if (stdDocElement == null) {
			throw new Exception("StandardDocument element not found");
		}
		
		Element stdDescElement = (Element) doc.getNode("/rdf:RDF/rdf:Description");
		if (stdDescElement == null) {
			throw new Exception("StandardDescription element not found");
		}

		AsnDocStatement stdDocStmnt = new AsnDocStatement(stdDocElement, stdDescElement);
		
		this.identifier = stdDocStmnt.getId();
		this.fileCreated = stdDocStmnt.getFileCreated();
		this.created = stdDocStmnt.getCreated();
		this.title = stdDocStmnt.getTitle();
		this.description = stdDocStmnt.getDescription();
		this.authorPurl = stdDocStmnt.getJurisdiction();
		this.author = asnHelper.getAuthor(this.authorPurl);
		this.topicPurl = stdDocStmnt.getSubject();
		this.topic = asnHelper.getTopic(this.topicPurl);

		String versionPurl = stdDocStmnt.getExportVersion();
		try {
			this.version = versionPurl.substring("http://purl.org/ASN/export/".length());
		} catch (Throwable t) {
			prtln("WARNING: could not extract version number (" + t.getMessage() + ") - using purl instead");
			this.version = versionPurl;
		}

		RootAsnStandard root = new RootAsnStandard(stdDocStmnt, this);
		asnIdMap.put(root.getId(), root);
		if (root == null)
			throw new Exception ("RootAsnStandard not found");
		statementIdMap.put(root.getStatementId(), root);

		// Create AsnStandard objects for each standard statement in this document
		List asnStmntNodes = doc.getNodes("/rdf:RDF/asn:Statement");
		// prtln (asnStmntNodes.size() + " items found");
		for (Iterator i = asnStmntNodes.iterator(); i.hasNext(); ) {
			Element e = (Element) i.next();
			AsnStandard std = this.makeAsnStandard(e);
			asnIdMap.put(std.getId(), std);
			statementIdMap.put(std.getStatementId(), std);
		}

		doc.destroy();
		System.runFinalization();
		System.gc();
	}


	/**
	 *  Description of the Method
	 *
	 * @return    Description of the Return Value
	 */
	public String toString() {
		String s = "\ntitle: " + this.getTitle();
		s += "\n\t" + "identifier: " + this.getIdentifier();
		s += "\n\t" + "fileCreated: " + this.getFileCreated();
		s += "\n\t" + "version: " + this.getVersion();
		s += "\n\t" + "description: " + this.getDescription();
		s += "\n\t" + "author: " + this.getAuthor();
		s += "\n\t" + "topic: " + this.getTopic();
		return s;
	}


	/**
	 *  The main program for the AsnDocument class
	 *
	 * @param  args           The command line arguments
	 * @exception  Exception  Description of the Exception
	 */
	public static void main(String[] args) throws Exception {

		String path = "/Users/ostwald/tmp/D100003B_full.xml";

		AsnDocument asnDoc = new AsnDocument(new File(path));
		prtln("author: " + asnDoc.getAuthor());
		prtln("topic: " + asnDoc.getTopic());
		prtln("created: " + asnDoc.getCreated());
		prtln("version: " + asnDoc.getVersion());

		/* 		prtln ("ASN DocStatement");
		prtln (asnDoc.getRootStandard().getAsnStatement().toString()); */
		// test a particular std
		// String asnId = "http://asn.jesandco.org/resources/S1015D9B";  // Colorado std
 		String asnId = "http://purl.org/ASN/resources/S1009A52"; 
		AsnStandard std = asnDoc.getStandard(asnId);
		if (std == null)
			prtln("no standard found for " + asnId);
		else
			prtln(std.toString());
	}


	/**
	 *  Gets the standardTest attribute of the AsnDocument class
	 *
	 * @param  asnDoc  Description of the Parameter
	 * @param  asnId   Description of the Parameter
	 */
	private static void getStandardTest(AsnDocument asnDoc, String asnId) {
		AsnStandard std = asnDoc.getStandard(asnId);
		List children = std.getChildren();
		prtln("children (" + children.size() + ")");
		for (Iterator i = children.iterator(); i.hasNext(); ) {
			AsnStandard childStd = (AsnStandard) i.next();
			if (childStd == null) {
				prtln("NULL");
			}
			else {
				prtln(childStd.getId());
			}
		}
	}


	/**
	 *  Description of the Method
	 *
	 * @param  s  Description of the Parameter
	 */
	private static void prtln(String s) {
		if (debug) {
			// SchemEditUtils.prtln(s, "AsnDocument");
			SchemEditUtils.prtln(s, "");
		}
	}
}

