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

package org.dlese.dpc.schemedit.standards.action;

import org.dlese.dpc.schemedit.standards.action.form.AsnStandardsForm;
import org.dlese.dpc.schemedit.standards.config.SuggestionServiceManager;
import org.dlese.dpc.standards.asn.util.AsnCatalog;
import org.dlese.dpc.schemedit.threadedservices.AsnFetchService;

import org.dlese.dpc.repository.RepositoryManager;
import org.dlese.dpc.index.reader.*;
import org.dlese.dpc.schemedit.*;
import org.dlese.dpc.schemedit.action.DCSAction;
import org.dlese.dpc.schemedit.repository.RepositoryUtils;
import org.dlese.dpc.schemedit.standards.StandardsManager;
import org.dlese.dpc.schemedit.standards.asn.AsnStandardsManager;
import org.dlese.dpc.schemedit.standards.asn.AsnDocKey;
import org.dlese.dpc.schemedit.dcs.*;
import org.dlese.dpc.schemedit.config.CollectionConfig;
import org.dlese.dpc.schemedit.config.CollectionRegistry;
import org.dlese.dpc.xml.*;
import org.dlese.dpc.xml.schema.*;
import org.dlese.dpc.util.*;
import org.dlese.dpc.util.strings.*;
import org.dlese.dpc.webapps.tools.GeneralServletTools;

import org.dlese.dpc.schemedit.security.user.User;

import org.dom4j.Document;

import java.util.*;
import java.io.*;
import java.net.*;
import java.text.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionServlet;
import org.apache.struts.util.MessageResources;

/**
 *  A Struts Action controlling interaction during editing of workflow status information asociated with
 *  individual metadata records.
 *
 *
 *
 * @author     Jonathan Ostwald
 *
 */
public final class AsnStandardsAction extends DCSAction {

	private static boolean debug = true;

	private AsnCatalog asnCatalog = null;
	private AsnFetchService asnFetchService = null;


	// --------------------------- Public Methods ------------------------------

	/**
	 *  Processes the specified HTTP request and creates the corresponding HTTP response by forwarding to a JSP
	 *  that will create it. A {@link org.dlese.dpc.repository.RepositoryManager} must be available to this class
	 *  via a ServletContext attribute under the key "repositoryManager." Returns an {@link
	 *  org.apache.struts.action.ActionForward} instance which must be configured in struts-config.xml to forward
	 *  to the JSP page that will handle the request.
	 *
	 * @param  mapping               The ActionMapping used to select this instance
	 * @param  request               The HTTP request we are processing
	 * @param  response              The HTTP response we are creating
	 * @param  form                  The ActionForm for the given page
	 * @return                       The ActionForward instance describing where and how control should be
	 *      forwarded
	 * @exception  IOException       if an input/output error occurs
	 * @exception  ServletException  if a servlet exception occurs
	 */
	public ActionForward execute(
	                             ActionMapping mapping,
	                             ActionForm form,
	                             HttpServletRequest request,
	                             HttpServletResponse response)
		 throws IOException, ServletException {
		/*
		 *  Design note:
		 *  Only one instance of this class gets created for the app and shared by
		 *  all threads. To be thread-safe, use only local variables, not instance
		 *  variables (the JVM will handle these properly using the stack). Pass
		 *  all variables via method signatures rather than instance vars.
		 */
		 
		prtln ("execute"); 
		 
		ActionErrors errors = initializeFromContext (mapping, request);
		if (!errors.isEmpty()) {
			saveErrors (request, errors);
			return (mapping.findForward("error.page"));
		}
		AsnStandardsForm asnForm = (AsnStandardsForm) form;
		String errorMsg = "";

		SessionBean sessionBean = this.getSessionBean(request);
		
		asnFetchService = (AsnFetchService) servlet.getServletContext().getAttribute("asnFetchService");
		if (asnFetchService == null)
			prtlnErr ("ERROR: asnFetchService not found in servlet context");
		asnForm.setIsFetching(asnFetchService.getIsProcessing());
		asnForm.setFetchingSession(asnFetchService.getSessionId());
		asnForm.setProgress(null);

		SchemEditUtils.showRequestParameters(request);

		try {
			asnCatalog = AsnCatalog.getInstance();
		} catch (Exception e) {
			prtln ("WARNING: could not get AsnCatalog: " + e.getMessage());
		}

		asnForm.setAsnCatalog(asnCatalog);

		try {
			if (request.getParameter("command") != null) {
				String param = request.getParameter("command");
				if (param.equalsIgnoreCase("all_docs")) {
					return doAllDocs(mapping, form, request, response);
				}
				if (param.equalsIgnoreCase("manage")) {
					return doManageDocs(mapping, form, request, response);
				}
				if (param.equalsIgnoreCase("reload")) {
					return doReload(mapping, form, request, response);
				}

				if (param.equalsIgnoreCase("edit")) {
					return doEdit(mapping, form, request, response);
				}

				if (param.equalsIgnoreCase("update")) {
					return doUpdate(mapping, form, request, response);
				}
				
				if (param.equalsIgnoreCase("stopFetching")) {
					if (asnFetchService.getIsProcessing()) {
						asnFetchService.stopProcessing();
						asnForm.setIsFetching(false);
						errors.add("message", new ActionError("generic.message", "ASN Document fetching has stopped."));
						errors.add("showFetchingMessagingLink", new ActionError("generic.message", ""));
					}
					else {
						errors.add("message", new ActionError("generic.message", "ASN Document fetching completed before stop command was received"));
						errors.add("showFetchingMessagingLink", new ActionError("generic.message", ""));
					}
				}
				
				else if (request.getParameter("progress") != null) {
					// TaskProgress progress = asnFetchService.getTaskProgress();
					// String rpt = progress.getProgressReport();
					String rpt = asnFetchService.getTaskProgress().getProgressReport();
					asnForm.setProgress(rpt);
					return mapping.findForward("progress");
				}
			}

			// asnForm.clear();
/* 			errors.add("error",
				new ActionError("generic.error", "No command submitted - no action taken"));*/
			saveErrors(request, errors); 
			return mapping.findForward("manage.asn.docs");
		} catch (Throwable e) {
			prtln("System Error: " + e);
			String msg = e.getMessage();
			if (msg != null && msg.trim().length() > 0)
				msg = "A system error has occurred (" + msg + ")";
			else
				msg = "An unknown system error has occurred";

			e.printStackTrace();
			
/* 			if (e instanceof NullPointerException)
				e.printStackTrace(); */
			errors.add("actionSetupError",
				new ActionError("generic.error", msg));

			saveErrors(request, errors);
			return mapping.findForward("error.page");
		}
	}

	private ActionForward doFetchAsnDocs (ActionMapping mapping,
	                                ActionForm form,
	                                HttpServletRequest request,
	                                HttpServletResponse response) throws Exception {
		ActionErrors errors = new ActionErrors();
		AsnStandardsForm asnForm = (AsnStandardsForm) form;
		
		saveErrors(request, errors); 
		return mapping.findForward("manage.asn.docs");
	}
	
	/**
	 *  Remove the StatusEntry corresponding to "entryKey" from the DcsDataRecord.
	 *
	 * @param  mapping        NOT YET DOCUMENTED
	 * @param  form           NOT YET DOCUMENTED
	 * @param  request        NOT YET DOCUMENTED
	 * @param  response       NOT YET DOCUMENTED
	 * @return                NOT YET DOCUMENTED
	 * @exception  Exception  NOT YET DOCUMENTED
	 */
	private ActionForward doAllDocs(ActionMapping mapping,
	                                        ActionForm form,
	                                        HttpServletRequest request,
	                                        HttpServletResponse response) throws Exception {
		ActionErrors errors = new ActionErrors();

		AsnStandardsForm asnForm = (AsnStandardsForm) form;

		Map standardsManagers = new HashMap(); // xmlFormt -> standardsManager

		for (Iterator i=this.frameworkRegistry.getItemFormats().iterator();i.hasNext();) {
			String xmlFormat = (String)i.next();
			MetaDataFramework framework = this.frameworkRegistry.getFramework(xmlFormat);
			StandardsManager stdmgr = framework.getStandardsManager();
			if (stdmgr != null && stdmgr instanceof AsnStandardsManager) {
				standardsManagers.put(xmlFormat, (AsnStandardsManager)stdmgr);
			}
		}

		asnForm.setStandardsManagers (standardsManagers);

		return mapping.findForward("all.asn.docs");
	}

	private ActionForward doManageDocs(ActionMapping mapping,
	                                        ActionForm form,
	                                        HttpServletRequest request,
	                                        HttpServletResponse response) throws Exception {
		ActionErrors errors = new ActionErrors();

		AsnStandardsForm asnForm = (AsnStandardsForm) form;

		String xmlFormat = request.getParameter("xmlFormat");
		if (xmlFormat != null && xmlFormat.trim().length() > 0) {
			asnForm.setXmlFormat (xmlFormat);
		}

		asnForm.setManageStandardsBean(new ManageStandardsBean (frameworkRegistry));

		return mapping.findForward("manage.asn.docs");
	}

	private ActionForward doReload(ActionMapping mapping,
	                                        ActionForm form,
	                                        HttpServletRequest request,
	                                        HttpServletResponse response) throws Exception {
		ActionErrors errors = new ActionErrors();

		AsnStandardsForm asnForm = (AsnStandardsForm) form;
		// command=update&xmlFormat=ncs_item
		String xmlFormat = request.getParameter("xmlFormat");
		if (xmlFormat != null && xmlFormat.trim().length() > 0) {
			asnForm.setXmlFormat (xmlFormat);
		}

		MetaDataFramework framework = this.frameworkRegistry.getFramework(xmlFormat);
		SuggestionServiceManager suggestionServiceManager =
			this.frameworkRegistry.getSuggestionServiceManager();
		framework.setStandardsManager(suggestionServiceManager.createStandardsManager(framework));


		asnForm.setManageStandardsBean(new ManageStandardsBean (frameworkRegistry));

		saveErrors(request, errors);
		return mapping.findForward("manage.asn.docs");
	}


	private ActionForward doEdit(ActionMapping mapping,
	                                        ActionForm form,
	                                        HttpServletRequest request,
	                                        HttpServletResponse response) throws Exception {
		ActionErrors errors = new ActionErrors();

		AsnStandardsForm asnForm = (AsnStandardsForm) form;
		// command=update&xmlFormat=ncs_item
		String subject = request.getParameter("subject");
		if (subject != null && subject.trim().length() > 0) {
			asnForm.setSubject (subject);
		}
		else {
			errors.add("error",
				new ActionError("generic.error", "subject required"));
		}

		String xmlFormat = request.getParameter("xmlFormat");
		if (xmlFormat != null && xmlFormat.trim().length() > 0) {
			asnForm.setXmlFormat (xmlFormat);
		}
		else {
			errors.add("error",
				new ActionError("generic.error", "xmFormat required"));
		}

		if (!errors.isEmpty()) {
			saveErrors(request, errors);
			return mapping.findForward("edit.asn.docs");
		}

		// load the StandardsManagerBean for this format
/* 		MetaDataFramework framework = this.frameworkRegistry.getFramework(xmlFormat);
		StandardsManager stdmgr = framework.getStandardsManager();
		StandardsManagerBean smb = null;
		if (stdmgr != null && stdmgr instanceof AsnStandardsManager) {
			smb = new StandardsManagerBean ((AsnStandardsManager)stdmgr);
		}
		else {
			errors.add("error",
				new ActionError("generic.error", "standardsManager not found"));
		}

		asnForm.setStandardsManagerBean (smb);
		asnForm.setManageStandardsBean(new ManageStandardsBean (frameworkRegistry));
 */
 		this.updateStandardsBeans(xmlFormat, asnForm);
		errors.add("msg",
				new ActionError("generic.error", "Test Message"));
		saveErrors(request, errors);
		return mapping.findForward("edit.asn.docs");
	}

	private void updateStandardsBeans (String xmlFormat, AsnStandardsForm asnForm) {
				// load the StandardsManagerBean for this format
		MetaDataFramework framework = this.frameworkRegistry.getFramework(xmlFormat);
		StandardsManager stdmgr = framework.getStandardsManager();
		StandardsManagerBean smb = null;
		if (stdmgr != null && stdmgr instanceof AsnStandardsManager) {
			smb = new StandardsManagerBean ((AsnStandardsManager)stdmgr);
		}

		asnForm.setStandardsManagerBean (smb);
		asnForm.setManageStandardsBean(new ManageStandardsBean (frameworkRegistry));
	}

	private ActionForward doUpdate(ActionMapping mapping,
	                                        ActionForm form,
	                                        HttpServletRequest request,
	                                        HttpServletResponse response) throws Exception {
		ActionErrors errors = new ActionErrors();

		AsnStandardsForm asnForm = (AsnStandardsForm) form;
		// command=update&xmlFormat=ncs_item
		String subject = request.getParameter("subject");
		if (subject != null && subject.trim().length() > 0) {
			asnForm.setSubject (subject);
		}
		else {
			errors.add("error",
				new ActionError("generic.error", "subject required"));
		}

		String xmlFormat = request.getParameter("xmlFormat");
		if (xmlFormat != null && xmlFormat.trim().length() > 0) {
			asnForm.setXmlFormat (xmlFormat);
		}
		else {
			errors.add("error",
				new ActionError("generic.error", "xmFormat required"));
		}

		if (!errors.isEmpty()) {
			saveErrors(request, errors);
			return mapping.findForward("edit.asn.docs");
		}

		String[] keys = request.getParameterValues("keys");
		if (keys == null) {
			// prtln ("KEYS is NULL");
			keys = new String[]{};
		}
		else {
/* 			prtln ("KEYS");
			for (int i=0;i<keys.length;i++)
				prtln (" - " + keys[i]); */
		}

		// UPDATE StandardsManager ....
		MetaDataFramework framework = this.frameworkRegistry.getFramework(xmlFormat);
		StandardsManager stdmgr = framework.getStandardsManager();
		if (stdmgr != null) {
			AsnStandardsManager asnStdMgr = (AsnStandardsManager)stdmgr;
			// prtln ("\ncalling setAvailableDocs with " + keys.length + " keys");
			asnStdMgr.setAvailableDocs(Arrays.asList(keys));
			
			// asnFetchService needs a list of docIds!
			List docIds = new ArrayList();
			for (int i=0;i<keys.length;i++) {
				AsnDocKey docKey = AsnDocKey.makeAsnDocKey (keys[i]);
				docIds.add (docKey.getAsnId());
				// prtln (" - " + docKey.getAsnId());
			}
			this.asnFetchService.fetchStandardsDocs(docIds);
			
		}
		SuggestionServiceManager suggestionServiceManager =
			frameworkRegistry.getSuggestionServiceManager();
		suggestionServiceManager.flush();
		prtln ("flushed service configuration to save " + xmlFormat + " update");

		this.updateStandardsBeans(xmlFormat, asnForm);

		saveErrors(request, errors);
		return mapping.findForward("manage.asn.docs");
	}



	// -------------- Debug ------------------


	/**
	 *  Sets the debug attribute of the AsnStandardsAction class
	 *
	 * @param  isDebugOutput  The new debug value
	 */
	public static void setDebug(boolean isDebugOutput) {
		debug = isDebugOutput;
	}


	/**
	 *  Print a line to standard out.
	 *
	 * @param  s  The String to print.
	 */
	private void prtln(String s) {
		if (debug)
			System.out.println("AsnStandardsAction: " + s);
	}
	
	private void prtlnErr(String s) {
		System.out.println("AsnStandardsAction: " + s);
	}
	
}
