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
*********************************************************************/
package org.cdlib.framework.utility;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cdlib.framework.utility.StringUtil;
import org.cdlib.framework.utility.RegexPatternCache;
import org.cdlib.framework.utility.RegexUtil;
 /**
  * DateUtil - date utility methods
  *
  * @author  David Loy
  */
public class DateUtil
{
    private static RegexPatternCache patternCache = new RegexPatternCache();
    private static final String [] months =
        {"January", "February", "March", "April", "May", "June",
         "July", "August", "September", "October", "November", "December"};
         
    private static final String [] monthsLower =
        {"january", "february", "march", "april", "may", "june",
         "july", "august", "september", "october", "november", "december"};
         
    public static String replaceAmericanDate(String inStr)
    {
        String patternS = "([a-zA-Z]+) (\\d{1,2}),\\s?+(\\d\\d\\d\\d)";
        
         
        // Compile regular expression
        Pattern pattern = patternCache.getPattern(patternS);
        Matcher matcher = pattern.matcher(inStr);
    
        // Replace all occurrences of pattern in input
        StringBuffer buf = new StringBuffer();
        boolean found = false;
        while ((found = matcher.find())) {
            
            System.out.println("groupCount=" + matcher.groupCount());
            // Convert to uppercase
            if (matcher.groupCount() != 3) continue;
            String month = matcher.group(1);
            String day = matcher.group(2);
            String year = matcher.group(3);
            System.out.println("m:" + month + " d:" + day + " y:" + year);
            int monthval = -1;
            int i = -1;
            for (i=0; i < months.length; i++) {
                if (month.equalsIgnoreCase(months[i]) ) {
                    monthval += i + 1;
                    break;
                }
            }
            if (monthval == -1) continue;
            String newDate = year + "%" + monthval + "%" + day;

            // Insert replacement
            matcher.appendReplacement(buf, newDate);
        }
        matcher.appendTail(buf);
        return buf.toString();
    }
    
    /**
     * Build a normalized year%month%day based on pattern match against
     * an English based month name
     * @param inStr string containing date
     * @param patternS a date pattern with paranthetical groups around the date
     * elements
     * "(\\d\\d\\d\\d)\\s+?([a-zA-Z]+)\\s+?(\\d{1,2})"
     * for year month(in English) day
     * @param yearInx - index of subpattern containing year (e.g. 1)
     * @param monInx - index of subpattern containing month (e.g. 2)
     * @param dayInx - index of subpattern containing day (e.g. 3)
     * Note that < 1 indicates no field for extraction
     * @return String in form yyyy%mm%dd
     */
    public static String replaceDate(
        String inStr, 
        String patternS,
        int yearInx,
        int monInx,
        int dayInx)
    {
         
        // Compile regular expression
        Pattern pattern = patternCache.getPattern(patternS);
        Matcher matcher = pattern.matcher(inStr);
    
        // Replace all occurrences of pattern in input
        StringBuffer buf = new StringBuffer();
        boolean found = false;
        String month = null;
        String day = null;
        String year = null;
        while ((found = matcher.find())) {
            
            //System.out.println("groupCount=" + matcher.groupCount());
            // Convert to uppercase
            if (yearInx > 0) 
                year = matcher.group(yearInx);
            if (monInx > 0)
                month = matcher.group(monInx);
            if (dayInx > 0)
                day = matcher.group(dayInx);
            
            if (matcher.groupCount() == 3) {
                
            }
            
            if (month == null) continue;
            
            //System.out.println("m:" + month + " d:" + day + " y:" + year);
            int monthval = -1;
            int i = -1;
            for (i=0; i < months.length; i++) {
                if (month.equalsIgnoreCase(months[i]) ) {
                    monthval = i + 1;
                    break;
                }
            }
            
            if (monthval == -1) continue;
            StringBuffer newDate = new StringBuffer(30);
            if (year != null)
                newDate.append(year);
            if (monthval > 0)
                newDate.append("%" + monthval);
            if (day != null)
                newDate.append("%" + day);
            
            if (newDate.length() == 0) continue;
            
            // Insert replacement
            matcher.appendReplacement(buf, newDate.toString());
        }
        matcher.appendTail(buf);
        return buf.toString();
    }
   
    /**
     * Replace date abreviation with full English form of date name
     * Replace any token matching the first two or more characters is replaced
     * with the appropriate English form month
     * @param inStr string containing potential date
     * @return String with month name replaced
     */ 
    public static String replaceAbbrevMonth(String inStr)
    {
        String patternS = "\\s([jJfFmMaAsSoOnNdD][a-zA-Z]{2,})[.\\s]";
                 
        // Compile regular expression
        Pattern pattern = patternCache.getPattern(patternS);
        Matcher matcher = pattern.matcher(inStr);
    
        // Replace all occurrences of pattern in input
        StringBuffer buf = new StringBuffer();
        boolean found = false;
        while ((found = matcher.find())) {
            
            //System.out.println("groupCount=" + matcher.groupCount());
            // Convert to uppercase
            if (matcher.groupCount() != 1) continue;
            String testMonth = matcher.group(1);
            testMonth = testMonth.toLowerCase();
            
            //System.out.println("t:" + testMonth);
            int monthval = -1;
            int i = -1;
            String matchMonth = null;
            String replaceMonth = null;
            for (i=0; i < monthsLower.length; i++) {
                matchMonth = monthsLower[i];
                if (matchMonth.length() <= testMonth.length()) continue;
                if (matchMonth.substring(0,testMonth.length()).equals(testMonth) ) {
                    monthval = i + 1;
                    replaceMonth = months[i];
                    break;
                }
            }
            if (monthval == -1) continue;

            // Insert replacement
            matcher.appendReplacement(buf, " " + replaceMonth + " ");
        }
        matcher.appendTail(buf);
        return buf.toString();
    }

    /**
     * Return century in hundreds format
     * 
     * @param inStr string containing century
     * @return String with century replaced with hundreds format
     */ 
    public static String setCenturies(String inStr)
    {
        String saveStr = inStr;
        String [] subPattern = null;
        String pattern = null;
        String lower = inStr.toLowerCase();
        if (lower.indexOf("century") < 0) {
            if (lower.indexOf("c.") < 0) return inStr;
        }
        //System.out.println("Century found:" + inStr);
        String [] patterns = {
            "\\s(\\d\\d)-?\\s+[cC]entury",
            "\\s(\\d\\d)th\\s+[cC]entury",
            "\\s(\\d\\d)th\\s+[cC]\\W"};
        for (int i=0; i < patterns.length; i++) {
            pattern = patterns[i];
            subPattern = RegexUtil.listPattern(inStr, pattern);
            if (subPattern != null) break;
        }
 
        if (subPattern == null) {
            //System.out.println("no match 2");
            return inStr;
        }
            
        String centuryS = subPattern[1];
        int century = Integer.parseInt(centuryS);
        String hundreds = "" + (century - 1) + "==";
                
        inStr = RegexUtil.substitute(
            inStr,
            pattern, 
            new String [] {" ", hundreds," "});
        if (inStr.equals(saveStr)) return inStr;
        else inStr = setCenturies(inStr);
        return inStr;
    }
    
    /**
     * Take a date in normalize form dddd-dddd,dddd,dddd-dddd
     * and return a corresponding token form: dddd dddd dddd dddd
     * @param rangeDate normalized dates and date ranges
     * @return tokenized form of dates
     */
    public static String getTokenDate(String rangeDate, Logger logger)
    {
        if ((rangeDate == null) || (rangeDate.length() == 0)) return rangeDate;
        if (!rangeDate.matches("[0123456789\\-\\,\\s]")) return rangeDate;
        try {
            StringBuffer buf = new StringBuffer(1000);
            int [] dateArr = new int[3000];
            for (int i=0; i < dateArr.length; i++) {
                dateArr[i] = 0;
            }
            String [] ranges = rangeDate.split("\\s*?,\\s*?");
            for (int ri=0; ri < ranges.length; ri++) {
                String range = ranges[ri];
                String [] dates = range.split("\\s*?-\\s*?");
                if (dates.length > 2) {
                
                } else if (dates.length == 1) {
                    int value = Integer.parseInt(dates[0]);
                    if ((value > 0) && (value < dateArr.length)) {
                        dateArr[value]++;
                    }
                
                } else {
                    int start = Integer.parseInt(dates[0]);
                    int last = Integer.parseInt(dates[1]);
                    for (int si=0; si <= last; si++) {
                        dateArr[si]++;
                    }
                }
            }
        
            for (int i=0; i < dateArr.length; i++) {
                if (dateArr[i] > 0) {
                    if (buf.length() > 0) buf.append(" ");
                    buf.append(i);
                }
            }
                   
            return buf.toString();
            
        } catch (Exception ex) {
            logger.logError("Exception parsing range:" + rangeDate + " Exception:" + ex, 3);
            return rangeDate;
        }
 
    }
}
