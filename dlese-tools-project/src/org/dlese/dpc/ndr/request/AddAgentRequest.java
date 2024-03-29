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

import org.dlese.dpc.schemedit.ndr.util.DcStream;

import org.dlese.dpc.ndr.apiproxy.NDRConstants;
import org.dlese.dpc.ndr.apiproxy.InfoXML;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

/**
 *  NOT YET DOCUMENTED
 *
 * @author     Jonathan Ostwald
 * @version    $Id: AddAgentRequest.java,v 1.3 2009/03/20 23:33:53 jweather Exp $
 */
public class AddAgentRequest extends SignedNdrRequest {

	/**  Constructor for the AddAgentRequest object */
	public AddAgentRequest() {
		super("addAgent");
		this.setObjectType(NDRConstants.NDRObjectType.AGENT);
	}


	/**
	 *  Creates an Agent object and returns it's handle.
	 *
	 * @param  id             The feature to be added to the Agent attribute
	 * @param  type           The feature to be added to the Agent attribute
	 * @return                NOT YET DOCUMENTED
	 * @exception  Exception  NOT YET DOCUMENTED
	 */
	public static String addAgent(String id, String type) throws Exception {
		AddAgentRequest request = new AddAgentRequest();

		request.setId (id, type);
		
		InfoXML response = request.submit();

		if (response.hasErrors())
			throw new Exception(response.getError());
		else
			return response.getHandle();
	}

	public void setId (String id, String type)  {

		Element identifier = DocumentHelper.createElement("identifier");
		identifier.setText (id);
		identifier.addAttribute ("type", type);
		this.addCommand ("property", identifier);
	}
	
	public void addDcsStream (String title, String description, String subject) throws Exception {
/* 		String title = "Agent for mud collection";
		String description = "its all about mud";
		String subject = "none that i can see"; */
		DcStream dc_stream = new DcStream(title, description, subject);
		
		this.addDCStreamCmd(dc_stream.asElement());
	}
	
}

