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

package org.dlese.dpc.schemedit.ndr.action.form;

import org.dlese.dpc.schemedit.ndr.util.integration.MappingsManager;
import org.dlese.dpc.schemedit.ndr.util.integration.MappingInfo;

import org.dlese.dpc.schemedit.*;
import org.dlese.dpc.ndr.apiproxy.NDRConstants;
import org.dlese.dpc.ndr.reader.AgentReader;
import org.dlese.dpc.xml.*;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionServlet;
import org.apache.struts.util.MessageResources;
import org.apache.struts.util.LabelValueBean;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.io.*;
import java.text.*;
import java.net.*;
import java.util.regex.*;

/**
 *  ActionForm bean for handling interactions with the NDR.
 *
 *@author    ostwald $Id: CollectionIntegrationForm.java,v 1.8 2005/08/31 08:09:59
 *      ostwald Exp $
 */
public class CollectionIntegrationForm extends ActionForm {

	private boolean debug = true;
	private MappingsManager mappingsManager = null;
	private MappingInfo mappingInfo = null;

	/**
	 *  Constructor
	 */
	public CollectionIntegrationForm() { }
	
	public MappingsManager getMappingsManager () {
		return this.mappingsManager;
	}
	
	public void setMappingsManager (MappingsManager mm) {
		this.mappingsManager = mm;
	}
	
	public MappingInfo getMappingInfo () {
		return this.mappingInfo;
	}
	
	public void setMappingInfo (MappingInfo mappingInfo) {
		this.mappingInfo= mappingInfo;
	}
	
	public String getNdrApiUrl () {
		return NDRConstants.getNdrApiBaseUrl();
	}

	public String getNcsAgentHandle () {
		return NDRConstants.getNcsAgent ();
	}

	/**
	 *  Output a line of text to standard out, with datestamp, if debug is set to
	 *  true.
	 *
	 *@param  s  The String that will be output.
	 */
	protected final void prtln(String s) {
		if (debug) {
			SchemEditUtils.prtln (s, "CollectionIntegrationForm");
		}
	}

}

