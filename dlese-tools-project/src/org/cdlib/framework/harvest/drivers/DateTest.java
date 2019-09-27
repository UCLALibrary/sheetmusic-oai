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
package org.cdlib.framework.harvest.drivers;

import java.io.BufferedReader;
import java.io.FileReader;

import java.util.Vector;

import org.cdlib.framework.dataModel.RangeNormalize;
import org.cdlib.framework.qa.QABase;
import org.cdlib.framework.utility.DateNormalizer;
import org.cdlib.framework.utility.Framework;
import org.cdlib.framework.utility.FrameworkException;
import org.cdlib.framework.utility.StringUtil;

/**
 * Driver for Date test
 * @author  David Loy
 */

public class DateTest extends QABase
{
    private static Framework fw = null;
    
    /**
     * Command line executer
     *
     * @param args Each argument is the name of a properties file
     * to be used to initialize the framework's global properties.
     * Path names are relative to the classpath.
     */
    public static void main(String[ ] args)
        throws FrameworkException
    {
        Vector propertyFiles = new Vector(10);
        propertyFiles.addElement(getQAPropertyFileName( ));
        /**
        propertyFiles.addElement(getFrameworkServicePropertyFileName( ));
        String [] ingestServer = getPropertyFileNames("ingest", "server");
        for (int i=0; i < ingestServer.length; i++) {
            propertyFiles.addElement(ingestServer[i]);
        }
         */
        propertyFiles.addElement(getFrameworkLocalPropertyFileName( ));
        String [] dummyS = new String[0];
        String [] propertyFileNames = (String[])propertyFiles.toArray(dummyS);
        
       
        //String userHome = fw.getProperty("TestSecurity.userHome");
        DateTest test = new DateTest( );
        fw = new Framework(new String[]{"resources/FrameworkService.properties","resources/FrameworkLocal.properties"});
        test.run();
    }

    public void run()
    {
        String fileName = null;
        String fileNames = null;
        DateNormalizer dateNormalizer = new DateNormalizer();
        dateNormalizer.initialize(fw);
        
        
        try {
            //System.out.println("Start fileLogger.path=" + fw.getProperty("fileLogger.path"));
            fw.getLogger( ).logMessage(
                "DateTest: Starting ----------------------------------------",
                3);
                       
            
            String type = fw.getProperty("DateTest.type");
            if (type == null) type = "date";
            String start = fw.getProperty("DateTest.file." + type);
            BufferedReader in = new BufferedReader(new FileReader(start));
            String line;
            String dateS;
            String dateRange = null;
            RangeNormalize rangeNormalize = null;
            while ((line = in.readLine()) != null) {
                if (line.length() == 0) continue;
                System.out.println("in:" + line);
                fw.getLogger().logMessage("**************************************", 3);
                fw.getLogger().logMessage("IN:" + line, 3);
                if (line.charAt(0) == '#') continue;
                if (line.length() == 0) continue;
                if (type.equalsIgnoreCase("date")) {
                    rangeNormalize = dateNormalizer.processDate(line);
                } else {
                    rangeNormalize = dateNormalizer.processText(line);
                }
                if (rangeNormalize != null) {
                    rangeNormalize.dump("test", fw.getLogger(), 3);
                    RangeNormalize range2 = new RangeNormalize();
                    String dateNormalize = rangeNormalize.getRangeNormalize();
                    if ((dateNormalize != null) && (dateNormalize.length() > 0)) {
                        System.out.println("range2:" + dateNormalize);
                        String [] dateArray = new String[1];
                        dateArray[0] = dateNormalize;
                        range2.setMetadata(dateArray);
                        range2.dump("range2", fw.getLogger(), 3);
                    }
                    
                }
                //fw.getLogger().logMessage("------------",3);
                
            }
            in.close();
            fw.getLogger().flush();
            
            
        }  catch(Exception ex) {
            fw.getLogger( ).logError("Exception in run:" + ex, 3);
            fw.getLogger().logMessage(StringUtil.stackTrace(ex), 10);
        }
    }

}
