/*********************************************************************
Copyright (c) 2005-2006, Regents of the University of California
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:
 
- Redistributions of source code must retain the above copyright notice,
  this list of conditions and the following disclaimer.
- Redistributions in binary form must reproduce the above copyright
  notice, this list of conditions and the following disclaimer in the
  documentation and/or other materials provided with the distribution.
- Neither the name of the University of California nor the names of its
  contributors may be used to endorse or promote products derived from
  this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
OF THE POSSIBILITY OF SUCH DAMAGE.
*********************************************************************/

package org.cdlib.framework.dataModel;

/**
 * <pre>
 * Date operater:
 * comma - single date
 * hyphen - date range
 *
 * The object is included as part of a tokenized date range list
 *
 * The range list creation is done in DateNormalizer and the range extraction
 * is handled in RangeNormalize
 * </pre>
 * @author  dloy
 */
public class DateOperator 
{
    public static final int NONE = -1;
    public static final int RANGE = 1;
    public static final int SERIES = 2;
    public static final int UNKNOWN = 3;
    private String stringType = null;
    private int type = NONE;

    public DateOperator(String token)
    {
        stringType = token;
        if (token.equals("-")) type = RANGE;
        else if (token.equals(",")) type = SERIES;
        else type = UNKNOWN;
    }
    
    public int getType() { return type;}
    public String getStringType() { return stringType ;}
    
        
    public void setType(int in) {type = in;}
    public void setStringType(String in) {stringType = in;}
}
