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

package org.dlese.dpc.schemedit.action.form;

import org.dlese.dpc.schemedit.*;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionServlet;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.*;

/**
 *  ActionForm supporting ProxyAction
 *
 *@author    ostwald 
 
 */
public class JsonProxyForm extends ActionForm {

	private boolean debug = false;
	private HttpServletRequest request;

	private String proxyResponse;


	/* private SetInfo setInfo = null; */
	// input params
	/**
	 *  Constructor
	 */
	public JsonProxyForm() { }


	/**
	 *  Description of the Method
	 */
	public void clear() {

	}

	public String getProxyResponse () {
		return this.proxyResponse;
	}
	
	public void setProxyResponse (String proxyResponse) {
		this.proxyResponse = proxyResponse;
	}


	/**
	 *  Output a line of text to standard out, with datestamp, if debug is set to
	 *  true.
	 *
	 *@param  s  The String that will be output.
	 */
	protected final void prtln(String s) {
		if (debug) {
			System.out.println("JsonProxyForm: " + s);
		}
	}

}

