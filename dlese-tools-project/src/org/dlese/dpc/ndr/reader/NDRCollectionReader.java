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

package org.dlese.dpc.ndr.reader;

import org.dlese.dpc.ndr.apiproxy.*;
import org.dlese.dpc.ndr.NdrUtils;
import org.dlese.dpc.ndr.reader.*;
import org.dlese.dpc.ndr.request.*;
import org.dlese.dpc.xml.Dom4jUtils;
import org.dlese.dpc.util.Files;
import org.dlese.dpc.util.strings.FindAndReplace;
import org.dom4j.*;
import java.util.*;
import java.io.File;

/**
 *  A Wrapper object for a NdrCollection as stored in the NDR independent of the NCS (no readers for
 ncs_collect).<p>
 A Composite Reader that assembles 
 information from NDR Object readers and exposes information on the collection level.
 Currently only provides information from
 - Aggregator
 - MetadataProvider

 *
 * @author    Jonathan Ostwald
 */
public class NDRCollectionReader {
	private static boolean debug = true;

	/**  NOT YET DOCUMENTED */
	public String aggHandle = null;
	/**  NOT YET DOCUMENTED */
	public AggregatorReader agg = null;
	/**  NOT YET DOCUMENTED */
	public ServiceDescriptionReader serviceDescription = null;
	/**  NOT YET DOCUMENTED */
	public String title = null;
	/**  NOT YET DOCUMENTED */
	public String description = null;
	/**  NOT YET DOCUMENTED */
	public int resourceCount = -1;

	/**  NOT YET DOCUMENTED */
	public MetadataProviderReader mdp = null;
	/**  NOT YET DOCUMENTED */
	public String mdpHandle = null;
	/**  NOT YET DOCUMENTED */
	public int metadataCount = -1;
	/**  NOT YET DOCUMENTED */
	public String setName = null;
	/**  NOT YET DOCUMENTED */
	public String setSpec = null;


	/**
	 *  Construct a NDRCollectionReader object for the provided Collection Aggregator
	 *
	 * @param  aggHandle      NOT YET DOCUMENTED
	 * @exception  Exception  NOT YET DOCUMENTED
	 */
	public NDRCollectionReader(String aggHandle) throws Exception {
		this.aggHandle = aggHandle;
		try {
			// prtln ("reading aggregator at " + aggHandle);
			agg = new AggregatorReader(aggHandle);
			resourceCount = agg.getMemberCount();
			ServiceDescriptionReader serviceDescription = agg.getServiceDescription();
			if (serviceDescription != null) {
				title = serviceDescription.getTitle();
				description = serviceDescription.getDescription();
			}
		} catch (Throwable t) {
			prtln("couldn't process aggregator (" + aggHandle + "): " + t);
			t.printStackTrace();
			return;
		}

		if (agg == null)
			return;

		try {
			mdpHandle = agg.getMdpHandle();
			if (mdpHandle != null) {
				mdp = new MetadataProviderReader(mdpHandle);
				if (mdp != null) {
					// metadataCount = mdp.getItemHandles().size();
					metadataCount = mdp.getMemberCount();
					setSpec = mdp.getSetSpec();
					setName = mdp.getSetName();
				}
			}
		} catch (Throwable t) {
			prtln("couldn't create NDRCollectionReader: " + t);
			t.printStackTrace();
			return;
		}

	}


	/**
	 *  The main program for the NDRCollectionReader class
	 *
	 * @param  args           The command line arguments
	 * @exception  Exception  NOT YET DOCUMENTED
	 */
	public static void main(String[] args) throws Exception {
	}


	/**
	 *  NOT YET DOCUMENTED
	 *
	 * @param  s  NOT YET DOCUMENTED
	 */
	private static void prtln(String s) {
		String prefix = null;
		if (debug) {
			NdrUtils.prtln(s, prefix);
		}
	}
}

