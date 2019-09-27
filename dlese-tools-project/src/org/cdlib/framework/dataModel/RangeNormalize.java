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
import java.util.Vector;
import org.cdlib.framework.utility.Logger;
import org.cdlib.framework.utility.RegexUtil;

/**
 * <pre>
 * RangeNormalize handles sets of date and date ranges
 * The class is used for generating the normalized form of date (date.normalize):
 * 1940,1942-1944,1950
 * The tokenized form of the date:
 * 1940 1942 1943 1944 1950
 * The tokenized decade:
 * 1940 1950
 * 
 * RangeNormalize will then build a tokenize form for both the date.tokens and 
 * date.era.
 *
 * Note the format of a range token list (saved as a Vector) contains
 * the corresponding elements of the normalized range:
 * year -> DateNormalize
 * range or operator (,-) -> DateOperator
 *
 * date.tokens takes the form date.tokens=1940 1942 1943 1944 1950
 *
 * RangeNormalize returns all constructed dates in a list. Each date takes
 * the form
 * name=value
 * The name is repeatable and contains no spacing characters (becomes element
 * name)
 * value may contain spacing character
 * </pre>
 * @author  David Loy
 */
public class RangeNormalize 
{
    private static final int EMPTY = -1;
    
    public static final String FOUNDTAG = "date.found";
    public static final String GUESSTAG = "date.guess";
    public static final String NORMALIZETAG = "date.normalize";
    public static final String TEMPERTAG = "date.temper";
    public static final String DECADETAG = "date.decade";
    public static final String TOKENTAG = "date.tokens";
    
    private String dateOriginal = null; 
    private String elementName = null;
    private String fileName = null;
    private String normalizeDecade = null;
    private String normalizeDate = null;
    private String normalizeYearTokens = null;
    private String rangeTemper = null;  
    private boolean secondaryDate = false;
    private String unknownDateType = null;
    private boolean unknownDate = false;  
    private boolean dateSet = false;
    private char [] yearArray = new char[3000];
    private char [] decadeArray = new char[300];
    private StringBuffer rangeTemperBuf = new StringBuffer(100);
    private int maxRange = EMPTY;
    private int minRange = EMPTY;
    private int yearCnt = 0;

    public RangeNormalize() {
        for (int i=0; i < yearArray.length; i++) {
            yearArray[i] = ' ';
        }
        for (int i=0; i < decadeArray.length; i++) {
            decadeArray[i] = ' ';
        }
    }
    
    public RangeNormalize(RangeNormalize in) {
        rangeTemper = set(in.rangeTemper);
        rangeTemper = set(in.rangeTemper );
        dateOriginal = set(in.dateOriginal );
        normalizeDate = set(in.normalizeDate );
        
    }
    
    /**
     * build the date ranges using normalized date
     * @param dateList array of normalized dates
     */ 
    public void setMetadata(Vector dateList) 
    {
        DateNormalize startNorm = (DateNormalize)dateList.elementAt(0);
        DateNormalize nextNorm = null;
   
        startNorm = null;
        DateOperator operator = null;
        DateNormalize normalize = null;
        String op = null;
        
        for (int i=0; i < dateList.size(); i++) {
            
            Object object = dateList.get(i);
            if (object instanceof DateOperator) {
                operator = (DateOperator) object;
                op = operator.getStringType();
                
            } else if (object instanceof DateNormalize) {
                normalize = (DateNormalize) object;
                if (normalize.getSecondaryDate()) secondaryDate = true;
                if (startNorm == null) {
                    startNorm = normalize;
                    
                } else if (nextNorm == null) {
                    nextNorm = normalize;
                }
            }
            if ((op != null) && op.equals(",")) {
                addRange(startNorm, nextNorm);
                startNorm = null;
                nextNorm = null;
                op = " ";
            }
            
        }
        addRange(startNorm, nextNorm);
        
        setRange();
        setTemperRange(dateList);
        return;
    }  
   
    /**
     * build this single  date ranges using normalized dates
     * @param dateList array of normalized dates
     */ 
    public void setMetadata(String [] dateList) 
    {

        if ((dateList == null) || (dateList.length == 0)) {
            System.out.println("dateList:" + null);
            return;
        }
        String rangeDate = null;
        Vector dateArray = new Vector(100);
        
        // go through a list of date ranges AND CONSOLIDATE
        for (int idl = 0; idl < dateList.length; idl++) {
            rangeDate = dateList[idl];
            System.out.println("rangeDate:" + rangeDate);
            
            
            // unknown type occurs
            if (rangeDate.equals(DateNormalize.UNAV) 
                || rangeDate.equals(DateNormalize.UNKN)
                || rangeDate.equals(DateNormalize.NONE)) {
                normalizeDate = rangeDate;
                rangeTemper = rangeDate;
                normalizeYearTokens = rangeDate;
                
            // invalid range
            } else if (!rangeDate.matches("[0123456789\\-\\,\\s]+")) continue;
            
            // valid date range
            else {
                stringToRange(dateArray, rangeDate);
            }
        }
        
        // may not occur for unknown types
        if (dateArray.size() > 0) {
            setMetadata(dateArray);
        }
        return;
    }
    
    /**
     * Takes a normalized date range 1933,1944-1945
     * and creates a range token list
     * @param dateArray Vector object to contain range token list
     * @param rangeDate normalized String date range
     */
    private void stringToRange(Vector dateArray, String rangeDate)
    {
        //System.out.println("stringToRange");
        try {
            
            String [] ranges = rangeDate.split("\\s*?,\\s*?");
            for (int ri=0; ri < ranges.length; ri++) {
                String range = ranges[ri];
                //System.out.println("range:" + range);
                String [] dates = range.split("\\s*?-\\s*?");
                if (dates.length > 2) {
                
                } else if (dates.length == 1) {
                    String date = dates[0];
                    if (date.length() != 4) continue;
                    if (dateArray.size() > 0) {
                        DateOperator op = new DateOperator(",");
                        dateArray.add(op);
                    }
                    DateNormalize nDate = new DateNormalize();
                    nDate.setDateNormalize(date);
                    dateArray.add(nDate);
                   
                } else {
                    String start = dates[0];
                    String last = dates[1];
                    if (start.length() != 4) continue;
                    if (last.length() != 4) continue;
                    if (dateArray.size() > 0) {
                        DateOperator op = new DateOperator(",");
                        dateArray.add(op);
                    }
                    DateNormalize nDate = new DateNormalize();
                    nDate.setDateNormalize(start);
                    dateArray.add(nDate);
                    DateOperator op = new DateOperator("-");
                    dateArray.add(op);
                    nDate = new DateNormalize();
                    nDate.setDateNormalize(last);
                    dateArray.add(nDate);
                }
            }

        } catch (Exception ex) {
            System.out.println("Exception parsing range:" + rangeDate + " Exception:" + ex);
            return;
        }
    }
    
    /**
     * add range values from start and end to year range array
     * @param startRange normalized beginning of range
     * @param endRange normalized end of range
     */
    public void addRange(DateNormalize startRange, DateNormalize endRange)
    {
        int sYear = -1;
        int eYear = -1;
        char ttype = 'e';
        boolean circa = false;
        if (startRange == null) return;
        sYear = Integer.parseInt(startRange.getDateNormalize());
        if (sYear >= yearArray.length) return;
        yearArray[sYear] = 's';
        eYear = sYear;
        if (startRange.getSecondaryDate()) {
            secondaryDate = true;
        }
        if (startRange.getCirca()) {
            circa = true;
        }
        if (!startRange.getExactYear() 
            || startRange.getCirca()
            || startRange.getProbableDate()) ttype='c';
        
        if (endRange != null) {
            eYear = Integer.parseInt(endRange.getDateNormalize());            
            if ((eYear < 0) || (eYear > yearArray.length)) return;
            if (endRange.getCirca()) {
                circa = true;;
            }
            if (!endRange.getExactYear() 
                || endRange.getCirca()
                || endRange.getProbableDate()) ttype='c';
        
            if (eYear < sYear) {
                yearArray[eYear] = ttype;
                eYear = sYear;
            } 
            
        }
        if (circa) {
            eYear += 5;
            sYear -= 5;
        }
        //System.out.println("RangeNormalize start=" + sYear + " - end=" + eYear);
        for (int i=sYear; i <= eYear; i++) {
            yearArray[i] = ttype;
        }
    }

    /**
     * setRange builds the normalized range from a year range array
     * The range array was build prior to this. The method loops through the
     * array and creates a standard range.
     */
    private void setRange()
    {

        int startRange = EMPTY;
        int endRange = EMPTY;
        yearCnt = 0;
        
        char year = ' ';
        StringBuffer range = new StringBuffer(100);
        StringBuffer temper = new StringBuffer(100);
        StringBuffer decade = new StringBuffer(100);
        StringBuffer yearTokens = new StringBuffer(5000);
        char rtype = ' ';
        char ttype = ' ';
        int normDecade = 0;
        for (int i=0; i < yearArray.length; i++) {
            year = yearArray[i];
            if (year != ' ') {
                if (startRange == EMPTY) startRange = i;
                endRange = i;
                if (year == 'c') ttype = 'c';
                yearCnt++;
                if (minRange == EMPTY) minRange = i;
                normDecade = i / 10;
                decadeArray[normDecade] = 's';
                yearTokens.append(" " + i);
                
                
            } else { 
                if (startRange > EMPTY) {
                    if (range.length() > 0) range.append(',');
                    if (temper.length() > 0) temper.append(',');
                    range.append(startRange);
                    temper.append(startRange);
                    if (ttype == 'c') temper.append('~');
                    if (endRange > startRange) {
                        range.append('-');
                        range.append(endRange);
                        temper.append('-');
                        temper.append(endRange);
                        if (ttype == 'c') temper.append('~');
                    }
                    maxRange = endRange;
                    startRange = EMPTY;
                    endRange = EMPTY;
                    ttype = ' ';
                }
            }
        }
        normalizeDate = range.toString();
        normalizeYearTokens = yearTokens.toString();
        for (int i=0; i < decadeArray.length; i++) {
            if (decadeArray[i] == 's') {
                if (decade.length() > 0) decade.append(' ');
                decade.append(i);
                decade.append('0');
            }
        }
        normalizeDecade = decade.toString();
        if (yearCnt > 0) dateSet = true;
        //rangeTemper = temper.toString();
    }
 
    
    /**
     * Build date.temper from a tokenized date list
     * @param dateList list of date and date operators
     */
    private void setTemperRange(Vector dateList) 
    {
        DateNormalize startNorm = (DateNormalize)dateList.elementAt(0);
        DateNormalize nextNorm = null;
        StringBuffer temper = new StringBuffer(200);
        
        startNorm = null;
        DateOperator operator = null;
        DateNormalize normalize = null;
        String op = null;
        boolean fuzzy = false;
        
        for (int i=0; i < dateList.size(); i++) {
            
            Object object = dateList.get(i);
            if (object instanceof DateOperator) {
                operator = (DateOperator) object;
                op = operator.getStringType();
                                
            } else if (object instanceof DateNormalize) {
                normalize = (DateNormalize) object;
                if (startNorm == null) {
                    startNorm = normalize;
                } else if (nextNorm == null) {
                    nextNorm = normalize;
                }
            }
            if ((op != null) && op.equals(",")) {
                addTemperRange(temper, startNorm, nextNorm);
                startNorm = null;
                nextNorm = null;
                op = " ";
            }
            
        }
        addTemperRange(temper, startNorm, nextNorm);
        
        rangeTemper = temper.toString();
        return;
    }  
    
    /**
     * build part of temper range for date.temper
     * @param temper StringBuffer containing final date.temper
     * @param startRange beginning of range part
     * @param endRange end of range part
     */
    private void addTemperRange(
            StringBuffer temper, 
            DateNormalize startRange, 
            DateNormalize endRange)
    {
        boolean fuzzy = false;
        
        if (startRange == null) return;
        if (temper.length() > 0) temper.append(',');    
        temper.append(startRange.getDateNormalize());
        if (startRange.getCirca()
                || startRange.getProbableDate()) {
            fuzzy = true;
            temper.append('~');
        }
        

        if (endRange != null) {
            temper.append('-');
            temper.append(endRange.getDateNormalize());
            if (endRange.getCirca()
                    || endRange.getProbableDate()
                    || fuzzy) {
                fuzzy = true;
                temper.append('~');
            }
        }
    }
    
    private String set(String in)
    {
        String retval = null;
        if (in != null) {
            retval = new String(in);
        }
        return retval;
    }
        
    public synchronized void dump(String hdr, Logger logger, int level)
    {
        if (hdr != null) 
            logger.logMessage("----->RangeNormalize DUMP " + hdr + " - " + fileName, level);
        logger.logMessage("original: " + getDateOriginal(), level);
        logger.logMessage("normalize:" + getRangeNormalize(),level);
        logger.logMessage("temper:   " + getRangeTemper(),level);
        logger.logMessage("decade:   " + getNormalizeDecade(),level);
        if (getNormalizeYearTokens() != null)
            logger.logMessage("token:   " + getNormalizeYearTokens(),level);
        logger.logMessage("maxRange=" + getMaxRange() 
            + " minRange=" + getMinRange() + " cnt=" + getYearCnt(), level);
        logger.logMessage("<---------------", level);
    }
    
    /**
     * Add output dates to a Vector
     * @param retDates vector to contain dates
     */
    public void addDateList(Vector retDates)
    {
        retDates.add(NORMALIZETAG + "=" + normalizeDate);
        retDates.add(TEMPERTAG + "=" + rangeTemper);
        retDates.add(DECADETAG + "=" + normalizeDecade);
        retDates.add(TOKENTAG + "=" + normalizeYearTokens);
        if ((elementName != null) && elementName.equals("date")) {
            retDates.add(FOUNDTAG + "=" + normalizeDate);
        } else {
            retDates.add(GUESSTAG + "=" + normalizeDate);
        }
        
    }

    public void setRangeUnknown(String unknownType)
    {
        if (unknownType == null) {
            unknownType = DateNormalize.NONE;
        }
        normalizeDate = unknownType;
        rangeTemper = unknownType;
    }
    
    public static void addUnknownDate(Vector retDates, String unknownType)
    {
        if (unknownType == null) {
            unknownType = DateNormalize.NONE;
        }
        retDates.add(NORMALIZETAG + "=" + unknownType);
        retDates.add(TEMPERTAG + "=" + unknownType);
    }
    
    public String[] getDateList()
    {
        Vector retDates = new Vector(4);
        addDateList(retDates);
        String [] dummy = new String[0];
        return (String[])retDates.toArray(dummy);
    }
    
    public String getRangeTemper() { return rangeTemper ;}
    public String getDateOriginal() { return dateOriginal ;}
    public String getFileName() { return fileName; }
    public String getRangeNormalize() { return normalizeDate ;}
    public String getNormalizeDecade() { return normalizeDecade; }
    public String getNormalizeYearTokens() { return normalizeYearTokens; }
    public String getElementName() { return elementName; }
    public String getUnknownDateType() { return unknownDateType ;}
    public boolean getUnknownDate() { return unknownDate ;}
    public boolean getSecondaryDate() { return secondaryDate ;}    
    public String getMaxRange() { return "" + maxRange; }
    public String getMinRange() { return "" + minRange; }
    public int getMaxValue() { return maxRange; }
    public int getMinValue() { return minRange; }
    public int getYearCnt() { return yearCnt; }
    public char[] getYearArray() { return yearArray; }

    public boolean isDateSet() { return dateSet; }
    
    public void setRangeTemper(String in) {rangeTemper = in;}
    public void setDateOriginal(String in) {dateOriginal = in;}
    public void setElementName(String in) { elementName = in; }
    public void setFileName(String in) { fileName = in; }
    public void setRangeNormalize(String in) {normalizeDate = in;}
    public void setUnknownDateType(String in) {unknownDateType = in;}
    public void setUnknownDate(boolean in) {unknownDate = in;}
    public void setDateSet(boolean in) {dateSet = in;}
    
}
