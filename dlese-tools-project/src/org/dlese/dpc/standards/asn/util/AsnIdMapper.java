package org.dlese.dpc.standards.asn.util;

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
 *  Reads mappings between ACSR_IDs and ASN Ids from XML files.
 *
 * @author    Jonathan Ostwald
 */
public class AsnIdMapper {
	private static boolean debug = true;

	private String mappingsDirectory;
	private Map acsrIdMap = null;
	private Map asnIdMap = null;


	/**
	 *  Constructor for the AsnIdMapper object
	 *
	 * @param  mappingsDirectory  NOT YET DOCUMENTED
	 * @exception  Exception      NOT YET DOCUMENTED
	 */
	public AsnIdMapper(String mappingsDirectory) throws Exception {
		if (mappingsDirectory == null)
			throw new Exception ("mappings directory not supplied");
		this.mappingsDirectory = mappingsDirectory;
		this.acsrIdMap = new HashMap();
		this.asnIdMap = new HashMap();
		this.readMappingFiles();
	}

	/**
	 *  Gets the asnId attribute of the AsnIdMappings object
	 *
	 * @param  acsrId  NOT YET DOCUMENTED
	 * @return         The asnId value
	 */
	public String getAsnId(String acsrId) {
		return (String) acsrIdMap.get(acsrId);
	}


	/**
	 *  Gets the acsrId attribute of the AsnIdMappings object
	 *
	 * @param  asnId  NOT YET DOCUMENTED
	 * @return        The acsrId value
	 */
	public String getAcsrId(String asnId) {
		return (String) asnIdMap.get(asnId);
	}


	// ------------- initialize -----------------

	/**
	 *  Read all mapping files in the mappings directory. If this directory does
	 *  not exist no mappings will be read.
	 *
	 * @exception  Exception  if an existing mappings file cannot be processed
	 */
	public void readMappingFiles() throws Exception {

		File mappingsDir = new File(mappingsDirectory);
		if (!mappingsDir.exists()) {
			throw new Exception("mappings directory does not exist at " + mappingsDirectory);
		}

		File[] subjectFiles = mappingsDir.listFiles(new XMLFileFilter());

		for (int i = 0; i < subjectFiles.length; i++) {
			try {
				readMappingFile(subjectFiles[i]);
			} catch (Exception e) {
				throw new Exception("could not process " + subjectFiles[i].getName() + ": " + e.getMessage());
			}
		}
	}


	/**
	 *  Read a mapping file for a particular subject and add the mappings to the
	 *  AsnIdMapper
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
			XPath acsrXpath = DocumentHelper.createXPath("acsrInfo/@id");
			String acsrId = acsrXpath.valueOf(mapping);
			XPath asnXpath = DocumentHelper.createXPath("asnInfo/@id");
			String asnId = asnXpath.valueOf(mapping);
			// prtln (acsrId + " -> " + asnId);
			this.asnIdMap.put(asnId, acsrId);
			this.acsrIdMap.put(acsrId, asnId);
		}
	}

	/**  produce debugging report for this AsnIdMapper */
	public void report() {
		prtln("\nAsnIdMapper report");
		prtln(" - " + this.acsrIdMap.size() + " acsrIDs");
		prtln(" - " + this.asnIdMap.size() + " asnIDs");
	}


	/**
	 *  The main program for the AsnIdMapper class
	 *
	 * @param  args           The command line arguments
	 * @exception  Exception  NOT YET DOCUMENTED
	 */
	public static void main(String[] args) throws Exception {
		TesterUtils.setSystemProps();
		String mappingsDirectory = "C:/tmp/ACSR-to-ASN-Mappings";
		// String mappingsDirectory = "/Users/ostwald/devel/projects/dcs-project/web/WEB-INF/data/ACSR-to-ASN-Mappings/";
		AsnIdMapper mapper = new AsnIdMapper(mappingsDirectory);
		mapper.report();

		// test the id maps
		 		prtln("");
		String asnId = "http://purl.org/ASN/resources/D1000034";
		String acsrId = mapper.getAcsrId(asnId);
		prtln("acsrId: " + acsrId);
		prtln("asnId: " + mapper.getAsnId(acsrId) + " (" + asnId + ")");

	}




	private static void prtln(String s) {
		if (debug) {
			SchemEditUtils.prtln(s, "");
		}
	}

}

