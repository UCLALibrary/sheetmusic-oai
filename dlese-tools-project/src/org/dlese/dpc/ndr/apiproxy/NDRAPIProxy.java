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

package org.dlese.dpc.ndr.apiproxy;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.dlese.dpc.ndr.connection.NDRConnection;

/**
 * The proxy object represents the concrete implementation 
 * of the NDRAPI.  
 * 
 * 
 * @author kmaull
 *
 */
public class NDRAPIProxy {
	
	static public boolean DEBUG_ON 		= true;
	static public boolean OFFLINE_TEST 	= false;
	 
	private NDRConnection connection = null;
	
	public NDRAPIProxy(){}
	
	/**
	 * Constructor based on the service ( FIND, GET, etc. ).
	 * 
	 * @param _service - the service ( usually the endpoint, FIND, GET, etc. )
	 */
	public NDRAPIProxy ( Service _service ) {
		try {
		 this.connection = new NDRConnection ( NDRConstants.getNdrApiBaseUrl() + "/" + _service.getTag() );
		} catch ( Exception e ) {
			e.printStackTrace();
		}
	}
	
	public NDRAPIProxy ( String verb, String handle) {
		String path = verb;
		if ( handle != null && handle.trim().length() > 0 )
			path = path +  "/" + handle;
		try {
			 this.connection = new NDRConnection ( NDRConstants.getNdrApiBaseUrl() + "/" + path );
			} catch ( Exception e ) {
				e.printStackTrace();
			}
		}
		
	public enum Service {
		/** TYPES and their XML tags **/
		FIND 			("find"),
		GET 			("get");
		
		/** ENUM data and access methods **/
		String serviceEndpoint;
		
		Service ( String _tag )
		{
			this.serviceEndpoint = _tag;
		}
		
		public String getTag () { return this.serviceEndpoint; }
	};
	
	/**
	 * Get the agent handle registered for this proxy object.
	 * 
	 * @return The agent handle for this proxy object
	 */
	public String getAgentHandle ()
	{
		try {
			return new NDRConnection(null).getAgentHandle();
		} catch ( Exception e ) {
			e.printStackTrace();
		}
		
		return null;
	}
	

	/**
	 * Make the request to the NDR for executing the contents of the
	 * XML String representing an InputXML object. This method added to allow
	 * requests to be submitted with arbitrary XML (rather than actually created
	 * from in InfoXML object.
	 * 
	 * @param _input - the inputXML object to execute
	 * @return
	 */
	public InfoXML requestExec ( String _input ) throws Exception
	{
		InfoXML returnXML = null;
		
		// for debugging 
		if ( DEBUG_ON ) {
			prtln ("DEBUG_ON");
			System.out.println ( _input );
		}
		//
		
		if (_input != null && _input.trim().length() > 0)
		
			this.connection.setContent( "inputXML="+_input );				
		this.connection.setCanonicalHeader( true );

		return new InfoXML( this.connection.request() );
	}
	
	/*** SEARCH API ***/
	public String searchDC ( int _numResults, int _startPos, String _query )
	{
		String returnXML = "";
		
		try {
			this.connection = new NDRConnection(NDRConstants.getNdrApiBaseUrl() + "/search/");		
			
			this.connection.setCanonicalHeader(false);
			
			String requestString = "search?n=" + _numResults + "&s=" + _startPos + "&q=" + _query;
			
			returnXML = this.connection.requestGET(requestString);
			
		} catch ( Exception e ) {
			// TODO : handle this exception
			e.printStackTrace();
		}
		
		return returnXML;
	}
	
	/*** ADDITIONAL METHODS ***/
	public String getConnectionHandle()
	{		
		return connection == null ? null : connection.getAgentHandle();
	}

	static void prtln (String s) {
		org.dlese.dpc.ndr.NdrUtils.prtln (s, "NDRAPIProxy");
	}

}
