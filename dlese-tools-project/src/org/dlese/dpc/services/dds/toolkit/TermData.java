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
package org.dlese.dpc.services.dds.toolkit;

import java.util.*;

/**
 *  Class that holds data about term in one or more fields in the index.
 *
 * @author    John Weatherley
 */
public class TermData {
	private int termCount;
	private int docCount;


	/**
	 *  Constructor for the TermData object
	 *
	 * @param  termCount  Number of times the term appears in the given field(s)
	 * @param  doccount   Number of documents (records) in the index in which the term resides in the given
	 *      field(s)
	 */
	public TermData(int termCount, int docCount) {
		this.termCount = termCount;
		this.docCount = docCount;
	}


	/**
	 *  Number of times the term appears in the given field(s).
	 *
	 * @return    The termCount value
	 */
	public int getTermCount() {
		return termCount;
	}


	/**
	 *  Number of documents (records) in the index in which the term resides in the given field(s).
	 *
	 * @return    The docCount value
	 */
	public int getDocCount() {
		return docCount;
	}
}

