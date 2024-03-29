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

package org.dlese.dpc.schemedit.security.auth;

import java.io.*;
import java.security.Principal;

/**
	A principle for denoting which login module authenticated.
 */
public class AuthPrincipal extends TypedPrincipal implements Principal, Serializable
{
	public AuthPrincipal(String name, int type) {
		super (name, type);
	}

	/**
	 * Create a AuthPrincipal with a name.
	 *
	 * <p>
	 *
	 * @param name the name for this Principal.
	 * @exception NullPointerException if the <code>name</code>
	 * 			is <code>null</code>.
	 */
	 public AuthPrincipal(String name) {
		this(name, AUTH);
	}


	/**
	 * Create a AuthPrincipal with a blank name.
	 *
	 * <p>
	 *
	 */
	public AuthPrincipal()
	{
		this("", AUTH);
	}

}
