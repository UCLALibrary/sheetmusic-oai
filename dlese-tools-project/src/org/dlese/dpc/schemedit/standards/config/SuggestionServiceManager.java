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
package org.dlese.dpc.schemedit.standards.config;

import org.dlese.dpc.schemedit.standards.StandardsManager;
import org.dlese.dpc.schemedit.standards.adn.DleseStandardsManager;
import org.dlese.dpc.schemedit.standards.asn.*;
import org.dlese.dpc.schemedit.standards.td.TeachersDomainStandardsManager;
import org.dlese.dpc.schemedit.config.AbstractConfigReader;
import org.dlese.dpc.xml.schema.*;
import org.dlese.dpc.schemedit.*;
import org.dlese.dpc.xml.Dom4jUtils;

import java.io.*;
import java.util.*;

import java.net.*;

import org.dom4j.*;

/**
 *  Reads a SuggestionService configuration file and provides access to the
 *  individual configurations (each corresponding to a MetaDataFramework). The
 *  schema for the SuggestionService file is at<br/>
 *  http://www.dls.ucar.edu/people/ostwald/Metadata/standardsService/standardsServiceConfig.xsd
 *  <p>
 *
 *  createStandardsManager method is a factory that instantiates a
 *  StandardsManager instance for a particular framework.<p>
 *
 *  SuggestionServiceManager is instantiated at startup by {@link
 *  org.dlese.dpc.schemedit.SetupServlet} and placed in the servletContext.
 *
 *@author    ostwald
 */
public class SuggestionServiceManager extends AbstractConfigReader {

	/**
	 *  NOT YET DOCUMENTED
	 */
	protected static boolean debug = true;
	private Map configMap = null;


	/**
	 *  Constructor for the SuggestionServiceManager object, which reads a
	 *  configuration file and creates SuggestionServiceConfig instances for each
	 *  configured framework. The configs for the various frameworks are stored in a map,
	 keyed by the xmlFormat.
	 *
	 *@param  source         NOT YET DOCUMENTED
	 *@exception  Exception  NOT YET DOCUMENTED
	 */
	public SuggestionServiceManager(File source) throws Exception {
		super(source);
		prtln ("reading standards config file at: " + source);
		configMap = new HashMap();
		for (Iterator i = getNodes("/SuggestionServiceConfig/config").iterator(); i.hasNext(); ) {
			Element config = (Element) i.next();
			Element format = config.element("xmlFormat");
			configMap.put(format.getTextTrim(), new SuggestionServiceConfig(config));
		}
	}

	/*
		// Add configured converters:
		Enumeration enumeration = servletContext.getInitParameterNames();
		String param;
		Map pluginMap = new HashMap();
		while (enumeration.hasMoreElements()) {
			param = (String) enumeration.nextElement();
			if (param.toLowerCase().startsWith("asnFrameworkPlugin")) {
				String splits = servletContext.getInitParameter(param).split("|");
				String xmlFormat = splits[0].trim();
				String pluginClassName = splits[1].trim();
				pluginMap.put (xmlFormat, pluginClassName);
			}
		}
		return pluginMap;
	*/
	

	/**
	 *  StandardsManager factory that uses a MetaDataFramework and a
	 *  SuggestionServiceConfig instance to create the appropriate instance.
	 *
	 *@param  framework      NOT YET DOCUMENTED
	 *@return                NOT YET DOCUMENTED
	 *@exception  Exception  NOT YET DOCUMENTED
	 */
	public StandardsManager createStandardsManager(MetaDataFramework framework)
			 throws Exception {

		prtln("\nCreating standards manager for " + framework.getXmlFormat());
		StandardsManager standardsManager = null;
		String xmlFormat = framework.getXmlFormat();

		SuggestionServiceConfig config = this.getConfig(framework.getXmlFormat());
		if (config == null) {
			return null;
		}

		String configVersion = config.getVersion();
		String frameworkVersion = framework.getVersion();
		if (configVersion.length() > 0 && !configVersion.equals(frameworkVersion)) {
			prtln("service for " + xmlFormat + " requires version " + configVersion +
					" - framework version is " + frameworkVersion);
			return null;
		}

		String sourceType = config.getStandardSourceType();
		String xpath = config.getXpath();

		if (sourceType == null || sourceType.trim().length() == 0 || 
			sourceType.equals(SuggestionServiceConfig.ASN)) {
				
			String defaultDocKey = config.getDefaultDocKey();
			standardsManager = new AsnStandardsManager(config, defaultDocKey);
		}
		
		else if (sourceType.equals(SuggestionServiceConfig.DATA_TYPES)) {
			
			standardsManager = new DleseStandardsManager(xpath,
					config.getDataTypes(),
					framework.getSchemaHelper());
 		} else if (xmlFormat.equals("res_qual")) {
			String standardsFilePath = (String) config.getStandardsFile();
			File standardsFile = new File(standardsFilePath);
			standardsManager = new ResQualStandardsManager(xmlFormat, xpath, standardsFile);

			// catch old-style configs
		} else if (sourceType.equals(SuggestionServiceConfig.STANDARDS_FILE)) {
			throw new Exception ("Depreciated config source type: " + sourceType);
		} else if (sourceType.equals(SuggestionServiceConfig.STANDARDS_DIRECTORY)) {

			throw new Exception ("Depreciated config source type: " + sourceType);
		} else {
			throw new Exception("Unrecognized sourceType: " + sourceType);
		}
		return standardsManager;
	}


	/**
	 *  Returns true if the specified framework is configured.
	 *
	 *@param  xmlFormat  NOT YET DOCUMENTED
	 *@return            NOT YET DOCUMENTED
	 */
	public boolean hasConfig(String xmlFormat) {
		return this.getKeys().contains(xmlFormat);
	}


	/**
	 *  Gets the xml_formats corresponding to the configured frameworks
	 *
	 *@return    The keys value
	 */
	public Set getKeys() {
		return this.configMap.keySet();
	}


	/**
	 *  Gets the configuration for the specified xmlFormat (framework)
	 *
	 *@param  xmlFormat  NOT YET DOCUMENTED
	 *@return            The config value
	 */
	public SuggestionServiceConfig getConfig(String xmlFormat) {
		return (SuggestionServiceConfig) configMap.get(xmlFormat);
	}

 	public void flush () throws Exception {

		Element root = DocumentHelper.createElement("SuggestionServiceConfig");
		Document doc = DocumentHelper.createDocument (root);
		for (Iterator i=this.configMap.values().iterator();i.hasNext();) {
			SuggestionServiceConfig config = (SuggestionServiceConfig)i.next();
			// prtln (Dom4jUtils.prettyPrint(config.getElement()));
			
			root.add (config.getElement().createCopy());
		}
		Dom4jUtils.writePrettyDocToFile(doc, source);
		this.refresh();
	}

	/**
	 *  The main program for the SuggestionServiceManager class
	 *
	 *@param  args  The command line arguments
	 */
	public static void main(String[] args) {
		org.dlese.dpc.schemedit.test.TesterUtils.setSystemProps();
		// String path = "C:/Program Files/Apache Software Foundation/Tomcat 5.5/var/dcs_conf/suggestionServiceConfig.xml";
		String path = "/Users/ostwald/devel/dcs-repos/tiny/dcs_conf/suggestionServiceConfig.xml";
		SuggestionServiceManager manager = null;
		try {
			manager = new SuggestionServiceManager(new File(path));

			for (Iterator i = manager.configMap.keySet().iterator(); i.hasNext(); ) {
				String xmlFormat = (String) i.next();
				SuggestionServiceConfig config = manager.getConfig(xmlFormat);
				prtln("\nxmlFormat: " + xmlFormat);
				prtln("version: " + config.getVersion());
				prtln("xpath: " + config.getXpath());
				String sourceType = config.getStandardSourceType();
				prtln("sourceType: " + sourceType);
				if (sourceType.equals(config.DATA_TYPES)) {
					prtln("\tdataTypes:");
					for (Iterator dt = config.getDataTypes().iterator(); dt.hasNext(); ) {
						prtln("\t\t" + (String) dt.next());
					}
				}
				if (sourceType.equals(config.STANDARDS_FILE)) {
					prtln("\tstandards file:");
					prtln("\t\t" + config.getStandardsFile());
				}
				if (sourceType.equals(config.STANDARDS_DIRECTORY)) {
					prtln("\tstandards directory: " + config.getStandardsDirectory());
					// prtln("\tdefault: " + config.getDefaultDoc());
					prtln("\tdefaultKEY: " + config.getDefaultDocKey());
				}
				if (sourceType.equals(config.ASN)) {
					prtln("\tdefaultKEY: " + config.getDefaultDocKey());
					prtln("asnDocs:");
					for (Iterator dt = config.getAsnDocKeys().iterator(); dt.hasNext(); ) {
						prtln("\t\t" + (String) dt.next());
					}
				}
			}
		} catch (Exception e) {
			prtln("ERROR: " + e.getMessage());
			e.printStackTrace();
		}
		
	}


	/**
	 *  NOT YET DOCUMENTED
	 */
	public void report() {
		prtln("\nsuggestion service configs");
		for (Iterator i = this.getKeys().iterator(); i.hasNext(); ) {
			String key = (String) i.next();
			SuggestionServiceConfig config = this.getConfig(key);
			// prtln("\t" + key + ": " + config.getPluginClass());
		}
	}


	/**
	 *  Print a line to standard out.
	 *
	 *@param  s  The String to print.
	 */
	protected static void prtln(String s) {
		if (debug) {
			// SchemEditUtils.prtln(s, "AbstractConfigReader");
			SchemEditUtils.prtln(s, "");
		}
	}

}

