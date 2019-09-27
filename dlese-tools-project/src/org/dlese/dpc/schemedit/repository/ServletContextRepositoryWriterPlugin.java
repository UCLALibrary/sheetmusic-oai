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

import javax.servlet.*;

/**
 *  This abstract class implements RepositoryWriterPlugin to provide access to the {@link
 *  javax.servlet.ServletContext} during the indexing process. This class should be used
 *  when using a RepositoryWriterPlugin in a Servlet environment.
 *
 * @author     John Weatherley, Jonathan Ostwald
 * @see        RepositoryWriterServiceWriter
 * @version    $Header: /cvsroot/dlsciences/dlese-tools-project/src/org/dlese/dpc/schemedit/repository/ServletContextRepositoryWriterPlugin.java,v 1.2 2009/03/20 23:33:57 jweather Exp $<p>
 *
 *      $Log: ServletContextRepositoryWriterPlugin.java,v $
 *      Revision 1.2  2009/03/20 23:33:57  jweather
 *      -updated the license statement in all Java files to Educational Community License v1.0.
 *
 *      Revision 1.1  2007/11/29 00:19:47  ostwald
 *      Revamped RepositoryWriterPlugin scheme. RWplugins registered as init-params to SchemEditServlet and implement interface, throwing RespositoryWriterPluginExceptions.
 *
 *      Revision 1.2  2004/09/10 22:46:05  jweather
 *      added XML format (docType) and collection (docGroup) to the RepositoryWriterServicePlugin
 *
 *      Revision 1.1  2004/09/10 01:58:06  jweather
 *      RepositoryWriterPlugins for use in the DDS Servlet indexing of items
 *<p>
 *
 *
 */
public abstract class ServletContextRepositoryWriterPlugin implements RepositoryWriterPlugin {
	private static ServletContext servletContext = null;


	/**
	 *  Sets the ServletContext to make it available to this plugin during the indexing
	 *  process.
	 *
	 * @param  context  The ServletContext
	 */
	public static void setServletContext(ServletContext context) {
		servletContext = context;
	}


	/**
	 *  Gets the ServletContext for use during the indexing process.
	 *
	 * @return    The ServletContext
	 */
	public static ServletContext getServletContext() {
		return servletContext;
	}
}

