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

package org.dlese.dpc.xml;

/**
 *  Utilities for working with XSL.
 *
 * @author     John Weatherley
 * @version    $Id: XSLUtils.java,v 1.2 2009/03/20 23:34:01 jweather Exp $
 */
public class XSLUtils {

	private static final String removeNamespacesXSL =
		"<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform' version='1.0' >" +
		"<xsl:template match='@*' >" +
		"<xsl:attribute name='{local-name()}' >" +
		"<xsl:value-of select='.' />" +
		"</xsl:attribute>" +
		"<xsl:apply-templates/>" +
		"</xsl:template>" +
		"<xsl:template match ='*' >" +
		"<xsl:element name='{local-name()}' >" +
		"<xsl:apply-templates select='@* | node()' />" +
		"</xsl:element>" +
		"</xsl:template>" +
		"</xsl:stylesheet>";


	/**
	 *  Gets an XSL style sheet that removes all namespaces from an XML document. With namespaces removed, the
	 *  XPath syntax necessary to work with the document is greatly simplified.
	 *
	 * @return    An XSL style sheet that removes all namespaces from an XML document
	 */
	public final static String getRemoveNamespacesXSL() {
		return removeNamespacesXSL;
	}
}

