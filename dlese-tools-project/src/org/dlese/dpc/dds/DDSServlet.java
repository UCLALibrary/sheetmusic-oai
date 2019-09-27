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
package org.dlese.dpc.dds;

import org.dlese.dpc.repository.*;
import org.dlese.dpc.repository.indexing.*;
import org.dlese.dpc.repository.action.*;
import org.dlese.dpc.datamgr.*;
import org.dlese.dpc.repository.action.form.*;
import org.dlese.dpc.action.*;
import org.dlese.dpc.xml.*;
import org.dlese.dpc.dds.ndr.*;
import org.dlese.dpc.util.*;
import org.dlese.dpc.ndr.request.*;
import org.dlese.dpc.services.dds.action.*;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.dlese.dpc.index.*;
import org.dlese.dpc.index.writer.*;
import org.dlese.dpc.vocab.*;
import java.text.*;
import org.dlese.dpc.webapps.tools.*;
import org.dlese.dpc.dds.action.*;
import org.dlese.dpc.propertiesmgr.*;

/**
 *  Provided as an administrative and intialization servlet for the Digital Discovery System (DDS).
 *
 * @author    John Weatherley, Dave Deniman, Ryan Deardorff
 */
public class DDSServlet extends HttpServlet {

	final static int VALID_REQUEST = 0;
	final static int REQUEST_EXCEPTION = -1;
	final static int UNRECOGNIZED_REQUEST = 1;
	final static int NO_REQUEST_PARAMS = 2;
	final static int INITIALIZING = 3;
	final static int NOT_INITIALIZED = 4;
	final static int INVALID_CONTEXT = 5;
	final static String DIRECTORY_DATA_DIR = "file_monitor_metadata";
	private String repositoryConfigDirPath = null;
	private String ddsContext = "unknown";

	private static boolean isInitialized = false;
	private boolean debug = true;
	private long currentRepositoryNumber = 1;
	
	private boolean doPerformBackgroundIndexing = false;
	private boolean doPerformBackgroundIndexingExternally = false;	
	private boolean doPerformContinuousIndexing = false;
	
	private long NUM_REPO_BACKUPS = 4;
	

	/**  Constructor for the DDSServlet object */
	public DDSServlet() { }


	/**
	 *  The standard <code>HttpServlet</code> init method, called only when the servlet is first loaded.
	 *
	 * @param  servletConfig         The config
	 * @exception  ServletException
	 */
	public void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);

		try {

			if (isInitialized) {
				prtlnErr("DDS has already been initialized. Call to DDSServlet.init() aborted...");
				return;
			}

			isInitialized = true;

			ServletContext servletContext = getServletContext();

			// Verbose debugging messages?
			if (((String) servletContext.getInitParameter("debug")).equalsIgnoreCase("true")) {
				debug = true;
				prtln("Outputting debug info");
			}
			else {
				debug = false;
				prtln("Debug info disabled");
			}
			
			// Put DDSServlet into app scope for use in RepositoryAdminAction:
			servletContext.setAttribute("ddsServlet", this);

			// The context in which DDS is running. Possible values: dds-webapps, dcs-webapp
			ddsContext = (String) servletConfig.getInitParameter("ddsContext");
			if (ddsContext == null || ddsContext.trim().length() == 0)
				ddsContext = "unknown";

			// Set the max number of service results allowed in DDSWS:
			String maxNumResultsDDSWS = (String) servletContext.getInitParameter("maxNumResultsDDSWS");
			int maxDDSWSResults = 1000;
			try {
				maxDDSWSResults = Integer.parseInt(maxNumResultsDDSWS);
			} catch (Throwable t) {}
			DDSServicesAction.setMaxSearchResults(maxDDSWSResults);
			servletContext.setAttribute("maxNumResultsDDSWS", Integer.toString(maxDDSWSResults));

			// Set all debugging:
			RepositoryManager.setDebug(debug);
			FileIndexingService.setDebug(debug);
			FileIndexingServiceWriter.setDebug(debug);
			SimpleLuceneIndex.setDebug(debug);
			SimpleQueryAction.setDebug(debug);
			RepositoryForm.setDebug(debug);
			SerializedDataManager.setDebug(debug);
			RepositoryAdminAction.setDebug(debug);
			RepositoryAction.setDebug(debug);
			RecordDataService.setDebug(debug);
			DDSAdminQueryAction.setDebug(debug);
			DDSQueryAction.setDebug(debug);
			SimpleNdrRequest.setDebug(debug);
			CollectionIndexer.setDebug(debug);

			// Set up background indexing if requested:
			String performBackgroundIndexing = (String) servletContext.getInitParameter("performBackgroundIndexing");
			doPerformBackgroundIndexing = (performBackgroundIndexing != null && performBackgroundIndexing.equalsIgnoreCase("true"));			

			// Set up manual indexing if requested:
			String performBackgroundIndexingExternally = (String) servletContext.getInitParameter("performBackgroundIndexingExternally");
			doPerformBackgroundIndexingExternally = (performBackgroundIndexingExternally != null && performBackgroundIndexingExternally.equalsIgnoreCase("true"));
			
			// Set up continuous background indexing if requested:
			String performContinuousIndexing = (String) servletContext.getInitParameter("performContinuousIndexing");
			doPerformContinuousIndexing = (performContinuousIndexing != null && performContinuousIndexing.equalsIgnoreCase("true"));
			
			// Remove any Lucene locks that may have persisted from a previous dirty shut-down:
			String tempDirPath = System.getProperty("java.io.tmpdir");
			if (tempDirPath != null) {
				File tempDir = new File(tempDirPath);

				FilenameFilter luceneLockFilter =
					new FilenameFilter() {
						public boolean accept(File dir, String name) {
							return (name.startsWith("lucene") && name.endsWith(".lock"));
						}
					};

				File[] tempFiles = tempDir.listFiles(luceneLockFilter);

				if (tempFiles != null) {
					for (int i = 0; i < tempFiles.length; i++) {
						prtlnErr("DDSServlet startup: Removing lucene lock file: " + tempFiles[i].getAbsolutePath());
						tempFiles[i].delete();
					}
				}
			}

			// Initialize and set up the Repository Managers:
			createRepositories(false);

			String recordDataSource = getRecordDataSource();
			servletContext.setAttribute("recordDataSource", recordDataSource);

			// Set the startup date and log the time:
			servletContext.setAttribute("ddsStartUpDate", new Date());

			System.out.println("\n\n" + getDateStamp() + " DDSServlet started for context " + ddsContext + ".\n\n");

		} catch (Throwable t) {
			prtlnErr("DDSServlet.init() error: " + t);
			t.printStackTrace();
			throw new ServletException(t);
		}
	}


	private Object indexingOperationsLock = new Object();


	/**  Commit the background index to production and swap indexes: */
	public void commitBackgroundIndex() throws Exception {
		prtln("commitBackgroundIndex()");

		try {
			if (doPerformBackgroundIndexing)
				createRepositories(true);
		} catch (Exception t) {
			prtln("Error commitBackgroundIndex(): " + t);
			throw t;
		}
	}

	/**
	 *  Create the repositories and rotate indexes/data if requested:
	 *
	 * @param  rotateBackgroundRepository  True to rotate the repository
	 * @exception  Exception               If error
	 */
	private void createRepositories(boolean rotateBackgroundRepository) throws Exception {
		synchronized (indexingOperationsLock) {
			try {

				ServletContext servletContext = getServletContext();

				// Grab the current live instances to shut down after new ones created:
				IndexingManager indexingManager = (IndexingManager) getServletContext().getAttribute("indexingManager");
				RepositoryManager repositoryManager = (RepositoryManager) getServletContext().getAttribute("repositoryManager");
				RepositoryManager backgroundIndexingRepositoryManager = (RepositoryManager) getServletContext().getAttribute("backgroundIndexingRepositoryManager");
				
				// Optimize the indexes before continuing:
				if(repositoryManager != null && !repositoryManager.getIsReadOnly())
					repositoryManager.optimizeIndexes();
				if(backgroundIndexingRepositoryManager != null && !backgroundIndexingRepositoryManager.getIsReadOnly())
					backgroundIndexingRepositoryManager.optimizeIndexes();
				
				File repositoryDataBaseDir = new File(getAbsolutePath((String) servletContext.getInitParameter("repositoryData")));
				File repositoryDataDirCurrent = null;
				File indexLocationDirCurrent = null;
				File repositoryDirectoryCurrent = null;

				File repositoryInfoBeanFile = new File(repositoryDataBaseDir,"repository_info_bean.xml");
				RepositoryInfoBean repositoryInfoBean = null;
				try {
					repositoryInfoBean = (RepositoryInfoBean)Beans.file2Bean(repositoryInfoBeanFile);
				} catch (FileNotFoundException fe) {
					//prtlnErr("Error reading repositoryInfoBean: " + t);	
				} catch (Throwable t) {
					prtlnErr("Error reading repositoryInfoBean: " + t);	
				}
				
				NumberFormat numberFormat = NumberFormat.getNumberInstance();
				numberFormat.setMinimumIntegerDigits(6);
				numberFormat.setGroupingUsed(false);
				String currentRepoNum = numberFormat.format(currentRepositoryNumber);
				String nextRepoNum = numberFormat.format(currentRepositoryNumber + 1);		
				
				if(!rotateBackgroundRepository && repositoryInfoBean != null && repositoryInfoBean.getCurrentRepository() != null) {
					repositoryDirectoryCurrent = new File(repositoryInfoBean.getCurrentRepository());
				}
				else {
					repositoryDirectoryCurrent = new File(repositoryDataBaseDir, "repository_" + currentRepoNum);
					if(doPerformBackgroundIndexingExternally && !repositoryDirectoryCurrent.exists()){
						throw new Exception("Can not load externally-generated repository. Directory does not exist: " + repositoryDirectoryCurrent);	
					}
				}
				repositoryDataDirCurrent = new File(repositoryDirectoryCurrent, "data");
				indexLocationDirCurrent = new File(repositoryDirectoryCurrent, "index");
								
				if (doPerformBackgroundIndexing) {
					String curDirName = repositoryDirectoryCurrent.getName();
					long curLong = Long.parseLong(curDirName.substring(11,curDirName.length()));	
					currentRepositoryNumber = curLong+1;
					prtln("setting currentRepositoryNumber to: " + currentRepositoryNumber);
				}

				RepositoryManager newRepositoryManager = null;
				RepositoryManager repositoryManagerForIndexing = null;
				
				// Create new repository live:
				boolean isReadOnly = false;
				if(doPerformBackgroundIndexing)
					isReadOnly = true;
				newRepositoryManager = createNewRepositoryManager(repositoryDataDirCurrent, indexLocationDirCurrent, isReadOnly);
				repositoryManagerForIndexing = newRepositoryManager;
				setRepositoryManagerLive(newRepositoryManager);				
				
				File repositoryDirectoryNext = null;
				if(!rotateBackgroundRepository && repositoryInfoBean != null && repositoryInfoBean.getBackgroundIndexerRepository() != null)
					repositoryDirectoryNext = new File(repositoryInfoBean.getBackgroundIndexerRepository());	
				else
					repositoryDirectoryNext = new File(repositoryDataBaseDir, "repository_" + nextRepoNum);				
				if (doPerformBackgroundIndexing && !doPerformBackgroundIndexingExternally) {
					RepositoryManager newBackgroundIndexingRepositoryManager = null;
					
					File repositoryDataDirNext = new File(repositoryDirectoryNext, "data");
					File indexLocationDirNext = new File(repositoryDirectoryNext, "index");
										
					// Make sure the background indexer is fresh and empty:
					//Files.deleteDirectory(repositoryDataDirNext);
					//Files.deleteDirectory(indexLocationDirNext);
					
					// Create new repository background indexer:
					newBackgroundIndexingRepositoryManager = createNewRepositoryManager(repositoryDataDirNext, indexLocationDirNext, false);
					repositoryManagerForIndexing = newBackgroundIndexingRepositoryManager;		
					setBackgroundIndexingRepositoryManagerLive(newBackgroundIndexingRepositoryManager);
				}
				
				// Create the new IndexingManager, if applicable:
				String recordDataSource = getRecordDataSource();
				boolean filesAreSavedToDisc = true;
				if (!recordDataSource.equals("fileSystem") && !recordDataSource.equals("dcs")) {
					initNewIndexingManager(repositoryManagerForIndexing);
					
					// If records are being handled by the IndexingManager, files are not saved to disk:
					filesAreSavedToDisc = false;
				}
				servletContext.setAttribute("filesAreSavedToDisc",filesAreSavedToDisc);

				// Clean up and the old instances:
				if (repositoryManager != null)
					repositoryManager.destroy();
				if (backgroundIndexingRepositoryManager != null)
					backgroundIndexingRepositoryManager.destroy();
				if (indexingManager != null)
					indexingManager.destroy();
				
				RepositoryInfoBean repositoryInfoBeanNew = new RepositoryInfoBean(repositoryDirectoryCurrent.getAbsolutePath(),repositoryDirectoryNext.getAbsolutePath());
				try {
					prtln("Writing repositoryInfoBean: " + repositoryInfoBeanNew);
					Beans.bean2File(repositoryInfoBeanNew,repositoryInfoBeanFile);
				} catch (Throwable t) {
					prtlnErr("Error writing repositoryInfoBean: " + t);	
				}
				
				// Delete old repositories no longer needed for backup:
				for(long i = (currentRepositoryNumber - 100); i < (currentRepositoryNumber - NUM_REPO_BACKUPS); i++) {
					try {
						if(i >= 0) {
							numberFormat = NumberFormat.getNumberInstance();
							numberFormat.setMinimumIntegerDigits(6);
							numberFormat.setGroupingUsed(false);
							String delRepoNum = numberFormat.format(i);						
							Files.deleteDirectory(new File(repositoryDataBaseDir, "repository_" + delRepoNum));	
						}
					} catch (Throwable t) {
						prtlnErr("Error deleting previous repository directory: " + t);	
					}						
				}

			} catch (Exception e) { 
				prtlnErr("Error createRepositories(): " + e);
				e.printStackTrace();
				throw e;
			} catch (Throwable t) { 
				prtlnErr("Error createRepositories(): " + t);
				t.printStackTrace();
			}
		}
	}


	private void initNewIndexingManager(RepositoryManager repositoryManager) throws Exception {
		IndexingManager indexingManager = new IndexingManager(repositoryManager, this);

		ServletContext servletContext = getServletContext();

		String recordDataSource = getRecordDataSource();
		recordDataSource = recordDataSource.replaceAll("\\s+", "");
		String[] classes = recordDataSource.split(",");
		for (int i = 0; i < classes.length; i++) {
			try {
				prtln("Adding ItemIndexer '" + classes[i] + "'");
				indexingManager.addIndexingEventHandler(classes[i]);
				prtln("Done Adding ItemIndexer '" + classes[i] + "'");
			} catch (Throwable t) {
				prtlnErr("Error initializing data source Java class for IndexingManager: " + t);
			}
		}

		try {
			indexingManager.fireIndexerReadyEvent(null);
			indexingManager.fireUpdateCollectionsEvent(null);
		} catch (Throwable t) {
			prtlnErr("Error occured with an ItemIndexer: " + t);
			t.printStackTrace();
		}

		String indexingStartTime = getIndexingStartTime();
		String indexingDaysOfWeek = getIndexingDaysOfWeek();
		try {
			if(doPerformContinuousIndexing)
				indexingManager.startContinuousIndexingTimer();
			else if (indexingStartTime != null)
				indexingManager.startIndexingTimer(indexingStartTime, indexingDaysOfWeek);
		} catch (Throwable t) {
			prtlnErr("Error starting the indexing timer: " + t);
		}

		IndexingManager existingIndexingManager = (IndexingManager) servletContext.getAttribute("indexingManager");
		if (existingIndexingManager != null)
			existingIndexingManager.destroy();
		servletContext.setAttribute("indexingManager", indexingManager);
	}



	private RepositoryManager createNewRepositoryManager(
			File repositoryDataDir,
			File indexLocationDir,
			boolean isReadOnly) throws Exception {

		ServletContext servletContext = getServletContext();

		if (!repositoryDataDir.exists()) {
			prtln("Creating directory " + repositoryDataDir.getAbsolutePath());
			boolean created = repositoryDataDir.mkdirs();
			if (created)
				prtln("Created directory " + repositoryDataDir.getAbsolutePath());
			else
				throw new Exception("Unable to create data dir: " + repositoryDataDir.getAbsolutePath());
		}

		if (!indexLocationDir.exists()) {
			prtln("Creating directory " + indexLocationDir.getAbsolutePath());
			boolean created = indexLocationDir.mkdirs();
			if (created)
				prtln("Created directory " + indexLocationDir.getAbsolutePath());
			else
				throw new Exception("Unable to create index dir: " + indexLocationDir.getAbsolutePath());
		}

		// Context init params set in the context definition in server.xml or web.xml:
		String contentCacheBaseUrl = servletContext.getInitParameter("contentCacheBaseUrl");
		String dbURL = servletContext.getInitParameter("dbURL");
		String idMapperExclusionFile = servletContext.getInitParameter("idMapperExclusionFile");
		String collectionRecordsLocation = (String) servletContext.getInitParameter("collectionRecordsLocation");
		String maxNumResultsDDSWS = (String) servletContext.getInitParameter("maxNumResultsDDSWS");
		
		// Determine the metadata records location:
		String collBaseDir = null;
		String collBaseDirVal = (String) servletContext.getInitParameter("collBaseDir");
		
		// Use the location specified, if available:
		if(collBaseDirVal != null && collBaseDirVal.trim().length() > 0) {
			collBaseDir = getAbsolutePath(collBaseDirVal);
		}
		// Otherwise, use the default location inside repositoryData (one level up from repositoryDataDir):
		else {
			File collBaseDirFile = new File(repositoryDataDir.getParentFile(),"records");
			collBaseDir = collBaseDirFile.getAbsolutePath();
		}
		
		// By default, only use records located in the ../dlese_collect/collect directory to configure
		// the collections for this repository:
		if (collectionRecordsLocation == null)
			collectionRecordsLocation = getAbsolutePath(collBaseDir + "/dlese_collect/collect");
		else
			collectionRecordsLocation = getAbsolutePath(collectionRecordsLocation);

		//String enableNewSets = servletContext.getInitParameter("enableNewSets");
		String doResourceDeDuplication = servletContext.getInitParameter("doResourceDeDuplication");

		// Indexing start time in 24hour time, for example 0:35 or 23:35
		String indexingStartTime = getIndexingStartTime();
		if (indexingStartTime == null || indexingStartTime.equalsIgnoreCase("disabled"))
			indexingStartTime = null;

		String recordDataSource = getRecordDataSource();

		// If an IndexingManager is being used, don't run the RepositoryManager file indexing timer.
		String indexingStartTimeFileSystem = indexingStartTime;
		if (!recordDataSource.equals("fileSystem"))
			indexingStartTimeFileSystem = null;

		// Indexing days of the week as a comma separated list of integers, for example 1,3,5 where 1=Sunday, 2=Monday, 7=Saturday etc.
		String indexingDaysOfWeek = getIndexingDaysOfWeek();
		if (indexingDaysOfWeek == null || indexingDaysOfWeek.equalsIgnoreCase("all"))
			indexingDaysOfWeek = null;

		prtln("Using collection-level metadata files located at " + collectionRecordsLocation);
		prtln("Using/writing metadata files located at " + collBaseDir);
		prtln("Using index located at " + indexLocationDir.getAbsolutePath());

		// Load metadata controlled vocabularies:
		String vocabConfigDir = getAbsolutePath((String) servletContext.getInitParameter("vocabConfigDir"));
		String sqlDriver = (String) servletContext.getInitParameter("sqlDriver");
		String sqlURL = (String) servletContext.getInitParameter("sqlURL");
		String sqlUser = (String) servletContext.getInitParameter("sqlUser");
		String sqlPassword = (String) servletContext.getInitParameter("sqlPassword");
		String annotationPathwaysSchemaUrl = (String) servletContext.getInitParameter("annotationPathwaysSchemaUrl");
		String vocabTextFile = (String) servletContext.getInitParameter("vocabTextFile");
		
		// MetadataVocab is instantiated by MetadataVocabServlet (LoadMetadataOPML class):
		MetadataVocab metadataVocab = (MetadataVocab) servletContext.getAttribute("MetadataVocab");
		if(metadataVocab == null)
			prtlnErr("WARNING: MetadataVocab is null");

		// Get the config directory.
		File repositoryConfigDir = null;
		try {
			repositoryConfigDir = new File(getAbsolutePath((String) servletContext.getInitParameter("repositoryConfigDir")));
			repositoryConfigDirPath = repositoryConfigDir.getAbsolutePath();
		} catch (Throwable e) {
			prtlnErr("Error getting repositoryConfigDir: " + e);
		}

		// Get the ItemIndexer config directory.
		File itemIndexerConfigDir = null;
		try {
			itemIndexerConfigDir = new File(getAbsolutePath((String) servletContext.getInitParameter("itemIndexerConfigDir")));
		} catch (Throwable e) {
			prtlnErr("Error getting itemIndexerConfigDir: " + e);
		}

		boolean doResourceDeDuplicationBoolean = false;
		if (doResourceDeDuplication != null && doResourceDeDuplication.equalsIgnoreCase("true"))
			doResourceDeDuplicationBoolean = true;

		// The maximum number of files to index per session. Set to 1 if de-duping is enabled:
		int maxNumFilesToIndex = 500;
		if (doResourceDeDuplication != null && doResourceDeDuplication.equalsIgnoreCase("true"))
			maxNumFilesToIndex = 1;

		// Set up the RecordDataService for use in indexing and search results:
		RecordDataService recordDataService = new RecordDataService(dbURL, metadataVocab, collBaseDir, annotationPathwaysSchemaUrl);

		// Set up an XMLConversionService for use in DDS web service, OAI and elsewhere:
		XMLConversionService xmlConversionService = null;
		File xslFilesDirecory = new File(GeneralServletTools.getAbsolutePath("WEB-INF/xsl_files/", getServletContext()));
		File xmlCachDir = new File(repositoryDataDir, "converted_xml_cache");
		try {
			xmlConversionService = XMLConversionService.xmlConversionServiceFactoryForServlets(getServletContext(), xslFilesDirecory, xmlCachDir, true);
		} catch (Throwable t) {
			prtlnErr("ERROR: Unable to initialize xmlConversionService: " + t);
		}

		RepositoryManager repositoryManager = null;
		prtln("Starting up RepositoryManager");
		repositoryManager = new RepositoryManager(
				repositoryConfigDir,
				itemIndexerConfigDir,
				repositoryDataDir.getAbsolutePath(),
				indexLocationDir.getAbsolutePath(),
				indexingStartTimeFileSystem,
				indexingDaysOfWeek,
				recordDataService,
				collectionRecordsLocation,
				collBaseDir,
				xmlConversionService,
				contentCacheBaseUrl,
				doResourceDeDuplicationBoolean,
				true,
				true,
				isReadOnly);

		// Set up the default metadata audience and language used by the indexer and search resultDocs for display:
		String metadataVocabAudience = (String) servletContext.getInitParameter("metadataVocabAudience");
		if (metadataVocabAudience == null) {
			metadataVocabAudience = "community";
		}
		String metadataVocabLanguage = (String) servletContext.getInitParameter("metadataVocabLanguage");
		if (metadataVocabLanguage == null) {
			metadataVocabLanguage = "en-us";
		}

		repositoryManager.setMetadataVocabAudienceDefault(metadataVocabAudience);
		repositoryManager.setMetadataVocabLanguageDefault(metadataVocabLanguage);

		// Set the URL that is used to display the baseURL in OAI-PMH responses and elsewhere:
		String oaiBaseUrlOverride = servletContext.getInitParameter("oaiBaseUrlOverride");
		if (oaiBaseUrlOverride != null && !oaiBaseUrlOverride.equals("[determine-from-client]"))
			repositoryManager.setOaiBaseUrlOverride(oaiBaseUrlOverride);

		// Set up the ending portion of the baseUrl for the data provider, if indicated:
		String dataProviderBaseUrlPathEnding = servletContext.getInitParameter("dataProviderBaseUrlPathEnding");
		if (dataProviderBaseUrlPathEnding != null)
			repositoryManager.setProviderBaseUrlEnding(dataProviderBaseUrlPathEnding);

		// Disable regular OAI-PMH responses to ListRecords and ListIdentifiers requests (accept ODL requests only), if indicated:
		String oaiPmhEnabled = (String) servletContext.getInitParameter("oaiPmhEnabled");
		if (oaiPmhEnabled != null && oaiPmhEnabled.equals("false"))
			repositoryManager.setIsOaiPmhEnabled(false);

		// Set the location of the file used to exclude IDs in the IDMapper service
		if (idMapperExclusionFile != null && idMapperExclusionFile.length() > 0 && !idMapperExclusionFile.equals("none")) {
			prtln("Using IDMapper ID exclusion file located at: " + idMapperExclusionFile);
			repositoryManager.setIdMapperExclusionFilePath(idMapperExclusionFile);
		}

		// Set up the configured FileIndexingPlugins prior to calling rm.init()
		setFileIndexingPlugins(repositoryManager);

		FileIndexingService fileIndexingService = repositoryManager.getFileIndexingService();

		boolean indexCollectionRecords = true;

		// Initialize RepositoryManager:
		int initResult = repositoryManager.init(indexCollectionRecords);
		if (initResult != 1) {
			String initErrorMsg = "Error initializing the repositoryManager";
			prtlnErr(initErrorMsg);
			throw new Exception(initErrorMsg);
		}

		// Add default xmlformat schema and namespaces for OAI:
		Enumeration enumeration = getInitParameterNames();
		String param;
		while (enumeration.hasMoreElements()) {
			param = (String) enumeration.nextElement();
			if (param.toLowerCase().startsWith("xmlformatinfo")) {
				try {
					repositoryManager.setDefaultXmlFormatInfo(getInitParameter(param));
				} catch (Throwable t) {
					String initErrorMsg = "Error reading init param for xmlformatinfo: " + t;
					t.printStackTrace();
					prtlnErr(initErrorMsg);
				}
			}
		}

		return repositoryManager;
	}


	// Make the given RepositoryManager the live one servicing Web requests:
	private void setRepositoryManagerLive(RepositoryManager repositoryManager) throws Exception {

		ServletContext servletContext = getServletContext();

		// Set the URL that is used to display the baseURL in DDSWS1-1 responses and elsewhere in the UI (for now only used in JSPs via app scope variable):
		String ddsws11BaseUrlOverride = servletContext.getInitParameter("ddsws11BaseUrlOverride");
		if (ddsws11BaseUrlOverride != null && !ddsws11BaseUrlOverride.equals("[determine-from-client]"))
			servletContext.setAttribute("ddsws11BaseUrlOverride", ddsws11BaseUrlOverride);

		// Set up a dir for the record meta-metadata
		File recordMetaMetadataDir = new File(repositoryManager.getRepositoryDataDir(), "record_meta_metadata");
		recordMetaMetadataDir.mkdirs();
		servletContext.setAttribute("recordMetaMetadataDir", recordMetaMetadataDir);

		// Perform context-specific initialization (right now just dds but can be used for dcs and joai if needed):
		if (ddsContext.equals("dds-webapp") || ddsContext.equals("dcs-webapp")) {
			// Set up OAI sets for each collection (run in the background...):
			repositoryManager.defineOaiSetsForCollections(true);
		}

		String enableNewSets = servletContext.getInitParameter("enableNewSets");
		servletContext.setAttribute("enableNewSets", enableNewSets);

		String queryLogFile = getAbsolutePath((String) servletContext.getInitParameter("queryLogFile"));
		servletContext.setAttribute("queryLogFile", queryLogFile);

		String resourceResultLinkRedirectURL = (String) servletContext.getInitParameter("resourceResultLinkRedirectURL");
		if ((resourceResultLinkRedirectURL == null) || (resourceResultLinkRedirectURL.equals("none"))) {
			resourceResultLinkRedirectURL = "";
		}
		else if (!resourceResultLinkRedirectURL.endsWith("/"))
			resourceResultLinkRedirectURL = resourceResultLinkRedirectURL + "/";
		servletContext.setAttribute("resourceResultLinkRedirectURL", resourceResultLinkRedirectURL);

		setCollectionsVocabDisplay(repositoryManager);

		// Load DDS-related properties from file
		Properties ddsConfigProperties = null;
		try {
			ddsConfigProperties = new PropertiesManager(repositoryConfigDirPath + "/dds_config.properties");
			servletContext.setAttribute("ddsConfigProperties", ddsConfigProperties);
		} catch (Throwable t) {
			prtlnErr("Error loading DDS config properties: " + t);
		}

		// Shut down existing ones:
		RepositoryManager existingRepositoryManager = (RepositoryManager) servletContext.getAttribute("repositoryManager");
		if (existingRepositoryManager != null)
			existingRepositoryManager.destroy();

		SimpleLuceneIndex existingIndex = (SimpleLuceneIndex) servletContext.getAttribute("index");
		if (existingIndex != null)
			existingIndex.close();
		

		servletContext.setAttribute("repositoryManager", repositoryManager);
		servletContext.setAttribute("index", repositoryManager.getIndex());
		//prtln("\n\n***setRepositoryManagerLive() to: " + repositoryManager.getRepositoryDataDir().getAbsolutePath() + " repo.numRecords: " + repositoryManager.getNumRecordsInIndex() + " index.numRecords: " + repositoryManager.getIndex().getNumDocs() + "\n\n");
	}


	// Make the given RepositoryManager the live one servicing background indexing:
	private void setBackgroundIndexingRepositoryManagerLive(RepositoryManager repositoryManager) throws Exception {
		ServletContext servletContext = getServletContext();

		// Shut down existing ones:
		RepositoryManager existingRepositoryManager = (RepositoryManager) servletContext.getAttribute("backgroundIndexingRepositoryManager");
		if (existingRepositoryManager != null)
			existingRepositoryManager.destroy();

		servletContext.setAttribute("backgroundIndexingRepositoryManager", repositoryManager);
	}



	/**
	 *  Sets the fileIndexingPlugins found in the servlet config.
	 *
	 * @param  repositoryManager  The new fileIndexingPlugins value
	 */
	private void setFileIndexingPlugins(RepositoryManager repositoryManager) {
		try {
			// Add configured FileIndexingPlugins:
			Enumeration enumeration = getInitParameterNames();
			String param;
			while (enumeration.hasMoreElements()) {
				param = (String) enumeration.nextElement();

				if (param.toLowerCase().startsWith("fileindexingplugin")) {
					String paramVal = getInitParameter(param);
					String[] vals = paramVal.split("\\|");
					if (vals.length != 2 && vals.length != 1) {
						prtlnErr("Error: setFileIndexingPlugins(): could not parse parameter '" + paramVal + "'");
						continue;
					}

					try {
						Class pluginClass = Class.forName(vals[0].trim());
						FileIndexingPlugin plugin = (FileIndexingPlugin) pluginClass.newInstance();
						String format = vals.length == 2 ? vals[1].trim() : RepositoryManager.PLUGIN_ALL_FORMATS;

						// Make the ServletContext available to all ServletContextFileIndexingPlugins
						if (plugin instanceof ServletContextFileIndexingPlugin) {
							((ServletContextFileIndexingPlugin) plugin).setServletContext(getServletContext());
						}

						//System.out.println("Adding plugin: " + plugin.getClass().getName() + " for format " + format);
						repositoryManager.setFileIndexingPlugin(format, plugin);
					} catch (Throwable e) {
						prtlnErr("Error: setFileIndexingPlugins(): could not instantiate class '" + vals[0].trim() + "'. " + e);
						continue;
					}
				}
			}
		} catch (Throwable e) {
			String initErrorMsg = "Error: setFileIndexingPlugins(): " + e;
			prtlnErr(initErrorMsg);
		}

	}


	private String getIndexingStartTime() {
		// Indexing start time in 24hour time, for example 0:35 or 23:35
		String indexingStartTime = (String) getServletContext().getInitParameter("indexingStartTime");
		if (indexingStartTime == null || indexingStartTime.equalsIgnoreCase("disabled"))
			indexingStartTime = null;
		return indexingStartTime;
	}


	private String getIndexingDaysOfWeek() {
		// Indexing days of the week as a comma separated list of integers, for example 1,3,5 where 1=Sunday, 2=Monday, 7=Saturday etc.
		String indexingDaysOfWeek = (String) getServletContext().getInitParameter("indexingDaysOfWeek");
		if (indexingDaysOfWeek == null || indexingDaysOfWeek.equalsIgnoreCase("all"))
			indexingDaysOfWeek = null;
		return indexingDaysOfWeek;
	}


	private String getRecordDataSource() {
		return (String) getServletContext().getInitParameter("recordDataSource");
	}


	/**  Shut down sequence. */
	public void destroy() {
		IndexingManager indexingManager = (IndexingManager) getServletContext().getAttribute("indexingManager");

		try {
			if (indexingManager != null)
				indexingManager.destroy();
		} catch (Throwable t) {
			prtlnErr("Problem shutting down indexingManager: " + t);
		}

		RepositoryManager repositoryManager = (RepositoryManager) getServletContext().getAttribute("repositoryManager");
		if(repositoryManager != null)
			repositoryManager.destroy();
		
		RepositoryManager backgroundIndexingRepositoryManager = (RepositoryManager) getServletContext().getAttribute("backgroundIndexingRepositoryManager"); 				
		if(backgroundIndexingRepositoryManager != null)
			backgroundIndexingRepositoryManager.destroy();
		
		System.out.println("\n\n" + getDateStamp() + " DDSServlet stopped." + "\n\n");
	}


	/**
	 *  Standard doPost method forwards to doGet
	 *
	 * @param  request
	 * @param  response
	 * @exception  ServletException
	 * @exception  IOException
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			 throws ServletException, IOException {
		doGet(request, response);
	}


	/**
	 *  The standard required servlet method, just parses the request header for known parameters. The <code>doPost</code>
	 *  method just calls this one. See {@link HttpServlet} for details.
	 *
	 * @param  request
	 * @param  response
	 * @exception  ServletException
	 * @exception  IOException
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			 throws ServletException, IOException {
		PrintWriter out = response.getWriter();

		int result = handleRequest(request, response, out);
		switch (result) {

						case VALID_REQUEST:
							response.setContentType("text/html");
							// processed a request okay
							return;
						case UNRECOGNIZED_REQUEST:
							// no recognized parameters
							response.setContentType("text/html");
							out.println("Called with unrecognized parameter(s)...");
							return;
						case NO_REQUEST_PARAMS:
							// no paramters
							response.setContentType("text/html");
							out.println("Request did not contain a parameter...");
							return;
						case INITIALIZING:
							response.setContentType("text/html");
							out.println("System is initializing...");
							out.println(" ... initializtion may take less than a second or several minutes.");
							out.println(" ... please try request again.");
							return;
						case NOT_INITIALIZED:
							out.println("System is not initialized...");
							out.println(" ... the server may need to be restarted,");
							out.println(" ... or there is a problem with configuration.");
							out.println("");
							out.println("Please inform support@your.org.");
							out.println("");
							out.println("Thank You");
							return;
						case INVALID_CONTEXT:
							response.setContentType("text/html");
							out.println("A request was recieved, but the context can not be identified...");
							out.println(" ... either  unable to initialize the catalog context," +
									" or the servlet container is in an invalid state.");
							return;
						default:
							// an exception occurred
							response.setContentType("text/html");
							out.println("An unexpected exception occurred processing request...");
							return;
		}
	}


	/**
	 *  Used to provide explicit command parameter processing.
	 *
	 * @param  request
	 * @param  response
	 * @param  out       DESCRIPTION
	 * @return
	 */
	private int handleRequest(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {

		try {
			//if ( catalog != null && monitor != null ) {
			//if (catalog.ready()) {
			Enumeration paramNames = request.getParameterNames();
			if (paramNames.hasMoreElements()) {
				while (paramNames.hasMoreElements()) {
					String paramName = (String) paramNames.nextElement();
					String[] paramValues = request.getParameterValues(paramName);
					// this next section can use an interface and hashmap -
					// see pg 228 of JavaServerPages
					if (paramValues.length == 1) {
						if (paramName.equals("command")) {
							if (paramValues[0].equals("stop")) {
								//fileIndexingService.stopTester();
							}
							return VALID_REQUEST;
							//if(paramValues[0].equals("start"))
							//	fileIndexingService.startTester();

						}
//  								if (paramName.equals("query")) {
//  									//catalog.reinit();
//  									//response.setContentType("text/html");
//  									//PrintWriter out = response.getWriter();
//  									//out.println("CatalogAdmin called Catalog.reinit() ");
//  									//out.println("See Catalog Activity Log for messages.");
//  									return VALID_REQUEST;
//  								}
//  								if (paramName.equals("unlock")) {
//  									//releaseLock(paramValues[0], request, response);
//  									return VALID_REQUEST;
//  								}
					}
				}
				return UNRECOGNIZED_REQUEST;
			}
			return NO_REQUEST_PARAMS;
			//}
			//else if (catalog.initializing())
			//	return CATALOG_INITIALIZING;
			//else
			//	return CATALOG_NOT_INITIALIZED;
			//}
			//return INVALID_CONTEXT;
		} catch (Throwable t) {
			return REQUEST_EXCEPTION;
		}
	}


	/**
	 *  Sets the "noDisplay" property of collection vocab nodes according the results of the repository manager's
	 *  getEnabledSetsHashMap()
	 *
	 * @param  repositoryManager  The new collectionsVocabDisplay value
	 */
	public static void setCollectionsVocabDisplay(RepositoryManager repositoryManager) {
		MetadataVocab vocab = repositoryManager.getMetadataVocab();
		if (vocab == null)
			return;
		Set vi = vocab.getVocabSystemInterfaces();
		Iterator i = vi.iterator();
		while (i.hasNext()) {
			doSetCollectionsVocabDisplay(repositoryManager, (String) i.next());
		}
	}


	/**
	 *  Description of the Method
	 *
	 * @param  systemInterface    Description of the Parameter
	 * @param  repositoryManager  RepositoryManager
	 */
	private static void doSetCollectionsVocabDisplay(RepositoryManager repositoryManager,
			String systemInterface) {
		HashMap sets = repositoryManager.getEnabledSetsHashMap();
		MetadataVocab vocab = repositoryManager.getMetadataVocab();
		ArrayList nodes = vocab.getVocabNodes(systemInterface + "/key", "");
		for (int i = 0; i < nodes.size(); i++) {
			if (sets.get(((VocabNode) nodes.get(i)).getId()) == null) {
				((VocabNode) nodes.get(i)).setNoDisplay(true);
			}
			// If the UI groups file said not to display (dwelanno for example), then don't override that:
			else if (!((VocabNode) nodes.get(i)).getNoDisplayOriginal()) {
				((VocabNode) nodes.get(i)).setNoDisplay(false);
			}
		}
	}


	/**
	 *  Gets the absolute path to a given file or directory. Assumes the path passed in is eithr already absolute
	 *  (has leading slash) or is relative to the context root (no leading slash). If the string passed in does
	 *  not begin with a slash ("/"), then the string is converted. For example, an init parameter to a config
	 *  file might be passed in as "WEB-INF/conf/serverParms.conf" and this method will return the corresponding
	 *  absolute path "/export/devel/tomcat/webapps/myApp/WEB-INF/conf/serverParms.conf." <p>
	 *
	 *  If the string that is passed in already begings with "/", nothing is done. <p>
	 *
	 *  Note: the super.init() method must be called prior to using this method, else a ServletException is
	 *  thrown.
	 *
	 * @param  fname                 An absolute or relative file name or path (relative the the context root).
	 * @return                       The absolute path to the given file or path.
	 * @exception  ServletException  An exception related to this servlet
	 */
	private String getAbsolutePath(String fname)
			 throws ServletException {
		return new File(GeneralServletTools.getAbsolutePath(fname, getServletContext())).getAbsolutePath();
	}


	/**
	 *  Gets the absolute path to a given file or directory. Assumes the path passed in is eithr already absolute
	 *  (has leading slash) or is relative to the context root (no leading slash). If the string passed in does
	 *  not begin with a slash ("/"), then the string is converted. For example, an init parameter to a config
	 *  file might be passed in as "WEB-INF/conf/serverParms.conf" and this method will return the corresponding
	 *  absolute path "/export/devel/tomcat/webapps/myApp/WEB-INF/conf/serverParms.conf." <p>
	 *
	 *  If the string that is passed in already begings with "/", nothing is done. <p>
	 *
	 *  Note: the super.init() method must be called prior to using this method, else a ServletException is
	 *  thrown.
	 *
	 * @param  fname    An absolute or relative file name or path (relative the the context root).
	 * @param  docRoot  The context document root as obtained by calling getServletContext().getRealPath("/");
	 * @return          The absolute path to the given file or path.
	 */
	private String getAbsolutePath(String fname, String docRoot) {
		return new File(GeneralServletTools.getAbsolutePath(fname, docRoot)).getAbsolutePath();
	}


	// -------------------- Logging/debugging methods --------------------


	/**
	 *  Return a string for the current time and date, sutiable for display in log files and output to standout:
	 *
	 * @return    The dateStamp value
	 */
	public static String getDateStamp() {
		return
				new SimpleDateFormat("MMM d, yyyy h:mm:ss a zzz").format(new Date());
	}


	/**
	 *  Output a line of text to error out, with datestamp.
	 *
	 * @param  s  The text that will be output to error out.
	 */
	private final void prtlnErr(String s) {
		System.err.println(getDateStamp() + " DDSServlet Error: " + s);
	}


	/**
	 *  Output a line of text to standard out, with datestamp, if debug is set to true.
	 *
	 * @param  s  The String that will be output.
	 */
	private final void prtln(String s) {
		if (debug) {
			System.out.println(getDateStamp() + " DDSServlet: " + s);
		}
	}


	/**
	 *  Sets the debug attribute of the DDSServlet object
	 *
	 * @param  db  The new debug value
	 */
	public final void setDebug(boolean db) {
		debug = db;
	}
}

