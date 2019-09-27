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
package org.cdlib.framework.harvest.dataManagement;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.Integer;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;
import org.cdlib.framework.client.IngestClient;
import org.cdlib.framework.dataModel.ResultPackage;
import org.cdlib.framework.dataModel.WorkQueue;
import org.cdlib.framework.ingest.IngestTool;
import org.cdlib.framework.security.SecurityUtil;
import org.cdlib.framework.utility.Framework;
import org.cdlib.framework.utility.FrameworkException;
import org.cdlib.framework.utility.Logger;
import org.cdlib.framework.utility.DOMParser;

import org.cdlib.framework.utility.StringUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.cdlib.framework.utility.DateNormalizer;
import org.cdlib.framework.dataModel.DateNormalize;
import org.cdlib.framework.dataModel.RangeNormalize;

/**
 * Run date routines from file
 *
 * @author  David Loy
 */
public class FileExtract_DATE extends FileExtract
{
    private static final int maxsize = 1000000;
    
    protected IngestTool m_tool = null;
    protected Properties m_toolProp = null;
    protected DateNormalizer m_dateNormalizer = null;
    protected String m_headerElement = null;
    public FileExtract_DATE() {}
    public FileExtract_DATE(FileExtract_DATE um)
    {
        m_tool = um.m_tool;
        m_toolProp = um.m_toolProp;
	m_fw = um.m_fw;
        m_queue = um.m_queue;
        m_resultPackages = um.m_resultPackages;
        m_dateNormalizer = um.m_dateNormalizer;
        m_headerElement = um.m_headerElement;
        //m_dateNormalizer = um.m_dateNormalizer;       
    }
        
 
    public void run(Properties prop)
        throws FrameworkException
    {
        Properties eraProp = new Properties();
        eraProp.setProperty("era1", "1500-1800");
        eraProp.setProperty("era2", "1801-1890");
        eraProp.setProperty("era3", "1891-1940");
        eraProp.setProperty("era4", "1941-3000");
        m_dateNormalizer = new DateNormalizer();
        m_dateNormalizer.initialize(
            m_fw, eraProp);
        m_fw.getLogger( ).logMessage(
                "FileExtract_DATE: Starting ----------------------------------------",
                3);

        String startFileName = prop.getProperty("extractDirectory");
        if (StringUtil.isEmpty(startFileName)) {
            throw new FrameworkException(FrameworkException.INVALID_PARM, "FileExtract_DATE: missing required parm: extractDirectory");
        }
        String threadCntS = prop.getProperty("threadCnt");
        if (StringUtil.isEmpty(threadCntS)) {
            throw new FrameworkException(FrameworkException.INVALID_PARM, "FileExtract_DATE: missing required parm: threadCnt");
        }
        setThreadCnt(Integer.parseInt(threadCntS));
        
        m_headerElement = prop.getProperty("headerElement");
               
        String fileExtension = prop.getProperty("fileExtension");
        if (StringUtil.isEmpty(fileExtension)) {
            throw new FrameworkException(FrameworkException.INVALID_PARM, "FileExtract_DATE: missing required parm: fileExtension");
        }

        m_fw.getLogger( ).logMessage("FileExtract_DATE PARAMETERS", 2);
        m_fw.getLogger( ).logMessage("extractDirectory:" + startFileName, 2);
        m_fw.getLogger( ).logMessage("threadCnt:" + threadCntS, 2);
        m_fw.getLogger( ).logMessage("headerElement:" + m_headerElement, 2);
        m_fw.getLogger( ).logMessage("fileExtension:" + fileExtension, 2);

        String fileName = null;
        String fileNames = null;
        
        try {
            //System.out.println("Start fileLogger.path=" + m_fw.getProperty("fileLogger.path"));
            m_fw.getLogger( ).logMessage(
                "FileExtract_DATE: Run  ----------------------------------------",
                3);
            m_fw.getLogger( ).logMessage(
                "FileExtract_DATE: threadCnt=" + m_threadCnt + " extractDirectory=" + startFileName,
                3);
            
            File startFile = new File(startFileName);
            Vector fileList = new Vector(1000);
            m_fw.getLogger( ).logMessage(
                "FileExtract_DATE: start file=" + startFile, 3);
            extractDirectory(fileExtension, fileList, startFile);
            
            m_fw.getLogger( ).logMessage(
                "FileExtract_DATE: filelist.size=" + fileList.size(), 3);
            m_fw.getLogger().flush();
    //if (true) return; //!!!*******************
            if (fileList.size() == 0) {
                m_fw.getLogger( ).logError(
                    "FileExtract_DATE: No fileList found", 3);
                return;
            }
            
            
            String record = null;
            int outcnt = 0;
            
            
            int runsize = (maxsize < fileList.size()) ? maxsize : fileList.size();
            for (int i=0; i < runsize; i++) {
                String file = (String)fileList.elementAt(i);
                //System.out.println("start processing:" + file);
                addRecord(file);                
                outcnt++;
            }
            addStopThreadMarkers();
            m_fw.getLogger().logMessage("-------Start threading=" + getThreadCnt(),  3);
            Thread threads[] = startThreads();                
            waitThreadingDone(threads);
            
            m_fw.getLogger( ).logMessage(
                "---------------------------------------------------------", 1);

            m_fw.getLogger( ).logMessage(
                "FileExtract_DATE RUN STATISTICS", 1);
            m_fw.getLogger().logMessage("Records to process:" + outcnt, 1);
            m_fw.getLogger( ).logMessage("Thread process count: " 
                + m_dateNormalizer.getRequestCnt(), 1);
            m_fw.getLogger( ).logMessage("Ouptut records with date set: " 
                + m_dateNormalizer.getRecordsWithDateSet(), 1);
            
            m_fw.getLogger( ).logMessage("No DC: " 
                + m_dateNormalizer.getNoDcCnt(), 1);
            m_fw.getLogger( ).logMessage(
                "---------------------------------------------------------", 1);
            Enumeration e = m_dateNormalizer.getStats().keys();
            String key = null;
        
            while( e.hasMoreElements() )
            {
                key = (String)e.nextElement();
                m_dateNormalizer.logStats(key);
            }
            
            m_fw.getLogger( ).logMessage(
                "FileExtract_DATE: End STATISTICS ------------------------------------------",
                3);
            m_fw.getLogger().flush();
/*
        } catch(FrameworkException fe) {
            fe.logException(fw, 10);
*/            
        }  catch(Exception ex) {
            m_fw.getLogger( ).logError("trace" + StringUtil.stackTrace(ex), 3);
            m_fw.getLogger( ).logError("Exception in run" + ex, 3);
        }
    }
    
    private void addRecord(String extractFileName)
    {
        String retMets = null;
        try {
/*
            m_fw.getLogger( ).logMessage(
                "FileExtract_DATE: getRecord:" + extractFileName, 3);
*/
            m_queue.addWork(new FileExtractRequest(extractFileName));
                        
        } catch(Exception ex) {
            m_fw.getLogger( ).logError("Exception in run", 3);
            return;
        }
    }
    
    public void run()
    {
        String retMets = null;
        FileExtractRequest request = null;
        ResultPackage resultPackage = null;
        String extractFileName = null;
        String status = null;
        Logger logger = m_fw.getLogger();
        logger.logMessage(
                    "FileExtract_DATE Start Thread(" + Thread.currentThread().getName(), 3);
        try {
            while (true) {
                request = (FileExtractRequest)m_queue.getWork();
                
                // Terminate if the end-of-stream marker was retrieved
                if (request.stop) {
                    break;
                }
 
                extractFileName = request.fileName;
 
                m_fw.getLogger( ).logMessage(
                    "FileExtract_DATE Thread(" + Thread.currentThread().getName() + " name:" + extractFileName, 4);
                
                try {
                    File extractFile = new File(extractFileName);
                    Element qdc = buildDC(extractFileName);
                    String [] dates = m_dateNormalizer.extract(extractFileName, qdc);
                    m_dateNormalizer.dumpFinal(extractFileName, dates, 3);
                                        
                } catch (Exception ex) {
                    logger.logError(
                        "Exception FileExtract_DATE Thread(" 
                        + Thread.currentThread().getName() 
                        + ") file:" + extractFileName + " exception:" + ex, 3);
                }
            }
            return ;
            
        } catch(Exception ex) {
            logger.logError("Exception in run", 3);
            m_fw.getLogger( ).logError("trace" + StringUtil.stackTrace(ex), 10);
            return;
        }
    }

   /**
     * Start threads for processing IngestTool
     * @param resultPackages array of IngestTool results
     * @param workQueue work queue
     */
    protected Thread[] startThreads()
        throws FrameworkException
    {
        try {
            
            // start threads
            FileExtract_DATE fileExtract =  null;
            Thread[] threads = new Thread[m_threadCnt];
            for (int i=0; i<threads.length; i++) {
                fileExtract =  new FileExtract_DATE(this);
                threads[i] = fileExtract;
                threads[i].start();
            }
            m_fw.getLogger( ).logMessage(
                "FileExtract: threads started:" + threads.length, 3);
            
            return threads;
 
                
        } catch (Exception ex) {
            throw new FrameworkException(ex);
        }
    }    
    private Element buildDC(String extractFileName)
        throws FrameworkException
    {
        
        try {
            FileInputStream extractFile = new FileInputStream( new File(extractFileName) );
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            byte[ ] buff = new byte[1024];
            int readSize = 0;
            int zipSize = 0;
            while ((readSize = extractFile.read(buff, 0, buff.length)) != -1)
            {
                os.write(buff, 0, readSize);
            }
            if (os.size() == 0) {
                m_fw.getLogger( ).logError(
                    "FileExtract_DATE:buildDC: No data found:" + extractFileName, 3);
                return null;
            }
            String dc = os.toString("utf-8");
            os.close();
            extractFile.close();
            
            dc = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" + dc;
            Document dcDoc = DOMParser.stringToDOM(dc, m_fw.getLogger(), "utf-8", false, false, false);
            Element dcNode = dcDoc.getDocumentElement();
            
            Element qdc = dcNode;      
            if (StringUtil.isNotEmpty(m_headerElement)) {
                qdc = DOMParser.getFirstNode(dcNode, m_headerElement, m_fw.getLogger());
            }
            if (qdc == null) {
                m_fw.getLogger( ).logError(
                    "FileExtract_DATE: no qdc for " + m_headerElement, 3);
                return null;
            }
            return qdc;
                    
        } catch (Exception ex) {
            m_fw.getLogger().logError(
                    "FileExtract_DATE:buildDC: exception:" + ex, 3);
            m_fw.getLogger().logMessage(
                    "stack:" + StringUtil.stackTrace(ex), 3);
            throw new FrameworkException(FrameworkException.GENERAL_EXCEPTION, ex);
        }
    }

}
