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

package org.dlese.dpc.ndr.request;

import org.dlese.dpc.ndr.apiproxy.*;
import org.dlese.dpc.ndr.connection.NDRConnection;
import org.dlese.dpc.xml.Dom4jUtils;
import org.dlese.dpc.standards.asn.NameSpaceXMLDocReader;
import org.dlese.dpc.ndr.reader.*;
import org.dom4j.*;
import org.jaxen.SimpleNamespaceContext;
import java.util.*;

/**
 *  Class to communiate directly with NDR via {@link org.dlese.dpc.ndr.connection.NDRConnection}.
 *  Builds the inputXML parameter that is sent as part a POST request.
 *
 * @author     Jonathan Ostwald
 */
public class GetMultipleRequest extends SimpleNdrRequest {

	/**  NOT YET DOCUMENTED */
	protected Element inputXML = null;
	/**  NOT YET DOCUMENTED */
	protected String payload = null;
	final static String myverb = "getMultiple";


	/**  Constructor for the GetMultipleRequest object */
	public GetMultipleRequest(List handles) {
		super (myverb);
		this.inputXML = getInputXML();
		Element handlesEL = this.inputXML.addElement("handles");
		if (handles != null) {
			for (Iterator i=handles.iterator();i.hasNext();) {
				Element handleEl = handlesEL.addElement("handle");
				handleEl.setText ((String)i.next());
			}
		}
	}


	/**
	 *  Gets the {@link inputXML} attribute of the GetMultipleRequest object, which stores
	 *  the commands for this request.
	 *
	 * @return    The inputXML value
	 */
	 private Element getInputXML() {
		Element inputXML = DocumentHelper.createElement("InputXML");
		String defaultNsUri = "http://ns.nsdl.org/ndr/request_v1.00/";
		String schemaUri = "http://ns.nsdl.org/schemas/ndr/request_v1.00.xsd";
		inputXML.addAttribute("xmlns", defaultNsUri);

		String xsiUri = "http://www.w3.org/2001/XMLSchema-instance";
		Namespace xsiNs = DocumentHelper.createNamespace("xsi", xsiUri);
		inputXML.add(xsiNs);

		inputXML.addAttribute("xsi:schemaLocation", defaultNsUri + " " + schemaUri);
		inputXML.addAttribute("schemaVersion", "1.00.000");
		return inputXML;
	}


	/**
	 *  A human readable representation of the XMLInput payload of the request.
	 *
	 * @return    The payload value
	 */
	protected String getPayload() {
		return payload;
	}


	/**
	 *  Creates connection and adds payload in the form of inputXML parameter.<p>
	 *
	 *  Payload is the request objects's inputXML attribute, which is overidden by
	 *  the inputXMLStr parameter if present. This allows a caller to create an
	 *  inputXMLStr external to the request, which is helpful in debugging.
	 *
	 * @param  path           NOT YET DOCUMENTED
	 * @param  inputXMLStr    NOT YET DOCUMENTED
	 * @return                The nDRConnection value
	 * @exception  Exception  NOT YET DOCUMENTED
	 */
	protected NDRConnection getNDRConnection(String path, String inputXMLStr) throws Exception {
		NDRConnection connection = super.getNDRConnection(path);

		payload = inputXMLStr;
		if (payload == null && this.inputXML != null) {
			// format the inputXML so it is in a human-readable form
			payload = Dom4jUtils.prettyPrint(this.inputXML);
		}

		if (payload != null)
			connection.setContent("inputXML=" + java.net.URLEncoder.encode(payload, "UTF-8"));
		return connection;
	}

		/**
	 *  NOT YET DOCUMENTED
	 *
	 * @return                NOT YET DOCUMENTED
	 * @exception  Exception  NOT YET DOCUMENTED
	 */
	public InfoXML submit() throws Exception {
		return submit(this.inputXML.asXML());
	}

	/**
	 *  NOT YET DOCUMENTED
	 *
	 * @param  inputXMLStr    NOT YET DOCUMENTED
	 * @return                NOT YET DOCUMENTED
	 * @exception  Exception  NOT YET DOCUMENTED
	 */
	public InfoXML submit(String inputXMLStr) throws Exception {
		if (verb == null || verb.trim().length() == 0)
			throw new Exception("attempting to submit request without specifying verb");

		String path = makePath();
		
		// report(path);

		NDRConnection connection = getNDRConnection(path, inputXMLStr);
		if (getVerbose()) {
			prtln("\n===============\nproxyRequest");
			prtln(path);

			if (getPayload() != null)
				prtln(getPayload());
		}

		InfoXML proxyResponse = new InfoXML(connection.request());

		if (getVerbose()) {
			prtln("\n===============\nproxyResponse");
			try {
				Document responseDoc = DocumentHelper.parseText(proxyResponse.getResponse());
				pp(responseDoc);
			} catch (Exception e) {
				prtln("response could not be displayed: " + e.getMessage());
			}
		}
		return proxyResponse;
	}
	
	private List getObjectList1 (Element resultData) {
		// pp (resultData);
		Document doc = DocumentHelper.createDocument(resultData.createCopy());
		NameSpaceXMLDocReader reader = null;
		List objects = new ArrayList();
		try {
			reader = new NameSpaceXMLDocReader (doc);
		} catch (Exception e) {
			prtln ("could not parse resultData: " + e.getMessage());
			return objects;
		}
		
		/* have to add "ndr" namespace (in the document it is default and therefore
		not known by prefix)
		*/
		SimpleNamespaceContext nsContext = reader.getNamespaceContext();
		// nsContext.addNamespace ("ndr", "http://ns.nsdl.org/ndr/response_v1.00/");
		
		String path = "/ndr:resultData/ndr:objectList/ndr:object/ndr:NDRObject";
		
		List nodes = reader.getNodes (path);
		prtln (nodes.size() + " objects found");
		for (Iterator i=nodes.iterator();i.hasNext();)
			objects.add ( ((Element)i.next()).createCopy());
		
		return objects;
	}
	
	private List getObjectList (InfoXML response) {
		// pp (resultData);
		NameSpaceXMLDocReader doc = null;
		List objects = new ArrayList();
		try {
			doc = new NameSpaceXMLDocReader (response.getResponse());
		} catch (Exception e) {
			prtln ("could not parse resultData: " + e.getMessage());
			return objects;
		}
		
		Element root = doc.getRootElement().createCopy();
		root.clearContent();
		
		/* have to add "ndr" namespace (in the document it is default and therefore
		not known by prefix)
		*/
		SimpleNamespaceContext nsContext = doc.getNamespaceContext();
		nsContext.addNamespace ("ndr", "http://ns.nsdl.org/ndr/response_v1.00/");
		
		String path = "/ndr:NSDLDataRepository/ndr:resultData/ndr:objectList/ndr:object/ndr:NDRObject";
		
		List nodes = doc.getNodes (path);
		// prtln (nodes.size() + " objects found");
		for (Iterator i=nodes.iterator();i.hasNext();) {
			Element objElement = (Element)i.next();
			Element objRoot = root.createCopy();
			objRoot.add (objElement.createCopy());
			Document objDoc = DocumentHelper.createDocument(objRoot.createCopy());
			NdrObjectReader ndrObjReader = null;
			try {
				ndrObjReader = new NdrObjectReader (objDoc);
			} catch (Exception e) {
				prtln ("WARNING: could not create ndrObject: " + e.getMessage());
			}
			objects.add (ndrObjReader);
			// prtln (ndrObjReader.getHandle());
		}
		
		return objects;
	}
	
	
	public List getNdrObjects() throws Exception {
		InfoXML response = this.submit();
		if (response.hasErrors())
			throw new Exception(response.getError());
		else {
			return getObjectList(response);
		}
	}

	/**  NOT YET DOCUMENTED */
	public void report(String path) {

		prtln("GetMultipleRequest: submit");
		prtln("\t path: " + path);
		prtln("\t verbose: " + getVerbose());
	}

}

