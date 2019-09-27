package org.dlese.dpc.standards.asn.util;

import org.dlese.dpc.schemedit.standards.asn.AsnDocKey;

import org.dlese.dpc.schemedit.test.TesterUtils;
import org.dlese.dpc.schemedit.SchemEditUtils;
import org.dlese.dpc.xml.Dom4jUtils;
import org.dlese.dpc.xml.XMLFileFilter;
import org.dlese.dpc.util.Files;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;

import org.dom4j.*;

/**
 *  Reads cached files on disk that contain all ASN Standards Documents
 *  associatged with a particular subject/topic (e.g., "Math", "Science"),
 *  available at the time they were compiled<p>
 *
 *  NOTE: getInstance() requires that "setMappingsDirectory" is called prior to
 *  "getInstance". The mapping directory points to the cached XML files, each of
 *  which is associated with a particular subject/topic (e.g., "Math",
 *  "Science"). NOTE: the cached files should be periodically updated from ASN
 *  (see {@link AsnIdMappingsUpdater.java}).
 *
 * @author    Jonathan Ostwald
 */
public class AsnCatalog {
	private static boolean debug = true;

	private File mappingsDir;
	private Map idMap = null;
	private Map subjectIdMap = null;
	private Map subjectItemsMap = null;
	private List subjects = null;

	private static String mappingsDirectory;
	private static AsnCatalog instance = null;


	/**
	 *  Gets the instance attribute of the AsnCatalog class - requires that
	 *  mappingsDirectory has already been initialized via setMappingsDirectory.
	 *
	 * @return                The instance value
	 * @exception  Exception  if mappingsDirectory is not initialized or if files
	 *      could not be processed.
	 */
	public static AsnCatalog getInstance() throws Exception {
		if (mappingsDirectory == null)
			throw new Exception("Cant instantiate: mappingsDirectory not initialized");

		if (instance == null) {
			instance = new AsnCatalog(mappingsDirectory);
		}
		return instance;
	}


	/**
	 *  Sets the mappingsDirectory attribute of the AsnCatalog class
	 *
	 * @param  path  The new mappingsDirectory value
	 */
	public static void setMappingsDirectory(String path) {
		mappingsDirectory = path;
	}


	/**
	 *  Constructor for the AsnCatalog object given path to a mappings directory
	 *
	 * @param  mappingsDirectory  NOT YET DOCUMENTED
	 * @exception  Exception      NOT YET DOCUMENTED
	 */
	public AsnCatalog(String mappingsDirectory) throws Exception {
		if (mappingsDirectory == null)
			throw new Exception("mappings directory not supplied");
		mappingsDir = new File(mappingsDirectory);
		if (!mappingsDir.exists()) {
			throw new Exception("mappings directory does not exist at " + mappingsDirectory);
		}

		this.idMap = new HashMap();
		this.subjectIdMap = new HashMap();
		this.subjectItemsMap = new HashMap();
		this.subjects = getSubjects();

		this.readMappingFiles();
		prtln("AsnCatalog instantiated with " + this.subjects.size() + " subjects");
	}


	/**
	 *  Gets the item attribute of the AsnCatalog object
	 *
	 * @param  asnId  NOT YET DOCUMENTED
	 * @return        The item value
	 */
	public AsnCatItem getItem(String asnId) {
		return (AsnCatItem) idMap.get(asnId);
	}


	/**
	 *  Gets the status attribute of the AsnCatalog object
	 *
	 * @param  asnId  NOT YET DOCUMENTED
	 * @return        The status value
	 */
	public String getStatus(String asnId) {
		// prtln ("getStatus (" + asnId + ")");
		AsnCatItem asnCatItem = this.getItem(asnId);
		if (asnCatItem == null) {
			prtln("WARNING: item not found!");
			return "";
		}

		return asnCatItem.getStatus();
	}


	/**
	 *  Gets the subjects attribute of the AsnCatalog object
	 *
	 * @return    The subjects value
	 */
	public List getSubjects() {
		if (this.subjects == null) {
			this.subjects = new ArrayList();

			File[] subjectFiles = mappingsDir.listFiles(new XMLFileFilter());
			for (int i = 0; i < subjectFiles.length; i++) {
				String filename = subjectFiles[i].getName();
				this.subjects.add(filename.substring(0, filename.length() - 4));
			}
		}
		return this.subjects;
	}


	/**
	 *  Gets the subjectItems attribute of the AsnCatalog object
	 *
	 * @return    The subjectItems value
	 */
	public Map getSubjectItems() {
		return this.subjectItemsMap;
	}


	/**
	 *  Read all mapping files in the mappings directory. If this directory does
	 *  not exist no mappings will be read.
	 *
	 * @exception  Exception  if an existing mappings file cannot be processed
	 */
	public void readMappingFiles() throws Exception {

		for (Iterator i = this.subjects.iterator(); i.hasNext(); ) {
			String subject = (String) i.next();

			try {
				readMappingFile(subject);
			} catch (Exception e) {
				throw new Exception("could not process file for" + subject + ": " + e.getMessage());
			}
		}
	}


	/**
	 *  Reads a mappingFile for the provided subject, and add the mappings to the
	 *  AsnCatalog
	 *
	 * @param  subject        An ASN subject (e.g., "Science")
	 * @exception  Exception  if file could not be read
	 */
	public void readMappingFile(String subject) throws Exception {
		// prtln ("readMappingFile() for " + subject);
		File subjectFile = new File(mappingsDir, subject + ".xml");
		if (!subjectFile.exists()) {
			throw new Exception("subject file does not exist at " + subjectFile);
		}

		try {
			readMappingFile(subjectFile);
		} catch (Exception e) {
			throw new Exception("could not process " + subjectFile.getName() + ": " + e.getMessage());
		}
	}


	/**
	 *  Read a mapping file for a particular subject and add the mappings to the
	 *  AsnCatalog
	 *
	 * @param  source         an XML file containing mappings for a single subject
	 * @exception  Exception  if this file cannot be processed
	 */
	public void readMappingFile(File source) throws Exception {
		Document doc = Dom4jUtils.getXmlDocument(source);
		List mappings = doc.selectNodes("//mapping");
		String fileName = source.getName();
		// prtln ("\nreading " + fileName);
		String subject = source.getName().substring(0, fileName.length() - 4);

		// prtln (mappings.size() + " mappings found");
		for (Iterator i = mappings.iterator(); i.hasNext(); ) {
			Element mapping = (Element) i.next();
			AsnCatItem catItem = new AsnCatItem(mapping.element("asnInfo"));
			// skip docs that have status of "In Process"
			// prtln ("status: " + catItem.getStatus());
			if ("In Process".equals(catItem.getStatus())) {
				// prtln ("Skipping " + catItem.getTitle());
				continue;
			}
			
			this.idMap.put(catItem.getId(), catItem);

			List subjectItemIds = (List) this.subjectIdMap.get(subject);
			if (subjectItemIds == null)
				subjectItemIds = new ArrayList();
			subjectItemIds.add(catItem.getId());
			this.subjectIdMap.put(subject, subjectItemIds);

			List subjectItems = (List) this.subjectItemsMap.get(subject);
			if (subjectItems == null)
				subjectItems = new ArrayList();
			subjectItems.add(catItem);
			this.subjectItemsMap.put(subject, subjectItems);
		}
	}


	/**  produce debugging report for this AsnCatalog */
	public void report() {
		prtln("\nAsnCatalog report");
		prtln(this.idMap.size() + " asnIDs read into the following collections");
		for (Iterator i = this.subjects.iterator(); i.hasNext(); ) {
			String subject = (String) i.next();
			prtln(" - " + subject);
		}
	}


	/**
	 *  The main program for the AsnCatalog class
	 *
	 * @param  args           The command line arguments
	 * @exception  Exception  NOT YET DOCUMENTED
	 */
	public static void main(String[] args) throws Exception {
		TesterUtils.setSystemProps();
		// String mappingsDirectory = "/Users/ostwald/devel/projects/dcs-project/web/WEB-INF/data/ACSR-to-ASN-Mappings";
		String mappingsDirectory = "C:/Documents and Settings/ostwald/devel/projects/dcs-project/web/WEB-INF/data/ACSR-to-ASN-Mappings";
		AsnCatalog asnCat = new AsnCatalog(mappingsDirectory);

		// test the id maps

		asnCat.readMappingFile("Math");

		asnCat.report();

		prtln("");
		String asnId = "http://purl.org/ASN/resources/D1000223";
		AsnCatItem item = asnCat.getItem(asnId);
		prtln(item.toString());

		prtln("status of " + asnId + ": " + asnCat.getStatus(asnId));

	}


	/**
	 *  Represents a single ASN Catalog item (an ASN Document) with attributes
	 *  obtained from mapping file
	 *
	 * @author    Jonathan Ostwald
	 */
	public class AsnCatItem {

		Element element = null;
		String id;
		String title;
		String created;
		String topic;
		String author;
		String key;
		String uid;
		String status;


		/**
		 *  Constructor for the AsnCatItem object
		 *
		 * @param  element  NOT YET DOCUMENTED
		 */
		public AsnCatItem(Element element) {
			this.element = element;
			this.id = this.element.attributeValue("id");
			this.title = this.getSubElementValue("title");
			this.created = this.getSubElementValue("created");
			this.topic = this.getSubElementValue("topic");
			this.author = this.getSubElementValue("author");
			this.status = this.getStatus();
		}


		/**
		 *  hack to get the status out of the acsrInfo branch, which is a sibling to
		 *  this.element
		 *
		 * @return    The status value
		 */
		public String getStatus() {
			Element statusEl = null;
			try {
				statusEl = (Element) this.element.getParent().selectSingleNode("acsrInfo/status");
				if (statusEl == null)
					throw new Exception("statusEl was null");
			} catch (Exception e) {
				prtln("status not found for " + this.id);
				return "";
			}
			return statusEl.getTextTrim();
		}


		/**
		 *  Gets the uid attribute of the AsnCatItem object
		 *
		 * @return    The uid value
		 */
		public String getUid() {

			if (this.uid == null) {
				try {
					this.uid = new File(this.id).getName();
				} catch (Throwable t) {
					prtln("makeAsnDocKey error (" + t.getMessage() + ") - will use full purl id for uid");
					this.uid = "???";
				}
			}
			return this.uid;
		}


		/**
		 *  Gets the key attribute of the AsnCatItem object
		 *
		 * @return    The key value
		 */
		public String getKey() {
			if (this.key == null) {
				AsnDocKey docKey = new AsnDocKey(this.author, this.topic, this.created, this.getUid());
				this.key = docKey.toString();
			}
			return this.key;
		}


		/**
		 *  Gets the id attribute of the AsnCatItem object
		 *
		 * @return    The id value
		 */
		public String getId() {
			return this.id;
		}


		/**
		 *  Gets the title attribute of the AsnCatItem object
		 *
		 * @return    The title value
		 */
		public String getTitle() {
			return this.title;
		}


		/**
		 *  Gets the created attribute of the AsnCatItem object
		 *
		 * @return    The created value
		 */
		public String getCreated() {
			return this.created;
		}


		/**
		 *  Gets the topic attribute of the AsnCatItem object
		 *
		 * @return    The topic value
		 */
		public String getTopic() {
			return this.topic;
		}


		/**
		 *  Gets the author attribute of the AsnCatItem object
		 *
		 * @return    The author value
		 */
		public String getAuthor() {
			return this.author;
		}


		/**
		 *  Gets the subElementValue attribute of the AsnCatItem object
		 *
		 * @param  tag  NOT YET DOCUMENTED
		 * @return      The subElementValue value
		 */
		String getSubElementValue(String tag) {
			try {
				return this.element.element(tag).getTextTrim();
			} catch (Exception e) {
				prtln("getSubElementValue error: " + e.getMessage());
			}
			return "";
		}


		/**
		 *  NOT YET DOCUMENTED
		 *
		 * @return    NOT YET DOCUMENTED
		 */
		public String toString() {
			String NL = "\n\t";
			String s = this.id;
			s += NL + "title: " + this.title;
			s += NL + "created: " + this.created;
			s += NL + "topic: " + this.topic;
			s += NL + "author: " + this.author;
			s += NL + "status: " + this.status;
			return s;
		}
	}


	/**
	 *  NOT YET DOCUMENTED
	 *
	 * @param  s  NOT YET DOCUMENTED
	 */
	private static void prtln(String s) {
		if (debug) {
			SchemEditUtils.prtln(s, "AsnCatalog");
		}
	}

}

