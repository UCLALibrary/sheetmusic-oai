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

package org.dlese.dpc.schemedit.repository;

import org.dlese.dpc.schemedit.dcs.DcsDataRecord;
import org.dlese.dpc.schemedit.config.CollectionConfig;

import javax.servlet.ServletContext;


/**
 *  Methods to create, copy, and put Records to the Repository<p>
 *
 *  NOTE: currently, this class implements a repositoryWriter plugin for the
 *  NDR. When we understand what the plugin INTERFACE should be, then the
 *  interface will be in this package, and the NDR implementation will be
 *  elsewhere...
 *
 * @author     ostwald <p>
 *
 *      $Id: RepositoryWriterPlugin.java,v 1.10 2009/03/20 23:33:57 jweather Exp $
 * @version    $Id: RepositoryWriterPlugin.java,v 1.5 2007/07/30 18:06:15
 *      ostwald Exp $
 */
public interface RepositoryWriterPlugin {

	// private ServletContext servletContext = null;

	/**
	 *  Constructor for the RepositoryWriterPlugin object
	 *
	 * @param  recId                                NOT YET DOCUMENTED
	 * @param  recordXml                            NOT YET DOCUMENTED
	 * @param  xmlFormat                            NOT YET DOCUMENTED
	 * @param  dcsDataRecord                        NOT YET DOCUMENTED
	 * @exception  RepositoryWriterPluginException  NOT YET DOCUMENTED
	 */
	// public RepositoryWriterPlugin (ServletContext servletContext);

	/**
	 *  Writes a metadata record to the NDR, with pecial handling for records of
	 *  ncs_collect format. For ncs_collect records (the collection management
	 *  format), we check to ensure the record is "FinalStatus" and valid, and if
	 *  so we update the entire collection definition in the NDR.
	 *
	 * @param  recId                                metadata record Id
	 * @param  dcsDataRecord                        dcsData for the record to be
	 *      written
	 * @param  recordXml                            metadata as an xml String
	 * @param  xmlFormat                            format of metadata record
	 * @exception  RepositoryWriterPluginException  NOT YET DOCUMENTED
	 */
	public void putRecord(String recId, String recordXml, String xmlFormat, DcsDataRecord dcsDataRecord)
		 throws RepositoryWriterPluginException;


	/**
	 *  NOT YET DOCUMENTED
	 *
	 * @param  id                                   NOT YET DOCUMENTED
	 * @param  collectionConfig                     NOT YET DOCUMENTED
	 * @param  dcsDataRecord                        NOT YET DOCUMENTED
	 * @exception  RepositoryWriterPluginException  NOT YET DOCUMENTED
	 */
	public void putCollectionData(String id, CollectionConfig collectionConfig, DcsDataRecord dcsDataRecord) 
		throws RepositoryWriterPluginException;


	/**
	 *  NOT YET DOCUMENTED
	 *
	 * @param  recId                                NOT YET DOCUMENTED
	 * @param  dcsDataRecord                        NOT YET DOCUMENTED
	 * @exception  RepositoryWriterPluginException  NOT YET DOCUMENTED
	 */
	public void deleteRecord(String recId, DcsDataRecord dcsDataRecord) 
		throws RepositoryWriterPluginException;

}

