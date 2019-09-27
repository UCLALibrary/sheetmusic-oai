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
package org.dlese.dpc.schemedit.standards;

import org.dlese.dpc.schemedit.Constants;
import org.dlese.dpc.schemedit.standards.asn.*;
import org.dlese.dpc.schemedit.SchemEditUtils;
import org.dlese.dpc.standards.asn.*;
import org.dlese.dpc.standards.asn.util.AsnCatalog;
import org.dlese.dpc.xml.XMLFileFilter;
import org.dlese.dpc.xml.Dom4jUtils;
import org.dlese.dpc.util.Files;

import org.dlese.dpc.serviceclients.cat.CATServiceToolkit;
import org.dlese.dpc.serviceclients.asn.AsnClientToolkit;

import org.dlese.dpc.serviceclients.asn.acsr.ACSRToolkit;

import org.dom4j.Document;
import java.util.*;
import java.io.File;
import java.io.FileFilter;

/**
 *  Class to manage potentially many ASN standards documents, refered to using
 *  {@link AsnDocKey} instances. <p>
 *
 *  Keys identify a standards Document uniquely, and we load each document,
 *  making no attempt to identify versions that may supercede other versions of
 *  the same standards doc.<p>
 *
 *  The StandardsRegistry contains {@link AsnDocInfo} instances to represent
 *  each loaded Standards Doc. The hierarchical structure of each ASN
 *  Document is represented as an {@link StandardsDocument} instance. The registry
 *  makes use of a {@link TreeCache} to cache StandardTrees and loads them when
 *  needed, so that many standards documents can be managed.
 *
 *@author     Jonathan Ostwald
 *@created    June 25, 2009
 */
public class StandardsRegistry {
	private static boolean debug = true;

	private Map docMap = null;   // key --> asnDocInfo

	private TreeCache treeCache = null;

	private Map docIdMap = null;  // docId --> key

	private AsnHelper asnHelper = null;
	private String lock = "lock";
	private List rejectedDocs = null;
	private Map allCatDocs = null;
	private AsnCatalog asnCatalog = null;

	private static StandardsRegistry instance = null;
	private static File libraryDir = null;

	/**
	 *  Gets the singleton StandardsRegistry instance
	 *
	 *@return    a StandardsRegistry instance
	 */
	public static StandardsRegistry getInstance() {
		if (instance == null) {

			try {
				instance = new StandardsRegistry();
			} catch (Exception e) {
				e.printStackTrace();
				prtln("ERROR: could not instantiate StandardsRegistry: " + e.getMessage());
			}
		}
		return instance;
	}


	/**
	 *  Constructor for the StandardsRegistry object
	 *
	 *@exception  Exception  NOT YET DOCUMENTED
	 */
	protected StandardsRegistry() throws Exception {
		prtln ("StandardsRegistry() libdir: " + libraryDir);
		if (libraryDir == null)
			throw new Exception ("libraryDir not initialized");
		if (!libraryDir.exists())
			throw new Exception ("libraryDir does not exist");
		asnHelper = AsnHelper.getInstance();
		this.asnCatalog = AsnCatalog.getInstance();
		this.docMap = new TreeMap();
		this.treeCache = new TreeCache(this);
		this.docIdMap = new HashMap();
		this.rejectedDocs = new ArrayList();
	}

	public static void setLibraryDir (File file) {
		libraryDir = file;

	}

	/* Gets the AsnDocument for provided key in the standards library */
	protected AsnDocument getLibraryDocument (String key) {
		File file = new File (this.libraryDir, key+".xml");
		if (!file.exists()) {
			prtln ("File doesnt exist at " + file);
			return null;
		}
		try {
			return new AsnDocument(file);
		} catch (Exception e) {
			prtln ("getLibraryDocument ERROR: " + e.getMessage());
		}
		return null;
	}

	protected String getLibraryKey (String docId) {
		prtln ("\ngetLibraryKey: " + docId);
		if (docId == null || !docId.startsWith(Constants.ASN_PURL_BASE)) {
			prtln ("getLibraryKey got bugus docId: " + docId);
			return null;
		}
		String uid = docId.substring(Constants.ASN_PURL_BASE.length(), docId.length());
		prtln ("  uid: '" + uid + "'");
		File[] files = this.libraryDir.listFiles (new XMLFileFilter());
		prtln ("  looking through " + files.length + " library files");
		for (int i=0;i<files.length;i++) {
			String fileName = files[i].getName();
			String key = fileName.substring(0, fileName.length() - 4);
			AsnDocKey asnDocKey = AsnDocKey.makeAsnDocKey(key);
			prtln ("comparing with '" + asnDocKey.getUid() + "'");
			if (uid.equals(asnDocKey.getUid())) {
				prtln ("  FOuND: " + key);
				return key;
			}
		}
		prtln ("  library key not found for " + docId);
		return null;
	}

	protected void syncWithLibrary (AsnDocument asnDoc, String key) throws Exception {
		File dest = new File (this.libraryDir, key + ".xml");
		if (!dest.exists()) {
			prtln ("wrote doc to standards library: " + key);
			Dom4jUtils.writeDocToFile(asnDoc.getDocument(), dest);
		}
	}

	/* register all the standards docs in the standards library
	public List load () throws Exception {
		return load (this.libraryDir);
	}


	/**
	 *  Load all standards documents found by traversing the specified standardsDirectory
	 *  recursively and making calls to register for each file found.
	 *
	 *@param  standardsDirectory  a directory containing standards Documents
	 *@return                     A list of AsnDocInfo instances representing
	 *      loaded docs
	 *@exception  Exception       NOT YET DOCUMENTED
	 */
	public List load(File dir) throws Exception {
		// prtln ("\nloading ..." + standardsDirectory);

		if (!dir.exists())
			throw new Exception ("load error: dir does not exist at: " + dir);

		List loaded = new ArrayList();

		FileFilter xmlFileFilter = new XMLFileFilter();

		File[] files = dir.listFiles();
		int numToRegister = 2000;
		for (int i = 0; i < files.length && i < numToRegister; i++) {
			File file = files[i];
			String filePath = file.getAbsolutePath();
			if (filePath.startsWith(".")) {
				continue;
			}
			if (file.isDirectory()) {
				try {
					loaded.addAll(load(file));
				} catch (Exception e) {
					prtln("error attempting to load from " + filePath + ": " + e.getMessage());
				}
				continue;
			}
			if (!xmlFileFilter.accept(file)) {
				continue;
			}

			// if files in the library are named by asnDocKey:
			String key = file.getName().substring(0, file.getName().length()-4);
			// the we call register with the key

			AsnDocInfo docInfo = null;
			try {
				AsnDocument asnDoc = new AsnDocument(file);
				docInfo = this.register(asnDoc);
				prtln((i + 1) + "/" + files.length + ": processed " + files[i].getName()
						 + " (" + docInfo.getKey() + ")");
			} catch (Exception e) {
				prtln((i + 1) + "/" + files.length + " NOT processed " + files[i].getName());
				prtln("load error: " + e.getMessage());
			}
			if (docInfo != null) {
				loaded.add(docInfo);
			}
		}
		return loaded;
	}

	/** register gets a docKey and returns an AsnDocInfo instance
		- if we already have a doc registered for this key, we
		  return the existing AsnDocInfo
		- otherwise, we have to obtain the AsnDoc XML source
			- if we have this key in our "standards library", then we
			  read the file.
			 - otherwise, we obtain the XML source from the ASN service
	*/
	public AsnDocInfo register(String key) throws Exception {
		// is this doc already registered

		// now we'll look for some other key (why not an asnDocKey??)
		AsnDocInfo doc = this.getAsnDocByKey(key);
		if (doc != null) {
			return doc;
		}

		// AsnDocument asnDoc = new AsnDocument(new File (path));
		AsnDocument asnDoc = null;

		// look in "standards library" on disk for file (named for key)
		asnDoc = this.getLibraryDocument(key);

		if (asnDoc != null) {
			prtln ("doc obtained from library");
		} else {
			// fetch from ASN and save to standards library
			prtln ("Fetching " + key + " from ASN");
			// should we have verified key with asnCatalog??
			Document xml = null;
			try {
				xml = AsnClientToolkit.fetchAsnSourceForKey (key);
				asnDoc = new AsnDocument (xml, key);
			} catch (Exception e) {
				prtln ("could NOT obtain ASN Doc from ASN service: " + e.getMessage());
				return null;
			}

/* 			// write to library
			File file = new File (this.libraryDir, key+".xml");
			prtln ("writing file to " + file);
			try {
				Dom4jUtils.writeDocToFile(xml, file);
				prtln ("wrote " + key + " to standards library");
			} catch (Exception e) {
				prtln ("WARNING: could not write " + key + " to standards library");
			} */
		}


		return register (asnDoc);
	}

	/**
	 *  Register a Standards Document located at specified path
	 *
	 *@param  path           filepath of xml document to be loaded
	 *@return                DocInfo for loaded doc
	 *@exception  Exception  if unable to register document.
	 */
	public AsnDocInfo register(AsnDocument asnDoc) throws Exception {

		if (asnDoc == null) {
			throw new Exception("Register recieved a null AsnDocument");
		}

		String key = new AsnDocKey (asnDoc).toString();
		// prtln ("  key: " + key);

		AsnDocInfo registeredDocInfo = this.getDocInfo(key);
		if (registeredDocInfo != null) {
			return registeredDocInfo;
		}

		try {
			if (asnDoc.getAuthor() == null) {
				throw new Exception("CAT Service does not support this document: " + asnDoc.getTitle());
			}
			AsnDocInfo docInfo = new AsnDocInfo(asnDoc);
			if (docInfo == null) {
				throw new Exception("docInfo could not be created for " + key);
			}
			docInfo.setStatus(asnCatalog.getStatus(docInfo.getDocId()));

			/* HAVEN'T WE ALREADY TESTED FOR EXISTANCE OF DOC INFO FOR KEY??
				would the docMap have changed since that test??
			*/
 			AsnDocInfo existing = (AsnDocInfo) docMap.get(docInfo.key);
			if (existing == null) {
				// this.put(docInfo, asnDoc);
			}
			else {
				prtln ("ASN_DOC_INFO WOULD NOT HAVE BEEN WRITTEN");
			}

			this.put(docInfo, asnDoc);

			// if this doc is not already in the library - write it
			try {
				this.syncWithLibrary(asnDoc, key);
			} catch (Exception exp) {
				throw new Exception ("could not sync with library: " + exp.getMessage());
			}

		} catch (Throwable e) {
			e.printStackTrace();
			throw new Exception("register ERROR (" + key + "): " + e.getMessage());
		}

		prtln ("registered " + key);

		return this.getDocInfo(key);
	}

	public AsnDocInfo registerId (String docId) throws Exception {
		// is a doc for this docId already registered??
		if (docId == null || docId.trim().length() == 0)
			throw new Exception ("registerId was given a null docId");

		// first try loaded keys, then library keys
		String key = this.getKey(docId);
		if (key != null)
			key = this.getLibraryKey(key);
		if (key != null)
			return register(key);

		// if not, fetch it
		AsnDocument asnDoc = AsnClientToolkit.fetchAsnDocument(docId);
		if (asnDoc == null)
			throw new Exception ("could not fetch ASN doc from ASN Service for " + docId);
		return this.register(asnDoc);
	}


	
	/**
	 * Creates a new standards document (tree) and adds it to the treeCache and docIdMap.
	 *
	 *@param  docInfo        NOT YET DOCUMENTED
	 *@param  asnDoc         NOT YET DOCUMENTED
	 *@exception  Exception  NOT YET DOCUMENTED
	 */
	private void put(AsnDocInfo docInfo, AsnDocument asnDoc) throws Exception {
		synchronized (lock) {
			docMap.put(docInfo.key, docInfo);
			AsnStandardsDocument tree = new AsnStandardsDocument(asnDoc);
			this.treeCache.addTree(docInfo.key, tree);
			docIdMap.put(asnDoc.getIdentifier(), docInfo.key);
		}
	}

	public AsnDocInfo get (String arg) {
		prtln ("\nGET: " + arg);
		AsnDocInfo docInfo = null;
		String key = null;
		if (arg.startsWith(Constants.ASN_PURL_BASE)) {
			prtln ("purl provided");
			key = this.getKey(arg);
			if (key == null) {
				// see if there is a library file for this docId
				key = this.getLibraryKey(arg);
				if (key == null) {
					prtln ("no key found for " + arg);
					try {
						return registerId(arg);
					} catch (Exception e) {
						prtln ("could not register doc for provded docId: " + arg);
					}
				}
				else {
					// we got a key, will be processed below
					arg = key;
				}
			}
		}

		// arg is a key
		docInfo  = this.getDocInfo (arg);
		if (docInfo == null) {
			try {
				docInfo = this.register(arg);
			} catch (Exception e) {
				prtln ("could not register doc for key: " + arg);
			}
		}
		return docInfo;
	}

	/**
	 *  delete standards doc from registry and tree cache
	 *
	 *@param  docInfo  the docInfo for the document to be deleted
	 */
	private void del(AsnDocInfo docInfo) {
		synchronized (lock) {
			docMap.remove(docInfo.key);
			this.treeCache.removeTree(docInfo.key);
			docIdMap.remove(docInfo.identifier);
		}
	}


	/**
	 *  Gets the rejectedDocs attribute of the StandardsRegistry object
	 *
	 *@return    The rejectedDocs value
	 */
	public List getRejectedDocs() {
		return this.rejectedDocs;
	}

	/**
	 *  Create a AsnStandardsDocument, trying first to read from file, and second
	 fetching the doc from ASN service via AsnClientToolkit.
	 *
	 *@param  key            asnDocKey
	 *@return                ASN standards doc associated with provided key
	 *@exception  Exception  if standards doc could not be instantitated
	 */
	protected AsnStandardsDocument instantiateStandardsDocument(String key) throws Exception {
		// prtln("instantiateStandardsDocument() key: " + key);

		AsnDocInfo docInfo = this.getDocInfo(key);
		if (docInfo == null) {
			throw new Exception("could not find docInfo for " + key);
		}
		String path = docInfo.path;
		AsnDocument asnDoc = null;
		if (new File(path).exists()) {
			asnDoc = new AsnDocument(new File(path));
		}
		else {
			prtln ("...fetching AsnDoc for " + key);
			asnDoc = AsnClientToolkit.fetchAsnDocument(docInfo.getDocId());
			this.register(asnDoc);
		}
		if (asnDoc == null)
			throw new Exception("asnDoc could not be instantiated for " + key + ": reason unknown");
		return new AsnStandardsDocument(asnDoc);
	}


	/**
	 *  Remove a standards doc from the registry
	 *
	 *@param  key            key designating the doc to unregister
	 *@exception  Exception  NOT YET DOCUMENTED
	 */
	private synchronized void unregister(String key) throws Exception {
		docMap.remove(key);
		// treeMap.remove(key);
		this.treeCache.removeTree(key);
		for (Iterator i = this.docIdMap.keySet().iterator(); i.hasNext(); ) {
			String docId = (String) i.next();
			String docKey = (String) docIdMap.get(docId);
			if (key.equals(docKey)) {
				docIdMap.remove(docId);
				return;
			}
		}
	}


	/**
	 *  Gets the key (e.g., "NSES.Science.1995.D10001D0") corresponding to provided Asn
	 *  Identifier (i.e, purl) for that Standards Doc.
	 *
	 *@param  docId  full ASN Id for a standards Document
	 *@return        the key used by the registry for this document
	 */
	public String getKey(String docId) {
		return (String) this.docIdMap.get(docId);
	}


	/**
	 *  Gets the keys of all registered standards Documents
	 *
	 *@return    The keys value
	 */
	public Set getKeys() {
		return this.docMap.keySet();
	}

	/** returns true if a document. as specifed by docId, is currently registered */
	public boolean getDocIsRegistered (String docId) {
		return (this.docIdMap.containsKey(docId));
	}

	/**
	 *  Gets the AsnDocInfo for the provided key
	 *
	 *@param  path  NOT YET DOCUMENTED
	 *@return       The asnDocByPath value
	 */
	public AsnDocInfo getAsnDocByKey(String key) {
		for (Iterator i = this.docMap.values().iterator(); i.hasNext(); ) {
			AsnDocInfo doc = (AsnDocInfo) i.next();
			if (doc.key.equals(key)) {
				return doc;
			}
		}
		return null;
	}


	/**
	 *  Gets the docInfo attribute of the StandardsRegistry object
	 *
	 *@param  key  key of form "author.topic.year"
	 *@return      The docInfo value
	 */
	public AsnDocInfo getDocInfo(String key) {
		return (AsnDocInfo) docMap.get(key);
	}


	/**
	 *  Gets the standardsTree associated with the provided key
	 *
	 *@param  key  NOT YET DOCUMENTED
	 *@return      The standardsTree value
	 */
	public AsnStandardsDocument getStandardsDocument(String key) {
		return this.treeCache.getTree(key);
	}


	/**
	 *  Gets the standardsTree corresponding to the ASN Document having the ASN
	 *  identifier (purl)
	 *
	 *@param  docId  ASN Purl Id
	 *@return        The standardsTree for that id
	 */
	public AsnStandardsDocument getStandardsDocumentForDocId(String docId) {
		String key = (String) this.docIdMap.get(docId);
		if (key == null) {
			return null;
		}
		return getStandardsDocument(key);
	}


	/**
	 *  Gets the standardsNode having the provided AsnID
	 *
	 *@param  asnId  an ASN identifier (purl)
	 *@return        The standardsNode value
	 */
	public AsnStandardsNode getStandardsNode(String asnId) {
		try {
			return this.treeCache.getStandardsNode(asnId);
		} catch (Exception e) {
			// prtln("Standard not found for \"" + asnId + "\": " + e.getMessage());
		}
		return null;
	}

	public AsnStandard getStandard (String asnId) {
		try {
			return this.getStandardsNode(asnId).getAsnStandard();
		} catch (Throwable t) {
			// prtln ("could not find node for " + asnId);
		}
		return null;
	}

	/**
	 *  Find a key for a registered standards doc that matches provided key (which may contain wildcards)
	 *
	 *@param  key  key to match againsted registred doc
	 *@return      key of matching registered doc
	 */
	public String matchKey(String key) {
		if (key == null)
			return null;
		return matchKey(new DocMatchKey(key));
	}


	/**
	 *  Find a key for a registered standards doc that matches provided DocKeyMatch instance.
	 *
	 *@param  docMatchKey  pattern to match against
	 *@return              matched key
	 */
	public String matchKey(DocMatchKey docMatchKey) {
		for (Iterator i = this.docMap.keySet().iterator(); i.hasNext(); ) {
			String myKey = (String) i.next();
			DocMatchKey myDocMatchKey = new DocMatchKey(myKey);
			if (docMatchKey.matches(myDocMatchKey)) {
				return myKey;
			}
		}
		prtln("no matching key found for \"" + docMatchKey.toString() + "\"");
		return null;
	}


	/**
	 *  Return the AsnDocInfo's for registered standards documents
	 *
	 *@return    The asnDocuments value
	 */
	public List getDocInfos() {
		List docList = new ArrayList();
		for (Iterator i = this.docMap.values().iterator(); i.hasNext(); ) {
			AsnDocInfo doc = (AsnDocInfo) i.next();
			docList.add(doc);
		}
		return docList;
	}

	public TreeCache getTreeCache() {
		return this.treeCache;
	}

	public Map getAllCatDocs () throws Exception {
		CATServiceToolkit catToolkit = new CATServiceToolkit();
		if (this.allCatDocs == null) {
			this.allCatDocs = catToolkit.getAllCatDocs();
		}

		return this.allCatDocs;
	}

	public void compareWithCatDocs () {
		prtln ("Comparing Loaded Docs with CAT docs");
		Map allCatDocs = null;
		try {
			allCatDocs = getAllCatDocs();
		} catch (Exception e) {
			prtln ("could not get allCatDocs: " + e.getMessage());
			return;
		}

		List foundDocs = new ArrayList();
		List missingDocs = new ArrayList();

		prtln (allCatDocs.size() + " cat docs found");
		for (Iterator i=this.getDocInfos().iterator();i.hasNext();) {
			AsnDocInfo docInfo = (AsnDocInfo)i.next();
			String docId = docInfo.getDocId();
			// prtln ("\nlooking for " + docId);
			if (!allCatDocs.containsKey(docId)) {
				// prtln ("\ncouldn't find " + docInfo.toString());
				String subject = docInfo.getTopic();
				String author = docInfo.getAuthor();
				String year = docInfo.getCreated();
				missingDocs.add (docInfo);
			}
			else {
				// prtln ("found!");
				foundDocs.add (docInfo);
			}
		}

		prtln (foundDocs.size() + "/" + this.getDocInfos().size() + " found");
		prtln ("Docs that could not be found:");
		for (Iterator i=missingDocs.iterator();i.hasNext();) {
			AsnDocInfo docInfo = (AsnDocInfo)i.next();
			String docId = docInfo.getDocId();
			String subject = docInfo.getTopic();
			String author = docInfo.getAuthor();
			String year = docInfo.getCreated();
			prtln (docInfo.toString());
			prtln ("");
		}
	}



	/**
	 *  The main program for the StandardsRegistry class
	 *
	 *@param  args           The command line arguments
	 *@exception  Exception  NOT YET DOCUMENTED
	 */
	public static void main(String[] args) throws Exception {
		prtln("\n------------------------------------\n");

		File propsFile = new File ("C:/mykeys/cat_service.properties");
		CATServiceToolkit.propertiesFile = propsFile;

		AsnCatalog.setMappingsDirectory("/Users/ostwald/devel/projects/dcs-project/web/WEB-INF/data/ACSR-to-ASN-Mappings");
		StandardsRegistry.setLibraryDir (new File ("/Documents/Work/DLS/ASN/standards-documents/v3.1"));

		// AsnCatalog.setMappingsDirectory("C:/Documents and Settings/ostwald/devel/projects/dcs-project/web/WEB-INF/data/ACSR-to-ASN-Mappings");
		// StandardsRegistry.setLibraryDir (new File ("C:/Program Files/Apache Software Foundation/Tomcat 5.5/var/standards_library"));

		StandardsRegistry reg = StandardsRegistry.getInstance();

		// reg.load();
		// reg.report();

		reg.register("Arizona.World History.2006.D1000288");
//		reg.register("NSES.Science.1995.D10001D0");
//		reg.register("Arizona.The Arts.2006.D1000305");

		reg.report();
		String asnDocId = "http://purl.org/ASN/resources/D1000288";
		String asnDocKey = "Arizona.World History.2006.D1000288";

		String noRegKey = "NSES.Science.1995.D10001D0";
		String noRegId = "http://purl.org/ASN/resources/D10001D0";

		AsnDocInfo docInfo = reg.get(noRegId);

		if (docInfo == null)
			prtln ("doc not found");
		else
			docInfo.report();

		// reg.register("2007-Colorado-Science-Model_Content_Standards_Science")

		/*
		 *  prtln ("\nRegistry Contents");
		 *  for (Iterator i = docs.iterator(); i.hasNext(); ) {
		 *  AsnDocInfo docInfo = (AsnDocInfo) i.next();
		 *  if (docInfo != null)
		 *  docInfo.report();
		 *  else
		 *  prtln ("docInfo not found!");
		 *  }
		 */
/* 		 String asnId = "http://purl.org/ASN/resources/S1005D1E";
		 prtln("\nfinding node for " + asnId); */
/* 		 AsnStandardsNode node = reg.getStandardsNode(asnId);
		 if (node == null)
			 prtln("not found");
		 else {
			 prtln("found: " + node.getId());
			 AsnStandard std = node.getAsnStandard ();
			 prtln (std.toString());
		 } */

		 // prtln (reg.getStandard(asnId).toString());
		 // reg.compareWithCatDocs();
	}


	/**
	 *  NOT YET DOCUMENTED
	 *
	 *@param  s  NOT YET DOCUMENTED
	 */
	private static void prtln(String s) {
		if (debug) {
			// System.out.println("StandardsRegistry: " + s);
			SchemEditUtils.prtln(s, "Registry");
		}
	}


	/**
	 *  Sets the debug attribute of the StandardsRegistry class
	 *
	 *@param  bool  The new debug value
	 */
	public static void setDebug(boolean bool) {
		debug = bool;
	}


	/**
	 *  Debugging utility
	 */
	public void report() {
		prtln("\nStandardsRegistry report");

		Set keys = this.docMap.keySet();
		prtln(keys.size() + " documents registered");

		// Collections.sort (keys);
		for (Iterator i = keys.iterator(); i.hasNext(); ) {
			String key = (String) i.next();
			AsnDocInfo docInfo = this.getDocInfo(key);
			String s = "\nkey: " + docInfo.getKey();
			s += "\n\t" + "asnId: " + docInfo.identifier;
			s += "\n\t" + "title: " + docInfo.getTitle();
			prtln(s);
			// prtln("\t key: " + key + "\n\tasnId: " + docId + "\n\t" + );
		}
		// this.treeCache.report();
	}


	/*
	 *  Represents a DocInfo key used to access the registered documents using wildcarding
	 *  for various key fields.
	 */
	/**
	 *  Description of the Class
	 *
	 *@author     ostwald
	 *@created    June 25, 2009
	 */
	private class DocMatchKey {
		String author = "*";
		String topic = "*";
		String year = "*";


		/**
		 *  Constructor for the DocMatchKey object
		 *
		 *@param  author  the author field
		 *@param  topic   the topic field
		 *@param  year    the year field
		 */
		DocMatchKey(String author, String topic, String year) {
			this.author = valueOrWildcard(author);
			this.topic = valueOrWildcard(topic);
			this.year = valueOrWildcard(year);
		}


		/**
		 *  Constructor for the DocMatchKey object from provided string, using
		 *  wildcards for unspecified fields.
		 *
		 *@param  key  a '.' delimited string
		 */
		DocMatchKey(String key) {
			String[] splits = key.split("\\.");

			try {
				this.author = valueOrWildcard(splits[0]);
				if (splits.length > 1) {
					this.topic = valueOrWildcard(splits[1]);
				}
				if (splits.length > 2) {
					this.year = valueOrWildcard(splits[2]);
				}
				// prtln ("   instantiated: " + this.toString());
			} catch (Throwable t) {
				prtln("DocMatchKey error: " + t.getMessage());
				t.printStackTrace();
			}
		}

		public String toString () {
			return this.author + "." + this.topic + "." + this.year;
		}

		/**
		 *  Returns true if provided DocMatchKey matches this instance, with wildcard
		 *  values always matching (e.g., "*.Science.1995" matches
		 *  "NSES.Science.1995").
		 *
		 *@param  otherKey  NOT YET DOCUMENTED
		 *@return           NOT YET DOCUMENTED
		 */
		boolean matches(DocMatchKey otherKey) {
			// prtln ("matches this: \"" + this.toString() + "\" vs otherKey: \"" + otherKey.toString() + "\"");
			if (!"*".equals(otherKey.author) && !"*".equals(this.author) && !otherKey.author.equals(this.author)) {
				// prtln ("\t authors don't match");
				return false;
			}
			if (!"*".equals(otherKey.topic) && !"*".equals(this.topic) && !otherKey.topic.equals(this.topic)) {
				// prtln ("\t topics don't match");
				return false;
			}
			if (!"*".equals(otherKey.year) && !"*".equals(this.year) && !otherKey.year.equals(this.year)) {
				// prtln ("\t years don't match");
				return false;
			}
			// prtln ("\t => MATCH!");
			return true;
		}


		/**
		 *  NOT YET DOCUMENTED
		 *
		 *@param  s  NOT YET DOCUMENTED
		 *@return    NOT YET DOCUMENTED
		 */
		String valueOrWildcard(String s) {
			String nonValue = "*";
			if (s == null) {
				return nonValue;
			}
			s = s.trim();
			if (s.length() == 0) {
				return nonValue;
			}
			if (s.equals("null")) {
				return nonValue;
			}
			return s;
		}
	}
}
