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
package org.dlese.dpc.schemedit.standards.action;

import org.dlese.dpc.standards.asn.util.AsnCatalog;

import org.dlese.dpc.schemedit.FrameworkRegistry;
import org.dlese.dpc.schemedit.MetaDataFramework;
import org.dlese.dpc.schemedit.standards.StandardsManager;
import org.dlese.dpc.schemedit.standards.asn.AsnStandardsManager;
import org.dlese.dpc.schemedit.standards.asn.AsnDocInfo;
import org.dlese.dpc.xml.*;

import org.dom4j.Document;

import java.util.*;
import java.io.*;
import java.net.*;
import java.text.*;


/**
 *  A Struts Action controlling interaction during editing of workflow status
 *  information asociated with individual metadata records.
 *
 *@author    Jonathan Ostwald
 */
public final class StandardsManagerBean {

	private static boolean debug = false;

	private AsnStandardsManager standardsManager = null;
	private List subjects = null;
	private List availableDocs = null;
	private List extraDocIds = null;
	private List extraDocs = null;
	private SubjectStandardsMap subectExtraDocsMap = null;
	private Map subjectIdsMap = null;
	private Map subjectItemsMap = null;
	private SubjectStandardsMap subjectAllDocsMap = null;
	private SubjectStandardsMap subjectAllDocIdsMap = null;
	private Map idMap = null;

	private AsnCatalog asnCatalog = null;
	private FrameworkRegistry frameworkRegistry = null;
	private Map standardsManagerMap = null;


	/**
	 *  Constructor for the StandardsManagerBean object
	 *
	 *@param  standardsManager  Description of the Parameter
	 */
	public StandardsManagerBean(AsnStandardsManager standardsManager) {
		this.standardsManager = standardsManager;
		this.availableDocs = standardsManager.getAvailableDocs();

		this.idMap = new HashMap();
		this.subjectIdsMap = new HashMap();
		this.subjectItemsMap = new HashMap();

		this.subjects = new ArrayList();

		// prtln (this.availableDocs.size() + " docs found");
		for (Iterator i = this.availableDocs.iterator(); i.hasNext(); ) {
			AsnDocInfo docInfo = (AsnDocInfo) i.next();
			String subject = docInfo.getTopic();
			String docId = docInfo.getDocId();
			this.idMap.put(docId, docInfo);

			if (!this.subjects.contains(subject))
				this.subjects.add(subject);

			List subjectItemIds = (List) this.subjectIdsMap.get(subject);
			if (subjectItemIds == null) {
				subjectItemIds = new ArrayList();
			}
			subjectItemIds.add(docId);
			this.subjectIdsMap.put(subject, subjectItemIds);

			List subjectItems = (List) this.subjectItemsMap.get(subject);
			if (subjectItems == null) {
				subjectItems = new ArrayList();
			}
			subjectItems.add(docInfo);
			this.subjectItemsMap.put(subject, subjectItems);
		}

		Collections.sort(this.subjects);
		// prtln ("instantiated with " + this.subjects.size() + " subjects");
	}


	/**
	 *  Gets the subjects attribute of the StandardsManagerBean object
	 *
	 *@return    The subjects value
	 */
	public List getSubjects() {
		return this.subjects;
	}

	public Set getAllSubjects() {
		return this.getSubjectAllDocsMap().keySet();
	}

	public List getExtraDocIds () {
		return standardsManager.getExtraDocIds();
	}

	public List getExtraDocs() {
		return this.standardsManager.getExtraDocs();
	}

	public SubjectStandardsMap getSubectExtraDocsMap () {
		if (this.subectExtraDocsMap == null) {
			prtln ("\ncreating subjectExtraDocsMap - " + this.getExtraDocIds().size() + " extra docs to map");
			this.subectExtraDocsMap = new SubjectStandardsMap();
			for (Iterator i=this.getExtraDocs().iterator();i.hasNext();) {
				AsnDocInfo docInfo = (AsnDocInfo)i.next();
				String subject = docInfo.getTopic();
				prtln (" - " + subject + "  " + docInfo.getKey());
				this.subectExtraDocsMap.add (subject, docInfo);
			}
		}
		return this.subectExtraDocsMap;
	}

	public Map getSubectExtraDocsMapOLD () {
		if (this.subectExtraDocsMap == null) {
			prtln ("\ncreating subjectExtraDocsMap - " + this.getExtraDocIds().size() + " extra docs to map");
			this.subectExtraDocsMap = new SubjectStandardsMap();
			for (Iterator i=this.getExtraDocs().iterator();i.hasNext();) {
				AsnDocInfo docInfo = (AsnDocInfo)i.next();
				String subject = docInfo.getTopic();
				prtln (" - " + subject + "  " + docInfo.getKey());
				List values = null;
				if (this.subectExtraDocsMap.containsKey(subject)) {
					values = (List)this.subectExtraDocsMap.get(subject);
				}
				else {
					values = new ArrayList();
				}
				values.add (docInfo);
				this.subectExtraDocsMap.put (subject, values);
			}
		}
		return this.subectExtraDocsMap;
	}

	public SubjectStandardsMap getSubjectAllDocIdsMap () {
		if (this.subjectAllDocIdsMap == null) {
			try {
				this.subjectAllDocIdsMap = new SubjectStandardsMap();
				SubjectStandardsMap subjectAllDocsMap = this.getSubjectAllDocsMap();
				Iterator subjectItr = subjectAllDocsMap.keySet().iterator();
				while (subjectItr.hasNext()) {
					String subject = (String)subjectItr.next();
					List items = subjectAllDocsMap.getItems(subject);
					Iterator itemItr = items.iterator();
					while (itemItr.hasNext()) {
						AsnDocInfo docInfo = (AsnDocInfo)itemItr.next();
						this.subjectAllDocIdsMap.add (subject, docInfo.getDocId());
					}
				}
				prtln ("successfully populated subjectAllDocIdsMap");
			} catch (Throwable t) {
				prtln ("ERROR unable to populate subjectAllDocIdsMap: " + t.getMessage());
				t.printStackTrace();
				this.subjectAllDocIdsMap = null;
			}
		}
		return this.subjectAllDocIdsMap;
	}


	public SubjectStandardsMap getSubjectAllDocsMap () {
		prtln ("getSubjectAllDocsMap");
		if (this.subjectAllDocsMap == null) {
			SubjectStandardsMap map = new SubjectStandardsMap();
			for (Iterator i=this.getSubjectItemsMap().keySet().iterator();i.hasNext();) {
				String subject = (String)i.next();
				prtln (subject);
				List DocIds = (List)this.getSubjectItemsMap().get(subject);
				for (Iterator j=DocIds.iterator();j.hasNext();) {
					AsnDocInfo docInfo = (AsnDocInfo)j.next();
					map.add (subject, docInfo);
					prtln (" - " + docInfo.key);
				}
			}

			prtln ("getting extra docs");
			Map subectExtraDocsMap = this.getSubectExtraDocsMap();
			for (Iterator i=this.subectExtraDocsMap.keySet().iterator();i.hasNext();) {
				String subject = (String)i.next();
				prtln (subject);
				List extraDocs = (List)subectExtraDocsMap.get(subject);
				if (extraDocs != null) {
					prtln (" - extraDocs");
					for (Iterator k=extraDocs.iterator();k.hasNext();) {
						AsnDocInfo docInfo = (AsnDocInfo)k.next();
						map.add (subject, docInfo);
						prtln ("   - " + docInfo.key);
					}

				}
				else {
					prtln (" - no extraDocs for " + subject);
				}
			}
			this.subjectAllDocsMap = map;
		}
		return this.subjectAllDocsMap;
	}
	public SubjectStandardsMap getSubjectAllDocsMapOLD () {
		prtln ("getSubjectAllDocsMap");
		if (this.subjectAllDocsMap == null) {
			SubjectStandardsMap map = new SubjectStandardsMap();
			for (Iterator i=this.getSubjectItemsMap().keySet().iterator();i.hasNext();) {
				String subject = (String)i.next();
				prtln (subject);
				List DocIds = (List)this.getSubjectItemsMap().get(subject);
				for (Iterator j=DocIds.iterator();j.hasNext();) {
					AsnDocInfo docInfo = (AsnDocInfo)j.next();
					map.add (subject, docInfo);
					prtln (" - " + docInfo.key);
				}

				prtln ("getting extra docs");
				List extraDocs = (List)this.getSubectExtraDocsMap().get(subject);
				if (extraDocs != null) {
					prtln (" - extraDocs");
					for (Iterator k=this.extraDocs.iterator();k.hasNext();) {
						AsnDocInfo docInfo = (AsnDocInfo)k.next();
						map.add (subject, docInfo);
						prtln ("   - " + docInfo.key);
					}

				}
				else {
					prtln (" - no extraDocs for " + subject);
				}
			}
			this.subjectAllDocsMap = map;
		}
		return this.subjectAllDocsMap;
	}


	class SubjectStandardsMap extends HashMap {

		public SubjectStandardsMap () {
			super();
		}

		public void add (String key, Object obj) {
			// prtln ("adding " + key + ": " + obj.toString());
			List values = null;
			if (this.containsKey(key))
				values = (List)this.get(key);
			else
				values = new ArrayList();
			if (!values.contains(obj))
				values.add (obj);
			this.put (key, values);
		}

		public List getItems (String key) {
			List items = (List)get(key);
			return (items != null ? items : new ArrayList());
		}

	}

	/**
	 *  Maps subject names to the standard documents associated with the subject for this framework
	 *
	 *@return    mappingi from subject to Lists of AsnDocInfo instances
	 */
	public Map getSubjectItemsMap() {
		return this.subjectItemsMap;
	}

	public Map getSubjectIdsMap() {
		return this.subjectIdsMap;
	}

	public List getSubjectIds (String subject) {
		return (List)this.subjectIdsMap.get(subject);
	}

	/**
	 *  Gets the xmlFormats attribute of the StandardsManagerBean object
	 *
	 *@return    The xmlFormats value
	 */
	public Set getXmlFormats() {
		return this.standardsManagerMap.keySet();
	}



	// -------------- Debug ------------------

	/**
	 *  Sets the debug attribute of the StandardsManagerBean class
	 *
	 *@param  isDebugOutput  The new debug value
	 */
	public static void setDebug(boolean isDebugOutput) {
		debug = isDebugOutput;
	}


	/**
	 *  Print a line to standard out.
	 *
	 *@param  s  The String to print.
	 */
	private static void prtln(String s) {
		if (debug) {
			System.out.println("StandardsManagerBean: " + s);
		}
	}
	/**
	* See schemedit.test.StandardsManagerBeanTester
	*/
	public static void main (String [] args) throws Exception {
		String xmlFormat = "ncs_item";
		String xpath = "/record/educational/standards/asnID";
		// String defaultAuthor = "NSES";
		// String defaultTopic = "Science";
		// String defaultDocKey = AsnDocKey.makeAsnDocKey;
		String defaultDocKey = "NSES";


	}
}
