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
package org.dlese.dpc.schemedit.repository;

import org.dlese.dpc.schemedit.SchemEditUtils;
import javax.servlet.ServletContext;

import org.dlese.dpc.util.TimedURLConnection;

import java.util.*;
import java.net.URL;

/**
 *  RepositoryEventListener that provides Smile specific handlers for some
 *  events.
 *  <li> recordMove - pings a url to transmit recordMove event info
 *
 *@author    jonathan
 */
public class SmileEventListener extends RepositoryEventListener {

	private static boolean debug = true;
	URL smileMoveEventUrl = null;


	/**
	 *  No parameter Constructor for the SmileEventListener object
	 */
	public SmileEventListener() { }


	/**
	 *  Constructor for the SmileEventListener object providing servletContext
	 *
	 *@param  servletContext  the servletContext
	 */
	public SmileEventListener(ServletContext servletContext) {
		super(servletContext);
		this.setServletContext(servletContext);
	}


	/**
	 *  Sets the servletContext attribute of the SmileEventListener object and
	 initializes the smileMoveEventUrl.
	 *
	 *@param  servletContext  The new servletContext value
	 */
	public void setServletContext(ServletContext servletContext) {
		super.setServletContext(servletContext);
		try {
			smileMoveEventUrl = new URL((String) servletContext.getInitParameter("smileMoveEventUrl"));
		} catch (Throwable t) {
			prtlnErr("SmileEventLister could not be instantiated: " + t.getMessage());
		}
		prtln("instantiated with url: " + smileMoveEventUrl);
	}


	/**
	 *  Handle a RepositoryEvent - in this case we only respond to "moveRecord" events
	 *
	 *@param  event      the repositoryEvent
	 */
	public void handleEvent(RepositoryEvent event) {
		System.out.println("SmileEventListener received a repositoryEvent: " + event.getName());
		System.out.println(event.toString());

		if ("deleteRecord".equals(event.getName()) ||
		  	"copyRecord".equals(event.getName()) ||
		  	"moveRecord".equals(event.getName()) ||
		  	"copyMoveRecord".equals(event.getName())) {
				
			Map data = event.getEventData();
			String url = this.smileMoveEventUrl + "?func=" + event.getName();
			for (Iterator i=data.keySet().iterator();i.hasNext();) {
				String param = (String)i.next();
				String val = (String)data.get(param);
				url += "&" + param + "=" + val;
			}
			prtln("URL: " + url);
			// ping the url
			try {
				String response = TimedURLConnection.importURL(url, 2000);
				prtln ("\nSmile Response: " + response);
			} catch (Exception e) {
				prtlnErr ("could not get response from smile server (" + url + ")");
			}
		}
		else {
			prtln ("\"" + event.getName() + " event ignored");
		}

	}


	/**
	 *  Description of the Method
	 *
	 *@param  s  Description of the Parameter
	 */
	static void prtln(String s) {
		if (debug) {
			SchemEditUtils.prtln(s, "SmileEventListener");
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  s  Description of the Parameter
	 */
	static void prtlnErr(String s) {
		SchemEditUtils.prtln(s, "SmileEventListener");
	}
}

