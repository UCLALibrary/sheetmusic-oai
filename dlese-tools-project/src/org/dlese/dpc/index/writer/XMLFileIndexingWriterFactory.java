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

package org.dlese.dpc.index.writer;

import java.util.*;
import org.dlese.dpc.repository.*;
import org.dlese.dpc.index.*;
import org.dlese.dpc.index.writer.xml.*;

/**
 *  Factory used to create the XmlFileIndexingWriter appropriate for handling a given XML format.
 *
 * @author     John Weatherley
 * @see        XMLFileIndexingWriter
 */
public class XMLFileIndexingWriterFactory {

	private RecordDataService recordDataService = null;
	private SimpleLuceneIndex index = null;
	private XMLIndexerFieldsConfig xmlIndexerFieldsConfig = null;

	/**  A HashMap of FileIndexingService Classes */
	public static HashMap indexerClasses = null;

	// Register new XMLFileIndexingWriters here:
	private final static Object[][] INDEXING_WRITER_CLASSES = {
		{"default_handler_class", SimpleXMLFileIndexingWriter.class},
		{"adn", ADNFileIndexingWriter.class},
		{"dlese_ims", DleseIMSFileIndexingWriter.class},
		{"dlese_anno", DleseAnnoFileIndexingServiceWriter.class},
		{"dlese_collect", DleseCollectionFileIndexingWriter.class},
		{"news_opps", NewsOppsFileIndexingWriter.class},
		{"ncs_collect", NCSCollectionFileIndexingWriter.class}
		};

	// Initialize the HashMap of index writer classes
	static {
		indexerClasses = new HashMap(INDEXING_WRITER_CLASSES.length);
		for (int i = 0; i < INDEXING_WRITER_CLASSES.length; i++)
			indexerClasses.put(INDEXING_WRITER_CLASSES[i][0], INDEXING_WRITER_CLASSES[i][1]);
	}


	/**
	 *  Constructor for use when no RecordDataService is needed.
	 *
	 * @param  index  The index being used
	 */
	public XMLFileIndexingWriterFactory(SimpleLuceneIndex simpleLuceneIndex, XMLIndexerFieldsConfig xmlIndexerFieldsConfig) { 
		index = simpleLuceneIndex;
		this.xmlIndexerFieldsConfig = xmlIndexerFieldsConfig;
	}


	/**
	 *  Constructor for use when a RecordDataService is needed.
	 *
	 * @param  rds                The RecordDataService being used, or null if none needed.
	 * @param  simpleLuceneIndex  The index being used
	 */
	public XMLFileIndexingWriterFactory(RecordDataService rds, SimpleLuceneIndex simpleLuceneIndex, XMLIndexerFieldsConfig xmlIndexerFieldsConfig) {
		index = simpleLuceneIndex;
		recordDataService = rds;
		this.xmlIndexerFieldsConfig = xmlIndexerFieldsConfig;
	}


	/**
	 *  Gets the XML indexingWriter appropriate for indexing the given xml format.
	 *
	 * @param  collection     The collection key, for example dcc, comet, etc.
	 * @param  xmlFormat      The xml format specifier, for example adn, news_opps, dlese_collect.
	 * @return                The indexingWriter value
	 * @exception  Exception  If error creating the writer
	 */
	public XMLFileIndexingWriter getIndexingWriter(String collection, String xmlFormat)
		 throws Exception {
			 
		//System.out.println("getIndexingWriter()");
		
		Class writerClass = (Class) indexerClasses.get(xmlFormat);
		if (writerClass == null)
			writerClass = (Class) indexerClasses.get("default_handler_class");

		XMLFileIndexingWriter xw = (XMLFileIndexingWriter) writerClass.newInstance();
		HashMap writerConfigAttributes = new HashMap(5);
		//System.out.println("collection: " + collection + " xmlFormat: " + xmlFormat);
		writerConfigAttributes.put("collection", collection);
		writerConfigAttributes.put("xmlFormat", xmlFormat);
		if(recordDataService != null)
			writerConfigAttributes.put("recordDataService", recordDataService);
		writerConfigAttributes.put("index", index);
		if(xmlIndexerFieldsConfig != null) {
			writerConfigAttributes.put("xmlIndexerFieldsConfig", xmlIndexerFieldsConfig);
			//System.out.println("xmlIndexerFieldsConfig NOT null!");
		}
		/* else
			System.out.println("xmlIndexerFieldsConfig is null!"); */
		xw.setConfigAttributes(writerConfigAttributes);
		return xw;
	}
}

