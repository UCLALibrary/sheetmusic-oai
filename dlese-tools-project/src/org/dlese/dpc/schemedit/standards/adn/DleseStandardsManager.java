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

package org.dlese.dpc.schemedit.standards.adn;

import org.dlese.dpc.schemedit.standards.StandardsManager;
import org.dlese.dpc.schemedit.standards.adn.util.MappingUtils;

import org.dlese.dpc.xml.schema.*;
import org.dlese.dpc.schemedit.*;

import java.io.*;
import java.util.*;

import java.net.*;

/**
 *  StandardsManager for the ADN Framework. Manages standards represented as
 *  ":"-delimited strings.
 *
 * @author    ostwald
 */
public class DleseStandardsManager implements StandardsManager {
	private static boolean debug = true;

	private SchemaHelper schemaHelper;
	String xpath;
	String version;
	DleseStandardsDocument standardsDocument = null;

	/**
	 *  Constructor for the DleseStandardsManager object
	 *
	 * @param  schemaHelper   NOT YET DOCUMENTED
	 * @param  xpath          NOT YET DOCUMENTED
	 * @param  dataTypeName   NOT YET DOCUMENTED
	 * @exception  Exception  NOT YET DOCUMENTED
	 */

	public DleseStandardsManager(String xpath,
	                           	 String dataTypeName,
								 SchemaHelper schemaHelper)
		 throws Exception {
		this.xpath = xpath;
		
		List dataTypeNames = new ArrayList();
		dataTypeNames.add(dataTypeName);
		init(schemaHelper, dataTypeNames);
	}


	/**
	 *  Constructor for the DleseStandardsManager object
	 *
	 * @param  schemaHelper   NOT YET DOCUMENTED
	 * @param  xpath          NOT YET DOCUMENTED
	 * @param  dataTypeNames  NOT YET DOCUMENTED
	 * @exception  Exception  NOT YET DOCUMENTED
	 */
	public DleseStandardsManager(String xpath,
	                             List dataTypeNames,
								 SchemaHelper schemaHelper)
		 throws Exception {
		this.xpath = xpath;

		init(schemaHelper, dataTypeNames);
	}

	public DleseStandardsDocument getStandardsDocument () {
		return this.standardsDocument;
	}
	
	public String getXmlFormat() {
		return "adn";
	}

	/**
	 *  Gets the xpath attribute of the DleseStandardsManager object
	 *
	 * @return    The xpath value
	 */
	public String getXpath() {
		return xpath;
	}
	
	/**
	 *  Gets the rendererTag attribute of the DleseStandardsManager object
	 *
	 * @return    The rendererTag value
	 */
	public String getRendererTag() {
		// return "dleseStandards_MultiBox";
		// return "dynaStandards_MultiBox";
		return "standards_MultiBox";
	}


	/**
	 *  NOT YET DOCUMENTED
	 *
	 * @param  dataTypeNames  NOT YET DOCUMENTED
	 * @return                NOT YET DOCUMENTED
	 */
	private List makeDataTypesList(List dataTypeNames) {
		List types = new ArrayList();
		for (Iterator i = dataTypeNames.iterator(); i.hasNext(); ) {
			String typeName = (String) i.next();
			GlobalDef typeDef = schemaHelper.getGlobalDef(typeName);
			types.add(typeDef);
		}
		return types;
	}


	/**
	 *  Initialize the DleseStandardsManager by populating the standardsMap and tree
	 *
	 * @exception  Exception  NOT YET DOCUMENTED
	 */
	private void init(SchemaHelper schemaHelper, List dataTypeNames) throws Exception {
		
		if (schemaHelper == null)
			throw new Exception("SchemaHelper not initialized");

		this.standardsDocument = new DleseStandardsDocument (schemaHelper, dataTypeNames);
	}

	/**
	 *  Description of the Method
	 *
	 * @param  s  Description of the Parameter
	 */
	private static void prtln(String s) {
		if (debug)
			SchemEditUtils.prtln(s, "DleseStandardsManager");
	}
}

