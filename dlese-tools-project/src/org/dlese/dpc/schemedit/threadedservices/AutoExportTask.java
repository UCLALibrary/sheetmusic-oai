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

package org.dlese.dpc.schemedit.threadedservices;

import org.dlese.dpc.schemedit.SchemEditUtils;
import org.dlese.dpc.schemedit.config.*;
import org.dlese.dpc.schemedit.dcs.*;
import org.dlese.dpc.schemedit.repository.RepositoryService;

import javax.servlet.ServletContext;

import java.io.File;
import java.util.*;

/**
 *  Export collections at regular intervals.
 *
 * @author     
 */
public class AutoExportTask extends TimerTask {
	// Main processing method for this thread.
	private static boolean debug = true;
	private ServletContext servletContext = null;
	private RepositoryService repositoryService = null;
	
	public AutoExportTask (ServletContext servletContext) {
		super();
		this.servletContext = servletContext;
	}
		
	
	public void run() {
		prtln ("\n ===============================");
		prtln("run()");
		
		ExportingService exportingService = 
			(ExportingService) servletContext.getAttribute ("exportingService");
		RepositoryService repositoryService = 
			(RepositoryService) servletContext.getAttribute ("repositoryService");
		CollectionRegistry collectionRegistry =
			(CollectionRegistry) servletContext.getAttribute("collectionRegistry");
		
		// for each collection
		String collection = null;
		for (Iterator i = repositoryService.getSetInfos().iterator();i.hasNext();) {
			try {
				DcsSetInfo dcsSetInfo = (DcsSetInfo)i.next();
				collection = dcsSetInfo.getSetSpec();
				CollectionConfig config = collectionRegistry.getCollectionConfig(collection, false);
				String relPath = config.getExportDirectory();
				prtln ("\ncollection: " + collection);
				if (relPath == null || relPath.trim().length() == 0) {
					prtln ("\t export path not defined - skipping");
					continue;
				}
				prtln ("\t exportPath: " + relPath);
				
				File destDir = 
					ExportingService.validateExportDestination (exportingService.getExportBaseDir(), 
																relPath);
				String [] statuses = new String [1];
				statuses[0] = config.getFinalStatusValue();
			
				// export records that are FINAL and VALID
				prtln ("\n\t Calling exportRecords for \"" + collection + "\"");
				prtln ("\t \t destDir: " + destDir.toString());
				prtln ("\t \t Status: " + statuses[0]);
				// exportingService.exportRecords (destDir, dcsSetInfo, statuses, null);
				
				exportingService.setDcsSetInfo(dcsSetInfo);
				exportingService.setStatuses(statuses);
				List idList = exportingService.getIdList(collection, statuses);
				exportingService.exportRecords (idList, destDir, null);
				
			} catch (Throwable t) {
				String errorMsg = "AutoExport error: collection \"" + collection +
					"\" could not be exported: " + t.getMessage();
				prtln (errorMsg);
				t.printStackTrace();
				// throw new Exception (errorMsg);
			}
			prtln ("AutoExportTask completed\n");
		}

	}
	
	/**
	 *  Print a line to standard out.
	 *
	 *@param  s  The String to print.
	 */
	static void prtln(String s) {
		if (debug) {
			SchemEditUtils.prtln (s, "AutoExportTask");
		}
	}
}

