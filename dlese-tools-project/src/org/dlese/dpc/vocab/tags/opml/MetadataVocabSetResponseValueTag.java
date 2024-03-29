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

package org.dlese.dpc.vocab.tags.opml;

import org.dlese.dpc.vocab.*;
import java.util.*;
import java.io.*;
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;
import javax.servlet.http.*;

/**
 *  Tag handler for setting a single value of a response list for reproducing as
 *  an OPML subset wtih MetadataVocabGetResponseOPMLTag
 *
 * @author    Ryan Deardorff
 */
public class MetadataVocabSetResponseValueTag extends MetadataVocabTag {
	String value = "";

	/**
	 *  Sets the value attribute of the MetadataVocabSetResponseTag object
	 *
	 * @param  value  The new value value
	 */
	public void setValue( String value ) {
		this.value = value;
	}

	/**
	 *  Set one of a potential list of response values
	 *
	 * @return
	 * @exception  JspException
	 */
	public int doStartTag() throws JspException {
		String contextAttributeName = (String)pageContext.getServletContext().getInitParameter( "metadataVocabInstanceAttributeName" );
		vocab = (MetadataVocab)pageContext.findAttribute( contextAttributeName );
		if ( vocab == null ) {
			throw new JspException( "Vocabulary not found" );
		}
		vocab.setResponseValue( value, pageContext );
		return SKIP_BODY;
	}

	/**
	 *  Description of the Method
	 *
	 * @return
	 * @exception  JspException
	 */
	public int doEndTag() throws JspException {
		return EVAL_PAGE;
	}

	/**
	 *  Description of the Method
	 */
	public void release() {
		super.release();
	}
}

