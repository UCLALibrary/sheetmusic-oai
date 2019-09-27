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

import org.cdlib.framework.dataModel.WorkQueue;
import org.cdlib.framework.utility.Framework;
import org.cdlib.framework.utility.FrameworkException;
import org.cdlib.framework.qa.QABase;

import java.io.File;
import java.io.IOException;
import java.lang.Runnable;
import java.util.Properties;
import java.util.Vector;

/**
 * Tests the basic operation of the framework Admin procedures
 * 
 */
public abstract class FileExtract extends Thread 
{
    protected Framework m_fw = null;
    protected WorkQueue m_queue = null;
    protected Vector m_resultPackages = null;
    protected int m_threadCnt = 5;
    protected String meta_type = null;
    
    public void initialize(Framework fw) {
        m_fw = fw;
        m_queue = new WorkQueue();
        m_resultPackages = new Vector(10000);
    }
    
    // Thread form initialize
    public void initialize(Framework fw, WorkQueue queue, Vector resultPackages) 
    {
        m_fw = fw;
        m_queue = queue;
        m_resultPackages = resultPackages;
    }
                
    // If dstDir does not exist, it will be created.
    protected void extractDirectory(String suffix, Vector list, File srcDir) throws IOException 
    {
        if (list.size() > 1000000) return;
        if (srcDir.isDirectory()) {            
            String[] children = srcDir.list();
            for (int i=0; i<children.length; i++) {
                if (list.size() < 10)
                    m_fw.getLogger( ).logMessage("file:" + children[i],  3);
                extractDirectory(suffix, list, new File(srcDir, children[i]));
            }
        } else {
            // This method is implemented in e1071 Copying a File

            String fileName = srcDir.getCanonicalPath();
            if (fileName.indexOf(suffix) > 0) {
                list.addElement(fileName);
                if ((list.size() % 10000) == 0)
                    m_fw.getLogger( ).logMessage("match(" + list.size() + "):" + suffix + " file2:" + fileName,  2);

            }
        }
    }

    public abstract void run(Properties prop)
        throws FrameworkException;

    
    /**
     * set thread end markers at the end of the queue - one for each running thread
     * @param workQueue work queue for threads
     */
    protected void addStopThreadMarkers()
    {
        for (int i=0; i<m_threadCnt; i++) {
            FileExtractRequest stopIndexer = new FileExtractRequest(true);
            m_queue.addWork(stopIndexer);
        }
    }        
   
    /**
     * Wait for all threads to complete
     * @param threads array of active threads to test
     */
    protected void waitThreadingDone(Thread[] threads)
    {
        for (int i=0; i < threads.length; i++) {
            try {
                threads[i].join();
            } catch (Exception ex) {
                m_fw.getLogger().logError("Thread " + i + " fails on join", 3);
            }
        }
    }
    
    public int getThreadCnt()
    {
        return m_threadCnt;
    }

    public void setThreadCnt(int in)
    {
        m_threadCnt = in;
    }
    
    protected class FileExtractRequest
    {
        public boolean stop = false;
        public String fileName = null;
        public String dcTag = null;
        public String metaType = null;
        public FileExtractRequest(boolean in)
        {
            stop = in;
        }
        public FileExtractRequest(String in)
        {
            fileName = in;
        }
        public FileExtractRequest(String fileName, String dcTag)
        {
            this.fileName = fileName;
            this.dcTag = dcTag;
        }
        public FileExtractRequest(String fileName, String metaType, String dcTag)
        {
            this.fileName = fileName;
            this.metaType = metaType;
            this.dcTag = dcTag;
        }
    }
    
}
