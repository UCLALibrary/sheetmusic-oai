<?xml version="1.0" encoding="UTF-8"?>
	<!--
	    Version 1.0 created 2006-11-01 by Clay Redding <cred@loc.gov>
	    
	    This stylesheet will transform simple Dublin Core (DC) expressed in either OAI DC [1] or SRU DC [2] schemata to MODS 
	    version 3.2.
	    
	    Reasonable attempts have been made to automatically detect and process the textual content of DC elements for the purposes 
	    of outputting to MODS.  Because MODS is more granular and expressive than simple DC, transforming a given DC element to the 
            proper MODS element(s) is difficult and may result in imprecise or incorrect tagging.  Undoubtedly local customizations will 
            have to be made by those who utilize this stylesheet in order to achieve deisred results.  No attempt has been made to 
            ignore empty DC elements.  If your DC contains empty elements, they should either be removed, or local customization to 
            detect the existence of text for each element will have to be added to this stylesheet.
	    
	    MODS also often encourages content adhering to various data value standards.  The contents of some of the more widely used value 
	    standards, such as IANA MIME types, ISO 3166-1, ISO 639-2, etc., have been added into the stylesheet to facilitate proper 
	    mapping of simple DC to the proper MODS elements.  A crude attempt at detecting the contents of DC identifiers and outputting them
	    to the proper MODS elements has been made as well.  Common persistent identifier schemes, standard numbers, etc., have been included.
	    To truly detect these efficiently, XSL/XPath 2.0 or XQuery may be needed in order to utilize regular expressions.
	    
	    [1] http://www.openarchives.org/OAI/openarchivesprotocol.html#MetadataNamespaces
	    [2] http://www.loc.gov/standards/sru/record-schemas.html
	        
	-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mods="http://www.loc.gov/mods/v3" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:sru_dc="info:srw/schema/1/dc-schema" xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns="http://www.loc.gov/mods/v3" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" exclude-result-prefixes="sru_dc oai_dc dc" version="1.0">
    <xsl:output method="xml" indent="yes" encoding="UTF-8"/>
    <xsl:include href="dcmiType.xsl"/>
    <xsl:include href="mimeType.xsl"/>
    <xsl:include href="csdgm.xsl"/>
    <xsl:include href="forms.xsl"/>
    <xsl:include href="iso3166-1.xsl"/>
    <xsl:include href="iso639-2.xsl"/>
    <!-- Do you have a Handle server?  If so, specify the base URI below including the trailing slash a la: http://hdl.loc.gov/ -->
    <xsl:variable name="handleServer">
		<xsl:text>http://hdl.loc.gov/</xsl:text>
    </xsl:variable>
    <xsl:template match="*[not(node())]"/> <!-- strip empty DC elements that are output by tools like ContentDM -->
    <xsl:template match="/">
        <xsl:if test="sru_dc:dcCollection">
            <xsl:apply-templates select="sru_dc:dcCollection"/>
        </xsl:if>
        <xsl:if test="sru_dc:dc">
            <xsl:apply-templates select="sru_dc:dc"/>
        </xsl:if>
        <xsl:if test="oai_dc:dc">
            <xsl:apply-templates/>
        </xsl:if>
		 <xsl:if test="dc">
            <xsl:apply-templates/>
        </xsl:if>
    </xsl:template>
    <xsl:template match="sru_dc:dcCollection">
        <mods:modsCollection xmlns="http://www.loc.gov/mods/v3" xmlns:mods="http://www.loc.gov/mods/v3" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.loc.gov/mods/v3 http://www.loc.gov/standards/mods/v3/mods-3-4.xsd">
			<xsl:apply-templates select="sru_dc:dc">
				<xsl:with-param name="dcCollection">
					<xsl:text>true</xsl:text>
				</xsl:with-param>
			</xsl:apply-templates>
        </mods:modsCollection>
    </xsl:template>
    <xsl:template match="sru_dc:dc">
		<xsl:param name="dcCollection"/>
		<xsl:choose>
			<xsl:when test="$dcCollection = 'true'">
			    <mods:mods >
					<xsl:call-template name="dcMain"/>
				</mods:mods>
			</xsl:when>
			<xsl:otherwise>
			    <mods:mods  xmlns:mods="http://www.loc.gov/mods/v3" xmlns="http://www.loc.gov/mods/v3" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.loc.gov/mods/v3 http://www.loc.gov/standards/mods/v3/mods-3-4.xsd">
					<xsl:call-template name="dcMain"/>
				</mods:mods>
			</xsl:otherwise>
		</xsl:choose>
        
    </xsl:template>
    <xsl:template match="oai_dc:dc">
        <mods:mods  xmlns:mods="http://www.loc.gov/mods/v3" xmlns="http://www.loc.gov/mods/v3" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.loc.gov/mods/v3 http://www.loc.gov/standards/mods/v3/mods-3-4.xsd">
			<xsl:call-template name="dcMain"/>
        </mods:mods>
    </xsl:template>
	    <xsl:template match="dc">
	        <mods:mods  xmlns:mods="http://www.loc.gov/mods/v3" xmlns="http://www.loc.gov/mods/v3" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.loc.gov/mods/v3 http://www.loc.gov/standards/mods/v3/mods-3-4.xsd">
			<xsl:call-template name="dcMaintemp"/>
        </mods:mods>
    </xsl:template>
    <xsl:template name="dcMain">
	    <xsl:for-each select="dc:title">
                <xsl:apply-templates select="."/>
            </xsl:for-each>
            <xsl:for-each select="dc:creator">
                <xsl:apply-templates select="."/>
            </xsl:for-each>
             <xsl:for-each select="dc:contributor">
                <xsl:apply-templates select="."/>
            </xsl:for-each>
            <xsl:for-each select="dc:type">
                <xsl:choose>
			<xsl:when test="contains(text(), 'Collection') or contains(text(), 'collection')">
				<mods:genre authority="dct">
                    			<xsl:text>collection</xsl:text>
                		</mods:genre>
			</xsl:when>
			<xsl:otherwise>
				<xsl:apply-templates select="."/>
			</xsl:otherwise>
		</xsl:choose>
            </xsl:for-each>
            <xsl:for-each select="dc:subject | dc:coverage">
                <xsl:apply-templates select="."/>
            </xsl:for-each>
            <xsl:for-each select="dc:description">
                <xsl:apply-templates select="."/>
            </xsl:for-each>
            <xsl:for-each select="dc:publisher">
                <xsl:apply-templates select="."/>
            </xsl:for-each>
            <xsl:for-each select="dc:date">
                <xsl:apply-templates select="."/>
            </xsl:for-each>
            <xsl:for-each select="dc:format">
                <xsl:apply-templates select="."/>
            </xsl:for-each>
            <xsl:for-each select="dc:identifier">
                <xsl:choose>
                    <xsl:when test="starts-with(text(), 'http://')">
                        <mods:location>
                            <mods:url>
                                <xsl:value-of select="."/>
                            </mods:url>
                        </mods:location>
                        <xsl:apply-templates select="."/>
                    </xsl:when>
					 <xsl:when test="starts-with(text(), 'https://')">
                        <mods:location>
                            <mods:url>
                                <xsl:value-of select="."/>
                            </mods:url>
                        </mods:location>
                        <xsl:apply-templates select="."/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:apply-templates select="."/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
            <xsl:for-each select="dc:source | dc:relation">
                <xsl:apply-templates select="."/>
            </xsl:for-each>
            <xsl:for-each select="dc:language">
                <xsl:apply-templates select="."/>
            </xsl:for-each>
            <xsl:for-each select="dc:rights">
                <xsl:apply-templates select="."/>
            </xsl:for-each>
    </xsl:template>
	    <xsl:template name="dcMaintemp">
	    <xsl:for-each select="title">
                <xsl:apply-templates select="."/>
            </xsl:for-each>
            <xsl:for-each select="creator">
                <xsl:apply-templates select="."/>
            </xsl:for-each>
             <xsl:for-each select="contributor">
                <xsl:apply-templates select="."/>
            </xsl:for-each>
            <xsl:for-each select="type">
                <xsl:choose>
			<xsl:when test="contains(text(), 'Collection') or contains(text(), 'collection')">
				<mods:genre authority="dct">
                    			<xsl:text>collection</xsl:text>
                		</mods:genre>
			</xsl:when>
			<xsl:otherwise>
				<xsl:apply-templates select="."/>
			</xsl:otherwise>
		</xsl:choose>
            </xsl:for-each>
            <xsl:for-each select="subject | coverage">
                <xsl:apply-templates select="."/>
            </xsl:for-each>
            <xsl:for-each select="description">
                <xsl:apply-templates select="."/>
            </xsl:for-each>
            <xsl:for-each select="publisher">
                <xsl:apply-templates select="."/>
            </xsl:for-each>
            <xsl:for-each select="date">
                <xsl:apply-templates select="."/>
            </xsl:for-each>
            <xsl:for-each select="format">
                <xsl:apply-templates select="."/>
            </xsl:for-each>
            <xsl:for-each select="identifier">
                <xsl:choose>
                    <xsl:when test="starts-with(text(), 'http://')">
                        <mods:location>
                            <mods:url>
                                <xsl:value-of select="."/>
                            </mods:url>
                        </mods:location>
                        <xsl:apply-templates select="."/>
                    </xsl:when>
					<xsl:when test="starts-with(text(), 'https://')">
                        <mods:location>
                            <mods:url>
                                <xsl:value-of select="."/>
                            </mods:url>
                        </mods:location>
                        <xsl:apply-templates select="."/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:apply-templates select="."/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
            <xsl:for-each select="source | relation">
                <xsl:apply-templates select="."/>
            </xsl:for-each>
            <xsl:for-each select="language">
                <xsl:apply-templates select="."/>
            </xsl:for-each>
            <xsl:for-each select="rights">
                <xsl:apply-templates select="."/>
            </xsl:for-each>
    </xsl:template>
    <xsl:template match="dc:title">
        <mods:titleInfo>
            <mods:title>
                <xsl:apply-templates/>
            </mods:title>
        </mods:titleInfo>
    </xsl:template>
    <xsl:template match="dc:creator">
        <mods:name>
            <mods:namePart>
                <xsl:apply-templates/>
            </mods:namePart>
            <mods:role>
                <mods:roleTerm type="text">
                    <xsl:text>creator</xsl:text>
                </mods:roleTerm>
            </mods:role>
            <!--<displayForm>
                <xsl:value-of select="."/>
            </displayForm>-->
        </mods:name>
    </xsl:template>
    <xsl:template match="dc:subject">
        <mods:subject>
            <mods:topic>
                <xsl:apply-templates/>
            </mods:topic>
        </mods:subject>
    </xsl:template>
    <xsl:template match="dc:description">
        <!--<abstract>
            <xsl:apply-templates/>
        </abstract>-->
        <mods:note>
            <xsl:apply-templates/>
        </mods:note>
        <!--<tableOfContents>
            <xsl:apply-templates/>
        </tableOfContents>-->
    </xsl:template>
    <xsl:template match="dc:publisher">
        <mods:originInfo>
            <mods:publisher>
                <xsl:apply-templates/>
            </mods:publisher>
        </mods:originInfo>
    </xsl:template>
    <xsl:template match="dc:contributor">
        <mods:name>
            <mods:namePart>
                <xsl:apply-templates/>
            </mods:namePart>
            <!-- <role>
                <roleTerm type="text">
                    <xsl:text>contributor</xsl:text>
                </roleTerm>
            </role> -->
        </mods:name>
    </xsl:template>
    <xsl:template match="dc:date">
        <mods:originInfo>
            <!--<dateIssued>
                <xsl:apply-templates/>
            </dateIssued>-->
            <!--<dateCreated>
                <xsl:apply-templates/>
            </dateCreated>
            <dateCaptured>
                <xsl:apply-templates/>
            </dateCaptured>-->
            <mods:dateOther>
                <xsl:apply-templates/>
            </mods:dateOther>
        </mods:originInfo>
    </xsl:template>
    <xsl:template match="dc:type">
        <!-- based on DCMI Type Vocabulary as of 2006-10-27 at http://dublincore.org/documents/dcmi-type-vocabulary/ ... see also the included dcmiType.xsl serving as variable $types -->
	<xsl:choose>
            <xsl:when test="string(text()) = 'Dataset' or string(text()) = 'dataset'">
                <mods:typeOfResource>
			<xsl:if test="../dc:type[string(text()) = 'collection' or string(text()) = 'Collection']">
				<xsl:attribute name="collection">
					<xsl:text>yes</xsl:text>
				</xsl:attribute>
			</xsl:if>
                    	<xsl:text>software</xsl:text>
                </mods:typeOfResource>
                <mods:genre authority="dct">
                    <xsl:text>dataset</xsl:text>
                </mods:genre>
            </xsl:when>
            <xsl:when test="string(text()) = 'Event' or string(text()) = 'event'">
                <mods:genre authority="dct">
                    <xsl:text>event</xsl:text>
                </mods:genre>
            </xsl:when>
            <xsl:when test="string(text()) = 'Image' or string(text()) = 'image'">
                <mods:typeOfResource>
			<xsl:if test="../dc:type[string(text()) = 'collection' or string(text()) = 'Collection']">
				<xsl:attribute name="collection">
					<xsl:text>yes</xsl:text>
				</xsl:attribute>
			</xsl:if>
			<xsl:text>still image</xsl:text>
                </mods:typeOfResource>
                <mods:genre authority="dct">
                    <xsl:text>image</xsl:text>
                </mods:genre>
            </xsl:when>
            <xsl:when test="string(text()) = 'InteractiveResource' or string(text()) = 'interactiveresource' or string(text()) = 'Interactive Resource' or string(text()) = 'interactive resource' or string(text()) = 'interactiveResource'">
                <mods:typeOfResource>
			<xsl:if test="../dc:type[string(text()) = 'collection' or string(text()) = 'Collection']">
				<xsl:attribute name="collection">
					<xsl:text>yes</xsl:text>
				</xsl:attribute>
			</xsl:if>
			<xsl:text>software, multimedia</xsl:text>
                </mods:typeOfResource>
                <mods:genre authority="dct">
                    <xsl:text>interactive resource</xsl:text>
                </mods:genre>
            </xsl:when>
            <xsl:when test="string(text()) = 'MovingImage' or string(text()) = 'movingimage' or string(text()) = 'Moving Image' or string(text()) = 'moving image' or string(text()) = 'movingImage'">
                <mods:typeOfResource>
			<xsl:if test="../dc:type[string(text()) = 'collection' or string(text()) = 'Collection']">
				<xsl:attribute name="collection">
					<xsl:text>yes</xsl:text>
				</xsl:attribute>
			</xsl:if>
			<xsl:text>moving image</xsl:text>
                </mods:typeOfResource>
                <mods:genre authority="dct">
                    <xsl:text>moving image</xsl:text>
                </mods:genre>
            </xsl:when>
            <xsl:when test="string(text()) = 'PhysicalObject' or string(text()) = 'physicalobject' or string(text()) = 'Physical Object' or string(text()) = 'physical object' or string(text()) = 'physicalObject'">
                <mods:typeOfResource>
			<xsl:if test="../dc:type[string(text()) = 'collection' or string(text()) = 'Collection']">
				<xsl:attribute name="collection">
					<xsl:text>yes</xsl:text>
				</xsl:attribute>
			</xsl:if>
			<xsl:text>three dimensional object</xsl:text>
                </mods:typeOfResource>
                <mods:genre authority="dct">
                    <xsl:text>physical object</xsl:text>
                </mods:genre>
            </xsl:when>
            <xsl:when test="string(text()) = 'Service' or string(text()) = 'service'">
                <mods:typeOfResource>
			<xsl:if test="../dc:type[string(text()) = 'collection' or string(text()) = 'Collection']">
				<xsl:attribute name="collection">
					<xsl:text>yes</xsl:text>
				</xsl:attribute>
			</xsl:if>
			<xsl:text>software, multimedia</xsl:text>
                </mods:typeOfResource>
                <mods:genre authority="dct">
                    <xsl:text>service</xsl:text>
                </mods:genre>
            </xsl:when>
            <xsl:when test="string(text()) = 'Software' or string(text()) = 'software'">
                <mods:typeOfResource>
			<xsl:if test="../dc:type[string(text()) = 'collection' or string(text()) = 'Collection']">
				<xsl:attribute name="collection">
					<xsl:text>yes</xsl:text>
				</xsl:attribute>
			</xsl:if>
			<xsl:text>software, multimedia</xsl:text>
                </mods:typeOfResource>
                <mods:genre authority="dct">
                    <xsl:text>software</xsl:text>
                </mods:genre>
            </xsl:when>
            <xsl:when test="string(text()) = 'Sound' or string(text()) = 'sound'">
                <mods:typeOfResource>
			<xsl:if test="../dc:type[string(text()) = 'collection' or string(text()) = 'Collection']">
				<xsl:attribute name="collection">
					<xsl:text>yes</xsl:text>
				</xsl:attribute>
			</xsl:if>
			<xsl:text>sound recording</xsl:text>
                </mods:typeOfResource>
                <mods:genre authority="dct">
                    <xsl:text>sound</xsl:text>
                </mods:genre>
            </xsl:when>
            <xsl:when test="string(text()) = 'StillImage' or string(text()) = 'stillimage' or string(text()) = 'Still Image' or string(text()) = 'still image' or string(text()) = 'stillImage'">
                <mods:typeOfResource>
			<xsl:if test="../dc:type[string(text()) = 'collection' or string(text()) = 'Collection']">
				<xsl:attribute name="collection">
					<xsl:text>yes</xsl:text>
				</xsl:attribute>
			</xsl:if>
			<xsl:text>still image</xsl:text>
                </mods:typeOfResource>
                <mods:genre authority="dct">
                    <xsl:text>still image</xsl:text>
                </mods:genre>
            </xsl:when>
            <xsl:when test="string(text()) = 'Text' or string(text()) = 'text'">
                <mods:typeOfResource>
			<xsl:if test="../dc:type[string(text()) = 'collection' or string(text()) = 'Collection']">
				<xsl:attribute name="collection">
					<xsl:text>yes</xsl:text>
				</xsl:attribute>
			</xsl:if>
			<xsl:text>text</xsl:text>
                </mods:typeOfResource>
                <mods:genre authority="dct">
                    <xsl:text>text</xsl:text>
                </mods:genre>
            </xsl:when>
            <xsl:otherwise>
                <xsl:if test="not(string($types) = text())">
			<xsl:variable name="lowercaseType" select="translate(text(), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')"/>
                	<!--<typeOfResource>
                    		<xsl:text>mixed material</xsl:text>
                	</typeOfResource>-->
                    <mods:genre>
                    		<xsl:value-of select="$lowercaseType"/>
                	</mods:genre>
		</xsl:if>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template match="dc:format">
        <mods:physicalDescription>
            <xsl:choose>
                <xsl:when test="contains(text(), '/')">
                    <xsl:variable name="mime" select="substring-before(text(), '/')"/>
                    <xsl:choose>
                        <xsl:when test="contains($mimeTypeDirectories, $mime)">
                            <mods:internetMediaType>
                            <xsl:apply-templates/>
                        </mods:internetMediaType>
                        </xsl:when>
                        <xsl:otherwise>
                            <mods:note>
                                <xsl:apply-templates/>
                            </mods:note>
                        </xsl:otherwise>
                    </xsl:choose>                    
                </xsl:when>
                <xsl:when test="starts-with(text(), '1') or starts-with(text(), '2') or starts-with(text(), '3') or starts-with(text(), '4') or starts-with(text(), '5') or starts-with(text(), '6') or starts-with(text(), '7') or starts-with(text(), '8') or starts-with(text(), '9')">
                    <mods:extent>
                        <xsl:apply-templates/>
                    </mods:extent>
                </xsl:when>
                <xsl:when test="contains($forms, text())">
                    <mods:form>
                        <xsl:apply-templates/>
                    </mods:form>
                </xsl:when>
                <xsl:otherwise>
                    <mods:note>
                        <xsl:apply-templates/>
                    </mods:note>
                </xsl:otherwise>
            </xsl:choose>
        </mods:physicalDescription>
    </xsl:template>
    <xsl:template match="dc:identifier">
        <xsl:variable name="iso-3166Check">
            <xsl:value-of select="substring(text(), 1, 2)"/>
        </xsl:variable>
        <mods:identifier>
            <xsl:attribute name="type">
                <xsl:choose>
                    <!-- handled by location/url -->
                    <xsl:when test="starts-with(text(), 'http://') or starts-with(text(), 'https://') and (not(contains(text(), $handleServer) or not(contains(substring-after(text(), 'http://'), 'hdl')) or not(contains(substring-after(text(), 'https://'), 'hdl'))))">
                        <xsl:text>uri</xsl:text>
                    </xsl:when>
                    <xsl:when test="starts-with(text(),'urn:hdl') or starts-with(text(),'hdl') or starts-with(text(),'http://hdl.') or starts-with(text(),'https://hdl.')">
                        <xsl:text>hdl</xsl:text>
                    </xsl:when>
                    <xsl:when test="starts-with(text(), 'doi')">
                        <xsl:text>doi</xsl:text>
                    </xsl:when>
                    <xsl:when test="starts-with(text(), 'ark')">
                        <xsl:text>ark</xsl:text>
                    </xsl:when>
                    <xsl:when test="contains(text(), 'purl')">
                        <xsl:text>purl</xsl:text>
                    </xsl:when>
                    <xsl:when test="starts-with(text(), 'tag')">
                        <xsl:text>tag</xsl:text>
                    </xsl:when>
                    <!-- will need to update for ISBN 13 as of January 1, 2007, see XSL tool at http://isbntools.com/ -->
                    <xsl:when test="(starts-with(text(), 'ISBN') or starts-with(text(), 'isbn')) or ((string-length(text()) = 13) and contains(text(), '-') and (starts-with(text(), '0') or starts-with(text(), '1'))) or ((string-length(text()) = 10) and (starts-with(text(), '0') or starts-with(text(), '1')))">
                        <xsl:text>isbn</xsl:text>
                    </xsl:when>
                    <xsl:when test="(starts-with(text(), 'ISRC') or starts-with(text(), 'isrc')) or ((string-length(text()) = 12) and (contains($iso3166-1, $iso-3166Check))) or ((string-length(text()) = 15) and (contains(text(), '-') or contains(text(), '/')) and contains($iso3166-1, $iso-3166Check))">
                        <xsl:text>isrc</xsl:text>
                    </xsl:when>
                    <xsl:when test="(starts-with(text(), 'ISMN') or starts-with(text(), 'ismn')) or starts-with(text(), 'M') and ((string-length(text()) = 11) and contains(text(), '-') or string-length(text()) = 9)">
                        <xsl:text>ismn</xsl:text>
                    </xsl:when>
                    <xsl:when test="(starts-with(text(), 'ISSN') or starts-with(text(), 'issn')) or ((string-length(text()) = 9) and contains(text(), '-') or string-length(text()) = 8)">
                        <xsl:text>issn</xsl:text>
                    </xsl:when>
                    <xsl:when test="starts-with(text(), 'ISTC') or starts-with(text(), 'istc')">
                        <xsl:text>istc</xsl:text>
                    </xsl:when>
                    <xsl:when test="(starts-with(text(), 'UPC') or starts-with(text(), 'upc')) or (string-length(text()) = 12 and not(contains(text(), ' ')) and not(contains($iso3166-1, $iso-3166Check)))">
                        <xsl:text>upc</xsl:text>
                    </xsl:when>
                    <xsl:when test="(starts-with(text(), 'SICI') or starts-with(text(), 'sici')) or ((starts-with(text(), '0') or starts-with(text(), '1')) and (contains(text(), ';') and contains(text(), '(') and contains(text(), ')') and contains(text(), '&lt;') and contains(text(), '&gt;')))">
                        <xsl:text>sici</xsl:text>
                    </xsl:when>
                    <xsl:when test="starts-with(text(), 'LCCN') or starts-with(text(), 'lccn')">
                        <!-- probably can't do this quickly or easily without regexes and XSL 2.0 -->
                        <xsl:text>lccn</xsl:text>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:text>local</xsl:text>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:attribute>
            <xsl:choose>
		<xsl:when test="starts-with(text(),'urn:hdl') or starts-with(text(),'hdl') or starts-with(text(),$handleServer)">
			<xsl:value-of select="concat('hdl:',substring-after(text(),$handleServer))"/>
		</xsl:when>
		<xsl:otherwise>
			<xsl:apply-templates/>
		</xsl:otherwise>
            </xsl:choose>
        </mods:identifier>
    </xsl:template>
    <xsl:template match="dc:source">
        <mods:relatedItem type="original">
            <mods:titleInfo>
                <mods:title>
                    <xsl:apply-templates/>
                </mods:title>
            </mods:titleInfo>
        </mods:relatedItem>
    </xsl:template>
    <xsl:template match="dc:language">
        <mods:language>
            <xsl:choose>
                <xsl:when test="string-length(text()) = 3 and contains($iso639-2, text())">
                    <mods:languageTerm type="code">
                        <xsl:apply-templates/>
                    </mods:languageTerm>
                </xsl:when>
                <xsl:otherwise>
                    <mods:languageTerm type="text">
                        <xsl:apply-templates/>
                    </mods:languageTerm>
                </xsl:otherwise>
            </xsl:choose>
        </mods:language>
    </xsl:template>
    <xsl:template match="dc:relation">
        <mods:relatedItem>
			<xsl:choose>
				<xsl:when test="starts-with(text(), 'http://')">
				    <mods:location>
				        <mods:url>
							<xsl:value-of select="."/>
						</mods:url>
					</mods:location>
				    <mods:identifer type="uri">
						<xsl:apply-templates/>
					</mods:identifer>
				</xsl:when>
				<xsl:when test="starts-with(text(), 'https://')">
				    <mods:location>
				        <mods:url>
							<xsl:value-of select="."/>
						</mods:url>
					</mods:location>
				    <mods:identifer type="uri">
						<xsl:apply-templates/>
					</mods:identifer>
				</xsl:when>
				<xsl:otherwise>
				    <mods:titleInfo>
				        <mods:title>
							<xsl:apply-templates/>
						</mods:title>
					</mods:titleInfo>
				</xsl:otherwise>
			</xsl:choose>            
        </mods:relatedItem>
    </xsl:template>
    <xsl:template match="dc:coverage">
            <xsl:choose>
                <xsl:when test="string-length(text()) >= 3 and (starts-with(text(), '1') or starts-with(text(), '2') or starts-with(text(), '3') or starts-with(text(), '4') or starts-with(text(), '5') or starts-with(text(), '6') or starts-with(text(), '7') or starts-with(text(), '8') or starts-with(text(), '9') or starts-with(text(), '-') or contains(text(), 'AD') or contains(text(), 'BC')) and not(contains(text(), ':'))">
                    <!-- using XSL 2.0 for date parsing is A Better And Saner Idea -->
                    <mods:subject>
                        <mods:temporal>
                           <xsl:apply-templates/>
                        </mods:temporal>
                    </mods:subject>
                </xsl:when>
                <xsl:when test="contains(text(), '°') or contains(text(), 'geo:lat') or contains(text(), 'geo:lon') or contains(text(), ' N ') or contains(text(), ' S ') or contains(text(), ' E ') or contains(text(), ' W ')">
                    <!-- predicting minutes and seconds with ' or " might break if quotes used for other purposes exist in the text node -->
                    <mods:subject>
                        <mods:cartographics>
                            <mods:coordinates>
                                <xsl:apply-templates/>
                            </mods:coordinates>
                        </mods:cartographics>
                    </mods:subject>
                </xsl:when>
                <xsl:when test="contains(text(), ':')">
                    <xsl:if test="starts-with(text(), '1') and contains(text(), ':') and (contains(substring-after(text(), ':'), '1') or contains(substring-after(text(), ':'), '2') or contains(substring-after(text(), ':'), '3') or contains(substring-after(text(), ':'), '4') or contains(substring-after(text(), ':'), '5') or contains(substring-after(text(), ':'), '6') or contains(substring-after(text(), ':'), '7') or contains(substring-after(text(), ':'), '8') or contains(substring-after(text(), ':'), '9'))">
                        <mods:subject>
                            <mods:cartographics>
                                <mods:scale>
                                    <xsl:apply-templates/>
                                </mods:scale>
                            </mods:cartographics>
                        </mods:subject>
                    </xsl:if>
                </xsl:when>
                <xsl:when test="contains($projections, text())">
                    <mods:subject>
                        <mods:cartographics>
                            <mods:projection>
                                <xsl:apply-templates/>
                            </mods:projection>
                        </mods:cartographics>
                    </mods:subject>
                </xsl:when>
                <xsl:otherwise>
                    <mods:subject>
                        <mods:geographic>
                            <xsl:apply-templates/>
                        </mods:geographic>
                    </mods:subject>
                </xsl:otherwise>
            </xsl:choose>
    </xsl:template>
    <xsl:template match="dc:rights">
        <mods:accessCondition>
            <xsl:apply-templates/>
        </mods:accessCondition>
    </xsl:template>
	
	
	
	
	
	   <xsl:template match="title">
	       <mods:titleInfo>
	           <mods:title>
                <xsl:apply-templates/>
            </mods:title>
        </mods:titleInfo>
    </xsl:template>
    <xsl:template match="creator">
        <mods:name>
            <mods:namePart>
                <xsl:apply-templates/>
            </mods:namePart>
            <mods:role>
                <mods:roleTerm type="text">
                    <xsl:text>creator</xsl:text>
                </mods:roleTerm>
            </mods:role>
            <!--<displayForm>
                <xsl:value-of select="."/>
            </displayForm>-->
        </mods:name>
    </xsl:template>
    <xsl:template match="subject">
        <mods:subject>
            <mods:topic>
                <xsl:apply-templates/>
            </mods:topic>
        </mods:subject>
    </xsl:template>
    <xsl:template match="description">
        <!--<abstract>
            <xsl:apply-templates/>
        </abstract>-->
        <mods:note>
            <xsl:apply-templates/>
        </mods:note>
        <!--<tableOfContents>
            <xsl:apply-templates/>
        </tableOfContents>-->
    </xsl:template>
    <xsl:template match="publisher">
        <mods:originInfo>
            <mods:publisher>
                <xsl:apply-templates/>
            </mods:publisher>
        </mods:originInfo>
    </xsl:template>
    <xsl:template match="contributor">
        <mods:name>
            <mods:namePart>
                <xsl:apply-templates/>
            </mods:namePart>
            <!-- <role>
                <roleTerm type="text">
                    <xsl:text>contributor</xsl:text>
                </roleTerm>
            </role> -->
        </mods:name>
    </xsl:template>
    <xsl:template match="date">
        <mods:originInfo>
            <!--<dateIssued>
                <xsl:apply-templates/>
            </dateIssued>-->
            <!--<dateCreated>
                <xsl:apply-templates/>
            </dateCreated>
            <dateCaptured>
                <xsl:apply-templates/>
            </dateCaptured>-->
            <mods:dateOther>
                <xsl:apply-templates/>
            </mods:dateOther>
        </mods:originInfo>
    </xsl:template>
    <xsl:template match="type">
        <!-- based on DCMI Type Vocabulary as of 2006-10-27 at http://dublincore.org/documents/dcmi-type-vocabulary/ ... see also the included dcmiType.xsl serving as variable $types -->
	<xsl:choose>
            <xsl:when test="string(text()) = 'Dataset' or string(text()) = 'dataset'">
                <mods:typeOfResource>
			<xsl:if test="../type[string(text()) = 'collection' or string(text()) = 'Collection']">
				<xsl:attribute name="collection">
					<xsl:text>yes</xsl:text>
				</xsl:attribute>
			</xsl:if>
                    	<xsl:text>software</xsl:text>
                </mods:typeOfResource>
                <mods:genre authority="dct">
                    <xsl:text>dataset</xsl:text>
                </mods:genre>
            </xsl:when>
            <xsl:when test="string(text()) = 'Event' or string(text()) = 'event'">
                <mods:genre authority="dct">
                    <xsl:text>event</xsl:text>
                </mods:genre>
            </xsl:when>
            <xsl:when test="string(text()) = 'Image' or string(text()) = 'image'">
                <mods:typeOfResource>
			<xsl:if test="../type[string(text()) = 'collection' or string(text()) = 'Collection']">
				<xsl:attribute name="collection">
					<xsl:text>yes</xsl:text>
				</xsl:attribute>
			</xsl:if>
			<xsl:text>still image</xsl:text>
                </mods:typeOfResource>
                <mods:genre authority="dct">
                    <xsl:text>image</xsl:text>
                </mods:genre>
            </xsl:when>
            <xsl:when test="string(text()) = 'InteractiveResource' or string(text()) = 'interactiveresource' or string(text()) = 'Interactive Resource' or string(text()) = 'interactive resource' or string(text()) = 'interactiveResource'">
                <mods:typeOfResource>
			<xsl:if test="../type[string(text()) = 'collection' or string(text()) = 'Collection']">
				<xsl:attribute name="collection">
					<xsl:text>yes</xsl:text>
				</xsl:attribute>
			</xsl:if>
			<xsl:text>software, multimedia</xsl:text>
                </mods:typeOfResource>
                <mods:genre authority="dct">
                    <xsl:text>interactive resource</xsl:text>
                </mods:genre>
            </xsl:when>
            <xsl:when test="string(text()) = 'MovingImage' or string(text()) = 'movingimage' or string(text()) = 'Moving Image' or string(text()) = 'moving image' or string(text()) = 'movingImage'">
                <mods:typeOfResource>
			<xsl:if test="../type[string(text()) = 'collection' or string(text()) = 'Collection']">
				<xsl:attribute name="collection">
					<xsl:text>yes</xsl:text>
				</xsl:attribute>
			</xsl:if>
			<xsl:text>moving image</xsl:text>
                </mods:typeOfResource>
                <mods:genre authority="dct">
                    <xsl:text>moving image</xsl:text>
                </mods:genre>
            </xsl:when>
            <xsl:when test="string(text()) = 'PhysicalObject' or string(text()) = 'physicalobject' or string(text()) = 'Physical Object' or string(text()) = 'physical object' or string(text()) = 'physicalObject'">
                <mods:typeOfResource>
			<xsl:if test="../type[string(text()) = 'collection' or string(text()) = 'Collection']">
				<xsl:attribute name="collection">
					<xsl:text>yes</xsl:text>
				</xsl:attribute>
			</xsl:if>
			<xsl:text>three dimensional object</xsl:text>
                </mods:typeOfResource>
                <mods:genre authority="dct">
                    <xsl:text>physical object</xsl:text>
                </mods:genre>
            </xsl:when>
            <xsl:when test="string(text()) = 'Service' or string(text()) = 'service'">
                <mods:typeOfResource>
			<xsl:if test="../type[string(text()) = 'collection' or string(text()) = 'Collection']">
				<xsl:attribute name="collection">
					<xsl:text>yes</xsl:text>
				</xsl:attribute>
			</xsl:if>
			<xsl:text>software, multimedia</xsl:text>
                </mods:typeOfResource>
                <mods:genre authority="dct">
                    <xsl:text>service</xsl:text>
                </mods:genre>
            </xsl:when>
            <xsl:when test="string(text()) = 'Software' or string(text()) = 'software'">
                <mods:typeOfResource>
			<xsl:if test="../type[string(text()) = 'collection' or string(text()) = 'Collection']">
				<xsl:attribute name="collection">
					<xsl:text>yes</xsl:text>
				</xsl:attribute>
			</xsl:if>
			<xsl:text>software, multimedia</xsl:text>
                </mods:typeOfResource>
                <mods:genre authority="dct">
                    <xsl:text>software</xsl:text>
                </mods:genre>
            </xsl:when>
            <xsl:when test="string(text()) = 'Sound' or string(text()) = 'sound'">
                <mods:typeOfResource>
			<xsl:if test="../type[string(text()) = 'collection' or string(text()) = 'Collection']">
				<xsl:attribute name="collection">
					<xsl:text>yes</xsl:text>
				</xsl:attribute>
			</xsl:if>
			<xsl:text>sound recording</xsl:text>
                </mods:typeOfResource>
                <mods:genre authority="dct">
                    <xsl:text>sound</xsl:text>
                </mods:genre>
            </xsl:when>
            <xsl:when test="string(text()) = 'StillImage' or string(text()) = 'stillimage' or string(text()) = 'Still Image' or string(text()) = 'still image' or string(text()) = 'stillImage'">
                <mods:typeOfResource>
			<xsl:if test="../type[string(text()) = 'collection' or string(text()) = 'Collection']">
				<xsl:attribute name="collection">
					<xsl:text>yes</xsl:text>
				</xsl:attribute>
			</xsl:if>
			<xsl:text>still image</xsl:text>
                </mods:typeOfResource>
                <mods:genre authority="dct">
                    <xsl:text>still image</xsl:text>
                </mods:genre>
            </xsl:when>
            <xsl:when test="string(text()) = 'Text' or string(text()) = 'text'">
                <mods:typeOfResource>
			<xsl:if test="../type[string(text()) = 'collection' or string(text()) = 'Collection']">
				<xsl:attribute name="collection">
					<xsl:text>yes</xsl:text>
				</xsl:attribute>
			</xsl:if>
			<xsl:text>text</xsl:text>
                </mods:typeOfResource>
                <mods:genre authority="dct">
                    <xsl:text>text</xsl:text>
                </mods:genre>
            </xsl:when>
            <xsl:otherwise>
                <xsl:if test="not(string($types) = text())">
			<xsl:variable name="lowercaseType" select="translate(text(), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')"/>
                	<!--<typeOfResource>
                    		<xsl:text>mixed material</xsl:text>
                	</typeOfResource>-->
                    <mods:genre>
                    		<xsl:value-of select="$lowercaseType"/>
                	</mods:genre>
		</xsl:if>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template match="format">
        <mods:physicalDescription>
            <xsl:choose>
                <xsl:when test="contains(text(), '/')">
                    <xsl:variable name="mime" select="substring-before(text(), '/')"/>
                    <xsl:choose>
                        <xsl:when test="contains($mimeTypeDirectories, $mime)">
                            <mods:internetMediaType>
                            <xsl:apply-templates/>
                        </mods:internetMediaType>
                        </xsl:when>
                        <xsl:otherwise>
                            <mods:note>
                                <xsl:apply-templates/>
                            </mods:note>
                        </xsl:otherwise>
                    </xsl:choose>                    
                </xsl:when>
                <xsl:when test="starts-with(text(), '1') or starts-with(text(), '2') or starts-with(text(), '3') or starts-with(text(), '4') or starts-with(text(), '5') or starts-with(text(), '6') or starts-with(text(), '7') or starts-with(text(), '8') or starts-with(text(), '9')">
                    <mods:extent>
                        <xsl:apply-templates/>
                    </mods:extent>
                </xsl:when>
                <xsl:when test="contains($forms, text())">
                    <mods:form>
                        <xsl:apply-templates/>
                    </mods:form>
                </xsl:when>
                <xsl:otherwise>
                    <mods:note>
                        <xsl:apply-templates/>
                    </mods:note>
                </xsl:otherwise>
            </xsl:choose>
        </mods:physicalDescription>
    </xsl:template>
    <xsl:template match="identifier">
        <xsl:variable name="iso-3166Check">
            <xsl:value-of select="substring(text(), 1, 2)"/>
        </xsl:variable>
        <mods:identifier>
            <xsl:attribute name="type">
                <xsl:choose>
                    <!-- handled by location/url -->
                    <xsl:when test="starts-with(text(), 'http://') or starts-with(text(), 'https://') and (not(contains(text(), $handleServer) or not(contains(substring-after(text(), 'http://'), 'hdl')) or not(contains(substring-after(text(), 'https://'), 'hdl'))))">
                        <xsl:text>uri</xsl:text>
                    </xsl:when>
                    <xsl:when test="starts-with(text(),'urn:hdl') or starts-with(text(),'hdl') or starts-with(text(),'http://hdl.') or starts-with(text(),'https://hdl.')">
                        <xsl:text>hdl</xsl:text>
                    </xsl:when>
                    <xsl:when test="starts-with(text(), 'doi')">
                        <xsl:text>doi</xsl:text>
                    </xsl:when>
                    <xsl:when test="starts-with(text(), 'ark')">
                        <xsl:text>ark</xsl:text>
                    </xsl:when>
                    <xsl:when test="contains(text(), 'purl')">
                        <xsl:text>purl</xsl:text>
                    </xsl:when>
                    <xsl:when test="starts-with(text(), 'tag')">
                        <xsl:text>tag</xsl:text>
                    </xsl:when>
                    <!-- will need to update for ISBN 13 as of January 1, 2007, see XSL tool at http://isbntools.com/ -->
                    <xsl:when test="(starts-with(text(), 'ISBN') or starts-with(text(), 'isbn')) or ((string-length(text()) = 13) and contains(text(), '-') and (starts-with(text(), '0') or starts-with(text(), '1'))) or ((string-length(text()) = 10) and (starts-with(text(), '0') or starts-with(text(), '1')))">
                        <xsl:text>isbn</xsl:text>
                    </xsl:when>
                    <xsl:when test="(starts-with(text(), 'ISRC') or starts-with(text(), 'isrc')) or ((string-length(text()) = 12) and (contains($iso3166-1, $iso-3166Check))) or ((string-length(text()) = 15) and (contains(text(), '-') or contains(text(), '/')) and contains($iso3166-1, $iso-3166Check))">
                        <xsl:text>isrc</xsl:text>
                    </xsl:when>
                    <xsl:when test="(starts-with(text(), 'ISMN') or starts-with(text(), 'ismn')) or starts-with(text(), 'M') and ((string-length(text()) = 11) and contains(text(), '-') or string-length(text()) = 9)">
                        <xsl:text>ismn</xsl:text>
                    </xsl:when>
                    <xsl:when test="(starts-with(text(), 'ISSN') or starts-with(text(), 'issn')) or ((string-length(text()) = 9) and contains(text(), '-') or string-length(text()) = 8)">
                        <xsl:text>issn</xsl:text>
                    </xsl:when>
                    <xsl:when test="starts-with(text(), 'ISTC') or starts-with(text(), 'istc')">
                        <xsl:text>istc</xsl:text>
                    </xsl:when>
                    <xsl:when test="(starts-with(text(), 'UPC') or starts-with(text(), 'upc')) or (string-length(text()) = 12 and not(contains(text(), ' ')) and not(contains($iso3166-1, $iso-3166Check)))">
                        <xsl:text>upc</xsl:text>
                    </xsl:when>
                    <xsl:when test="(starts-with(text(), 'SICI') or starts-with(text(), 'sici')) or ((starts-with(text(), '0') or starts-with(text(), '1')) and (contains(text(), ';') and contains(text(), '(') and contains(text(), ')') and contains(text(), '&lt;') and contains(text(), '&gt;')))">
                        <xsl:text>sici</xsl:text>
                    </xsl:when>
                    <xsl:when test="starts-with(text(), 'LCCN') or starts-with(text(), 'lccn')">
                        <!-- probably can't do this quickly or easily without regexes and XSL 2.0 -->
                        <xsl:text>lccn</xsl:text>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:text>local</xsl:text>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:attribute>
            <xsl:choose>
		<xsl:when test="starts-with(text(),'urn:hdl') or starts-with(text(),'hdl') or starts-with(text(),$handleServer)">
			<xsl:value-of select="concat('hdl:',substring-after(text(),$handleServer))"/>
		</xsl:when>
		<xsl:otherwise>
			<xsl:apply-templates/>
		</xsl:otherwise>
            </xsl:choose>
        </mods:identifier>
    </xsl:template>
    <xsl:template match="source">
        <mods:relatedItem type="original">
            <mods:titleInfo>
                <mods:title>
                    <xsl:apply-templates/>
                </mods:title>
            </mods:titleInfo>
        </mods:relatedItem>
    </xsl:template>
    <xsl:template match="language">
        <mods:language>
            <xsl:choose>
                <xsl:when test="string-length(text()) = 3 and contains($iso639-2, text())">
                    <mods:languageTerm type="code">
                        <xsl:apply-templates/>
                    </mods:languageTerm>
                </xsl:when>
                <xsl:otherwise>
                    <mods:languageTerm type="text">
                        <xsl:apply-templates/>
                    </mods:languageTerm>
                </xsl:otherwise>
            </xsl:choose>
        </mods:language>
    </xsl:template>
    <xsl:template match="relation">
        <mods:relatedItem>
			<xsl:choose>
				<xsl:when test="starts-with(text(), 'http://')">
				    <mods:location>
				        <mods:url>
							<xsl:value-of select="."/>
						</mods:url>
					</mods:location>
				    <mods:identifer type="uri">
						<xsl:apply-templates/>
					</mods:identifer>
				</xsl:when>
				<xsl:when test="starts-with(text(), 'https://')">
				    <mods:location>
				        <mods:url>
							<xsl:value-of select="."/>
						</mods:url>
					</mods:location>
				    <mods:identifer type="uri">
						<xsl:apply-templates/>
					</mods:identifer>
				</xsl:when>
				<xsl:otherwise>
				    <mods:titleInfo>
				        <mods:title>
							<xsl:apply-templates/>
						</mods:title>
					</mods:titleInfo>
				</xsl:otherwise>
			</xsl:choose>            
        </mods:relatedItem>
    </xsl:template>
    <xsl:template match="coverage">
            <xsl:choose>
                <xsl:when test="string-length(text()) >= 3 and (starts-with(text(), '1') or starts-with(text(), '2') or starts-with(text(), '3') or starts-with(text(), '4') or starts-with(text(), '5') or starts-with(text(), '6') or starts-with(text(), '7') or starts-with(text(), '8') or starts-with(text(), '9') or starts-with(text(), '-') or contains(text(), 'AD') or contains(text(), 'BC')) and not(contains(text(), ':'))">
                    <!-- using XSL 2.0 for date parsing is A Better And Saner Idea -->
                    <mods:subject>
                        <mods:temporal>
                           <xsl:apply-templates/>
                        </mods:temporal>
                    </mods:subject>
                </xsl:when>
                <xsl:when test="contains(text(), '°') or contains(text(), 'geo:lat') or contains(text(), 'geo:lon') or contains(text(), ' N ') or contains(text(), ' S ') or contains(text(), ' E ') or contains(text(), ' W ')">
                    <!-- predicting minutes and seconds with ' or " might break if quotes used for other purposes exist in the text node -->
                    <mods:subject>
                        <mods:cartographics>
                            <mods:coordinates>
                                <xsl:apply-templates/>
                            </mods:coordinates>
                        </mods:cartographics>
                    </mods:subject>
                </xsl:when>
                <xsl:when test="contains(text(), ':')">
                    <xsl:if test="starts-with(text(), '1') and contains(text(), ':') and (contains(substring-after(text(), ':'), '1') or contains(substring-after(text(), ':'), '2') or contains(substring-after(text(), ':'), '3') or contains(substring-after(text(), ':'), '4') or contains(substring-after(text(), ':'), '5') or contains(substring-after(text(), ':'), '6') or contains(substring-after(text(), ':'), '7') or contains(substring-after(text(), ':'), '8') or contains(substring-after(text(), ':'), '9'))">
                        <mods:subject>
                            <mods:cartographics>
                                <mods:scale>
                                    <xsl:apply-templates/>
                                </mods:scale>
                            </mods:cartographics>
                        </mods:subject>
                    </xsl:if>
                </xsl:when>
                <xsl:when test="contains($projections, text())">
                    <mods:subject>
                        <mods:cartographics>
                            <mods:projection>
                                <xsl:apply-templates/>
                            </mods:projection>
                        </mods:cartographics>
                    </mods:subject>
                </xsl:when>
                <xsl:otherwise>
                    <mods:subject>
                        <mods:geographic>
                            <xsl:apply-templates/>
                        </mods:geographic>
                    </mods:subject>
                </xsl:otherwise>
            </xsl:choose>
    </xsl:template>
    <xsl:template match="rights">
        <mods:accessCondition>
            <xsl:apply-templates/>
        </mods:accessCondition>
    </xsl:template>
	
</xsl:stylesheet>
