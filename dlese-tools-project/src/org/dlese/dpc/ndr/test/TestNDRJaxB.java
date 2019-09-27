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

package org.dlese.dpc.ndr.test;

import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;

/*import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.util.JAXBSource;

import org.nsdl.ns.api.descriptivexml_v1.InfoXMLType;
import org.nsdl.ns.api.descriptivexml_v1.NSDLRepositoryType;
import org.nsdl.ns.ndr.api_components_v1.PropertyType;
import org.nsdl.ns.ndr.request_v1.AggregatorType;
import org.nsdl.ns.ndr.request_v1.InputXML;
import org.nsdl.ns.ndr.request_v1.ObjectFactory;
import org.dlese.dpc.ndr.apiproxy.NDRAPIProxy;
import org.dlese.dpc.ndr.connection.NDRConnection;
*///import javax.xml.stream.XMLStreamWriter;

import junit.framework.TestCase;

/**
 * @author kmaull
 *
 */
public class TestNDRJaxB extends TestCase {
	
	public void t001() {
	
		
/*		try {
			JAXBContext jc = JAXBContext.newInstance("org.nsdl.ns.ndr.request_v1:org.nsdl.ns.ndr.api_components");// "org.nsdl.ns.ndr.request_v1" );
			
			ObjectFactory o = new ObjectFactory();
			
			InputXML inXML = o.createInputXML();
			
			// create the agg type
			AggregatorType aggType = new AggregatorType();
			aggType.setProperties(new AggregatorType.Properties());
			
			// create the property object
			PropertyType propType = new PropertyType();
			propType.setValue("col:DLESE");
		
			// set up the JAXBElement node
			JAXBElement propJAXB = new JAXBElement<PropertyType>( ObjectFactory._SetName_QNAME, PropertyType.class, propType );
			propJAXB.setValue(propType);
			
			// create the  peroper
			aggType.getProperties().getAggregatorPropertyOrAggregatablePropertyOrAgentServiceProperty().add(propJAXB);
			
			inXML.setAggregator(aggType);
*/			
/*			// build up NDR Object
			NdrObjectType ndrObject = o.createNdrObjectType();

			*//** create the relationships container object **//*
			RelationshipsType relnObject = o.createRelationshipsType();

			RelationshipType reln0 = new RelationshipType();
			reln0.setValue("handle/test.972332899");
	
			RelationshipCommandType relnCmd = new RelationshipCommandType();

			RelationshipType relnCmdRelnMDPF = new RelationshipType();
			relnCmdRelnMDPF.setValue("metaDataProviderFor");
			
			RelationshipType relnCmdRelnAB = new RelationshipType();
			relnCmdRelnAB.setValue("aggregatedBy");

			relnCmd.getRelationship().add(relnCmdRelnMDPF);
			relnCmd.getRelationship().add(relnCmdRelnAB);
				
			relnObject.setRelationship( reln0 );
			relnObject.setRelationshipCommand(relnCmd);

			// set the container for the 
			ndrObject.setRelationshipContainer( relnObject );
*/			////
			//JAXBElement root = o.createNDRObject(ndrObject);
			
/*			Marshaller marshaller = jc.createMarshaller();
			
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
					   new Boolean(true));

			marshaller.marshal( inXML, System.out );
			
		} catch ( Exception e ) {
			e.printStackTrace();
			fail(e.getMessage());
		}
*/	}
	
	public void test002()
	{
/*		try {
			JAXBContext jc = JAXBContext.newInstance("org.nsdl.ns.api.descriptivexml_v1");// "org.nsdl.ns.ndr.request_v1" );
			Unmarshaller u = jc.createUnmarshaller();
			
			ObjectFactory o = new ObjectFactory();;
			NDRAPIProxy proxy = new NDRAPIProxy ();
			
			String responseXML = proxy.get("2200/test.20060907170433602T"); 
			
			System.out.println ( responseXML );
			
			JAXBElement<NSDLRepositoryType> infoXML = (JAXBElement<NSDLRepositoryType>)u.unmarshal( new FileReader ( "C:\\response.XML" ) );
			
			System.out.println ( infoXML.getValue().getDescriptiveXML().getData() );
			
		} catch ( Exception e ) {
			e.printStackTrace();
			fail(e.getMessage());
		}
*/	}

}
