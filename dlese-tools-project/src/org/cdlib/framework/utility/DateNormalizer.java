/*
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
package org.cdlib.framework.utility;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;
import java.util.regex.Pattern;

import org.cdlib.framework.utility.Framework;
import org.cdlib.framework.utility.FrameworkException;
import org.cdlib.framework.utility.Logger;
import org.cdlib.framework.utility.StringUtil;
import org.cdlib.framework.dataModel.DateNormalize;
import org.cdlib.framework.dataModel.DateOperator;
import org.cdlib.framework.dataModel.EraNormalize;
import org.cdlib.framework.dataModel.RangeNormalize;
import org.cdlib.framework.utility.DateUtil;
import org.cdlib.framework.utility.RegexUtil;
import org.cdlib.framework.utility.DOMParser;

import org.cdlib.framework.utility.StringUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * <pre>
 * DateNormalizer is the top level class for handling
 * the creation of normalized date indexes 
 * This routine will only process Christian Era dates and the output 
 * is primarily used for indexing purposes
 *
 * The methods in this class function at different levels:
 * The methods "extract" and "findDates" deal with a DC formatted DOM
 * The methods "processDate" and "processText" deal with extracted element values
 *
 * Created on January 29, 2004, 12:22 PM
 * </pre>
 * @author  David Loy
 * @version %I%, %G%
 */
public class DateNormalizer
{
    private Framework m_fw = null;
    private Logger logger = null;
    private final static String CIRCA = "+CA+";
    private int currentYear = -1;
    private static final int maxsize = 1000000;
    
    private static int requestCnt = 0;
    private static int noDcCnt = 0;
    private static int outUnknownCnt = 0;
    private static int recordsWithDateSet = 0;
    protected Properties m_toolProp = null;
    protected String m_headerElement = null;
    protected static Hashtable stats = new Hashtable();

    public DateNormalizer() {}
    public void initialize(Framework fw, Properties eras)
    {
        m_fw = fw;
        logger = m_fw.getLogger();
        Calendar cal = new GregorianCalendar();
        currentYear = cal.get(Calendar.YEAR);
        
    }
    public void initialize(Framework fw)
    {
        m_fw = fw;
        logger = m_fw.getLogger();
        Calendar cal = new GregorianCalendar();
        currentYear = cal.get(Calendar.YEAR);
    }
    
    public int getRequestCnt()
    {
        return requestCnt;
    }
    
    public int getNoDcCnt()
    {
        return noDcCnt;
    }
    public int getRecordsWithDateSet()
    {
        return recordsWithDateSet;
    }
    
    public Hashtable getStats()
    {
        return stats;
    }
   
    /**
     * Get normalized date
     * @param extractFileName name of external file - used for logging
     * not extraction
     * @param qdc dom element for top of Dublin Core
     * @return name=values for normalize/temper dates
     */
    public String [] extract(String extractFileName, Element qdc)
    {
        return extract(extractFileName, qdc, null);
    }
    
    /**
     * Get normalized date
     * @param extractFileName name of external file - used for logging
     * not extraction
     * @param qdc dom element for top of Dublin Core
     * @return name=values for normalize/temper dates
     */
    public String [] extract(String extractFileName, Element qdc, EraNormalize eras)
    {
        Counts count = null;
        String status = null;
        Logger logger = m_fw.getLogger();
        
        try {
            if (qdc == null) {
                m_fw.getLogger( ).logMessage(
                    "DateList: No qdc found for:" + extractFileName, 3);
                 noDcCnt++;
                 return null;
            } else {
                requestCnt++;
                m_fw.getLogger( ).logMessage(
                    "****DateNormalizer - Begin processing:" + extractFileName, 10);
            }
            Vector foundDates = findDates(extractFileName, qdc);
            return buildDates(foundDates, eras);

            
        } catch(Exception ex) {
            logger.logError("Exception in run:" + ex, 3);
            logger.logMessage("DateNormalizer.extract stack:" + StringUtil.stackTrace(ex), 10);
            return null;
        }
    }

    /**
     * Return normalized date - File level
     * This routine handles the logic of which elements to select for date extraction
     * and processing
     * @param extractFileName name of external file to be processed
     * @param qdc dom element for top of Dublin Core
     * @return name=values for normalize/temper dates
     */   
    public Vector findDates(String extractFileName, Element qdc)
    {
        Vector dates = new Vector(10);
        Vector datesElement = null;
        Logger logger = m_fw.getLogger();
        boolean dateFound = false;
        String unknownType = null;
        Counts count = null;
        
        try {

            RangeNormalize[] rangeDates = processDate(extractFileName, qdc);
            RangeNormalize range = null;
            if (rangeDates != null) {
                unknownType = testUnknown(rangeDates);
                if (unknownType == null) {
                    datesElement = testDateValues(rangeDates);
                    if (datesElement != null) {
                        count = stat("date");
                        count.recordSetCnt++;
                        recordsWithDateSet++;
                        return datesElement;
                    }
                }
            }

            
            rangeDates = processText(extractFileName, "title", qdc);
            if (rangeDates != null) {
                for (int i=0; i < rangeDates.length; i++) {
                    range = rangeDates[i];
                    dates.add(range);
                }
                if (dates.size() > 0) {
                    count = stat("title");
                    count.recordSetCnt++;
                    recordsWithDateSet++;
                }
                return dates;
            }
           
            rangeDates = processText(extractFileName, "description", qdc);
            if (rangeDates != null) {
                for (int i=0; i < rangeDates.length; i++) {
                    range = rangeDates[i];
                    dates.add(range);
                }
                if (dates.size() > 0) {
                    count = stat("description");
                    count.recordSetCnt++;
                    recordsWithDateSet++;
                }
                return dates;
            }
            if (unknownType != null) {
                outUnknownCnt++;
                return setUnknown(unknownType);
            }
 
                    
        } catch (Exception ex) {
            logger.logError(
                        "Exception DateList Thread(" 
                        + Thread.currentThread().getName() 
                        + ") file:" + extractFileName + " exception:" + ex, 3);
            logger.logError("trace" + StringUtil.stackTrace(ex), 10);
        }
        return null;
    }
    
    /**
     * Get normalized date from <date> element
     * @param extractFileName name of external file to be processed
     * @param qdc dom element for top of Dublin Core
     * @return name=values for normalize/temper dates
     */
    public RangeNormalize[] processDate(String extractFileName, Element qdc)
    {
        String name = "date";
        Logger logger = m_fw.getLogger();
        
        RangeNormalize rangeNormalize = null;
        RangeNormalize [] rangeNormalizeList  = null;
        Counts count = stat(name);
        try {
            String [] values = extractElement(qdc, name);
            if ((values == null) || (values.length == 0)) {
                count.noElement++;
                m_fw.getLogger( ).logMessage(
                    "DateList: Nothing found for " + name + ":" + extractFileName, 3);
                count.elementCnt[0]++;
                return null;
            }            
            if (values.length > 10) count.elementCnt[10]++;
            else count.elementCnt[values.length]++;

            rangeNormalizeList = processDate(extractFileName, values);
            if ((rangeNormalizeList == null) || (rangeNormalizeList.length == 0)) {
                count.noElement++;
                m_fw.getLogger( ).logMessage(
                    "DateList: Nothing found for " + name + ":" + extractFileName, 3);
                count.elementCnt[0]++;
                return null;
            }

                    //System.out.println("**** DATE LIST: size=" + dates.length);
            for (int i=0; i < rangeNormalizeList.length; i++) {
                rangeNormalize = rangeNormalizeList[i];
                
                //System.out.println("***rangeNormalize NULL***");
                if (rangeNormalize != null) {
                    countIt(name, count, rangeNormalize);
                }
            }
            logger.logMessage(
                        "DateList Thread(" 
                        + Thread.currentThread().getName() 
                        + " name:"  + extractFileName , 4);
                    
        } catch (Exception ex) {
                    logger.logError(
                        "Exception DateList Thread(" 
                        + Thread.currentThread().getName() 
                        + ") file:" + extractFileName + " exception:" + ex, 3);
                    logger.logError("trace" + StringUtil.stackTrace(ex), 10);
        }
        
        return rangeNormalizeList;
 
    }
    
    /**
     * Get normalized date from non-<date> element
     * @param extractFileName name of external file to be processed
     * @param name name of element to be processed
     * @param qdc dom element for top of Dublin Core
     * @return name=values for normalize/temper dates
     */                    
    public RangeNormalize[] processText(String extractFileName, String name, Element qdc)
    {
        
        Logger logger = m_fw.getLogger();
        logger.logMessage(
                    "DateList Start Thread(" + Thread.currentThread().getName(), 3);
        RangeNormalize rangeNormalize = null;
        RangeNormalize [] rangeNormalizeList  = null;
        Counts count = stat(name);
        try {
            String [] values = extractElement(qdc, name);
            if ((values == null) || (values.length == 0)) {
                count.noElement++;
                m_fw.getLogger( ).logMessage(
                    "DateList: Nothing found for " + name + ":" + extractFileName, 3);
                count.elementCnt[0]++;
                return null;
            }            
            if (values.length > 10) count.elementCnt[10]++;
            else count.elementCnt[values.length]++;

            rangeNormalizeList = processText(extractFileName, name, values);
            if ((rangeNormalizeList == null) || (rangeNormalizeList.length == 0)) {
                count.noElement++;
                m_fw.getLogger( ).logMessage(
                    "DateList: Nothing found for " + name + ":" + extractFileName, 3);
                count.elementCnt[0]++;
                return null;
            }
            
                    //System.out.println("**** DATE LIST: size=" + dates.length);
            for (int i=0; i < rangeNormalizeList.length; i++) {
                rangeNormalize = rangeNormalizeList[i];
                
                //System.out.println("***rangeNormalize NULL***");
                if (rangeNormalize != null) {
                    countIt(name, count, rangeNormalize);
                }
            }
            logger.logMessage(
                        "DateList Thread(" 
                        + Thread.currentThread().getName() 
                        + " name:"  + extractFileName , 4);
                    
        } catch (Exception ex) {
                    logger.logError(
                        "Exception DateList Thread(" 
                        + Thread.currentThread().getName() 
                        + ") file:" + extractFileName + " exception:" + ex, 3);
                    logger.logError("trace" + StringUtil.stackTrace(ex), 10);
        }
        
        return rangeNormalizeList;
 
    }

    /** log stats for date count object
     * @param name element name of these stats
     */
    public void logStats(String name)
    {
        if (StringUtil.isEmpty(name)) return;
        Counts count = stat(name);
        
        m_fw.getLogger( ).logMessage(
                "Stats " + name + " --------------------", 1);

        m_fw.getLogger( ).logMessage("Thread process count: " + requestCnt, 1);
        m_fw.getLogger().logMessage("Unknown count:" + outUnknownCnt, 1);
        m_fw.getLogger().logMessage("Elements processed:" + count.processCnt, 1);
        m_fw.getLogger().logMessage("Record dates set:" + count.recordSetCnt, 1);
        m_fw.getLogger().logMessage("Dates set:" + count.dateSetCnt, 1);
        m_fw.getLogger().logMessage("No element:" + count.noElement, 1);
        m_fw.getLogger().logMessage("Secondary Date:" + count.secondaryDateCnt, 1);
        m_fw.getLogger().logMessage("Unknown date:" + count.unknownCnt, 1);                    
        m_fw.getLogger().logMessage("Undefined date:" + count.nullCnt, 1);                    
        m_fw.getLogger().logMessage("unkn date:" + count.unknCnt, 1); 
        m_fw.getLogger().logMessage("unav date:" + count.unavCnt, 1);
        int totalDateRecords = 0;
        int totalDates = 0;
        for (int i=0; i < count.elementCnt.length; i++) {
            int value = count.elementCnt[i];
            if (value > 0) {
                    m_fw.getLogger().logMessage(name + "[" + i + "]:" + value, 1);
                if (i > 0) {
                        totalDateRecords += value;
                        totalDates += value * i;
                }
            }
        }
        m_fw.getLogger().logMessage("Total " + name + " Records:" + totalDateRecords, 1);
        m_fw.getLogger().logMessage("Total " + name + ":" + totalDates, 1);
    }
  
    /**
     * accumulate date statistics for this element
     * @param name name of element processed
     * @param count stats object for this element
     * @param rangeNormalize normalized range for this stat
     */
    private synchronized void countIt(String name, Counts count, RangeNormalize rangeNormalize)
    {
        if (rangeNormalize != null) {
            count.processCnt++;
            if (rangeNormalize.isDateSet()) count.dateSetCnt++;
            if (rangeNormalize.getUnknownDate()) {
                    count.unknownCnt++;
                    String unknownDateType = rangeNormalize.getUnknownDateType();
                    if (unknownDateType  != null) {
                        if (unknownDateType.equals(":none")) {
                            m_fw.getLogger().logMessage("Undefined:" + rangeNormalize.getDateOriginal() 
                                + " -->" + rangeNormalize.getFileName() , 3);
                            count.nullCnt++;

                        } else if (unknownDateType.equals(":unkn")) {
                            m_fw.getLogger().logMessage("unkn:" + rangeNormalize.getDateOriginal(), 3);
                            count.unknCnt++;

                        } else if (unknownDateType.equals(":unav")) {
                            m_fw.getLogger().logMessage("unav:" + rangeNormalize.getDateOriginal(), 4);
                            count.unavCnt++;
                        }

                    } else if (rangeNormalize.getSecondaryDate()) {
                        count.secondaryDateCnt++;
                    }
            }
        }
    }
        
    /**
     * build array of metadata tags matching with the element name
     * @param qdc dublin core dom
     * @param name name of element to extract
     * @return array of matching element values
     */
    private String [] extractElement(Element qdc, String name)
        throws FrameworkException
    {
        if (qdc == null) {
            m_fw.getLogger( ).logError(
                    "DateList: no qdc", 3);
            return null;
        }
        
        if (name == null) {
            m_fw.getLogger( ).logError(
                    "DateList: no name", 3);
            return null;
        }
                       
        //System.out.println("extractDate entered");
        Vector dcArray = new Vector(100);
        try {

            NodeList dcChildren = qdc.getChildNodes();
            for (int i=0; i < dcChildren.getLength(); i++) {
                Node n = dcChildren.item(i);
                String key = n.getNodeName();
                
                if (key.charAt(0) == '#') continue;
                if (key.substring(0,3).equals("dc:")) {
                    key = key.substring(3);
                }
                //System.out.println("key=" + key);
                if (key.equalsIgnoreCase(name)) {
                    String value = DOMParser.getSimpleElementText(n, m_fw.getLogger());
                    String save = value;
                    //m_fw.getLogger().logMessage("**** save:" + save, 3);
                    dcArray.addElement(save);
                }
            }
            String [] dummy = new String[0];
            return (String[])dcArray.toArray(dummy);
                    
        } catch (Exception ex) {
            m_fw.getLogger( ).logError(
                    "DateList:extractDate: exception:" + ex, 3);
            m_fw.getLogger( ).logError(
                    "DateList:extractDate: cause:" + ex.getCause(), 3);
            m_fw.getLogger().logMessage(
                    "stack:" + StringUtil.stackTrace(ex), 3);
            throw new FrameworkException(FrameworkException.GENERAL_EXCEPTION, ex);
        }
    }

    /**
     * get a count stats object based on name if already exists otherwise allocate one
     * @param name name of stats object (e.g. element)
     * @return stats object
     */
    private synchronized Counts stat(String name)
    {
        Counts counts = (Counts)stats.get(name);
        if (counts == null) {
            counts = new Counts();
            stats.put(name, counts);
        }
        return counts;
    }
   
    /**
     * dump results
     */
    public synchronized void dumpFinal(String extractFileName, String [] dates, int lvl) 
    {
        m_fw.getLogger( ).logMessage(
                    "****Extract name:" + extractFileName, lvl);
        if (dates == null) {
            m_fw.getLogger( ).logMessage(
                " d: NONE", lvl);
        } else  {
            String date = null;
            for (int i=0; i < dates.length; i++) {
                date = dates[i];
                       
                m_fw.getLogger( ).logMessage(
                    "Extract:" + date, lvl);
            }
        }
        m_fw.getLogger( ).logMessage(
                    "*****************", lvl);
    }
    

    /**
     * Given a list of dates element values return the normalized ranges
     * @param extractFileName name of file to have extraction
     * @param values array of date values to be tested
     * @return array of normalized ranges
     */
   public RangeNormalize[] processDate(String extractFileName, String [] values)
    {
        String name = "date";
        Logger logger = m_fw.getLogger();
        RangeNormalize rangeNormalize = null;
        Vector rangeList = new Vector(10);

        try {
            if ((values == null) || (values.length == 0)) {
                m_fw.getLogger( ).logMessage(
                    "FileExtract_DATE: Nothing found for " + name + ":" + extractFileName, 3);
                return null;
            }
                    
            String value = null;

                    //System.out.println("**** DATE LIST: size=" + dates.length);
            for (int i=0; i < values.length; i++) {
                value = values[i];
                rangeNormalize = processDate(value);
                
                //System.out.println("***rangeNormalize NULL***");
                if (rangeNormalize != null) {
                    rangeNormalize.setFileName(extractFileName);
                    rangeNormalize.setElementName(name);
                    rangeNormalize.dump(name, logger, 3);
                    rangeList.add(rangeNormalize);
                }
            }
            logger.logMessage(
                        "FileExtract_DATE Thread(" 
                        + Thread.currentThread().getName() 
                        + " name:"  + extractFileName , 4);
                    
        } catch (Exception ex) {
                    logger.logError(
                        "Exception FileExtract_DATE Thread(" 
                        + Thread.currentThread().getName() 
                        + ") file:" + extractFileName + " exception:" + ex, 3);
        }
        if (rangeList.size() == 0) return null;
        RangeNormalize[] dummy = new RangeNormalize[0];
        return (RangeNormalize[])rangeList.toArray(dummy);
 
    }

    /**
     * Given a list of non-date element values return the normalized ranges
     * @param extractFileName name of file to have extraction
     * @param values array of date values to be tested
     * @return array of normalized ranges
     */                    
    public RangeNormalize[] processText(String extractFileName, String name, String [] values)
    {       
        Logger logger = m_fw.getLogger();
        
        RangeNormalize rangeNormalize = null;
        Vector rangeList = new Vector(10);
        try {
            if ((values == null) || (values.length == 0)) {
                m_fw.getLogger( ).logMessage(
                    "FileExtract_DATE: Nothing found for " + name + ":" + extractFileName, 3);
                return null;
            }
                    
            String value = null;

                    //System.out.println("**** DATE LIST: size=" + dates.length);
            for (int i=0; i < values.length; i++) {
                value = values[i];
                rangeNormalize = processText(value);
                
                //System.out.println("***rangeNormalize NULL***");
                if (rangeNormalize != null) {
                    rangeNormalize.setFileName(extractFileName);
                    rangeNormalize.setElementName(name);
                    if (rangeNormalize.isDateSet()) {
                        rangeNormalize.dump(name, logger, 2);
                    } else {
                        rangeNormalize.dump(name, logger, 3);
                    }
                    rangeList.add(rangeNormalize);
                }
            }
            logger.logMessage(
                        "FileExtract_DATE Thread(" 
                        + Thread.currentThread().getName() 
                        + " name:"  + extractFileName , 4);
                    
        } catch (Exception ex) {
                    logger.logError(
                        "Exception FileExtract_DATE Thread(" 
                        + Thread.currentThread().getName() 
                        + ") file:" + extractFileName + " exception:" + ex, 3);
        }
        if (rangeList.size() == 0) return null;
        RangeNormalize[] dummy = new RangeNormalize[0];
        return (RangeNormalize[])rangeList.toArray(dummy);
 
    }  
    
    /**
     * Return a normalized range using date element value
     * @param line date element value
     * @return normalized range object
     */
    public RangeNormalize processDate(String line)
    {
        if (StringUtil.isEmpty(line)) return null;

        line = " " + line + " ";
        String token = tokenizeDate(line);
        logger.logMessage("out:" + token, 7);
        Vector dateList = normalizeDate(token);
        if (dateList == null) {
            logger.logMessage("RangeNormalize- null process1: " + line, 2);
            return null;
        }
        if (dateList.size() == 0) {
            if (!getSecondaryDate("(\\d\\d\\d\\d)", line, dateList)) {
                testUnknown(line, dateList);
            }
        }
        dateList = normalizeList(dateList);
        dumpDate(dateList);
        RangeNormalize rangeNormalize = buildMetadata(dateList);
        if (rangeNormalize == null) {
            logger.logMessage("RangeNormalize- null process2: " + line, 2);
            return null;
        }
        rangeNormalize.setDateOriginal(line);
        
        return rangeNormalize;
    }
   
    /**
     * Return a normalized range using non-date element value
     * @param line value from non-date element
     * @return normalized range object
     */    
    public RangeNormalize processText(String line)
    {
        if (StringUtil.isEmpty(line)) return null;

        line = " " + line + " ";
        String token = tokenizeText(line);
        logger.logMessage("out:" + token, 7);
        Vector dateList = normalizeText(token);
        if (dateList == null) {
            logger.logMessage("RangeNormalize text - null process1: " + line, 2);
            return null;
        }
        if (dateList.size() == 0) {
            getSecondaryDate("\\s(\\d\\d\\d\\d)\\s", line, dateList);
        }
        dateList = normalizeList(dateList);
        dumpDate(dateList);
        RangeNormalize rangeNormalize = buildMetadata(dateList);
        if (rangeNormalize == null) {
            logger.logMessage("RangeNormalize text - null process2: " + line, 2);
            return null;
        }
        rangeNormalize.setDateOriginal(line);
        
        return rangeNormalize;
    }

    /**
     * build range object from array of normalized date objects
     * @param dates array of normalized date objects
     */
    public String[] buildDates(Vector dates, EraNormalize eras)
    {
        if ((dates == null) || (dates.size() == 0))
            return buildUnknown(DateNormalize.NONE);
        Vector retDates = new Vector(dates.size()*8);
        RangeNormalize range = null;
        for (int i=0; i < dates.size(); i++) {
            range = (RangeNormalize)dates.get(i);
            range.addDateList(retDates);
            if (eras != null) {             
                eras.addErasBrowse(range, "date.era", retDates);
            } else {
                logger.logMessage("DateNormalize - era processing not performed", 10);
            }
        }
        String [] dummy = new String[0];
        return (String[])retDates.toArray(dummy);
        
    }

    /**
     * build range object from array of normalized date objects
     * @param dates array of normalized date objects
     */
    public String[] buildEras(Vector dates, EraNormalize eras)
    {
        Vector retDates = new Vector(dates.size()*8);
        RangeNormalize range = null;
        for (int i=0; i < dates.size(); i++) {
            range = (RangeNormalize)dates.get(i);
            if (eras != null) {             
                eras.addErasBrowse(range, "date.era", retDates);
            }
        }
        String [] dummy = new String[0];
        return (String[])retDates.toArray(dummy);       
    }

    public static String[] buildUnknown(String unknown)
    {
        if (unknown == null) return null;
        
        Vector retDates = new Vector(3);
        RangeNormalize.addUnknownDate(retDates, unknown);
        
        String [] dummy = new String[0];
        return (String[])retDates.toArray(dummy);
        
    }

    public static Vector setUnknown(String unknown)
    {
        RangeNormalize unknownRange = new RangeNormalize();
        unknownRange.setRangeUnknown(unknown);
        Vector unknownList = new Vector(1);
        unknownList.add(unknownRange);
        return unknownList;
        
    }
                
    public static String testUnknown(RangeNormalize [] dateRanges)
    {
        RangeNormalize range = null;
        String type = null;
        
        if ((dateRanges == null) || (dateRanges.length != 1)) return null;
        range = dateRanges[0];
        if (range.getUnknownDate()) {
            type = range.getUnknownDateType();
            if ((type != null) && !type.equals(DateNormalize.NONE)) {
                return range.getUnknownDateType();
            }
        }
        return null;
    }
    
    /**
     * find the spec defined valid dates (from date element)
     * @param dateRanges array of date ranges to be tested
     * @return a Vector of filtered ranges
     */
    public static Vector testDateValues(RangeNormalize [] dateRanges)
    {
        Vector setRange = new Vector(10);
        RangeNormalize range = null;
        Vector retValues = new Vector();
        
        if ((dateRanges == null) || (dateRanges.length == 0)) return null;
        for (int i=0; i < dateRanges.length; i++) {
            range = dateRanges[i];
            if (range.isDateSet()) {
                setRange.add(range);
            }
        }
        if (setRange.size() == 0) return null;
        
        // replace with date ranges having a "valid" date
        dateRanges = (RangeNormalize[])setRange.toArray(new RangeNormalize[0]);
        
        // if only one date then use
        if (dateRanges.length == 1) {
            range = (RangeNormalize)dateRanges[0];
            retValues.add(range);
            
        } else {
            for (int i=0; i < dateRanges.length; i++) {
                range = dateRanges[i];
                // if range has more than one year then accept
                if (range.getYearCnt() > 1) {
                    retValues.add(range);
                // if maximum year of range is less than 1995 then accept
                } else if (range.getMaxValue() <= 1995) {
                    retValues.add(range);
                }
            }
            // if anything valid found then leave
            if (retValues.size() > 0) {
                return retValues;
            }
            
            // all dates > 1995 - eliminate the highest
            if (dateRanges.length > 1) {
                int maxyear = -1;
                int maxinx = -1;
                
                for (int i=0; i < dateRanges.length; i++) {
                    range = (RangeNormalize)dateRanges[i];
                    if (range.getMaxValue() > maxyear) {
                        maxyear = range.getMaxValue();
                        maxinx = i;
                    }
                }
                for (int i=0; i < dateRanges.length; i++) {
                    range = (RangeNormalize)dateRanges[i];
                    if (i != maxinx) retValues.add(range);

                }
            }
        }
        if (retValues.size() == 0) return null;
        return retValues;
    }

    /**
     * test unknown patterns and set normalized date object if matched
     * @param line date value to be tested
     * @param dateList add unknown to the date array if found
     * @return true=found, false=not found
     */
    private boolean testUnknown(String line, Vector dateList)
    {
        String unknownType = null;
        DateNormalize normDate = new DateNormalize();
        String lower = line.toLowerCase();
        
        unknownType = unknownPatterns(line);
        normDate.setUnknownDate(true);
        normDate.setUnknownDateType(unknownType);
        dateList.add(normDate);
        return true;        
    }
    
    /**
     * match patterns of unknown date against contents of line
     * @param line line to be stated
     * @return standard unknown value
     */
    private String unknownPatterns(String line)
    {

        String lower = line.toLowerCase();
        int pos = lower.indexOf("unknown");
        if (pos >= 0) {            
            logger.logMessage("Unknown found",10);
            return DateNormalize.UNAV;
        }
        
        pos = lower.indexOf("unavailable");
        if (pos >= 0) {            
            logger.logMessage("unavailable found",10);
            return DateNormalize.UNAV;
        }

        String [] result = RegexUtil.listPattern(line,"\\W?[Nn]\\.?\\s*?[Dd]\\.?\\W?");
        if (result != null) {
            logger.logMessage("N.D. found",10);
            return DateNormalize.UNAV;
        }

        result = RegexUtil.listPattern(line,"Not\\s+Determined");
        if (result != null) {
            logger.logMessage("Not Determined found",10);
            return DateNormalize.UNAV;
        }
        
        result = RegexUtil.listPattern(line,"[Dd]ate\\s+[Nn]ot\\s+[Ii]ndicated");
        if (result != null) {
            logger.logMessage("Date Not Indicated found",10);
            return DateNormalize.UNAV;
        }
        
        result = RegexUtil.listPattern(line,"[Nn]o\\s+[Dd]ate");
        if (result != null) {
            logger.logMessage("No Date found",10);
            return DateNormalize.UNAV;
        }
        
        result = RegexUtil.listPattern(line,"[Ss].?\\s*?[Dd].?");
        if (result != null) {
            logger.logMessage("s.d. found",10);
            return DateNormalize.UNAV;
        }
                                   
        logger.logMessage("Null unknown",10);
        return DateNormalize.NONE;      
    }
   
    /**
     * add operators to list to build form value, value-value, etc.
     */
    private Vector normalizeList(Vector dateList) 
    {       
        Vector newList = new Vector(100);
        DateOperator operator = null;
        DateNormalize normalize = null;
        
        char prevType = ' ';
        char currType = ' ';
        for (int i=0; i < dateList.size(); i++) {
            prevType = currType;
            Object object = dateList.get(i);
            if (object instanceof DateOperator) {
                if (i == 0) continue; // no operator 1st position
                operator = (DateOperator) object;
                currType = 'o';
                
            } else if (object instanceof DateNormalize) {
                normalize = (DateNormalize) object;
                currType = 'd';
            }
            if ((prevType == 'd') && (currType == 'd')) {
                DateOperator tempOperator = new DateOperator(",");
                newList.add(tempOperator);
            }
            newList.add(object);
        }
        return newList;
    }
    
    /**
     * From a list of normalized dates build a normalized ranged
     * @param dateList array of normalized dates
     * @return normalized range
     */
    private RangeNormalize buildMetadata(Vector dateList) 
    {
        if (dateList.size() == 0) return null;
        RangeNormalize rangeNormalize = new RangeNormalize();
        DateNormalize startNorm = (DateNormalize)dateList.elementAt(0);
        DateNormalize nextNorm = null;
        if (startNorm.getUnknownDate()) {
            rangeNormalize.setUnknownDate(true);
            rangeNormalize.setUnknownDateType(startNorm.getUnknownDateType());
            rangeNormalize.setRangeTemper(startNorm.getUnknownDateType());
            rangeNormalize.setRangeNormalize(startNorm.getUnknownDateType());
            return rangeNormalize;
        }
        rangeNormalize.setMetadata(dateList);
        return rangeNormalize;
    }
    
    /**
     * takes a second pass to identify dates in date field
     * @param pattern date pattern to test
     * @param line string to be matched
     * @param dateList list of normalized dates to save the matches in
     * @return true=pattern matched, false=pattern did not match
     */
    private boolean getSecondaryDate(String pattern, String line, Vector dateList)
    {    
        logger.logMessage("getSecondaryDate:" + line, 10);
        String [] dateArr = RegexUtil.listMatches(line, "\\s[12][4567890]\\d\\d");
        if (dateArr != null)
            logger.logMessage("RegexUtil.listMatches length:" + dateArr.length,10);
        String dateValue = null;
        String [] result = null;
        DateNormalize normDate = null;
        String year = null;
        boolean match = false;
        int numYear = -1;
        if (dateArr != null) {
            for (int i=0; i < dateArr.length; i++) {
                dateValue = dateArr[i];
                logger.logMessage("returned:" + dateValue,10);
                result = RegexUtil.listPattern(dateValue, pattern);
                if (result != null) {
                    year = result[1];
                    if (!isValidYear(year)) continue;
                    match = true;
                    if (dateList.size() > 0) {
                        DateOperator tempOperator = new DateOperator(",");
                        dateList.add(tempOperator);
                    }
                                       
                    normDate = new DateNormalize();
                    logger.logMessage("secondary date",10);
                    normDate.setDateOriginal(year);
                    normDate.setNormalizeYear(year);            
                    normDate.setDateNormalize(year);
                    normDate.setDateTemper(year);
                    normDate.setExactYear(true);
                    normDate.setSecondaryDate(true);
                    dateList.add(normDate);
                }
            }
        }
        return match;
    }

    /**
     * does this year value meet extract qualifications
     * @return true=yes, false=no
     */
    private boolean isValidYear(String year)
    {
        int numYear = -1;
        try {
            numYear = Integer.parseInt(year);
            if ((numYear < 1600) || (numYear > currentYear)) return false;
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
    
    private void dumpDate(Vector dateList)
    {
        if ((dateList == null) || (dateList.size() == 0)) {
            logger.logMessage("Empty dateList",10);
            return;
        }
        logger.logMessage("list size=" + dateList.size(),7);
        DateOperator operator = null;
        DateNormalize normalize = null;
        for (int i=0; i < dateList.size(); i++) {
            Object object = dateList.get(i);
            if (object instanceof DateOperator) {
                operator = (DateOperator) object;
                logger.logMessage("Operator stype=" 
                    + operator.getStringType() 
                    + " - type=" + operator.getType(),5);
                
            } else if (object instanceof DateNormalize) {
                normalize = (DateNormalize) object;
                if (normalize.getUnknownDate()) {
                    logger.logMessage("Normalize Unknown:" + normalize.getUnknownDateType(),5); 
                    
                } else {
                    logger.logMessage(normalize.dump("Normalize"),5);
                }
            }
        }
    }
    
    /**
     * tokenize the <date> field for extraction
     * Note:
     * hyphens are overloaded so 19-- is 19== 
     * Normal form of date: 1989%12%1 (yyyy%mm%dd
     * [] are removed - too difficult to handle all variant forms
     * ca -> CA
     * cnnnn -> C nnnn
     * @param line line to be tokenized
     * @return tokenized line
     */
    private String tokenizeDate(String line)
    {
        if (line == null) return null;
        
        line = line.replace('(', ' ');
        line = line.replace(')', ' ');
        line = line.replace('/', '%');
        line = RegexUtil.replaceString(line, "&apos;", "'");
               
        line = RegexUtil.substitute(
            line,
            "(&lt;.*?&gt;)", 
            new String [] {" "});
            
        line = line.replace(';',',');
        
        line = RegexUtil.replaceString(line, "[", "");
        line = RegexUtil.replaceString(line, "]", "");
        
        line = RegexUtil.replaceString(line, " or ", " , ");
        line = RegexUtil.replacePatternString(line, " \\d\\d\\d\\d-\\d\\d-\\d\\d ",  "-", "%");
        line = RegexUtil.replacePatternString(line, " \\d\\d\\d\\d-\\d\\d ", "-", "%");
        line = RegexUtil.replacePatternString(line, " \\d\\d\\d\\d[.]\\d\\d[.]\\d\\d ",  ".", "%");
        line = RegexUtil.replacePatternString(line, " \\d\\d\\d\\d[.]\\d\\d ", ".", "%");
        line = RegexUtil.replacePatternString(line, " \\d\\d[\\d\\-]{2}", "-", "=");
        line = RegexUtil.replacePatternString(line, " \\d\\d[\\d\\?]{2}", "?", "=");
        line = RegexUtil.replacePatternString(line, " \\d\\d[\\d\\-][\\d\\-][ ]+\\?", " ", "");
        
        
        line = DateUtil.setCenturies(line);
        line = line.replace('.', ' '); 
        
        line = RegexUtil.substitute( // cannnn -> CA nnnn
            line,
            " ([Cc][Aa])(\\d\\d\\d\\d) ", 
            new String [] {" " + CIRCA + " ", "$2"," "});

        line = RegexUtil.replaceString(line, " ca ", " " + CIRCA + " ");
        
        line = RegexUtil.substitute( // arrount. nnnn -> CA nnnn
            line,
            " around\\s+(\\d\\d\\d\\d) ", 
            new String [] {" " + CIRCA + " ", "$1"," "}, 
            Pattern.CASE_INSENSITIVE);
        
        line = RegexUtil.substitute( // approx. nnnn -> CA nnnn
            line,
            " approx[\\.]*\\s+(\\d\\d\\d\\d) ", 
            new String [] {" " + CIRCA + " ", "$1"," "}, 
            Pattern.CASE_INSENSITIVE);
        
        line = RegexUtil.substitute( // approx. nnnn -> CA nnnn
            line,
            " approximate(ly)*\\s+(\\d\\d\\d\\d) ", 
            new String [] {" " + CIRCA + " ", "$2"," "}, 
            Pattern.CASE_INSENSITIVE);
            
        //line = replaceAmericanDate(line);
        line = DateUtil.replaceAbbrevMonth(line);
                
        line = RegexUtil.substitute(
            line,
            "([a-zA-Z]+)\\s+'(\\d\\d)\\s", 
            new String [] {"$1", " 19","$2"});
        
        line = DateUtil.replaceDate(
            line,
            "([a-zA-Z]+) (\\d{1,2}),\\s+?(\\d\\d\\d\\d)", 
            3, 1, 2);
        line = DateUtil.replaceDate(
            line,
            "([a-zA-Z]+)\\s+?(\\d\\d\\d\\d)", 
            2, 1, -1);
        line = DateUtil.replaceDate(
            line,
            "(\\d\\d\\d\\d)\\s+?([a-zA-Z]+)\\s+?(\\d{1,2})", 
            1, 2, 3);

        line = RegexUtil.substitute(
            line,
            "(between[ ]+)(\\d\\d\\d\\d)([ ]+and[ ]+)(\\d\\d\\d\\d )", 
            new String [] {"$2", "-", "$4"});
        
        line = RegexUtil.substitute(
            line,
            "(before[ ]+)(\\d\\d\\d\\d )", 
            new String [] {"< ", "$2"});
        
        line = RegexUtil.substitute(
            line,
            "(\\d\\d)(\\d\\d)([ ]*+-[ ]*)'(\\d\\d )", 
            new String [] {"$1", "$2", "$3", "$1", "$4"});
            
        line = RegexUtil.substitute(   // n<n>/n<n>/nnnn
            line,
            "\\s(\\d{1,2})%(\\d{1,2})%(\\d\\d\\d\\d)\\s", 
            new String [] {"$3", "%", "$1", "%", "$2"});       
            
        line = RegexUtil.substitute(   // n<n>/nnnn
            line,
            "\\s(\\d{1,2})%(\\d\\d\\d\\d)\\s", 
            new String [] {" ", "$2", "%", "$1", " "});       
  
        line = RegexUtil.substitute(
            line,
            "\\s(9\\d)(\\d\\d)(\\d\\d)\\s", 
            new String [] {" ","19","$1", "%", "$2", "%", "$3"," "});
            
        line = RegexUtil.substitute(
            line,
            "\\s(0\\d)(\\d\\d)(\\d\\d)\\s", 
            new String [] {" ","20","$1", "%", "$2", "%", "$3"," "});

        line = RegexUtil.substitute(
            line,
            " [cC](\\d\\d\\d\\d)", 
            new String [] {" C ", "$1"});

        line = RegexUtil.substitute(
            line,
            "(\\d\\d\\d0)'?s\\s?-\\s?(\\d\\d\\d)0'?s", 
            new String [] {" ","$1", " - ", "$2", "9"});
        
        line = RegexUtil.substitute(
            line,
            "(\\d\\d\\d)0'?s", 
            new String [] {" ","$1", "=", " "});

        //line = RegexUtil.replacePatternString(line, "c\\d\\d\\d\\d",  "c", " C ");
        line = RegexUtil.replaceString(line, "--", " ");
        line = line.replace(',', ' ');
        line = RegexUtil.replaceString(line, "-", " - ");
        //line = line.replace('.', ' ');
        return line;
    }
    
    /**
     * tokenize the non-date fields for date extraction
     * Note:
     * hyphens are overloaded so 19-- is 19== 
     * Normal form of date: 1989%12%1 (yyyy%mm%dd
     * [] are removed - too difficult to handle all variant forms
     * ca -> CA
     * cnnnn -> C nnnn
     * @param line line to be tokenized
     * @return tokenized line
     */
    private String tokenizeText(String line)
    {
        if (line == null) return null;
        
        line = line.replace('(', ' ');
        line = line.replace(')', ' ');
        line = line.replace('/', '%');
        line = RegexUtil.replaceString(line, "&apos;", "'");
               
        line = RegexUtil.substitute(
            line,
            "(&lt;.*?&gt;)", 
            new String [] {" "});
            
        line = line.replace(';',',');
        
        line = RegexUtil.replaceString(line, "[", "");
        line = RegexUtil.replaceString(line, "]", "");
        
        line = RegexUtil.replaceString(line, " or ", " , ");
        line = RegexUtil.replacePatternString(line, " \\d\\d\\d\\d-\\d\\d-\\d\\d ",  "-", "%");
        line = RegexUtil.replacePatternString(line, " \\d\\d\\d\\d-\\d\\d ", "-", "%");
        line = RegexUtil.replacePatternString(line, " \\d\\d\\d\\d[.]\\d\\d[.]\\d\\d ",  ".", "%");
        line = RegexUtil.replacePatternString(line, " \\d\\d\\d\\d[.]\\d\\d ", ".", "%");
        line = RegexUtil.replacePatternString(line, " \\d\\d[\\d\\-]{2}", "-", "=");
        line = RegexUtil.replacePatternString(line, " \\d\\d[\\d\\?]{2}", "?", "=");
        line = RegexUtil.replacePatternString(line, " \\d\\d[\\d\\-][\\d\\-][ ]+\\?", " ", "");
        
        
        line = DateUtil.setCenturies(line);
        line = line.replace('.', ' '); 

        //line = replaceAmericanDate(line);
        line = DateUtil.replaceAbbrevMonth(line);
                
        line = RegexUtil.substitute(
            line,
            "([a-zA-Z]+)\\s+'(\\d\\d)\\s", 
            new String [] {"$1", " 19","$2"});
        
        line = DateUtil.replaceDate(
            line,
            "([a-zA-Z]+) (\\d{1,2}),\\s+?(\\d\\d\\d\\d)", 
            3, 1, 2);
        line = DateUtil.replaceDate(
            line,
            "([a-zA-Z]+)\\s+?(\\d\\d\\d\\d)", 
            2, 1, -1);
        line = DateUtil.replaceDate(
            line,
            "(\\d\\d\\d\\d)\\s+?([a-zA-Z]+)\\s+?(\\d{1,2})", 
            1, 2, 3);

        line = RegexUtil.substitute(
            line,
            "(between[ ]+)(\\d\\d\\d\\d)([ ]+and[ ]+)(\\d\\d\\d\\d )", 
            new String [] {"$2", "-", "$4"});
        
        line = RegexUtil.substitute(
            line,
            "(before[ ]+)(\\d\\d\\d\\d )", 
            new String [] {"< ", "$2"});
        
        line = RegexUtil.substitute(
            line,
            "(\\d\\d)(\\d\\d)([ ]*+-[ ]*)'(\\d\\d )", 
            new String [] {"$1", "$2", "$3", "$1", "$4"});
            
        line = RegexUtil.substitute(   // n<n>/n<n>/nnnn
            line,
            "\\s(\\d{1,2})%(\\d{1,2})%(\\d\\d\\d\\d)\\s", 
            new String [] {"$3", "%", "$1", "%", "$2"});       
            
        line = RegexUtil.substitute(   // n<n>/nnnn
            line,
            "\\s(\\d{1,2})%(\\d\\d\\d\\d)\\s", 
            new String [] {" ", "$2", "%", "$1", " "});       
  
        line = RegexUtil.substitute(
            line,
            "\\s(9\\d)(\\d\\d)(\\d\\d)\\s", 
            new String [] {" ","19","$1", "%", "$2", "%", "$3"," "});
            
        line = RegexUtil.substitute(
            line,
            "\\s(0\\d)(\\d\\d)(\\d\\d)\\s", 
            new String [] {" ","20","$1", "%", "$2", "%", "$3"," "});

        line = RegexUtil.substitute(
            line,
            " [cC](\\d\\d\\d\\d)", 
            new String [] {" C ", "$1"});

        line = RegexUtil.substitute(
            line,
            "(\\d\\d\\d0)'?s\\s?-\\s?(\\d\\d\\d)0'?s", 
            new String [] {" ","$1", " - ", "$2", "9"});
        
        line = RegexUtil.substitute(
            line,
            "(\\d\\d\\d)0'?s", 
            new String [] {" ","$1", "=", " "});
        
        line = RegexUtil.substitute(
            line,
            " ([Cc][Ii][Rr][Cc][Aa]) ", 
            new String [] {" " + CIRCA + " "});
        
        line = RegexUtil.substitute(
            line,
            "[Cc][Aa]\\.?\\s+?(\\d\\d\\d\\d)", 
            new String [] {" " + CIRCA + " ", "$1"});

        //line = RegexUtil.replacePatternString(line, "c\\d\\d\\d\\d",  "c", " C ");
        line = RegexUtil.replaceString(line, "--", " ");
        line = line.replace(',', ' ');
        line = RegexUtil.replaceString(line, "-", " - ");
        //line = line.replace('.', ' ');
        return line;
    }
     
    /**
     * Build array (vector) containing normalized dates and date operators
     * @param list date element value
     * @return array of normalized dates and date operators
     */        
    private Vector normalizeDate(String line)
    {
        Vector dateList = new Vector();
        String [] tokens = line.split("\\s+");
        if (tokens.length == 0) {
            return null;
        }
        DateNormalize normDate = new DateNormalize();
        String token = null;
        for (int i=0; i<tokens.length; i++) {
            token = tokens[i];
            if (token.length() == 0) continue;
            logger.logMessage("Token(" + i + "): [" + token + "]", 7);
            if (token.equals("-") || token.equals(",")) {
                DateOperator dateOperator = new DateOperator(token);
                dateList.addElement(dateOperator);
                normDate = new DateNormalize();
                logger.logMessage("Add operator", 10);
                
            } else if (token.equals(CIRCA)) {
                normDate.setCirca(true);
                logger.logMessage("Set Circa", 10 );
                
            } else if (token.equals("C")) {
                normDate.setCopyright(true);
                logger.logMessage("Set Copyright",10);
                
            } else if (token.equals("<")) {
                normDate.setBeforeDate(true);
                logger.logMessage("Set Before",10);
                                
            } else if ((token.length() > 2) 
                && (token.substring(0,2).matches("\\d\\d"))) {
                logger.logMessage("Before extractDate: ca=" + normDate.getCirca(),10);
                if (extractDate(token, normDate, dateList) ){
                    logger.logMessage("After extractDate: ca=" + normDate.getCirca(),10);
                    normDate = new DateNormalize();
                }
                
            }
        }
        if (normDate.getNormalizeYear() != null) {
            dateList.addElement(normDate);
        }
        return dateList;
    }
        
    private Vector normalizeText(String line)
    {
        Vector dateList = new Vector();
        String [] tokens = line.split("\\s+");
        if (tokens.length == 0) {
            return null;
        }
        DateNormalize normDate = new DateNormalize();
        String token = null;
        for (int i=0; i<tokens.length; i++) {
            token = tokens[i];
            if (token.length() == 0) continue;
            logger.logMessage("Token(" + i + "): [" + token + "]", 7);
            if (token.equals("-") || token.equals(",")) {
                DateOperator dateOperator = new DateOperator(token);
                dateList.addElement(dateOperator);
                normDate = new DateNormalize();
                logger.logMessage("Add operator", 10);
                
            } else if (token.equals(CIRCA)) {
                normDate.setCirca(true);
                logger.logMessage("Set Circa", 10 );
                                
            } else if ((token.length() > 2) 
                && (token.substring(0,2).matches("\\d\\d"))) {
                logger.logMessage("Before extractDate",10);
                if (extractDate(token, normDate, dateList) ){
                    normDate = new DateNormalize();
                }
            }
        }
        if (normDate.getNormalizeYear() != null) {
            dateList.addElement(normDate);
        }
        return dateList;
    }
     
    /**
     * determine if token is a date and extract values to DateNormalize object if it is
     * @param token token to be tested
     * @param normDate normalized date object >>> value returned
     * @param dateList Vector of normalized dates
     * @return true if date is valid
     */
    private boolean extractDate(
            String token, 
            DateNormalize normDate, 
            Vector dateList)
    {
        String [] result = null;
        result = RegexUtil.listPattern(token,"^(\\d\\d\\d\\d)%(\\d{1,2})%(\\d{1,2})$");
        if (result != null) {
            logger.logMessage("norm dddd%dd%dd", 10);
            normDate.setDateOriginal(token);
            normDate.setNormalizeYear(result[1]);
            normDate.setNormalizeMonth(result[2]);
            normDate.setNormalizeDay(result[3]);
            normDate.setDateTemper(result[1]);
            normDate.setDateNormalize(result[1]);
            normDate.setExactYear(true);
            normDate.setCirca(false); // this is done because an exact date is used so circa is wrong
            dateList.add(normDate);
            return true;
        }
        
        result = RegexUtil.listPattern(token,"^(\\d\\d\\d\\d)%(\\d{1,2})$");
        if (result != null) {
            logger.logMessage("norm dddd%dd", 10);
            normDate.setDateOriginal(token);
            normDate.setNormalizeYear(result[1]);
            normDate.setNormalizeMonth(result[2]);
            normDate.setDateTemper(result[1]);
            normDate.setDateNormalize(result[1]);
            normDate.setExactYear(true);
            normDate.setCirca(false);
            dateList.add(normDate);
            return true;
        }
                        
        result = RegexUtil.listPattern(token,"^(\\d\\d[\\d\\=]{2})\\?$");
        if (result != null) {
            logger.logMessage("norm ?", 10);
            normDate.setDateOriginal(token);
            StringBuffer buf = new StringBuffer(10);
            normDate.setProbableDate(true);
            String year = RegexUtil.replaceString(result[1],"=", "-");
            normDate.setNormalizeYear(year);
            if (year.indexOf('-') > 0) {
                DateNormalize startDate = new DateNormalize(normDate);                
                String startYear = year.replace('-', '0');
                startDate.setDateNormalize(startYear);
                startDate.setDateTemper(startYear + '~');
                dateList.add(startDate);
                
                DateOperator tempOperator = new DateOperator("-");
                dateList.add(tempOperator);
                
                DateNormalize endDate = new DateNormalize(normDate);
                String endYear = year.replace('-', '9');
                endDate.setDateNormalize(endYear);
                endDate.setDateTemper(endYear + '~');
                dateList.add(endDate);
                
            } else {
                normDate.setDateNormalize(year);
                normDate.setDateTemper(year + '~');
                normDate.setExactYear(true);
                dateList.add(normDate);
            }

            return true;
        }
                                                                      
        result = RegexUtil.listPattern(token,"^(\\d\\d[\\d\\=]{2})$");
        if (result != null) {
            logger.logMessage("norm standard", 10);
            normDate.setDateOriginal(token);
            StringBuffer buf = new StringBuffer(10);
            String year = RegexUtil.replaceString(result[1],"=", "-");
            normDate.setNormalizeYear(year);
            if (year.indexOf('-') > 0) {
                try {
                DateNormalize startDate = new DateNormalize(normDate);                
                String startYear = year.replace('-', '0');
                startDate.setDateNormalize(startYear);
                startDate.setDateTemper(startYear);
                dateList.add(startDate);
                
                DateOperator tempOperator = new DateOperator("-");
                dateList.add(tempOperator);
                
                DateNormalize endDate = new DateNormalize(normDate);
                String endYear = year.replace('-', '9');
                endDate.setDateNormalize(endYear);
                endDate.setDateTemper(endYear);
                dateList.add(endDate);

                } catch (Exception ex) {
                    logger.logMessage(StringUtil.stackTrace(ex), 10);
                }
            } else {
                if (isValidYear(year)) {
                    normDate.setDateNormalize(year);
                    normDate.setDateTemper(year);
                    normDate.setExactYear(true);
                    dateList.add(normDate);
                }
            }

            return true;
        }
        return false;
    }

    
    private class Counts
    {
        public int noElement = 0;
        public int processCnt = 0;
        public int requestCnt = 0;
        public int recordSetCnt = 0;
        public int dateSetCnt = 0;
        public int nullCnt = 0;
        public int secondaryDateCnt = 0;
        public int unknCnt = 0;
        public int unavCnt = 0;
        public int unknownCnt = 0;
        public int [] elementCnt = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    }
}
