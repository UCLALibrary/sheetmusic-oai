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
package org.dlese.dpc.schemedit.autoform;

import org.dlese.dpc.schemedit.autoform.mde.*;
import org.dlese.dpc.schemedit.standards.StandardsManager;
import org.dlese.dpc.xml.*;
import org.dlese.dpc.xml.schema.*;

import java.util.*;
import java.util.regex.*;

import org.dom4j.Node;
import org.dom4j.Attribute;
import org.dom4j.Element;

/**
 *  Includes res_qual-specific kludges ...
 *
 * @author    ostwald
 */
public class ResQualEditorRenderer extends DleseEditorRenderer {
	private static boolean debug = false;

	/**
	 *  Render configured paths as textArea inputs instead of regular text inputs.
	 *
	 * @param  xpath       xpath of input
	 * @param  schemaNode  schemaNode for this xpath
	 * @param  typeDef     the typeDefinion for this node
	 * @return             The textInput value
	 */
	protected Element getTextInput(String xpath, SchemaNode schemaNode, GlobalDef typeDef) {

		String xmlFormat = this.getXmlFormat();
		prtln("getTextInput: " + xmlFormat);

		if (xmlFormat.equals("res_qual") && this.normalizedXPath.startsWith("/record/contentAlignment")) {
			String nodeName = XPathUtils.getNodeName(this.normalizedXPath);
			// prtln("NodeName: " + nodeName);

			if (nodeName.equals("comment") || nodeName.equals("learnGoalPart"))
				return getTextAreaInput(xpath, 2);
		}
		return super.getTextInput(xpath, schemaNode, typeDef);
	}


	/**
	 *  Sets the debug attribute of the ResQualEditorRenderer class
	 *
	 * @param  bool  The new debug value
	 */
	public static void setDebug(boolean bool) {
		debug = bool;
	}


	/**
	 *  Description of the Method
	 *
	 * @param  s  Description of the Parameter
	 */
	private static void prtln(String s) {
		if (debug) {
			while (s.length() > 0 && s.charAt(0) == '\n') {
				System.out.println("");
				s = s.substring(1);
			}
			System.out.println("ResQualEditorRenderer: " + s);
		}
	}


	/**
	 *  NOT YET DOCUMENTED
	 *
	 * @param  s  NOT YET DOCUMENTED
	 */
	private final void prtlnErr(String s) {
		System.err.println("ResQualEditorRenderer: " + s);
	}

}

