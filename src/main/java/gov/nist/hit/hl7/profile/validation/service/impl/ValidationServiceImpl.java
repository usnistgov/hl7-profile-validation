/**
 * This software was developed at the National Institute of Standards and Technology by employees of
 * the Federal Government in the course of their official duties. Pursuant to title 17 Section 105
 * of the United States Code this software is not subject to copyright protection and is in the
 * public domain. This is an experimental system. NIST assumes no responsibility whatsoever for its
 * use by other parties, and makes no guarantees, expressed or implied, about its quality,
 * reliability, or any other characteristic. We would appreciate acknowledgement if the software is
 * used. This software can be redistributed and/or modified freely provided that any derivative
 * works bear some notice that they are derived from it, and any modified versions bear some notice
 * that they have been modified.
 */
package gov.nist.hit.hl7.profile.validation.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.NoSuchElementException;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import gov.nist.hit.hl7.profile.validation.domain.CustomProfileError;
import gov.nist.hit.hl7.profile.validation.domain.ProfileValidationReport;
import gov.nist.hit.hl7.profile.validation.domain.ProfileValidationReport.DocumentTarget;
import gov.nist.hit.hl7.profile.validation.domain.ProfileValidationReport.ErrorType;
import gov.nist.hit.hl7.profile.validation.domain.XSDVerificationResult;
import gov.nist.hit.hl7.profile.validation.service.ValidationService;
import gov.nist.hit.hl7.profile.validation.service.util.XMLManager;
import hl7.v2.profile.XMLDeserializer;

/**
 * @author jungyubw
 *
 */

public class ValidationServiceImpl implements ValidationService {
	private static String localProfilePath = "/schema/Profile.xsd";
	private static String localValueSetPath = "/schema/ValueSets.xsd";
	private static String localConstraintPath = "/schema/ConformanceContext.xsd";
	private static String localCoConstraintPath = "/schema/CoConstraintContext.xsd";
	private static String localBindingPath = "/schema/ValueSetBindings.xsd";
	private static String localSlicingPath = "/schema/ProfileSlicing.xsd";

	@Override
	public ProfileValidationReport validationXMLs(String profileXMLStr, String constraintXMLStr, String valuesetXMLStr,
			String coconstraintXML, String pofileSlicingXML, String bindingXML) {
		ProfileValidationReport report = new ProfileValidationReport();
		// 1. XML Validation by XSD

		report.setProfileXSDValidationResult(this.verifyXMLByLocalXSD(localProfilePath, profileXMLStr));
		report.setValueSetXSDValidationResult(this.verifyXMLByLocalXSD(localValueSetPath, valuesetXMLStr));
		report.setConstraintsXSDValidationResult(this.verifyXMLByLocalXSD(localConstraintPath, constraintXMLStr));
		
		if(coconstraintXML != null) report.setCoconstraintsXSDValidationResult(this.verifyXMLByLocalXSD(localCoConstraintPath, coconstraintXML));
		if(pofileSlicingXML != null) report.setSlicingXSDValidationResult(this.verifyXMLByLocalXSD(localSlicingPath, pofileSlicingXML));
		if(bindingXML != null) report.setBindingXSDValidationResult(this.verifyXMLByLocalXSD(localBindingPath, bindingXML));
		
		try {
			Document profileDoc = XMLManager.stringToDom(profileXMLStr);
			Document constrintsDoc = XMLManager.stringToDom(constraintXMLStr);
			Document valuesetsDoc = XMLManager.stringToDom(valuesetXMLStr);

			Element messagesElm = (Element) profileDoc.getElementsByTagName("Messages").item(0);
			Element segmentsElm = (Element) profileDoc.getElementsByTagName("Segments").item(0);
			Element datatypesElm = (Element) profileDoc.getElementsByTagName("Datatypes").item(0);

			NodeList messages = messagesElm.getElementsByTagName("Message");
			NodeList segments = segmentsElm.getElementsByTagName("Segment");
			NodeList datatypes = datatypesElm.getElementsByTagName("Datatype");

			HashMap<String, Element> messageMap = new HashMap<String, Element>();
			HashMap<String, Element> segmentMap = new HashMap<String, Element>();
			HashMap<String, Element> datatypeMap = new HashMap<String, Element>();

			for (int i = 0; i < messages.getLength(); i++) {
				Element m = (Element) messages.item(i);
				messageMap.put(m.getAttribute("ID"), m);
			}

			for (int i = 0; i < segments.getLength(); i++) {
				Element s = (Element) segments.item(i);
				segmentMap.put(s.getAttribute("ID"), s);
			}

			for (int i = 0; i < datatypes.getLength(); i++) {
				Element d = (Element) datatypes.item(i);
				datatypeMap.put(d.getAttribute("ID"), d);
			}
			// 2. Checking 5 level Violation
			for (String id : datatypeMap.keySet()) {
				Element dtElm = datatypeMap.get(id);
				NodeList components = dtElm.getElementsByTagName("Component");
				for (int i = 0; i < components.getLength(); i++) {
					Element componentElm = (Element) components.item(i);
					String comnponentDTId = componentElm.getAttribute("Datatype");
					Element componentDTElm = (Element) datatypeMap.get(comnponentDTId);
					NodeList subComponents = componentDTElm.getElementsByTagName("Component");
					for (int j = 0; j < subComponents.getLength(); j++) {
						Element subComponentElm = (Element) subComponents.item(j);
						String subComnponentDTId = subComponentElm.getAttribute("Datatype");
						Element subComponentDTElm = (Element) datatypeMap.get(subComnponentDTId);

						if (subComponentDTElm.getElementsByTagName("Component").getLength() > 0) {
							report.addProfileError(new CustomProfileError(ErrorType.FiveLevelComponent,
									dtElm.getAttribute("Label") + "." + (i + 1) + "." + (j + 1) + " Datatype is "
											+ subComponentDTElm.getAttribute("Label") + ", but it is not primitive.",
									DocumentTarget.DATATYPE, subComnponentDTId));
						}
					}
				}
			}

			// 3. Checking Dynamic Mapping
			HashMap<String[], Element> dmCaseMap = new HashMap<String[], Element>();
			for (String id : segmentMap.keySet()) {
				Element segElm = segmentMap.get(id);
				NodeList dmCases = segElm.getElementsByTagName("Case");

				if (dmCases.getLength() > 0) {
					for (int i = 0; i < dmCases.getLength(); i++) {
						Element dmCaseElm = (Element) dmCases.item(i);
						String[] key = new String[] { dmCaseElm.getAttribute("Value"),
								dmCaseElm.getAttribute("SecondValue") };
						if (dmCaseMap.containsKey(key)) {
							report.addProfileError(new CustomProfileError(ErrorType.DuplicatedDynamicMapping,
									"Segment " + segElm.getAttribute("Label")
											+ " has duplicated Dynamic mapping definition for " + key + ".",
									DocumentTarget.SEGMENT, id));
						} else {
							dmCaseMap.put(key, dmCaseElm);
						}

					}
				}
			}

			// 4. Checking Missing ValueSet
			HashMap<String, Element> valueSetMap = new HashMap<String, Element>();
			NodeList valueSetDefinitions = valuesetsDoc.getElementsByTagName("ValueSetDefinition");
			for (int i = 0; i < valueSetDefinitions.getLength(); i++) {
				Element v = (Element) valueSetDefinitions.item(i);
				valueSetMap.put(v.getAttribute("BindingIdentifier"), v);
			}

			for (String id : segmentMap.keySet()) {
				Element segElm = segmentMap.get(id);
				NodeList fields = segElm.getElementsByTagName("Field");

				for (int i = 0; i < fields.getLength(); i++) {
					Element feildElm = (Element) fields.item(i);
					String bindingIds = feildElm.getAttribute("Binding");
					if (bindingIds != null && !bindingIds.equals("")) {
						for (String bindingId : bindingIds.split("\\:")) {
							if (bindingId != null && !bindingId.equals("") && !valueSetMap.containsKey(bindingId)) {
								report.addProfileError(new CustomProfileError(ErrorType.MissingValueSet,
										"ValueSet " + bindingId + " is missing for Segment "
												+ segElm.getAttribute("Label") + "." + (i + 1),
										DocumentTarget.VALUESET, bindingId));
							}
						}
					}
				}
			}

			for (String id : datatypeMap.keySet()) {
				Element dtElm = datatypeMap.get(id);
				NodeList components = dtElm.getElementsByTagName("Component");

				for (int i = 0; i < components.getLength(); i++) {
					Element componentElm = (Element) components.item(i);
					String bindingIds = componentElm.getAttribute("Binding");
					if (bindingIds != null && !bindingIds.equals("")) {
						for (String bindingId : bindingIds.split("\\:")) {
							if (bindingId != null && !bindingId.equals("") && !valueSetMap.containsKey(bindingId)) {
								report.addProfileError(new CustomProfileError(ErrorType.MissingValueSet,
										"ValueSet " + bindingId + " is missing for Datatype "
												+ dtElm.getAttribute("Label") + "." + (i + 1),
										DocumentTarget.VALUESET, bindingId));
							}
						}
					}
				}
			}

			NodeList valueSetAssertions = constrintsDoc.getElementsByTagName("ValueSet");
			for (int i = 0; i < valueSetAssertions.getLength(); i++) {
				Element valueSetAssertionElm = (Element) valueSetAssertions.item(i);
				String bindingId = valueSetAssertionElm.getAttribute("ValueSetID");
				if (bindingId != null && !bindingId.equals("") && !valueSetMap.containsKey(bindingId)) {
					report.addProfileError(new CustomProfileError(ErrorType.MissingValueSet,
							"ValueSet " + bindingId + " is missing for Constraints.", DocumentTarget.VALUESET,
							bindingId));
				}
			}

		} catch (SAXException e) {
			report.addProfileError(new CustomProfileError(ErrorType.Unknown, e.getMessage(), null, null));
			;
		} catch (ParserConfigurationException e) {
			report.addProfileError(new CustomProfileError(ErrorType.Unknown, e.getMessage(), null, null));
			;
		} catch (IOException e) {
			report.addProfileError(new CustomProfileError(ErrorType.Unknown, e.getMessage(), null, null));
			;
		}

		// 5. Pasing by core

		if (report.isSuccess()) {
			InputStream profileXMLIO = IOUtils.toInputStream(profileXMLStr, StandardCharsets.UTF_8);
			try {
				XMLDeserializer.deserialize(profileXMLIO).get();
			} catch (Error error) {
				report.addProfileError(
						new CustomProfileError(ErrorType.CoreParsingError, error.getMessage(), null, null));
				;
			} catch (NoSuchElementException nsee) {
				report.addProfileError(
						new CustomProfileError(ErrorType.CoreParsingError, nsee.getMessage(), null, null));
				;

			} catch (Exception e) {
				report.addProfileError(new CustomProfileError(ErrorType.Unknown, e.getMessage(), null, null));
				;
			}
		}

		return report;
	}
	
	@Override
	public ProfileValidationReport validationXMLs(String profileXMLStr, String constraintXMLStr, String valuesetXMLStr) {
		return this.validationXMLs(profileXMLStr, constraintXMLStr, valuesetXMLStr, null, null, null);
	}
	
	@Override
	public ProfileValidationReport validationXMLs(InputStream profileXMLIO, InputStream constraintXMLIO,
			InputStream valuesetXMLIO, InputStream coconstraintXMLIO, InputStream pofileSlicingXMLIO,
			InputStream bindingXMLIO) throws IOException {
		String profileXMLStr = IOUtils.toString(profileXMLIO, StandardCharsets.UTF_8);
		String constraintXMLStr = IOUtils.toString(constraintXMLIO, StandardCharsets.UTF_8);
		String valuesetXMLStr = IOUtils.toString(valuesetXMLIO, StandardCharsets.UTF_8);
		
		String coconstraintXMLStr = null;
		if(coconstraintXMLIO != null) coconstraintXMLStr = IOUtils.toString(coconstraintXMLIO, StandardCharsets.UTF_8);
		
		String pofileSlicingXMLStr = null;
		if(pofileSlicingXMLIO != null) pofileSlicingXMLStr = IOUtils.toString(pofileSlicingXMLIO, StandardCharsets.UTF_8);

		String bindingXMLStr = null;
		if(bindingXMLIO != null) bindingXMLStr = IOUtils.toString(bindingXMLIO, StandardCharsets.UTF_8);

		return this.validationXMLs(profileXMLStr, constraintXMLStr, valuesetXMLStr, coconstraintXMLStr, pofileSlicingXMLStr, bindingXMLStr);
	}
	
	@Override
	public ProfileValidationReport validationXMLs(InputStream profileXMLIO, InputStream constraintXMLIO,
			InputStream valuesetXMLIO) throws IOException {
		return this.validationXMLs(profileXMLIO, constraintXMLIO, valuesetXMLIO, null, null, null);
	}
	

	@Override
	public String validationXMLsHTML(String profileXMLStr, String constraintXMLStr, String valuesetXMLStr,
			String coconstraintXML, String pofileSlicingXML, String bindingXML) {
		return validationXMLs(profileXMLStr, constraintXMLStr, valuesetXMLStr, coconstraintXML, pofileSlicingXML, bindingXML).generateHTML();
	}
	
	@Override
	public String validationXMLsHTML(String profileXMLStr, String constraintXMLStr, String valuesetXMLStr) {
		return this.validationXMLsHTML(profileXMLStr, constraintXMLStr, valuesetXMLStr, null, null, null);
	}
	
	@Override
	public String validationXMLsHTML(InputStream profileXMLIO, InputStream constraintXMLIO, InputStream valuesetXMLIO,
			InputStream coconstraintXMLIO, InputStream pofileSlicingXMLIO, InputStream bindingXMLIO)
			throws IOException {
		return validationXMLs(profileXMLIO, constraintXMLIO, valuesetXMLIO, coconstraintXMLIO, pofileSlicingXMLIO, bindingXMLIO).generateHTML();
	}

	



	@Override
	public String validationXMLsHTML(InputStream profileXMLIO, InputStream constraintXMLIO, InputStream valuesetXMLIO)
			throws IOException {
		return this.validationXMLsHTML(profileXMLIO, constraintXMLIO, valuesetXMLIO, null, null, null);
	}

	
	private XSDVerificationResult verifyXMLByLocalXSD(String xsdLocalPath, String xml) {
		try {
			Source xmlFile = new StreamSource(new StringReader(xml));
			SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = schemaFactory.newSchema(this.getClass().getResource(xsdLocalPath));
			Validator validator = schema.newValidator();
			validator.validate(xmlFile);
			return new XSDVerificationResult(true, null);
		} catch (SAXException e) {
			return new XSDVerificationResult(false, e);
		} catch (IOException e) {
			return new XSDVerificationResult(false, e);
		} catch (Exception e) {
			return new XSDVerificationResult(false, e);
		}
	}
}
