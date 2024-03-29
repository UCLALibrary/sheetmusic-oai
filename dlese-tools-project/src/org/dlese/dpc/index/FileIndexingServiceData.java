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

package org.dlese.dpc.index;

import org.apache.lucene.document.*;

import java.util.*;
import java.io.*;


public class FileIndexingServiceData {
	private Document newDoc = null;
	private HashMap docsToRemove = null;
	
	public FileIndexingServiceData() {}
	
	public void setDoc(Document documentToAdd){
		newDoc = documentToAdd;	
	}
	
	public Document getDoc(){
		return newDoc;	
	}

	public HashMap getDocsToRemove(){
		return docsToRemove;	
	}
	
	public void addDocToRemove(String field, String value){
		if(docsToRemove == null)
			docsToRemove = new HashMap();
		ArrayList values = (ArrayList)docsToRemove.get(field);
		if(values == null)
			values = new ArrayList();
		values.add(value);
		docsToRemove.put(field,values);	
	}
	
	public void clearAll(){
		newDoc = null;
		if(docsToRemove != null)
			docsToRemove.clear();
	}
}
