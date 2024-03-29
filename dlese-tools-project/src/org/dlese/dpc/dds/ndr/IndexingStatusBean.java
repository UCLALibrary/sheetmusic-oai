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
 *  Copyright 2002-2011 by Digital Learning Sciences, University Corporation for Atmospheric Research (UCAR).
 *  All rights reserved.
 */
package org.dlese.dpc.dds.ndr;

import java.io.*;
import java.util.*;

/**
 *  Bean that holds info about the indexing status.
 *
 * @author    John Weatherley
 */
public class IndexingStatusBean implements Serializable {
	
	public static final int PROGRESS_STATUS_INDEXING_NOT_STARTED = 0;
	public static final int PROGRESS_STATUS_INDEXING_UNDERWAY = 1;
	public static final int PROGRESS_STATUS_INDEXING_COMPLETE = 2;	
	
	private ArrayList completedCollections = null;
	private Date statusLastModifiedDate =  null;	
	private int progressStatus = PROGRESS_STATUS_INDEXING_NOT_STARTED;	
	
	/**
	 * Returns the value of progressStatus.
	 */
	public int getProgressStatus()
	{
		return progressStatus;
	}

	/**
	 * Sets the value of progressStatus.
	 * @param progressStatus The value to assign progressStatus.
	 */
	public void setProgressStatus(int progressStatus)
	{
		this.progressStatus = progressStatus;
		statusLastModifiedDate = new Date();
		
		// Clear the completed collections to indicate none have been complted for the next indexing session...
		if(progressStatus == PROGRESS_STATUS_INDEXING_COMPLETE)
			completedCollections = new ArrayList();
	}

		
	/**
	 * Returns the value of statusLastModifiedDate.
	 */
	public Date getStatusLastModifiedDate()
	{
		return statusLastModifiedDate;
	}

	/**
	 * Sets the value of statusLastModifiedDate.
	 * @param statusLastModifiedDate The value to assign statusLastModifiedDate.
	 */
	public void setStatusLastModifiedDate(Date statusLastModifiedDate)
	{
		this.statusLastModifiedDate = statusLastModifiedDate;
	}
	
	/**
	 * Returns the value of completedCollections.
	 */
	public ArrayList getCompletedCollections()
	{
		return completedCollections;
	}

	/**
	 * Sets the value of completedCollections.
	 * @param completedCollections The value to assign completedCollections.
	 */
	public void setCompletedCollections(ArrayList completedCollections)
	{
		this.completedCollections = completedCollections;
	}
	
	public void addCompletedCollection(String collectionKey) {
		if(completedCollections == null)
			completedCollections = new ArrayList();
		if(!completedCollections.contains(collectionKey))
			completedCollections.add(collectionKey);
		statusLastModifiedDate = new Date();
		
		// Indicate status that collection indexing is underway:
		progressStatus = PROGRESS_STATUS_INDEXING_UNDERWAY;
	}
	
	public boolean hasCompletedCollection(String collectionKey) {
		if(completedCollections == null)
			return false;
		return completedCollections.contains(collectionKey);
	}

	/**  Constructor for the IndexingStatusBean object */
	public IndexingStatusBean() { }


	public String toString() {
		if(completedCollections == null || completedCollections.size() == 0)
			return "Status:" + progressStatus + " No collections have been indexed.";
		else
			return "Status:" + progressStatus + " These collections have been indexed: " + Arrays.toString(completedCollections.toArray());
	}

}

