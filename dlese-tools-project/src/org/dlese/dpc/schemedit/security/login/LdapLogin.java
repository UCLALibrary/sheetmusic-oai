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

package org.dlese.dpc.schemedit.security.login;

import java.util.Map;
import java.io.*;
import java.util.*;
import java.security.Principal;
import javax.security.auth.*;
import javax.security.auth.callback.*;
import javax.security.auth.login.*;
import javax.security.auth.spi.*;

import org.dlese.dpc.schemedit.security.auth.nsdl.NSDLLdapClient;
import org.dlese.dpc.schemedit.security.auth.AuthUtils;
import org.dlese.dpc.ldap.LdapException;

// import org.dlese.dpc.schemedit.security.auth.TypedPrincipal;
import org.dlese.dpc.schemedit.security.auth.UserPrincipal;
import org.dlese.dpc.schemedit.security.auth.AuthPrincipal;


/**
* Login Module that authenticates against Ldap (in particular, the NSDL Ldap server).
*/
public class LdapLogin extends SimpleLogin
{
	private static boolean debug = false;
	
	private NSDLLdapClient ldapClient = null;
	
	/**
	* Validate user against using LdapClient
	*/
	protected synchronized Vector validateUser(String username, char password[]) throws LoginException
	{
		prtln ("validateUser() username: " + username);
		boolean auth = false;
		try {
			auth = ldapClient.userAuthenticates(username, new String (password));
		} catch (LdapException ex) {
			System.out.println(ex.getMessage());
			throw new LoginException (ex.getMessage());
		}
		
		if (!auth) {
			prtln ("  validateUser throwing an exception");
			throw new FailedLoginException("Bad password");
		}
		
		Vector principals = new Vector();
		principals.add(new UserPrincipal(username));
		principals.add(new AuthPrincipal(this.getLoginModuleName()));
		prtln ("  ... user is validated");
		return principals;
	}
	
	/**
	* Initialize Ldap client using props file specified in the login config file
	*/
	public void initialize(Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options)
	{
		prtln ("initialize");
		// AuthUtils.showSubject (subject, "on entry to intitialize");
		super.initialize(subject, callbackHandler, sharedState, options);
		
		String propsFile = getOption("propsFile", null);
		if (null == propsFile)
		   throw new Error("A propsFile file must be named in ldapconfig");
		try {
			ldapClient = new NSDLLdapClient (propsFile);
		} catch (LdapException ex) {
			throw new Error ("could not instantiate ldapClient: " + ex.getMessage());
		}
		org.dlese.dpc.schemedit.security.auth.AuthUtils.ldapClient = ldapClient;
		this.sharedState.put ("LdapWasHere", "true");
	}
	
	static void prtln (String s) {
		if (debug)
			System.out.println ("LdapLogin: " + s);
	}
}
