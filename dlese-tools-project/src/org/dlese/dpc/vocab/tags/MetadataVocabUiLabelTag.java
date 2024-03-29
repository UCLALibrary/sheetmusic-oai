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

package org.dlese.dpc.vocab.tags;

import org.dlese.dpc.vocab.MetadataVocab;
import org.dlese.dpc.util.strings.StringUtil;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.http.*;

/**
 *  Tag handler for retreiving a UI label from the given field and value
 *
 * @author    Ryan Deardorff
 */
public class MetadataVocabUiLabelTag extends MetadataVocabTag {
	String field = "";
	String value = "";
	boolean useMetaNames = false;                                  // use metadata names instead of system ids?
	boolean getAbbreviated = false;                                // return abbreviated version of label?

	/**
	 *  Sets the system attribute of the MetadataVocabGetFieldValueUiLabel object
	 *
	 * @param  field  The new fieldId value
	 */
	public void setField( String field ) {
		this.field = field;
	}

	/**
	 *  Sets the system attribute of the MetadataVocabGetFieldValueUiLabel object
	 *
	 * @param  value  The new value value
	 */
	public void setValue( String value ) {
		this.value = value;
	}

	/**
	 *  Sets the useMetaNames attribute of the MetadataVocabFieldValueUiLabelTag
	 *  object
	 *
	 * @param  bool  The new useMetaNames value
	 */
	public void setUseMetaNames( String bool ) {
		if ( bool.toLowerCase().equals( "true" ) ) {
			useMetaNames = true;
		}
	}

	/**
	 *  Sets the getAbbreviated attribute of the MetadataVocabUiLabelTag object
	 *
	 * @param  bool  The new getAbbreviated value
	 */
	public void setGetAbbreviated( String bool ) {
		if ( bool.toLowerCase().equals( "true" ) ) {
			getAbbreviated = true;
		}
	}

	/**
	 *  Start tag
	 *
	 * @return
	 * @exception  JspException
	 */
	public int doStartTag() throws JspException {
		try {
			setupTag( pageContext );
			StringBuffer outStr = new StringBuffer();
			if ( useMetaNames ) {
				// Retreive a single value using metadata names
				pageContext.getOut().print( StringUtil.replace(
					vocab.getUiLabelOf( system, field, value, getAbbreviated ), "||", "<br>", false ) );
			}
			else {
				// When using system ids, it is assumed to be a LIST of values, seperated by '+'
				value = value + "+";
				int ind1 = 0;
				int ind2 = value.indexOf( "+" );
				while ( ind2 > -1 ) {
					outStr.append( vocab.getUiLabelOfSystemIds( system, field,
						value.substring( ind1, ind2 ), getAbbreviated ) + ", " );
					ind1 = ind2 + 1;
					ind2 = value.indexOf( "+", ind1 );
				}
				outStr.setLength( outStr.length() - 2 );
				pageContext.getOut().print( StringUtil.replace( outStr.toString(), "||", "<br>", false ) );
			}
		}
		catch ( java.io.IOException ex ) {
			throw new JspException( ex.getMessage() );
		}
		return SKIP_BODY;
	}
}

