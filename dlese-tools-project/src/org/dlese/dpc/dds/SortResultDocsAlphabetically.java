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

package org.dlese.dpc.dds;

import java.util.Comparator;
import org.dlese.dpc.index.*;
import org.apache.lucene.document.*;

/**
 *  Description of the Class
 *
 * @author    ryandear
 */
public class SortResultDocsAlphabetically implements Comparator
{

	/**
	 *  Provide comparison for sorting Lucene documents alphabetically by "title"
	 *
	 * @param  o1  document 1
	 * @param  o2  document 2
	 * @return     DESCRIPTION
	 */
	public int compare(Object o1,
			Object o2) {
		String s1 = ((ResultDoc) o1).getDocument().get("title");
		String s2 = ((ResultDoc) o2).getDocument().get("title");
		if (s1.compareTo(s2) > 0) {
			return 1;
		}
		if (s1.equals(s2)) {
			return 0;
		}
		return -1;
	}
}

