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

package org.dlese.dpc.xml.schema.action;

import java.util.*;

import org.dlese.dpc.schemedit.test.TesterUtils;
import org.dlese.dpc.schemedit.test.FrameworkTester;
import org.dlese.dpc.schemedit.*;
import org.dlese.dpc.schemedit.config.*;
import org.dlese.dpc.xml.*;
import org.dlese.dpc.xml.schema.*;


/**
 *  Tester for {@link org.dlese.dpc.schemedit.config.FrameworkConfigReader} and
 {@link org.dlese.dpc.schemedit.MetaDataFramework}
 *
 *@author    ostwald
 */
public class FieldBean {
	
	private List frameworks = null;
	
	public FieldBean (List frameworks) {
		this.frameworks = frameworks;
	}


	public List getChoices (int level, String parent) {
		return getChoices (level, parent, null);
	}
	
	public List getChoices (int level, String parent, List _frameworks) {
		List myFrameworks = _frameworks;
		if (myFrameworks == null) 
			myFrameworks = this.frameworks;
		
		prtln (myFrameworks.size() + " frameworks");
		List choices = new ArrayList();
		for (Iterator i=myFrameworks.iterator();i.hasNext();) {
			MetaDataFramework frm = (MetaDataFramework)i.next();
			SchemaHelper sh = frm.getSchemaHelper();
			SchemaNodeMap schemaNodeMap = sh.getSchemaNodeMap();
			Iterator pathIter = schemaNodeMap.getKeys().iterator();
			while (pathIter.hasNext()) {
				String path = (String)pathIter.next();
				String[] splits = path.split("/");
				prtln (path + " (" + splits.length + ")");
				if (splits.length <= level+1)
					continue;
				if (parent != null && parent.equals(splits[level])) {
					String candidate = splits[level+1];
					if (!choices.contains(candidate))
						choices.add (candidate);
				}
			}
		}
		return choices;
	}
	
	private static MetaDataFramework instantiateFramework (String xmlFormat) throws Exception {
		FrameworkTester ft = new FrameworkTester(xmlFormat);
		prtln ("ft class: " + ft.framework.getClass().getName());
		return ft.framework;
	}
	
	public static void main (String [] args) throws Exception {
		TesterUtils.setSystemProps ();
		List frameworks = new ArrayList ();
		frameworks.add (instantiateFramework ("collection_config"));
		frameworks.add (instantiateFramework ("dcs_data"));
		FieldBean fb = new FieldBean (frameworks);
		prtln ("loaded");
		// List choices = fb.getChoices(0, "");
		List choices = fb.getChoices(1, "collectionConfigRecord");
		prtln ("\nCHOICES");
		for (Iterator i=choices.iterator();i.hasNext();)
			prtln (" - " + (String)i.next());
	}
	
	
	/**
	 *  Description of the Method
	 *
	 *@param  s  Description of the Parameter
	 */
	public static void prtln(String s) {
		System.out.println(s);
	}
}

