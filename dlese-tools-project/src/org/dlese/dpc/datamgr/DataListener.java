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

package org.dlese.dpc.datamgr;

import java.util.*;

/**
 * Provides the interface for Objects listening to a DataManager source.
 * <p>
 * @author	Dave Deniman
 * @version	1.0,	9/30/02
 */
public interface DataListener extends EventListener {

    /**
     * Invoked when a DataManager has added new data.
     *
     * @param dataEvent	The data event source
     */
    void dataAdded(DataEvent dataEvent);

    /**
     * Invoked when a DataManager has changed existing data.
     *
     * @param dataEvent	The data event source
     */
    void dataChanged(DataEvent dataEvent);

    /**
     * Invoked when a DataManager has removed data.
     *
     * @param dataEvent	The data event source
     */
    void dataRemoved(DataEvent dataEvent);

}
