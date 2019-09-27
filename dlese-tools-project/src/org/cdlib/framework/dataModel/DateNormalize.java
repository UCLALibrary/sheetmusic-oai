/*
Copyright (c) 2005-2006, Regents of the University of California
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:
 *
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
 ***********************************************************************/

package org.cdlib.framework.dataModel;


/**
 * <pre>
 * Tokenized CE date
 *
 * The object is included as part of a tokenized date range list
 *
 * The range list creation is done in DateNormalizer and the range extraction
 * is handled in RangeNormalize
 * </pre>
 * @author  dloy
 */
public class DateNormalize 
{
    public static final String UNAV = "(:unav)Unknown";
    public static final String UNKN = "(:unkn)Unknown";
    public static final String NONE = "(:none)Unknown";
    
    private String dateTemper = null;
    private String dateOriginal = null;
    private String dateNormalize = null;
    private String normalizeYear = null;
    private String normalizeMonth = null;
    private String normalizeDay = null;
    private String unknownDateType = null;
    private boolean unknownDate = false;
    private boolean beforeDate = false;
    private boolean copyright = false;
    private boolean circa = false;
    private boolean probableDate = false;
    private boolean exactYear = false;
    private boolean secondaryDate = false;


    public DateNormalize() {}
    
    public DateNormalize(DateNormalize in) {
        dateTemper = set(in.dateTemper);
        dateTemper = set(in.dateTemper );
        dateOriginal = set(in.dateOriginal );
        dateNormalize = set(in.dateNormalize );
        normalizeYear = set(in.normalizeYear );
        normalizeMonth = set(in.normalizeMonth );
        normalizeDay = set(in.normalizeDay );
        unknownDate = in.unknownDate ;
        beforeDate = in.beforeDate ;
        copyright = in.copyright ;
        circa = in.circa ;
        probableDate = in.probableDate ;
        exactYear = in.exactYear ;
        secondaryDate = in.secondaryDate;
    }
    
    public String dump(String hdr)
    {
        if (hdr == null) hdr = "";
        else hdr = hdr + " ";
        String out = hdr 
                        + " dn=" + getDateNormalize()
                        + " dt=" + getDateTemper()
                        + " do=" + getDateOriginal()
                        + " yr=" + getNormalizeYear()
                        + " mo=" + getNormalizeMonth()
                        + " day=" + getNormalizeDay()
                        + " cp=" + getCopyright()
                        + " ca=" + getCirca()
                        + " pd=" + getProbableDate()
                        + " ey=" + getExactYear();
        return out;
    }

    private String set(String in)
    {
        String retval = null;
        if (in != null) {
            retval = new String(in);
        }
        return retval;
    }

    public String getDateTemper() { return dateTemper ;}
    public String getDateOriginal() { return dateOriginal ;}
    public String getDateNormalize() { return dateNormalize ;}
    public String getNormalizeYear() { return normalizeYear ;}
    public String getNormalizeMonth() { return normalizeMonth ;}
    public String getNormalizeDay() { return normalizeDay ;}
    public String getUnknownDateType() { return unknownDateType ;}
    public boolean getBeforeDate() { return beforeDate ;}
    public boolean getUnknownDate() { return unknownDate ;}
    public boolean getCopyright() { return copyright;}
    public boolean getCirca() { return circa ;}
    public boolean getProbableDate() { return probableDate ;}
    public boolean getExactYear() { return exactYear ;}        
    public boolean getSecondaryDate() { return secondaryDate ;}        
    
    public void setDateTemper(String in) {dateTemper = in;}
    public void setDateOriginal(String in) {dateOriginal = in;}
    public void setDateNormalize(String in) {dateNormalize = in;}
    public void setNormalizeYear(String in) {normalizeYear = in ;}
    public void setNormalizeMonth(String in) {normalizeMonth = in ;}
    public void setNormalizeDay(String in) {normalizeDay = in ;}
    public void setUnknownDateType(String in) {unknownDateType = in ;}
    public void setBeforeDate(boolean in) {beforeDate = in;}
    public void setUnknownDate(boolean in) {unknownDate = in;}
    public void setCopyright(boolean in) {copyright= in;}
    public void setCirca(boolean in) {circa = in;}
    public void setProbableDate(boolean in) {probableDate = in;}
    public void setExactYear(boolean in) {exactYear = in;}
    public void setSecondaryDate(boolean in) {secondaryDate = in;}
}
