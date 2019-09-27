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

package org.dlese.dpc.ndr.test;

import java.util.ArrayList;
import java.util.HashMap;

import org.dlese.dpc.ndr.dds.CollectionImporter;

import junit.framework.TestCase;

/**
 * @author kmaull
 *
 */
public class TestCollectionImporter extends TestCase {
	private CollectionImporter importer = null;
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		this.importer = new CollectionImporter();		
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		this.importer = null;
	}

	public void _testImport()
	{
		assertTrue ( importer.importCollection("comet", false) >= 0 ) ;
	}
	
	public void testGetRecordCount() 
	{
		long time = importer.importCollection("globe", true);
		
		System.out.println ( "Total import time = " + time/1000 );
		
		assertTrue (  importer.getRecordCount() >= 0 );
	}
	
	public void _testGetMetadataObjects()
	{
		importer.importCollection("comet", false);		
		
		HashMap<String,ArrayList<String>> map = importer.getMetadataObjects();

		System.out.println ( map.get("CRS-COMET-106-R") );
		
		assertTrue ( importer.getRecordCount() == importer.getMetadataObjects().size() );
	}
	
	public void _testGetCollectionKey() 
	{
		assertNotNull ( importer.getCollectionKeyFromDDS("comet") );
	}
	
	
}
