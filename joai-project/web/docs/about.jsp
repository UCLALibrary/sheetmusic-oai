<%@ page contentType="text/html; charset=UTF-8" %>

<%@ taglib prefix='dds' uri='http://www.dlese.org/dpc/dds/tags' %>

<%@ taglib prefix="x" uri="http://java.sun.com/jsp/jstl/xml" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ page isELIgnored ="false" %>



<html>



<head>

<title>About jOAI</title>

<c:import url="../head.jsp"/></head>



<body>

<c:import url="../top.jsp?sec=doc"/>



<h1>About jOAI </h1>

<p align="justify">The jOAI software is developed by the 

<a href="http://www.ucar.edu/">University Corporation for Atmospheric Research</a> (UCAR), 

<a href="http://www.dlsciences.org/">Digital Learning Sciences</a> (DLS).

</p>



<p>The software supports the <a href="http://www.openarchives.org/">Open Archives Initiative</a>

Protocol for Metadata Harvesting (OAI-PMH) version 2.0. The harvester portion of this software is also compatible with OAI-PMH version 1.1.</p>



<p>jOAI uses the <a href="http://lucene.apache.org/">Apache Lucene</a> search API

and the <a href="http://struts.apache.org/">Apache Struts</a> Web application framework, 

as well as other open source software. See a partial <a href="<c:url value='/docs/licenses'/>">listing of licenses</a>.

This product includes software developed by the Apache Software Foundation 

(<a href="http://www.apache.org/">http://www.apache.org/</a>).</p>



<p>Contributors to the software include: John Weatherley, Sonal Bhushan, Katy Ginger, Shelley Olds and Marianne Weingroff. </p>



<h3>Version</h3>

<p>This is jOAI software version @VERSION@.</p>

<p>

  Read the <a href="<c:url value='/docs/CHANGES.txt'/>" title="Read release notes and change information">release notes and changes</a>.</p>



<h3>Licensing</h3>



<p>jOAI is provided under the <a href="<c:url value='/docs/LICENSE.txt'/>">GNU general public license</a>.

When using, modifying or redistributing this product, please include the following acknowledgment:</p>

<p>

<div style="padding-left:40px;padding-right:30px">

	<i>
		"This product includes software developed by Digital Learning Sciences (DLS) (<a href="http://www.dlsciences.org/">http://www.dlsciences.org/</a>)
		at the University Corporation for Atmospheric Research (<a href="http://www.ucar.edu/">http://www.ucar.edu/</a>)."		
	</i>

</div>

</p>



<p>This software is Copyright (c) University Corporation for Atmospheric Research (UCAR). All rights reserved. This copyright applies to the entire software package. It does not apply to harvested or provided metadata files. </p>





<h3>Support</h3>

<p>
	The <a href="http://sourceforge.net/projects/dlsciences/forums/forum/1138932">jOAI User Forum</a> at SourceForge 
	is a place where members of the jOAI community can seek advice, provide tips, share experiences, report bugs, and interact with the developers and other users of the tool. 
	Inquiries may also be sent via e-mail to <a href="mailto:support@dlese.org">support@dlese.org</a>.
</p>

<p>&nbsp;</p>

<p>&nbsp;</p>



<div align="center"><a href="http://www.openarchives.org/"><img src="<c:url value='/images/OAI_logo.gif'/>" alt="oai logo" width="100" height="70" border="0"></a></div>



<p>&nbsp;</p>

<p>&nbsp;</p>







<c:import url="../bottom.jsp?page=about"/>



</body>

</html>



