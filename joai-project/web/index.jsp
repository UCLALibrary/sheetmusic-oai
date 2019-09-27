<%@ include file="TagLibIncludes.jsp" %>

<%@ include file="baseUrl.jsp" %>



<html>

<head>

<title>jOAI Overview</title>

<c:import url="head.jsp"/>

</head>

<body>

<c:import url="top.jsp?sec=overview"/>

<!-- content begins-->

<h1>Overview</h1>

<h3>OAI defined </h3>

<p>jOAI is a Java-based OAI software that supports the Open Archives Initiative Protocol for Metadata Harvesting (OAI-PMH), version 2.0. The  protocol establishes methods for sharing individual metadata files that are XML  documents. The software contains a data provider that makes XML metadata files available  to others and a data harvester that can get XML metadata files from others.</p>

<h3>Data Provider</h3>

<p>Use the data provider to make metadata files available to others via the OAI protocol.  <a href="<c:url value='/docs/provider.jsp#provsetup'/>">Set up the Provider</a>. If only harvesting  metadata files,  provider setup is not necessary. </p>

<p>See the <a href="<c:url value='/docs/provider.jsp'/>">Data provider documentation</a> for detailed information about the jOAI data provider. </p>

<p>To harvest from this data provider, use this <nobr>baseURL: ${myBaseUrl}</nobr></p>

<h3>Harvester</h3>

<p>Use the harvester to get metadata files from OAI data providers.  <a href="<c:url value='/docs/harvester.jsp#how'/>">Set up the Harvester</a>. If only  providing metadata files, harvester setup is not necessary. </p>

<p>See the <a href="<c:url value='/docs/harvester.jsp'/>">Harvester documentation</a> for detailed information about the jOAI harvester. </p>

<h3>Search</h3>

<p>Use the <a href="<c:url value='/search.jsp'/>">Search</a> page to view and find items in the data provider. Free-text searches operate over the full text of all metadata files. Searches may be limited by set and/or keyword terms.</p>

<p>See the <a href="<c:url value='/docs/odlsearch.do'/>">ODL Search  documentation</a> for detailed information about the search functionality available in jOAI. </p>

<h3>jOAI system requirements</h3>

<p>The jOAI software runs on Windows, Mac, Linux and Unix platforms. The following  components are needed to operate the software:</p>

<ul>

  <li><a href="http://www.dlese.org/dds/services/joai_software.jsp">oai.war</a> - The jOAI software</li>

  <li><a href="http://tomcat.apache.org/">Apache Tomcat</a> servlet container, version 5.5.x or 6.x</li>

  <li><a href="http://java.sun.com/javase/">Java Standard Edition (SE)</a> version 5 or 6</li>

</ul>

Version @VERSION@ of the jOAI software has been tested with Java 5 and 6 and Tomcat version 5.5.x and 6.x on Linux and Windows XP. 

<h3>Downloading jOAI</h3>

<p>Download the software from the <a href="http://www.dlese.org/dds/services/joai_software.jsp">jOAI download page</a> at DLESE. </p>

<h3>Installing jOAI</h3>

<p>See the <a href='<c:url value="/docs/INSTALL.txt"/>'>installation instructions</a> for information about installing jOAI. </p>

<h3>Configuring jOAI</h3>

<p>See  <a href="<c:url value='/docs/configuring_joai.jsp'/>">Configuring jOAI</a> for  information about optional configuration options jOAI. </p>

<h3>Version and licensing</h3>

<p>See <a href="docs/about.jsp">About jOAI</a> for version and licensing information.</p>

<h3>Source Code</h3>

<p>See the <a href="<c:url value='/docs/BUILD_INSTRUCTIONS.txt'/>">build instructions</a> for detailed infromation about obtaining the source code and building jOAI using Ant.</p>

<h3>Support</h3>

<p>
	The <a href="http://sourceforge.net/projects/dlsciences/forums/forum/1138932">jOAI User Forum</a> at SourceForge 
	is a place where members of the jOAI community can seek advice, provide tips, share experiences, report bugs, and interact with the developers and other users of the tool. 
	Inquiries may also be sent via e-mail to <a href="mailto:support@dlese.org">support@dlese.org</a>.
</p>

<p>See also the <a href='<c:url value="/docs/"/>'>jOAI documentation pages</a> for information about using the software.</p>

<p>&nbsp;</p>

<p>&nbsp;</p>

<p>&nbsp;</p>

<p>&nbsp;</p>

<p><!-- content ends-->

</p>



<c:import url="bottom.jsp"/>

</body>

</html>



