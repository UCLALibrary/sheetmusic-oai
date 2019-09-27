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
package org.dlese.dpc.schemedit.standards.asn;

import org.dlese.dpc.schemedit.SchemEditUtils;
import org.dlese.dpc.schemedit.standards.StandardsRegistry;
import org.dlese.dpc.standards.asn.util.AsnCatalog.AsnCatItem;
import org.dlese.dpc.standards.asn.AsnDocument;
import org.dlese.dpc.serviceclients.cat.CATStandardDocument;

import java.io.File;

/**
 *  Bean to hold summary information about {@link AsnDocument} for use by the
 *  suggestion service.
 *
 * @author    Jonathan Ostwald
 */

public class AsnDocInfo {

	private static boolean debug = true;

	/**  NOT YET DOCUMENTED */
	public String path;
	/**  NOT YET DOCUMENTED */
	public String author;
	/**  NOT YET DOCUMENTED */
	public String topic;
	/**  NOT YET DOCUMENTED */
	public String fileCreated;
	/**  NOT YET DOCUMENTED */
	public String created;
	/**  NOT YET DOCUMENTED */
	public int numItems;
	/**  NOT YET DOCUMENTED */
	public String filename;
	/**  NOT YET DOCUMENTED */
	public String identifier;
	/**  NOT YET DOCUMENTED */
	public String title;
	/**  NOT YET DOCUMENTED */
	public String key;
	/**  NOT YET DOCUMENTED */
	public String uid;
	public String status;


	/**
	 *  Constructor for the AsnDocInfo object
	 *
	 * @param  asnDoc  the full AsnDocument object to be summarized
	 */
	public AsnDocInfo(AsnDocument asnDoc) {
		this.path = asnDoc.getPath();
		this.identifier = asnDoc.getIdentifier();
		this.author = asnDoc.getAuthor();
		this.topic = asnDoc.getTopic();
		this.fileCreated = asnDoc.getFileCreated();
		this.created = asnDoc.getCreated();
		this.title = asnDoc.getTitle();
		this.numItems = asnDoc.getIdentifiers().size();
		this.filename = new File(path).getName();
		this.key = new AsnDocKey(asnDoc).toString();
		this.uid = this.makeUid();
		this.status = null;
	}


	/**
	 *  Constructor for the AsnDocInfo object for a CATStandardDocument, which
	 *  comes from the CAT service, rather than from file, and has less
	 *  information, e.g., no filename).
	 *
	 * @param  catDocInfo  NOT YET DOCUMENTED
	 */
	public AsnDocInfo(CATStandardDocument catDocInfo) {
		this.identifier = catDocInfo.getId();
		this.author = catDocInfo.getAuthor();
		this.topic = catDocInfo.getTopic();
		this.created = catDocInfo.getCreated();
		this.title = catDocInfo.getTitle();
		this.uid = this.makeUid();
		this.key = new AsnDocKey(this.author, this.topic, this.created, this.uid).toString();
		this.status = null;
		
	}

	public AsnDocInfo(AsnCatItem asnCatItem) {
		this.identifier = asnCatItem.getId();
		this.author = asnCatItem.getAuthor();
		this.topic = asnCatItem.getTopic();
		this.created = asnCatItem.getCreated();
		this.title = asnCatItem.getTitle();
		this.uid = asnCatItem.getUid();
		this.key = asnCatItem.getKey();
		this.status = asnCatItem.getStatus();
	}

	/**
	 *  Gets the docId attribute of the AsnDocInfo object
	 *
	 * @return    The docId value
	 */
	public String getDocId() {
		return this.identifier;
	}


	/**
	 *  Gets the key attribute of the AsnDocInfo object
	 *
	 * @return    The key value
	 */
	public String getKey() {
		return this.key;
	}


	/**
	 *  Gets the author attribute of the AsnDocInfo object
	 *
	 * @return    The author value
	 */
	public String getAuthor() {
		return this.author;
	}


	/**
	 *  Gets the topic attribute of the AsnDocInfo object
	 *
	 * @return    The topic value
	 */
	public String getTopic() {
		return this.topic;
	}


	/**
	 *  Gets the title attribute of the AsnDocInfo object
	 *
	 * @return    The title value
	 */
	public String getTitle() {
		return this.title;
	}


	/**
	 *  Gets the created attribute of the AsnDocInfo object
	 *
	 * @return    The created value
	 */
	public String getCreated() {
		return this.created;
	}


	/**
	 *  Gets the source attribute of the AsnDocInfo object
	 *
	 * @return    The source value
	 */
	public File getSource() {
		return new File(this.path);
	}


	/**
	 *  Gets the uid attribute of the AsnDocInfo object
	 *
	 * @return    The uid value
	 */
	public String getUid() {
		return this.uid;
	}


	/**
	 *  Gets the unique part of the ASN purl id, creating one from the identifier
	 *  when necessary (e.g., for a AsnDocInfo crated from CATServiceDocument)
	 *
	 * @return    The uid value (e.g., "S1015D9B")
	 */
	public String makeUid() {
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

	public void setStatus(String status) {
		this.status = status;
	}

	public String getStatus () {
		return this.status;
	}


	/**  NOT YET DOCUMENTED */
	public void report() {
		prtln("\n" + this.key);
		// prtln("\t author: " + this.author);
		// prtln("\t topic: " + this.topic);
		// prtln("\t created: " + this.created);
		// prtln ("\t fileCreated: " + this.fileCreated);
		// prtln("\t identifier: " + this.identifier);
		// prtln("\t numItems: " + this.numItems);
		prtln("\t filename: " + this.filename);
		//prtln ("\t path: " + this.path);
	}


	/**
	 *  NOT YET DOCUMENTED
	 *
	 * @return    NOT YET DOCUMENTED
	 */
	public String toString() {
		String s = "\nkey: " + this.getKey();
		s += "\n\t" + "asnId: " + this.identifier;
		s += "\n\t" + "title: " + this.getTitle();
		return s;
	}


	/**
	 *  NOT YET DOCUMENTED
	 *
	 * @param  s  NOT YET DOCUMENTED
	 */
	private static void prtln(String s) {
		if (debug) {
			SchemEditUtils.prtln(s, "AsnDocInfo");
		}
	}
}
