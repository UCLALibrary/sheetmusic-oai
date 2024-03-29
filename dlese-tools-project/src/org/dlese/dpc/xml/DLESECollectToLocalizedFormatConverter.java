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

import org.dlese.dpc.util.*;
import javax.servlet.ServletContext;

/**
 *  Converts from DLESE Collect format to a localized DLESE Collect format, which contains
 *  no namespace declarations. Localized XML may be accessed using XPath notation without
 *  the local-name() function, making it easier to use.
 *
 * @author    John Weatherley
 * @see       XMLConversionService
 */
public class DLESECollectToLocalizedFormatConverter implements XMLFormatConverter {

	/**
	 *  Converts from the DLESE Collect format.
	 *
	 * @return    The String "dlese_collect".
	 */
	public String getFromFormat() {
		return "dlese_collect";
	}


	/**
	 *  Converts to the dlese_collect-localized format
	 *
	 * @return    The String "dlese_collect-localized".
	 */
	public String getToFormat() {
		return "dlese_collect-localized";
	}


	/**
	 *  Gets the time this converter code was last modified. If unknown, this method should
	 *  return -1.
	 *
	 * @param  context  Servlet context
	 * @return          The time this converter code was last modified.
	 */
	public long lastModified(ServletContext context) {
		return -1;
	}


	/**
	 *  Performs XML conversion from DLESE Collect to DLESE Collect  localized.
	 *
	 * @param  xml      XML input in the 'dlese_collect' format.
	 * @param  context  Servlet context
	 * @return          XML in the converted 'dlese_collect-localized' format.
	 */
	public String convertXML(String xml, ServletContext context) {
		//return Dom4jUtils.localizeXml(xml.replaceFirst("<\\?xml.+version=.+encoding=.+\\?>", ""), "itemRecord");
		// return Dom4jUtils.localizeXml(xml, "collectionRecord");
		return Dom4jUtils.localizeXml(xml);
	}

}

