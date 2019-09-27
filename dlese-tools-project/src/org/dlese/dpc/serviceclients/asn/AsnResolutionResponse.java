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

import org.dlese.dpc.standards.asn.*;

import org.dlese.dpc.util.strings.FindAndReplace;
import org.dlese.dpc.schemedit.SchemEditUtils;
import org.dom4j.*;
import org.dlese.dpc.xml.Dom4jUtils;
import java.util.*;
import java.net.*;

/**
 *  Encapsulates response from the ASN Resolution Service (see http://www.thegateway.org/asn/services-description#uri_resolver).
 *
 * @author    Jonathan Ostwald
 */
public class AsnResolutionResponse extends
	NameSpaceXMLDocReader {

	private static boolean debug = false;
	final static String COLORADO_AUTHOR_PURL = "http://purl.org/ASN/scheme/ASNJurisdiction/CO";
	URL url;
	String id;
	Map nodeMap = null;
	AsnStatement target = null;
	AsnDocStatement docStmnt = null;
	List ancestors = null;
	String topic;
	String author;


	/**
	 *  Constructor for the AsnResolutionResponse object
	 *
	 * @param  url            NOT YET DOCUMENTED
	 * @exception  Exception  if document cannot be parsed, or if both target and
	 *      document nodes cannot be found.
	 */
	public AsnResolutionResponse(URL url) throws Exception {
		super(url);
		this.url = url;
		this.id = idFromUrl (this.url);
		// pp(this.getDocument());
		parse();
		
	}

	String idFromUrl (URL url) {
		String urlStr = url.toString();
		int x = urlStr.lastIndexOf(".");
		return urlStr.substring(0, x);
	}
	
	/**
	 *  Initialize this response.
	 *
	 * @exception  Exception  if either target or document nodes are not found.
	 */
	private void parse() throws Exception {
		// id = this.url.toString();
		List nodes = this.getNodes("/rdf:RDF/asn:Statement[@rdf:about]");

		// if the target node is a document, then there will be no other entries in the node map
		nodeMap = new HashMap();

		prtln (nodes.size() + " nodes found");
		for (Iterator i = nodes.iterator(); i.hasNext(); ) {
			Element el = (Element) i.next();
			// String myid = el.attributeValue(getQName("rdf:about"));
			
			AsnStatement node = new AsnStatement(el);
			prtln ("  " + node.getId());
			nodeMap.put(node.getId(), node);
		}

		Element el = (Element) this.getNode("/rdf:RDF/asn:StandardDocument");
		if (el == null)
			throw new Exception("doc node not found for " + id);
		
		Element descEl = (Element) this.getNode("/rdf:RDF/rdf:Description");
		if (descEl == null)
			throw new Exception("desc node not found for " + id);
		
		// String myid = el.attributeValue(getQName("rdf:about"));
		docStmnt = new AsnDocStatement(el, descEl);
		
		String myid = docStmnt.getId();
		prtln ("\ndocID: " + myid);
		nodeMap.put(myid, docStmnt);

		// this.target = (AsnStatement) nodeMap.get(id);
		this.target = this.getStd(id);
		
		if (target == null) {
			showNodeMap();
			throw new Exception("Target node not found for " + id);
		}
		
		prtln("target level: " + this.getTargetLevel());
		if (this.isColoradoBenchmark() && this.getTargetLevel() == 2) {
			AsnStatement parent = (AsnStatement) this.getStd(this.target.getParentId());
			this.target.setDescription(ColoradoStandardDisplay.getDisplayText(this.target, parent));
		}
	}

	void showNodeMap () {
		prtln ("nodeMap (" + this.nodeMap.size() + " nodes)");
		for (Iterator i=this.nodeMap.keySet().iterator();i.hasNext();) {
			String key = (String)i.next();
			AsnStatement st = (AsnStatement)this.nodeMap.get(key);
			prtln (" - " + key + ": " + st.getId());
		}
	}
	
	/**
	 *  Gets the docId attribute of the AsnResolutionResponse object
	 *
	 * @return    The docId value
	 */
	public String getDocId() {
		return this.docStmnt.getId();
	}


	/**
	 *  Gets the std attribute of the AsnResolutionResponse object
	 *
	 * @param  id  NOT YET DOCUMENTED
	 * @return     The std value
	 */
	public AsnStatement getStd(String id) {
		return (AsnStatement) this.nodeMap.get(id);
	}


	/**
	 *  Gets the coloradoBenchmark attribute of the AsnResolutionResponse object
	 *
	 * @return    The coloradoBenchmark value
	 */
	public boolean isColoradoBenchmark() {
		prtln("isColoradoBenchmark (" + this.getDocStmnt().getJurisdiction() + ")");
		return (COLORADO_AUTHOR_PURL.equals(this.getDocStmnt().getJurisdiction()));
	}


	/**
	 *  Gets the targetLevel attribute of the AsnResolutionResponse object
	 *
	 * @return    The targetLevel value
	 */
	private int getTargetLevel() {
		return this.getAncestors().size() + 1;
	}


	/**
	 *  Gets the target attribute of the AsnResolutionResponse object
	 *
	 * @return    The target value
	 */
	public AsnStatement getTarget() {
		return this.target;
	}


	/**
	 *  Gets the nodeMap attribute of the AsnResolutionResponse object
	 *
	 * @return    The nodeMap value
	 */
	private Map getNodeMap() {
		return nodeMap;
	}


	/**
	 *  Gets the docStmnt attribute of the AsnResolutionResponse object
	 *
	 * @return    The docStmnt value
	 */
	public AsnDocStatement getDocStmnt() {
		return docStmnt;
	}


	/**
	 *  Gets the created attribute of the AsnResolutionResponse object
	 *
	 * @return    The created value
	 */
	public String getCreated() {
		return getDocStmnt().getSubElementText("created");
	}


	/**
	 *  Gets the author attribute of the AsnResolutionResponse object
	 *
	 * @return    The author value
	 */
	public String getAuthor() {
		String purl = getDocStmnt().getJurisdiction();
		return AsnHelper.getInstance().getAuthor(purl);
	}


	/**
	 *  Gets the topic attribute of the AsnResolutionResponse object
	 *
	 * @return    The topic value
	 */
	public String getTopic() {
		String purl = getDocStmnt().getSubject();
		return AsnHelper.getInstance().getTopic(purl);
	}


	/**
	 *  Gets the displayText attribute of the AsnResolutionResponse object
	 *
	 * @return    The displayText value
	 */
	public String getDisplayText() {

		String s = "";
		List aList = getAncestors();
		for (int i = 0; i < aList.size(); i++) {
			AsnStatement std = (AsnStatement) aList.get(i);
			s += std.getDescription();
			s += ": ";
		}
		s += this.getTarget().getDescription();

		s = FindAndReplace.replace(s, "<br>", "\n", true);
		// return removeEntityRefs (s);
		return s;
	}


	/**
	 *  Gets the ancestors attribute of the AsnResolutionResponse object
	 *
	 * @return    The ancestors value
	 */
	public List getAncestors() {
		if (this.ancestors == null) {
			this.ancestors = new ArrayList();
			String parentId = target.getParentId();
			while (parentId != null) {
				AsnStatement parent = this.getStd(parentId);
				if (parent == null)
					break;
				else {
					this.ancestors.add(parent);
					parentId = parent.getParentId();
				}
			}
		}
		return this.ancestors;
	}


	/**
	 *  The main program for the AsnResolutionResponse class
	 *
	 * @param  args           The command line arguments
	 * @exception  Exception  NOT YET DOCUMENTED
	 */
	public static void main(String[] args) throws Exception {

		// String urlStr = "http://purl.org/ASN/resources/S103EC6D";  // Colorado
		// String urlStr = "http://purl.org/ASN/resources/S100D316"; // nses
		// String urlStr = "http://purl.org/ASN/resources/D100027B"; // Doc level

		String urlStr = "http://purl.org/ASN/resources/S11419E5";

		if (args.length > 0) {
			urlStr = args[0];
			if (!urlStr.startsWith("http"))
				urlStr = "http://purl.org/ASN/resources/" + urlStr;
			urlStr += ".xml";
		}

		prtln("ASN Resolver: " + urlStr);

		AsnResolutionResponse response = new AsnResolutionResponse(new URL(urlStr));

		prtln("author: " + response.getAuthor());
		prtln("topic: " + response.getTopic());

		prtln("\nTarget");
		prtln(response.getTarget().toString());

		prtln("\nAncestors (from target parent upwards)");
		for (Iterator i = response.getAncestors().iterator(); i.hasNext(); ) {
			AsnStatement node = (AsnStatement) i.next();
			prtln(node.toString());
		}

		prtln("\nDocNode:");
		prtln(response.docStmnt.toString());

		prtln("\nDisplayText: " + response.getDisplayText());

		// pp (response.getDocument());
	}


	/**  NOT YET DOCUMENTED */
	public void report() {
		prtln("\nCreated: " + this.getCreated());
		prtln("\nTarget: " + this.getTarget().toString());

		for (Iterator i = this.getAncestors().iterator(); i.hasNext(); ) {
			AsnStatement node = (AsnStatement) i.next();
			// prtln (node.toString());
		}

		prtln("\nDoc node: " + this.getDocStmnt().toString());

		prtln("\n" + this.getDisplayText());
	}


	/**
	 *  NOT YET DOCUMENTED
	 *
	 * @param  node  NOT YET DOCUMENTED
	 */
	private static void pp(Node node) {
		prtln(Dom4jUtils.prettyPrint(node));
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

