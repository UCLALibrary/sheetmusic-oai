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

import org.dlese.dpc.vocab.MetadataVocab;
import org.dlese.dpc.util.strings.StringUtil;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.http.*;

/**
 *  Tag handler for retreiving the "display" attribute of the given field and
 *  value
 *
 * @author    Ryan Deardorff
 */
public class MetadataVocabUiDisplayTag extends MetadataVocabTag {
	String field = "";
	String value = "";

	/**
	 *  Sets the field attribute of the MetadataVocabUiDisplayTag object
	 *
	 * @param  field  The new field value
	 */
	public void setField( String field ) {
		this.field = field;
	}

	/**
	 *  Sets the value attribute of the MetadataVocabUiDisplayTag object
	 *
	 * @param  value  The new value value
	 */
	public void setValue( String value ) {
		this.value = value;
	}

	/**
	 *  Start tag
	 *
	 * @return
	 * @exception  JspException
	 */
	public int doStartTag() throws JspException {
		setupTag( pageContext );
		StringBuffer outStr = new StringBuffer();
		try {
			pageContext.getOut().print( vocab.getUiValueDisplay( metaFormat, metaVersion,
				audience, language, field, value ) );
		}
		catch ( Exception e ) {
			System.out.println( "Exception in MetadataVocabUiDisplayTag: " + e.getMessage() );
			throw new JspException( e.getMessage() );
		}
		return SKIP_BODY;
	}
}

