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

package org.dlese.dpc.schemedit.autoform.mde;

import org.dlese.dpc.schemedit.autoform.*;
import org.dlese.dpc.schemedit.SchemEditUtils;
import org.dlese.dpc.xml.*;
import org.dlese.dpc.xml.schema.*;
import org.dlese.dpc.xml.schema.compositor.InlineCompositor;
import org.dlese.dpc.util.Files;
import org.dlese.dpc.util.strings.FindAndReplace;

import java.util.*;
import org.dom4j.Node;
import org.dom4j.Attribute;
import org.dom4j.Element;
import org.dom4j.DocumentHelper;

/**
 *  Renders JSP for SimpleType schema elements in the metadata editor. SimpleType elements have an input
 and no children elements or attributes.
 *
 * @author     ostwald<p>
 *
 */
public class MdeSimpleType  extends MdeNode {
	/**  Description of the Field */
	private static boolean debug = false;

	public MdeSimpleType (RendererImpl renderer) {
		super(renderer);
	}

	public void render() {
		if (schemaNode.isHeadElement()) {
			// renderSimpleTypeSubstitutionGroup();
			prtln ("WARNING: SimpleTypeSubstitution group not yet implemented!");
			return;
		}

		if (isEditMode()) {
			render_edit_mode();
		}
		else {
			render_display_mode();
		}
	}

	private boolean isMultiSelect () {
		return (typeDef != null &&
			this.sh.isEnumerationType(typeDef) &&
			this.sh.isMultiSelect(this.schemaNode));
	}

	protected void render_edit_mode() {

		Element parentExists = rhelper.parentNodeExistsTest(xpath);
		parent.add(parentExists);
		insertDisplaySetup (parentExists);

		// LABEL - if we have a multiSelect, then use a complex label so we can make it
		// collapsible. Otherwise, use simpleTypeLabel
		Label label = null;

		if (this.isMultiSelect() && !this.sh.isSingleton(this.schemaNode)) {
			label = renderer.getMultiBoxLabel (xpath);
		}
		else {
			label = renderer.getSimpleTypeLabel(xpath);
		}

		// DELETE CONTROLLER
		// add a controller to delete this element if it is a repeating element BUT
		// not if it is a repeatingComplexSingleton, since repeatingComplexSingltons
		// have a separate label/controller for doing deletes.
		if (sh.isRepeatingElement (normalizedXPath) &&
			!sh.isRepeatingComplexSingleton (normalizedXPath)) {
			Element deleteController = renderer.getDeleteController(xpath, "field");
			((SimpleTypeLabel)label).control = deleteController;
		}

		// create box decorated with id attribute
		Element box = DocumentHelper.createElement("div")
			.addAttribute("id", "${id}_box");
		embedDebugInfo(box, "simpleType");

		// attach box
		parentExists.add(box);

		// create fieldElement (containing input element)
		Element fieldElement = DocumentHelper.createElement("div");

		// attachMessages(fieldElement);
		fieldElement.add(getInputElement());

		box.add(getRenderedField(label, fieldElement));
	}

	protected void render_display_mode() {

		attachElementId (parent);

		// LABEL
		SimpleTypeLabel label = renderer.getSimpleTypeLabel(xpath);

 		// create fieldElement (containing input element)
		Element fieldElement = DocumentHelper.createElement("div");
		fieldElement.add(getInputElement());

		parent.add(getRenderedField(label, fieldElement));
	}


 	public static void setDebug(boolean bool) {
		debug = bool;
	}

	protected void prtln (String s) {
		String prefix = "MdeSimpleType";
		if (debug) {
			SchemEditUtils.prtln (s, prefix);
		}
	}

}
